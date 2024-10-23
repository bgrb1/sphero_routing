package sphero.common;

import java.util.Collections;
import java.util.List;

/**
 * Aggregates information for the result of the route computation
 */
public class RoutingResult {

    private final List<Path> route;
    private final double cost;

    private final boolean valid;
    private final String errorMessage;

    public RoutingResult(List<Path> route, double cost) {
        this.route = Collections.unmodifiableList(route);
        this.cost = cost;
        valid = true;
        errorMessage = null;
    }

    public RoutingResult(String errorMessage){
        this.errorMessage = errorMessage;
        route = null;
        cost = -1;
        valid = false;
    }

    /**
     * @return returns an ordered list of paths
     */
    public List<Path> getRoute() {
        return route;
    }

    /**
     * @return total cost of the route
     */
    public double getCost() {
        return cost;
    }

    public boolean isValid() {
        return valid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
