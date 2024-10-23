package sphero.algo;

import org.junit.jupiter.api.Test;
import sphero.algo.global.LocalSearch;
import sphero.algo.local.AllPairShortestPaths;
import sphero.common.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sphero.algo.TestTools.*;


public class LocalSearchTest {

    @Test
    public void testSimpleConfiguration() {
        Configuration config = generateConfiguration(4);
        var mockAllPairShortestPaths = new AllPairShortestPaths() {
            @Override
            public Path[][] allPairs(List<Location> locationList, Map map) {
                ArrayBuilder b = new ArrayBuilder(4);
                b.appendLine(0, 9, 7, 2);
                b.appendLine(4, 0, 2, 9);
                b.appendLine(1, 5, 0, 6);
                b.appendLine(1, 1, 8, 0);
                return distancesToPathMatrix(b.arr, config.getOrderedLocations());
            }
        };
        LocalSearch localSearch = new LocalSearch(mockAllPairShortestPaths, 1000);
        RoutingResult res = localSearch.calculate(config);
        assertTrue(res.isValid());
        assertEquals(res.getCost(), 6);
        verifyRoute(res.getRoute(), 0, 3, 1, 2, 0);
    }

    @Test
    public void testSimpleConfiguration2() {
        Configuration config = generateConfiguration(6);
        var mockAllPairShortestPaths = new AllPairShortestPaths() {
            @Override
            public Path[][] allPairs(List<Location> locationList, Map map) {
                ArrayBuilder b = new ArrayBuilder(6);
                b.appendLine(0, 25, 19, 19, 23, 28);
                b.appendLine(25, 0, 24, 30, 27, 17);
                b.appendLine(19, 24, 0, 18, 20, 23);
                b.appendLine(19, 30, 18, 0, 19, 32);
                b.appendLine(23, 27, 20, 19, 0, 41);
                b.appendLine(28, 17, 23, 32, 41, 0);
                return distancesToPathMatrix(b.arr, config.getOrderedLocations());
            }
        };
        LocalSearch localSearch = new LocalSearch(mockAllPairShortestPaths, 1000);
        RoutingResult res = localSearch.calculate(config);
        assertTrue(res.isValid());
        assertEquals(res.getCost(), 123);
        verifyRoute_symmetric(res.getRoute(), 0, 1, 5, 2, 4, 3, 0);
    }

    @Test
    public void testSmallConfiguration1() {
        Configuration config = generateConfiguration(1);
        var mockAllPairShortestPaths = new AllPairShortestPaths() {
            @Override
            public Path[][] allPairs(List<Location> locationList, Map map) {
                ArrayBuilder b = new ArrayBuilder(1);
                b.appendLine(0);
                return distancesToPathMatrix(b.arr, config.getOrderedLocations());
            }
        };
        LocalSearch localSearch = new LocalSearch(mockAllPairShortestPaths, 1000);
        RoutingResult res = localSearch.calculate(config);
        assertTrue(res.isValid());
        assertEquals(res.getCost(), 0);
        verifyRoute_symmetric(res.getRoute(), 0);
    }

    @Test
    public void testSmallConfiguration2() {
        Configuration config = generateConfiguration(2);
        var mockAllPairShortestPaths = new AllPairShortestPaths() {
            @Override
            public Path[][] allPairs(List<Location> locationList, Map map) {
                ArrayBuilder b = new ArrayBuilder(2);
                b.appendLine(0, 2);
                b.appendLine(2, 0);
                return distancesToPathMatrix(b.arr, config.getOrderedLocations());
            }
        };
        LocalSearch localSearch = new LocalSearch(mockAllPairShortestPaths, 1000);
        RoutingResult res = localSearch.calculate(config);
        assertTrue(res.isValid());
        assertEquals(res.getCost(), 4);
        verifyRoute_symmetric(res.getRoute(), 0, 1, 0);
    }

    @Test
    public void testInvalidConfigurations() {
        Configuration config = generateConfiguration(2);
        var mockAllPairShortestPaths = new AllPairShortestPaths() {
            @Override
            public Path[][] allPairs(List<Location> locationList, Map map) {
                return null;
            }
        };
        LocalSearch localSearch = new LocalSearch(mockAllPairShortestPaths, 1000);
        RoutingResult res = localSearch.calculate(config);
        assertFalse(res.isValid());
    }
}
