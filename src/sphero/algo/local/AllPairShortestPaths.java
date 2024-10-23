package sphero.algo.local;

import sphero.common.Location;
import sphero.common.Map;
import sphero.common.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class AllPairShortestPaths {

    /**
     * Calculates the shortest Path between every possible pair of destinations.
     *
     * @param locationList - destinations for which the shortest Paths are calculated
     * @return - a 2-dimensional array of Paths, where the index of each dimension corresponds to the index of that
     * location in the given list
     **/
    //return null falls das Ergebnis einer Path Berechnung null ist
    public Path[][] allPairs(List<Location> locationList, Map map) {
        Path[][] pair_array = new Path[locationList.size()][locationList.size()];
        BFS d = new BFS();
        AtomicBoolean invalidFlag = new AtomicBoolean(false);
        IntStream.range(0, locationList.size()).parallel().forEach(i -> {
            Location current1 = locationList.get(i);

            for (int j = i; j < locationList.size(); j++) {
                Location current2 = locationList.get(j);

                if (current1 == current2) {
                    pair_array[i][j] = new Path(Arrays.asList(current1.getField(), current2.getField()), current1, current2, 0);
                    //pair_array[i][j] = null; Alternativ
                } else {
                    Path ab = d.shortestDistanceAStar(current1, current2, map);
                    if(ab == null){
                        invalidFlag.set(true);
                        return;
                    }
                    Path ba = new Path(new ArrayList<>(ab.getFields()), ab.getTo(), ab.getFrom(), ab.getCost());
                    Collections.reverse(ba.getFields());
                    ab.getFields().add(ab.getTo().getField());
                    ba.getFields().add(ba.getTo().getField());
                    pair_array[i][j] = ab;
                    pair_array[j][i] = ba;
                }
            }
        });
        if(invalidFlag.get()) return null;
        return pair_array;
    }
}
