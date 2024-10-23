package sphero.algo.global;

import sphero.algo.MazeGenerator;
import sphero.algo.local.AllPairShortestPaths;
import sphero.common.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Implementation of the AntColony heuristic algorithm
 * Performs worse than Local Search, so it was abandoned at some point
 */
@Deprecated
public class AntColony implements RoutingAlgorithm {

    private final AllPairShortestPaths allPairShortestPaths;
    private final int m;
    private final int n;

    public AntColony(AllPairShortestPaths allPairShortestPaths, int m, int n){
        this.allPairShortestPaths = allPairShortestPaths;
        this.m = m;
        this.n = n;
    }

    public static void main(String[] args) {
        Configuration config = (new MazeGenerator()).generate(50,50,75, true);
        (new AntColony(new AllPairShortestPaths(), 100000,16)).calculate(config);
    }

    @Override
    public RoutingResult calculate(Configuration config) {
        Path[][] paths = allPairShortestPaths.allPairs(config.getOrderedLocations(), config.getMap());
        RoutingResult rs = new RoutingResult(new LinkedList<>(), (new CalculationContext()).simulateAnts(config, paths));
        System.out.println("Ant: " + rs.getCost());
        LocalSearch ls = new LocalSearch(allPairShortestPaths,1000);
        System.out.println("TwoOpt: " + ls.calculate(config).getCost());
        return rs;
    }

    private class CalculationContext {
        Ant ant;
        double [][] trails;
        Path[][] paths;
        private Location [][] routes;
        private boolean [][] visited;
        double tau0;
        Random [] randoms;

        long mandatory;
        List<Location> locations;
        Location start;

        double simulateAnts(Configuration config, Path[][] paths){
            mandatory = config.getOrderedLocations().stream().filter(Location::isMandatory).count();
            locations = config.getOrderedLocations();
            start = config.getStart();
            trails = new double[locations.size()][locations.size()];
            this.paths = paths;
            ant = Ant.DEFAULT;
            routes = new Location[n][locations.size()+1];
            visited = new boolean[n][locations.size()];
            randoms = new Random[n];
            for (int i = 0; i < n; i++) {
                randoms[i] = new Random();
            }
            tau0 = 1/(mandatory * estimate(paths,locations));

            for (int i = 0; i < trails.length; i++) {
                for (int j = 0; j < trails.length; j++) {
                    Path p = paths[i][j];
                    trails[i][j] = p == null ? 0 : Math.pow(p.getDistance(),-ant.beta);
                }
            }

            double bestbest = Double.MAX_VALUE;
            Solution s = new Solution(locations.size()+1);
            s.best_solution = new Location[locations.size()+1];

            for (int i = 0; i < m; i++) { //runs

                IntStream.range(0,n).parallel().forEach(j -> {
                    walkAnt(j,s);
                });
                s.save();
                globalUpdate(ant, s.best_solution, s.len, s.best, trails);
                bestbest = Math.min(s.best,bestbest);
                s.best = Double.MAX_VALUE;
            }
            return bestbest;
        }

        void walkAnt(int i, Solution s){
            Arrays.fill(visited[i],false);
            int tour_count = 0;
            int mand_count = 0;
            Location from = start;

            double cost = 0;
            double timer = 0;
            //double reach = 1000;
            while(mand_count < mandatory){
                routes[i][tour_count] = from;
                Location next = next(ant, i, from, timer, mand_count == mandatory-1);
                localUpdate(ant, from, next, trails, tau0);
                timer+=paths[from.getId()][next.getId()].getTravelTime();
                cost += paths[from.getId()][next.getId()].getCost();
                if(next.isTimeCritical() && timer > next.getDeadline())
                    cost += 100000 * timer / next.getDeadline();

                mand_count = next.isMandatory() ? mand_count+1 : mand_count;
                from = next;
                tour_count++;
                visited[i][from.getId()] = true;
            }
            s.update(routes[i],tour_count,cost);
        }

        Location next(Ant ant, int i, Location from, double time, boolean allowStart){
            double q = randoms[i].nextDouble();
            if(q < ant.q0){
                Location next = next_deterministic(ant,i,from, time, allowStart);
                return next;
            } else {
                Location next = next_probabilistic(ant,i,from, time, allowStart);
                return next;
            }
        }

        Location next_deterministic(Ant ant, int i, Location from, double time, boolean allowStart){
            double best = Double.MIN_VALUE;
            Location best_location = null;
            for (int j = allowStart ? 0 : 1; j < visited[i].length; j++) {
                if(visited[i][j]) continue;
                Path path = paths[from.getId()][j];
                double penalty = 0;
                double arrival = time + path.getTravelTime();
                if(path.getTo().isTimeCritical() && path.getTo().getDeadline() < arrival)
                    penalty += 10000* arrival / path.getTo().getDeadline();
                double x = trails[from.getId()][j] * Math.pow(1/(path.getCost()+penalty), ant.beta);
                if(x > best){
                    best = x;
                    best_location = path.getTo();
                }
            }
            return best_location;
        }

        Location next_probabilistic(Ant ant, int i, Location from, double time, boolean allowStart){
            double [] probabilities = new double[visited[i].length];
            double sum = 0;
            for (int j = allowStart ? 0 : 1; j < visited[i].length; j++) {
                if(visited[i][j]) continue;
                Path path = paths[from.getId()][j];
                double penalty = 0;
                double arrival = time + path.getTravelTime();
                if(path.getTo().isTimeCritical() && path.getTo().getDeadline() < arrival)
                    penalty += 10000* arrival / path.getTo().getDeadline();
                double p = trails[from.getId()][j] * Math.pow(1/(path.getCost()+penalty), ant.beta);
                probabilities[j] = p;
                sum += p;
            }
            double threshold = randoms[i].nextDouble();
            double cumulative = 0;
            int j = allowStart ? 0 : 1;
            for (; j < visited[i].length && cumulative < threshold; j++) {
                if(visited[i][j]) continue;
                cumulative += probabilities[j] / sum;
            }
            return paths[from.getId()][j-1].getTo();
        }

        void globalUpdate(Ant ant, Location[] route, int len, double cost, double [][] trails){
            for (int i = 0; i < len-1; i++) {
                Location from = route[i];
                Location to = route[(i+1)];
                double old = trails[from.getId()][to.getId()];
                trails[from.getId()][to.getId()] = old * (1-ant.alpha) + 1/cost * ant.alpha;
            }
        }
        void localUpdate(Ant ant, Location from, Location to, double [][] trails, double tau0){
            double old = trails[from.getId()][to.getId()];
            trails[from.getId()][to.getId()] = old * (1-ant.alpha) + tau0 * ant.alpha;
        }

    }

    public double estimate(Path [][] matrix, List<Location> locations){
        Location [] route = new Location[locations.size()];
        boolean [] used = new boolean[locations.size()+1];
        int i = 0;
        int v = 0;
        while(i < locations.size()){
            int min_d = 0;
            double min = Integer.MAX_VALUE;
            for (int j = 1; j < locations.size(); j++) {
                if(!used[j]){
                    Path p = matrix[v][j];
                    if(p.getCost() < min){
                        min = p.getCost();
                        min_d = j;
                    }
                }
            }
            used[min_d] = true;
            route[i] = locations.get(min_d);
            v = min_d;
            i++;
        }
        return cost(matrix,route) + matrix[route.length-1][0].getCost();
    }

    protected double cost(Path [][] matrix, Location[] tour){
        double sum = 0;
        double timer = 0;
        for (int i = 0; i < tour.length-1; i++) {
            Location a = tour[i];
            Location b = tour[i+1];
            sum+=matrix[a.getId()][b.getId()].getCost();
            timer+=matrix[a.getId()][b.getId()].getTravelTime();
            if(b.isTimeCritical() && timer > b.getDeadline())
                sum += 100000 * timer / b.getDeadline();
        }
        sum+=matrix[tour[tour.length-1].getId()][tour[0].getId()].getCost();
        return sum;
    }


    protected enum Ant {
        DEFAULT(0.1, 2, 0.9);

        double alpha;
        double beta;
        double q0;

        Ant(double alpha, double beta, double q0) {
            this.alpha = alpha;
            this.beta = beta;
            this.q0 = q0;
        }
    }


    protected static class Solution {
        Location[] best_solution = null;
        double best = Double.MAX_VALUE;
        int len = -1;

        public Solution(int l){
            temp = new Location[l];
        }

        Location[] temp;

        synchronized void update(Location[] solution, int len, double cost) {
            if (cost < best) {
                this.len = len;
                best = cost;
                best_solution = solution;
            }
        }

        synchronized void save(){
            System.arraycopy(best_solution, 0, temp, 0, best_solution.length);
            best_solution = temp;
        }
    }



}
