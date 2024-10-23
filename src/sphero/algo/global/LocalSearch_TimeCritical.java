package sphero.algo.global;

import sphero.algo.local.AllPairShortestPaths;
import sphero.common.Configuration;
import sphero.common.Location;
import sphero.common.Path;
import sphero.common.RoutingResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Further iteration of the Local Search algorithm
 * Tries to solve problems with deadline constraints by including them in the cost-function
 *
 * @see sphero.common.RoutingAlgorithm
 * @see sphero.algo.global.LocalSearch
 */
public class LocalSearch_TimeCritical extends LocalSearch {


    /**
     *
     * @param allPairShortestPaths dependency injection of an APSP implementation
     * @param iterations           Amount of randomized runs (more runs -> better results, but also longer runtime)
     */
    public LocalSearch_TimeCritical(AllPairShortestPaths allPairShortestPaths, int iterations) {
        super(allPairShortestPaths, iterations);
    }


    /**
     * runs the algorithm for the desired number of iterations
     * runs multiple tasks in parallel to make use of all CPU cores
     * will perform a check whether the best solution violates any deadlines
     *
     * @see sphero.common.RoutingAlgorithm
     * @return best solution over all iterations
     */
    @Override
    public RoutingResult calculate(Configuration config) {
        RoutingResult res = super.calculate(config);
        if(res.isValid()){
            int x = violatedDeadlines(res.getRoute());
            if(x != 0) return new RoutingResult("Keine Route gefunden, die alle Anforderungen erfüllt");
        }
        return res;
    }



    /**
     * Performs one run of the heuristic algortihm to find a local minimum
     * Will overwrite the initial tour
     *
     * @param distances distance matrix for all destinations
     */
    @Override
    protected void minimize(Path[][] distances, Location[] tour) {
        int i = 0;
        double res = 0;
        double diff = 1;
        while (diff != 0 && i < 100) {
            i++;
            diff = improve_twoOpt(distances, tour);
            res -= diff;
        }
    }

    /**
     * Führt die 2-Opt-Heuristik auf einer gegebenen Tour aus
     *
     * @param distances Path-Matrix von AllPairShortestPaths
     * @param tour      zu verbessernde Tour
     * @return Größe der Verbesserung durch 2-Opt
     */
    @Override
    protected double improve_twoOpt(Path[][] distances, Location[] tour) {
        double diff = 0;
        double cost = cost(distances, tour);
        Location[] res = tour;
        for (int i = 0; i < tour.length - 1; i++) {
            for (int j = i + 1; j < tour.length; j++) {
                Location[] alt = reversed(tour, i + 1, j);
                double alt_cost = cost(distances, alt);
                if (cost > alt_cost) {
                    diff = cost - alt_cost;
                    res = alt;
                    cost = alt_cost;
                }
            }
        }
        System.arraycopy(res, 0, tour, 0, tour.length);
        return diff;
    }


    /**
     * Reversed the order of a sub-array
     *
     * @param tour tour array
     * @param i    start of the region to be reversed (inclusive)
     * @param j    end of the region to be reversed (inclusive)
     */
    protected Location[] reversed(Location[] tour, int i, int j) {
        Location[] copy = new Location[tour.length];
        System.arraycopy(tour, 0, copy, 0, i);
        if (j < tour.length - 1) System.arraycopy(tour, j + 1, copy, j + 1, tour.length - j - 1);
        for (int a = j, b = i; a >= i; a--, b++) {
            copy[a] = tour[b];
        }
        return copy;
    }

    /**
     * Computes the cost for a given route
     * Adds a heavy penalty for missed deadlines
     */
    @Override
    protected double cost(List<Path> route) {
        double distance = 0;
        double time = 0;
        double penalty = 0;
        for (Path p : route) {
            distance += p.getDistance();
            time += p.getTravelTime();
            if (p.getTo().isTimeCritical() && p.getTo().getDeadline() < time) {
                penalty += 100000;
            }
        }
        return distance + penalty;
    }

    /**
     * Computes the cost for a given tour
     * Adds a heavy penalty for missed deadlines
     */
    @Override
    protected double cost(Path[][] matrix, Location[] tour) {
        double distance = 0;
        double time = 0;
        double penalty = 0;
        for (int i = 0; i < tour.length; i++) {
            Location a = tour[i];
            Location b = tour[(i + 1) % tour.length];
            distance += matrix[a.getId()][b.getId()].getDistance();
            time += matrix[a.getId()][b.getId()].getTravelTime();
            if (b.isTimeCritical() && b.getDeadline() < time) {
                penalty += 100000 * time / b.getDeadline();
            }
        }
        double cost = distance + penalty;
        return cost;
    }

    /**
     * Computes the amount of violated deadlines for a route
     */
    protected int violatedDeadlines(List<Path> route) {
        double time = 0;
        int c = 0;
        for (Path p : route) {
            time += p.getTravelTime();
            if (p.getTo().isTimeCritical() && p.getTo().getDeadline() < time) {
                c++;
            }
        }
        return c;
    }

    /**
     * Creates a random tour that starts the desired start position
     */
    protected static Location[] randomOrder(Configuration configuration) {
        List<Location> copy = new ArrayList<>(configuration.getOrderedLocations());
        Collections.shuffle(copy);
        Location[] arr = new Location[copy.size()];
        copy.toArray(arr);
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].getType() == Location.Type.START) {
                Location start = arr[i];
                arr[i] = arr[0];
                arr[0] = start;
            }
        }
        return arr;
    }


}
