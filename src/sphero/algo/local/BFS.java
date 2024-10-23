package sphero.algo.local;

import sphero.common.*;
import sphero.common.Map;

import java.util.*;

public class BFS {
   /* public static void main(String[] args) throws IOException {
//        BFS algorithm1 = new BFS();
//        Configuration data1 = (new MazeGenerator()).generate(10, 10, 3);
//        algorithm1.shortestDistanceBFS(data1.getStart(), data1.getDestinations().get(0).getField(), data1.getMap());

        GUI gui = new GUI("Hello Darkness my old friend");
        ConfigurationBuilder builder = new ConfigurationBuilder(gui.getCurrentFile());
        Configuration data = new Configuration(builder);
        BFS algorithm = new BFS();

        System.out.println("Running the Algorithm for start node: " + data.getStart() + ", end Node: " +  data.getDestinations().get(0).getField());
        Path path = algorithm.shortestDistanceBFS(data.getStart().getField(), data.getDestinations().get(0).getField(), data.getMap());
        System.out.println(path.getFields() + "\n");
        for (int i = 1; i < data.getDestinations().size(); i++) {
            Field currentStart = data.getDestinations().get(i-1).getField();
            Field currentDestination = data.getDestinations().get(i).getField();
            System.out.println("Running the Algorithm for start node: " + currentStart + ", end Node: " + currentDestination);
            path = algorithm.shortestDistanceBFS(currentStart, currentDestination, data.getMap());
            System.out.println(path.getFields() + "\n");
        }
    }
    */

    /**
     * Finds the shortest Distance between two Locations by applying breadth-first search (BFS).
     *
     * @param from : the starting position of the bolt
     * @param to   : the first destination of the bolt
     * @return the path to the destination
     */
    public Path shortestDistanceBFS(Location from, Location to, Map map) {
        Field startPosition = from.getField();
        Field destination = to.getField();
        if(startPosition.equals(destination)) {
            return new Path(Arrays.asList(startPosition, destination),from,to,0);
        }
        boolean[] visited = new boolean[map.getSizeX() * map.getSizeY()];
        Queue<Field> queue = new LinkedList<>();
        queue.add(startPosition);
        visited[startPosition.getId()] = true;
        int[] parent = new int[map.getSizeY() * map.getSizeX()];
        boolean destFound = false;
        while (queue.size() != 0) {
            Field current = queue.poll();
            for (Field neighbor : current.getNeighbors()) {
                if (!visited[neighbor.getId()]) {
                    visited[neighbor.getId()] = true;
                    queue.add(neighbor);
                    parent[neighbor.getId()] = current.getId();
                    if (neighbor.equals(destination)) {
                        destFound = true;
                        break;
                    }
                }
            }
        }
        if (!destFound) return null;
        LinkedList<Field> path = new LinkedList<>();
        Field currentField = destination;
        while (true) {
            currentField = map.getField(parent[currentField.getId()]);
            if (currentField.equals(startPosition)) {
                break;
            }
            path.addFirst(currentField);
        }
        return new Path(path, from, to, path.size() + 1);
    }

    public Path shortestDistanceAStar(Location from, Location to, Map map) {
        Field startPosition = from.getField();
        Field destination = to.getField();
        if(startPosition.equals(destination)) {
            return new Path(Arrays.asList(startPosition, destination),from,to,0);
        }
        boolean[] visited = new boolean[map.getSizeX() * map.getSizeY()];
        IndexMinPQ<Entry> queue = new IndexMinPQ<>(map.getAllFields().size());
        Entry[] entries = new Entry[map.getAllFields().size()];
        entries[startPosition.getId()] = new Entry(startPosition,0,destination);
        queue.insert(startPosition.getId(), entries[startPosition.getId()]);
        visited[startPosition.getId()] = true;
        int[] parent = new int[map.getSizeY() * map.getSizeX()];
        boolean destFound = false;
        while (queue.size() != 0) {
            Entry current = queue.minKey();
            queue.delMin();
            if (current.f.equals(destination)) {
                destFound = true;
                break;
            }
            for (Field neighbor : current.f.getNeighbors()) {
               // if (!visited[neighbor.getId()]) {

                    //visited[neighbor.getId()] = true;
                    if(entries[neighbor.getId()] == null) {
                        entries[neighbor.getId()] = new Entry(neighbor, current.d+1, destination);
                        parent[neighbor.getId()] = current.f.getId();
                        queue.insert(neighbor.getId(),entries[neighbor.getId()]);
                    } else {
                        if(entries[neighbor.getId()].d > current.d+1){
                            entries[neighbor.getId()].d = current.d+1;
                            queue.decreaseKey(neighbor.getId(),entries[neighbor.getId()]);
                            parent[neighbor.getId()] = current.f.getId();
                        }
                    }

               // }
            }
        }
        if (!destFound) return null;
        LinkedList<Field> path = new LinkedList<>();
        Field currentField = destination;
        while (true) {
            currentField = map.getField(parent[currentField.getId()]);
            if (currentField.equals(startPosition)) {
                break;
            }
            path.addFirst(currentField);
        }
        return new Path(path, from, to, path.size() + 1);
    }

    static class Entry implements Comparable<Entry>{
        Field f;
        int h;
        int d;

        public Entry(Field f, int d, Field destination) {
            this.f = f;
            this.d = d;
            this.h = manhattan(f, destination);
        }

        @Override
        public int compareTo(Entry o) {
            if(o == this) return 1;
            return this.d + this.h - (o.d + o.h);
        }

        @Override
        public boolean equals(Object o){
            return false;
            /*if(o instanceof Entry){
                Entry other = (Entry) o;
                return other.d + other.h == this.d + this.h;
            }
            return false; */
        }

    }

    static int manhattan(Field a, Field b){
        return Math.abs(a.getX()-b.getX()) + Math.abs(a.getY()-b.getY());
    }
}
