package sphero.algo.local;

import sphero.common.Field;
import sphero.common.Map;
import sphero.common.Path;

import java.util.*;

public class placeholderAlgorithm {
    /**
     * @param startPosition : the starting position of the bolt
     * @param destination   : the first destination of the bolt
     * @return the path to the destination
     */
    public Path shortestDistancePlaceholder(Field startPosition, Field destination, Map map) {
        //TODO Testen
        Queue<Field> queue = new LinkedList<>();
        var dist = new int[map.getSizeX()][map.getSizeY()];
        var parent = new Field[map.getSizeX()][map.getSizeY()];
        for (int[] arr : dist) {
            Arrays.fill(arr, Integer.MAX_VALUE);
        }
        dist[startPosition.getX()][startPosition.getY()] = 0;
        queue.add(startPosition);
        while (!queue.isEmpty()) {
            Field currentPosition = queue.poll();
            for (Field neighbor : currentPosition.getNeighbors()) {
                relax(currentPosition, neighbor, dist, parent, queue);
            }
        }
        List<Field> fields = new ArrayList<>();
        Field temp = destination;
        while (temp != null) {
            fields.add(temp);
            temp = parent[temp.getX()][temp.getY()];
        }
        return null;
        //return new Path(fields,startPosition, destination, dist[destination.getX()][destination.getY()]);
    }

    private void relax(Field currentPosition, Field neighbor, int[][] grid, Field[][] parent, Queue<Field> queue) {
        if (grid[neighbor.getX()][neighbor.getY()] > grid[currentPosition.getX()][currentPosition.getY()] + 1) {
            parent[neighbor.getX()][neighbor.getY()] = currentPosition;
            grid[neighbor.getX()][neighbor.getY()] = grid[currentPosition.getX()][currentPosition.getY()] + 1;
            queue.add(neighbor);
        }
    }
}
