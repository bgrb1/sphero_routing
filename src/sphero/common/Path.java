package sphero.common;

import java.util.List;

/**
 * A path is a connection between two destinations
 * A path consists of an ordered list of fields
 */
public class Path implements Comparable<Path> {

    private final List<Field> fields;
    private final Location from;
    private final Location to;
    private final double cost;
    private final double distance;
    private double time;

    /***
     * @param fields   ordered list of fields
     * @param distance path distance
     * @param from     start field
     * @param to       end field
     */
    public Path(List<Field> fields, Location from, Location to, double distance) {
        this.fields = fields;
        this.from = from;
        this.to = to;
        this.distance = distance;
        cost = distance;
        estimateTime();
    }

    /**
     * ordered list of fields as a path from start (exclusive) to end (inclusive)
     */
    public List<Field> getFields() {
        return fields;
    }


    public double getCost() {
        return cost;
    }


    public double getDistance() {
        return distance;
    }


    public double getTravelTime() {
        return time;
    }


    public Location getFrom() {
        return from;
    }


    public Location getTo() {
        return to;
    }

    @Override
    public int compareTo(Path o) {
        return (int) (this.cost - o.cost);
    }


    /**
     * rudimentary estimate of the travel time
     */
    private void estimateTime() {
        if(fields == null || fields.isEmpty()){
            time = 0;
            return;
        }
        double sum = 0;
        Field last = null;
        boolean flag = true;
        int x_vec = 0;
        int y_vec = 0;
        for (Field f : fields) {
            if (flag) {
                flag = false;
                last = f;
                continue;
            }
            int x_diff = Math.abs(f.getX() - last.getX());
            int y_diff = Math.abs(f.getY() - last.getY());
            if (x_vec == x_diff && y_vec == y_diff) {
                sum += 1;
            } else {
                sum += 5;
            }
        }
        time = sum + to.getTimeImpact();
    }


}
