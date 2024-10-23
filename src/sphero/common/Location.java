package sphero.common;

public class Location {

    private static int id_counter = 0;
    private final Field field;
    private final Type type;
    private final int id;
    private final int sub_id;

    private final boolean mandatory;

    private final double time_impact;
    private final double range_impact;
    private boolean time_critical;

    private double deadline;

    public Location(Field field, Type type, int sub_id, boolean mandatory, double time_impact, double range_impact, boolean time_critical, double deadline) {
        this.field = field;
        this.id = id_counter;
        this.sub_id = sub_id;
        this.type = type;
        this.mandatory = mandatory;
        this.time_impact = time_impact;
        this.range_impact = range_impact;
        this.time_critical = time_critical;
        this.deadline = deadline;
        id_counter++;
    }

    public Location(Field field, Type type) {
        this.field = field;
        this.type = type;
        this.id = id_counter;
        this.sub_id = type.getSubIdAndIncrement();
        id_counter++;

        mandatory = type.mandatory_default;
        time_impact = type.time_impact_default;
        range_impact = type.range_impact_default;
        time_critical = false;
        deadline = 0;
    }

    public Location(Field field, Type type, double deadline) {
        this.field = field;
        this.type = type;
        this.id = id_counter;
        this.sub_id = type.getSubIdAndIncrement();
        id_counter++;

        mandatory = type.mandatory_default;
        time_impact = type.time_impact_default;
        range_impact = type.range_impact_default;
        this.time_critical = true;
        this.deadline = deadline;
    }

    public static int getId_counter() {
        return id_counter;
    }

    protected static void resetCounter() {
        id_counter = 0;
        Type.START.sub_id_counter = 0;
        Type.DESTINATION.sub_id_counter = 0;
    }

    public Field getField() {
        return field;
    }

    public Type getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public int getSubId() {
        return sub_id;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public double getTimeImpact() {
        return time_impact;
    }

    public double getRangeImpact() {
        return range_impact;
    }

    public boolean isTimeCritical() {
        return time_critical;
    }

    public double getDeadline() {
        return deadline;
    }

    public void setDeadline(double deadline) { //Testing only
        time_critical = true;
        this.deadline = deadline;
    }


    public enum Type {
        START(true, 0, 0),
        DESTINATION(true, 5, 0);

        final boolean mandatory_default;
        final double time_impact_default;
        final double range_impact_default;
        private int sub_id_counter = 0;

        Type(boolean mandatory_default, double time_impact_default, int range_impact_default) {
            this.mandatory_default = mandatory_default;
            this.time_impact_default = time_impact_default;
            this.range_impact_default = range_impact_default;
        }

        int getSubIdAndIncrement() {
            int temp = sub_id_counter;
            sub_id_counter++;
            return temp;
        }
    }
}
