package sphero.algo.global;

import sphero.algo.local.AllPairShortestPaths;
import sphero.common.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Exact TSP algorithm which evaluates all permutations
 * Not practical for routes with > 15 destiontions
 * Time complexity: O(n*n!)
 *
 * @see sphero.common.RoutingAlgorithm
 */
public class PathPermutations implements RoutingAlgorithm {

    private final AllPairShortestPaths allPairShortestPaths;

    /**
     * @param allPairShortestPaths dependency injection of an ASPS implementation
     */
    public PathPermutations(AllPairShortestPaths allPairShortestPaths) {
        this.allPairShortestPaths = allPairShortestPaths;
    }

    /**
     * @see sphero.common.RoutingAlgorithm
     */
    @Override
    public RoutingResult calculate(Configuration config) {
        Path[][] paths = allPairShortestPaths.allPairs(config.getOrderedLocations(), config.getMap());
        if(paths == null) return new RoutingResult("Mindestens ein Ort auf der Karte ist unerreichbar.");
        CalculationContext context = new CalculationContext(config.getOrderedLocations(), paths);
        return new RoutingResult(context.getShortestRoute(), context.getDistance());
    }

    private static class CalculationContext {
        private final List<Location> locationList;
        private final Path[][] paths;

        private final boolean[] used; //marks already visited destinations
        private final int[] currentRoute; //ordered array of destinations (partial solution)

        private final int[] min_route; //shortest route found so far
        private double min_distance = Double.MAX_VALUE; //cost of the min_route


        public CalculationContext(List<Location> locationList, Path[][] paths) {
            this.locationList = locationList;
            this.paths = paths;
            used = new boolean[locationList.size()];
            used[0] = true;
            currentRoute = new int[locationList.size()];
            min_route = new int[locationList.size()];
            recurse(0, 1, 0);
        }

        /**
         * Extends the current partial solution by one destination
         *
         * @param v        previous destination
         * @param i        route array index
         * @param distance current route cost
         */
        void recurse(int v, int i, double distance) {
            if (distance >= min_distance) return; //simple bounding

            if (i == paths.length) {
                distance += paths[v][0].getCost();
                if (distance >= min_distance) return;
                min_distance = distance;
                System.arraycopy(currentRoute, 0, min_route, 0, i);
            } else {
                for (int w = 1; w < paths.length; w++) {
                    if (!used[w]) {
                        used[w] = true;
                        currentRoute[i] = w;
                        recurse(w, i + 1, distance + paths[v][w].getCost());
                        used[w] = false;
                    }
                }
            }
        }

        public List<Path> getShortestRoute() {
            if(min_route.length < 2) return new ArrayList<>();
            List<Path> route = new ArrayList<>(locationList.size());
            for (int i = 0; i < min_route.length; i++) {
                Path p = paths[min_route[i]][min_route[(i + 1)%min_route.length]];
                route.add(p);
            }
            return route;
        }

        public double getDistance() {
            return min_distance;
        }
    }

}
