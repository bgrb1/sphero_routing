package sphero.common;

import sphero.algo.global.LocalSearch;
import sphero.algo.global.PathPermutations;
import sphero.algo.local.AllPairShortestPaths;

public class AlgorithmFactory {

    /**
     * Selects a routing algorithm and creates an instance
     *
     * @param config map configuration for selecting an algorithm
     * @return routing algorithm instance
     */
    public static RoutingAlgorithm createInstance(Configuration config) {
        if (config.getOrderedLocations().size() < 10) {
            return new PathPermutations(new AllPairShortestPaths());
        } else {
            return new LocalSearch(new AllPairShortestPaths(), 30);
        }
    }
}
