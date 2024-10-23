package sphero.algo;

import sphero.common.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTools {


    public static Configuration generateConfiguration(int destinationCount) {
        Map map = new Map(destinationCount, 1, 1, false);
        List<Location> locations = new ArrayList<>();
        ConfigurationBuilder b = new ConfigurationBuilder();
        b.withMap(map);
        b.withStart(new Location(map.getField(0, 0), Location.Type.START));
        locations.add(b.getStart());
        for (int i = 0; i < destinationCount - 1; i++) {
            locations.add(new Location(map.getField(i + 1, 0), Location.Type.DESTINATION));
        }
        b.withLocations(locations);
        return b.create();
    }

    public static Path[][] distancesToPathMatrix(double[][] distances, List<Location> locations) {
        Path[][] paths = new Path[distances.length][distances.length];
        for (int i = 0; i < distances.length; i++) {
            for (int j = 0; j < distances.length; j++) {
                paths[i][j] = new Path(new LinkedList<>(), locations.get(i), locations.get(j), distances[i][j]);
            }
        }
        return paths;
    }

    public static void verifyRoute(List<Path> route, int... expectedOrder) {
        assertEquals(expectedOrder.length - 1, route.size());
        if (expectedOrder.length == 1) return;
        int i = 0;
        for (Path p : route) {
            assertEquals(expectedOrder[i], p.getFrom().getId());
            assertEquals(expectedOrder[(i + 1)], p.getTo().getId());
            i++;
        }
    }

    public static void verifyRoute_symmetric(List<Path> route, int... expectedOrder) {
        assertEquals(expectedOrder.length - 1, route.size());
        if (expectedOrder.length == 1) return;
        int first = route.get(0).getTo().getId();
        if (first == expectedOrder[expectedOrder.length - 2])
            invertArray(expectedOrder);
        int i = 0;
        for (Path p : route) {
            assertEquals(expectedOrder[i], p.getFrom().getId());
            assertEquals(expectedOrder[(i + 1)], p.getTo().getId());
            i++;
        }
    }

    public static void invertArray(int[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            int temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
    }

    public static class ArrayBuilder {
        double[][] arr;
        int i = 0;

        public ArrayBuilder(int dim) {
            arr = new double[dim][];
        }

        void appendLine(double... entries) {
            assert entries.length == arr.length;
            arr[i] = entries;
            i++;
        }
    }
}
