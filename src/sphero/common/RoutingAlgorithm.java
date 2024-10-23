package sphero.common;

public interface RoutingAlgorithm {

    /**
     * runs the routing algorithm
     * @return best solution over all iterations
     */
    RoutingResult calculate(Configuration config);
}
