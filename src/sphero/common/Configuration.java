package sphero.common;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Map configuration, aggregating all information for the route computation
 */
public class Configuration {
    private final Map map;
    private final Location start;
    private final List<Location> locationList;

    /**
     * Creates a configuration using the given builder
     */
    public Configuration(ConfigurationBuilder builder) {
        map = builder.getMap();
        start = builder.getStart();
        List<Location> tempList = builder.getLocations();
        tempList.sort(Comparator.comparing(Location::getId));
        locationList = Collections.unmodifiableList(tempList);
        Location.resetCounter();
    }

    public Map getMap() {
        return map;
    }

    public Location getStart() {
        return start;
    }

    public List<Location> getOrderedLocations() {
        return locationList;
    }
}
