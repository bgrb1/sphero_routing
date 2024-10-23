package sphero.algo;

import org.junit.Ignore;
import org.junit.Test;
import sphero.algo.local.AllPairShortestPaths;
import sphero.algo.local.BFS;
import sphero.common.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LocalRoutingTest {
    @Test
    public void testBFSWithInputFile1() {
        ConfigurationBuilder cb;
        try {
            cb = new ConfigurationBuilder().fromFile(new File("resources/inputs/input_04.txt"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Configuration config = new Configuration(cb);
        Map map = config.getMap();
        Location firstLoc = config.getStart();
        Location secondLoc = config.getOrderedLocations().get(1);
        BFS bfs = new BFS();
        Path result1 = bfs.shortestDistanceBFS(firstLoc, secondLoc, map);
        secondLoc = config.getOrderedLocations().get(2);
        Path result2 = bfs.shortestDistanceBFS(firstLoc, secondLoc, map);
        secondLoc = config.getOrderedLocations().get(3);
        Path result3 = bfs.shortestDistanceBFS(firstLoc, secondLoc, map);

        assertEquals(result1.getDistance(), 7);
        assertEquals(result2.getDistance(), 26);
        assertEquals(result3.getDistance(), 20);
        assertEquals(1, result1.getFields().get(3).getX());
        assertEquals(1, result1.getFields().get(3).getY());
        assertEquals(1, result2.getFields().get(21).getX());
        assertEquals(17, result2.getFields().get(21).getY());
        assertEquals(6, result3.getFields().get(16).getX());
        assertEquals(11, result3.getFields().get(16).getY());
    }

    @Test
    public void testBFSWithInputFile2() {
        ConfigurationBuilder cb;
        try {
            cb = new ConfigurationBuilder().fromFile(new File("resources/inputs/input_01.txt"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Configuration config = new Configuration(cb);
        Map map = config.getMap();
        Location firstLoc = config.getStart();
        BFS bfs = new BFS();
        Location secondLoc = config.getOrderedLocations().get(0);
        Path result1 = bfs.shortestDistanceBFS(firstLoc, secondLoc, map);
        secondLoc = config.getOrderedLocations().get(1);
        Path result2 = bfs.shortestDistanceBFS(firstLoc, secondLoc, map);
        secondLoc = config.getOrderedLocations().get(3);
        Path result3 = bfs.shortestDistanceBFS(firstLoc, secondLoc, map);
        secondLoc = config.getOrderedLocations().get(4);
        Path result4 = bfs.shortestDistanceBFS(firstLoc, secondLoc, map);
        Path nullResult = bfs.shortestDistanceBFS(firstLoc, firstLoc, map);

        assertEquals(16, result1.getDistance());
        assertEquals(12, result2.getDistance());
        assertEquals(9, result3.getDistance());
        assertEquals(9, result4.getDistance());
        assertEquals(3, result1.getFields().get(6).getX());
        assertEquals(5, result1.getFields().get(6).getY());
        assertEquals(6, result3.getFields().get(4).getX());
        assertEquals(4, result3.getFields().get(4).getY());
        assertEquals(10, result4.getFields().get(7).getX());
        assertEquals(7, result4.getFields().get(7).getY());
    }

    @Test
    public void testImpossiblePathBFS() {
        ConfigurationBuilder cb;
        try {
            cb = new ConfigurationBuilder().fromFile(new File("resources/inputs/input_04.txt"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Configuration config = new Configuration(cb);
        Map map = config.getMap();
        map.disconnect(map.getField(1), map.getField(2));
        Location firstLoc = config.getStart();
        BFS bfs = new BFS();
        Location secondLoc = config.getOrderedLocations().get(1);
        Path result1 = bfs.shortestDistanceBFS(firstLoc, secondLoc, map);
        secondLoc = config.getOrderedLocations().get(2);
        Path result2 = bfs.shortestDistanceBFS(firstLoc, secondLoc, map);
        secondLoc = config.getOrderedLocations().get(3);
        Path result3 = bfs.shortestDistanceBFS(firstLoc, secondLoc, map);

        assertNull(result1);
        assertNull(result2);
        assertNull(result3);
    }

    @Test
    public void testAllPairs1() {
        ConfigurationBuilder cb;
        try {
            cb = new ConfigurationBuilder().fromFile(new File("resources/inputs/input_01.txt"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Configuration config = new Configuration(cb);
        Map map = config.getMap();
        AllPairShortestPaths ap = new AllPairShortestPaths();
        Path[][] result = ap.allPairs(config.getOrderedLocations(), map);

        assertEquals(13, result[0][1].getFrom().getField().getId());
        assertEquals(19, result[0][1].getTo().getField().getId());
        assertEquals(63, result[2][4].getFrom().getField().getId());
        assertEquals(116, result[2][4].getTo().getField().getId());

        assertEquals(15, result[0][3].getDistance());
        assertEquals(19, result[4][1].getDistance());

       /* System.out.println(Arrays.deepToString(result[0]));
        System.out.println(Arrays.deepToString(result[1]));
        System.out.println(Arrays.deepToString(result[2]));
        System.out.println(Arrays.deepToString(result[3]));*/

    }

    @Test
    public void testAllPairs2() {
        ConfigurationBuilder cb;
        try {
            cb = new ConfigurationBuilder().fromFile(new File("resources/inputs/input_03.txt"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Configuration config = new Configuration(cb);
        Map map = config.getMap();
        BFS bfs = new BFS();
        AllPairShortestPaths ap = new AllPairShortestPaths();
        Path[][] result = ap.allPairs(config.getOrderedLocations(), map);

        assertEquals(5, result[0][1].getDistance());
        assertEquals(26, result[0][6].getDistance());

        Path examplePath = result[0][4];
        assertEquals(34, examplePath.getFields().get(13).getId());
        assertEquals(45, examplePath.getFields().get(14).getId());
        assertEquals(44, examplePath.getFields().get(15).getId());
        assertEquals(55, examplePath.getFields().get(16).getId());
        assertEquals(66, examplePath.getFields().get(17).getId());

        for (int i = 0; i < config.getOrderedLocations().size(); i++) {
            for (int j = 0; j < config.getOrderedLocations().size(); j++) {
                Path expected = bfs.shortestDistanceBFS(config.getOrderedLocations().get(i)
                        , config.getOrderedLocations().get(j), config.getMap());
                Path actual = result[i][j];
                assertEquals(expected.getFrom().getId(), actual.getFrom().getId());
                assertEquals(expected.getTo().getId(), actual.getTo().getId());
                assertEquals(expected.getCost(), actual.getCost());
                assertEquals(expected.getDistance(), actual.getDistance());
                /*
                System.out.println(expected.getFields());
                System.out.println(actual.getFields());
                if(actual.getFields().size() == 5){
                    System.out.println(actual.getFields().get(4).getId() == expected.getTo().getField().getId());
                } */
                if (expected.getFields().size() == 2) {
                    assertEquals(expected.getFields().size(), actual.getFields().size());
                } else {
                    assertEquals(expected.getFields().size() + 1, actual.getFields().size());
                    // +1. da BFS noch nicht das letzte Feld zum Path hinzufügt
                }
                if ((i == 0 && j == 6) || (i == 6 && j == 0)) continue;
                if ((i == 4 && j == 6) || (i == 6 && j == 4)) continue;

                for (int k = 0; k < expected.getFields().size(); k++) {
                    System.out.println("BFS: " + expected.getFields().get(k) + "\nallPairs:" + actual.getFields().get(k));
                    System.out.println("Iteration: i = " + i + " and j = " + j);
                    assertEquals(expected.getFields().get(k).getId(), actual.getFields().get(k).getId());

                    //Dieser Test failt für i,j = 0 und 6 oder i,j = 4 und 6,
                    //da hier verschiedene wege mit gleicher distanz berechnet werden
                }

            }
        }
    }

}
