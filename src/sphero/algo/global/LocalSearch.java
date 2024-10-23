package sphero.algo.global;

import sphero.algo.local.AllPairShortestPaths;
import sphero.common.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Heuristic search for a good route
 * Starts with a random solution and improves it as much as possible through "2-Opt" and "Relocate"
 * Will run the algorithm multiple times in parallel with different initial solutions and select the best solution
 *
 * @see sphero.common.RoutingAlgorithm
 */
public class LocalSearch implements RoutingAlgorithm {


    protected final AllPairShortestPaths allPairShortestPaths;
    protected final int iterations;

    /**
     *
     * @param allPairShortestPaths dependency injection of an APSP implementation
     * @param iterations           Amount of randomized runs (more runs -> better results, but also longer runtime)
     */
    public LocalSearch(AllPairShortestPaths allPairShortestPaths, int iterations) {
        this.allPairShortestPaths = allPairShortestPaths;
        this.iterations = iterations;
    }

    /**
     * runs the algorithm for the desired number of iterations
     * runs multiple tasks in parallel to make use of all CPU cores
     *
     * @see sphero.common.RoutingAlgorithm
     * @return best solution over all iterations
     */
    @Override
    public RoutingResult calculate(Configuration config) {
        Path[][] distances = allPairShortestPaths.allPairs(config.getOrderedLocations(), config.getMap());
        if(distances == null) return new RoutingResult("Mindestens ein Ort auf der Karte ist unerreichbar.");
        Solution s = runParallelized(config, distances);
        List<Path> route = toRoute(distances, s.best_tour);
        return new RoutingResult(route, s.best_cost);
    }

    /**
     * helper method for calculate() to execute iterations in parallel
     */
    protected Solution runParallelized(Configuration config, Path[][] distances) {
        Solution s = new Solution();
        try {
            int threads = Runtime.getRuntime().availableProcessors();
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            CountDownLatch cdl = new CountDownLatch(iterations);
            for (int i = 0; i < iterations; i++) {
                pool.submit(() -> {
                    Location[] tour = randomOrder(config);
                    minimize(distances, tour);
                    double cost = cost(distances, tour);
                    s.update(tour, cost);
                    cdl.countDown();
                });
            }
            cdl.await();
            pool.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * helper class for thread-safe tracking of the best solution
     */
    protected static class Solution {
        Location[] best_tour = null;
        double best_cost = Double.MAX_VALUE;

        synchronized void update(Location[] tour, double cost) {
            if (cost < best_cost) {
                best_tour = tour;
                best_cost = cost;
            }
        }
    }

    /**
     * Performs one run of the heuristic algortihm to find a local minimum
     * Will overwrite the initial tour
     *
     * @param distances distance matrix for all destinations
     */
    protected void minimize(Path[][] distances, Location[] tour) {
        int i = 0;
        double res = 0;
        double diff = 1;
        for (int j = 0; j < 10 && diff != 0; j++) {
            while (diff != 0 && i < 100) {
                i++;
                diff = improve_twoOpt(distances, tour);
                res -= diff;
            }
            i = 0;
            diff = improve_relocate(distances, tour);
        }
    }

    /**
     * Performs the 2-opt heuristic
     *
     * @param distances distance matrix for all destinations
     * @param tour      tour to be improved
     * @return improvement over given tour
     */
    protected double improve_twoOpt(Path[][] distances, Location[] tour) {
        double diff = 0;

        for (int i = 0; i < tour.length - 1; i++) { //edge a
            Location a_from = tour[i];
            Location a_to = tour[i + 1];
            if(a_from == null ||a_to == null) break;
            for (int j = i + 1; j < tour.length; j++) { //edge b
                Location b_from = tour[j];
                Location b_to = tour[(j + 1) % tour.length];
                if(b_from == null ||b_to == null) break;
                double dist = distances[a_from.getId()][a_to.getId()].getCost() + distances[b_from.getId()][b_to.getId()].getCost();
                double dist_alternative = distances[a_from.getId()][b_from.getId()].getCost() + distances[a_to.getId()][b_to.getId()].getCost();
                if (dist > dist_alternative) { //found a 2-opt improvement
                    a_to = b_from;
                    diff = dist - dist_alternative;
                    reverse(tour, i + 1, j);
                }
            }
        }
        return diff;
    }

    /**
     * Reversed the order of a sub-array
     *
     * @param tour tour array
     * @param i    start of the region to be reversed (inclusive)
     * @param j    end of the region to be reversed (inclusive)
     */
    protected void reverse(Location[] tour, int i, int j) {
        while (i < j) {
            Location temp = tour[i];
            tour[i] = tour[j];
            tour[j] = temp;
            i++;
            j--;
        }
    }

    /**
     * Performs the relocate heuristic
     *
     * @param distances distance matrix for all destinations
     * @param tour      tour to be improved
     * @return improvement over given tour
     */
    protected double improve_relocate(Path[][] distances, Location[] tour) {
        double diff = 0;
        for (int i = 0; i < tour.length; i++) { //Target node
            Location a_l = i == 0 ? tour[tour.length - 1] : tour[i - 1];
            Location m = tour[i];
            Location a_r = i == tour.length - 1 ? tour[0] : tour[i + 1];

            double dist_a = distances[a_l.getId()][m.getId()].getCost() + distances[m.getId()][a_r.getId()].getCost();
            double dist_a_alt = distances[a_l.getId()][a_r.getId()].getCost();

            double best_delta = Double.MIN_VALUE;
            int best_j = -1;
            for (int j = 0; j < tour.length - 1; j++) { //Connection, into which the node can be inserted
                if (Math.abs(i - j) <= 1) continue;
                Location b_l = tour[j];
                Location b_r = tour[j + 1];

                double dist_b = distances[b_l.getId()][b_r.getId()].getCost();
                double dist_b_alt = distances[b_l.getId()][m.getId()].getCost() + distances[m.getId()][b_r.getId()].getCost();
                double delta = (dist_a + dist_b) - (dist_a_alt + dist_b_alt);
                if (delta > 0 && delta > best_delta) { //Found an improvment
                    best_delta = delta;
                    best_j = j;
                }
            }
            if (best_j != -1) {
                diff -= best_delta;
                insert(tour, i, best_j + 1);
            }
        }
        return diff;
    }

    /**
     * Inserts a location at a different position in the tour
     * Parts of the array will be shifted
     *
     * @param tour target tour
     * @param from previous index of the location
     * @param to   target index
     */
    protected void insert(Location[] tour, int from, int to) {
        if (from < to) {
            Location temp = tour[from];
            System.arraycopy(tour, from + 1, tour, from, to - from);
            tour[to] = temp;
        } else {
            Location temp = tour[from];
            System.arraycopy(tour, to, tour, to + 1, from - to);
            tour[to] = temp;
        }
    }

    /**
     * Computes the cost for a route
     */
    protected double cost(List<Path> route) {
        double sum = 0;
        for (Path p : route) {
            sum += p.getCost();
        }
        return sum;
    }

    /**
     * Computes the cost for a route
     */
    protected double cost(Path[][] matrix, Location[] tour) {
        double sum = 0;
        for (int i = 0; i < tour.length - 1; i++) {
            Location a = tour[i];
            Location b = tour[i + 1];
            sum += matrix[a.getId()][b.getId()].getCost();
        }
        sum += matrix[tour[tour.length - 1].getId()][tour[0].getId()].getCost();
        return sum;
    }


    /**
     * Creates a random tour
     *
     * @param configuration routing scenario configuration
     */
    protected static Location[] randomOrder(Configuration configuration) {
        List<Location> copy = new ArrayList<>(configuration.getOrderedLocations());
        Collections.shuffle(copy);
        Location[] arr = new Location[copy.size()];
        copy.toArray(arr);
        return arr;
    }

    /**
     * Converts a tour (ordered list of destinations) to a route (list of paths)
     */
    protected static List<Path> toRoute(Path[][] matrix, Location[] tour) {
        if(tour.length < 2) return new ArrayList<>();
        List<Path> route = new ArrayList<>(tour.length + 1);
        int start = 0;
        for (int i = 0; i < tour.length; i++) {
            if (tour[i].getType() == Location.Type.START) {
                start = i;
                break;
            }
        }
        for (int i = 0; i < tour.length; i++) {
            Path p = matrix[tour[(start + i) % tour.length].getId()][tour[(start + i + 1) % tour.length].getId()];
            route.add(p);
        }
        return route;
    }


}
