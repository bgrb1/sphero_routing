package sphero.algo;

import sphero.algo.local.AllPairShortestPaths;
import sphero.common.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static sphero.common.Location.Type.DESTINATION;
import static sphero.common.Location.Type.START;

public class MazeGenerator {
    private boolean[] visited;

    public static void main(String[] args) {
        MazeGenerator g = new MazeGenerator();
        Configuration c = g.generate(15, 15, 10);
        RoutingAlgorithm algo = AlgorithmFactory.createInstance(c);
        RoutingResult obs = algo.calculate(c);
    }

    public Configuration generate(int xSize, int ySize, int destinations) {
        Map map = new Map(xSize, ySize, 1, true);
        visited = new boolean[xSize * ySize];
        randomDFS(map, map.getField(0, 0));
        List<Location> locationList = new ArrayList<>(destinations + 1);
        Collections.shuffle(map.getAllFields());
        Field start_field = map.getAllFields().get(0);
        Location start = new Location(start_field, START);
        for (int i = 0; i < destinations; i++) {
            locationList.add(new Location(map.getAllFields().get(i + 1), DESTINATION));
        }
        locationList.add(start);
        return new ConfigurationBuilder().withMap(map).withStart(start).withLocations(locationList).create();
    }

    public Configuration generate(int xSize, int ySize, int destinations, boolean noWalls) {
        Map map = new Map(xSize, ySize, 1, false);
        visited = new boolean[xSize * ySize];
        if (!noWalls) randomDFS(map, map.getField(0, 0));
        List<Location> locationList = new ArrayList<>(destinations);
        Collections.shuffle(map.getAllFields());
        Field start_field = map.getAllFields().get(0);
        Location start = new Location(start_field, START);
        for (int i = 0; i < destinations; i++) {
            locationList.add(new Location(map.getAllFields().get(i + 1), DESTINATION));
        }
        locationList.add(start);
        return new ConfigurationBuilder().withMap(map).withStart(start).withLocations(locationList).create();
    }

    public Configuration generateCircular(int xSize, int ySize, int destinations, boolean noWalls) {
        Map map = new Map(xSize, ySize, 1, false);
        visited = new boolean[xSize * ySize];
        if (!noWalls) randomDFS(map, map.getField(0, 0));
        List<Location> locationList = new ArrayList<>(destinations);
        List<Field> fields = new ArrayList<>();

        int n = destinations + 1;
        int c = Math.min(xSize, ySize) / 2;
        int r = ((int) (0.9 * c));
        for (int i = 0; i < n; ++i) {
            final double angle = Math.toRadians(((double) i / n) * 360d);
            int x = (int) (Math.cos(angle) * r) + r;
            int y = (int) (Math.sin(angle) * r) + r;
            fields.add(map.getField(x, y));
        }


        Field start_field = fields.get(0);
        Location start = new Location(start_field, START);
        for (int i = 0; i < destinations; i++) {
            locationList.add(new Location(fields.get(i + 1), DESTINATION));
        }
        locationList.add(start);
        return new ConfigurationBuilder().withMap(map).withStart(start).withLocations(locationList).create();
    }

    private void randomDFS(Map map, Field f) {
        visited[f.getId()] = true;
        List<Field> neighbors = new ArrayList<>(4);
        neighbors.add(map.getField(f.getX() + 1, f.getY()));
        neighbors.add(map.getField(f.getX() - 1, f.getY()));
        neighbors.add(map.getField(f.getX(), f.getY() + 1));
        neighbors.add(map.getField(f.getX(), f.getY() - 1));
        Collections.shuffle(neighbors);
        for (Field f2 : neighbors) {
            if (f2 != null && !visited[f2.getId()]) {
                map.connect(f, f2);
                randomDFS(map, f2);
            }
        }
    }

    public AllPairShortestPaths mockDistanceMatrix(List<Location> locationList) {
        Path[][] matrix = new Path[locationList.size()][locationList.size()];
        for (int i = 0; i < matrix.length - 1; i++) {
            for (int j = i + 1; j < matrix.length; j++) {
                Location a = locationList.get(i);
                Location b = locationList.get(j);

                matrix[j][i] = new Path(new ArrayList<>(), a, b, Math.abs(a.getField().getX() - b.getField().getX()) + Math.abs(b.getField().getY() - a.getField().getY()));
                matrix[i][j] = new Path(new ArrayList<>(), b, a, Math.abs(a.getField().getX() - b.getField().getX()) + Math.abs(b.getField().getY() - a.getField().getY()));
            }
        }
        return new AllPairShortestPaths() {
            @Override
            public Path[][] allPairs(List<Location> locationList, Map map) {
                return matrix;
            }
        };
    }
}
