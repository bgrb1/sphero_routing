package sphero.common;

import java.util.ArrayList;
import java.util.List;

public class Map {
    private final List<Field> allFields;

    private final int sizeX;
    private final int sizeY;
    private final double scalingFactor;
    //private final Field[][] grid;
    private final Field[] fieldsById;

    /**
     * @param sizeX         length of the x axis in fields
     * @param sizeY         length of the y axis in fields
     * @param scalingFactor scaling of the map (to translate field distances into real world distances)
     * @param noEdges       set to true, if no edges should be created
     */
    public Map(int sizeX, int sizeY, double scalingFactor, boolean noEdges) {
        assert sizeX > 0;
        assert sizeY > 0;
        assert scalingFactor > 0;
        allFields = new ArrayList<>();
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        //this.grid = new Field[sizeY][sizeX];
        this.scalingFactor = scalingFactor;
        this.fieldsById = new Field[sizeX * sizeY];
        createFields();
        if (!noEdges) connectFields();

    }


    /**
     * creates a grid of fields
     */
    private void createFields() {
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                Field field = new Field(coordinatesToId(x, y), x, y);
                fieldsById[field.getId()] = field;
                //grid[y][x] = field;
                allFields.add(field);
            }
        }
    }

    /**
     * connects all fields with their neighbors
     */
    private void connectFields() {
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                Field f = getField(x, y);
                connect(f, getField(x + 1, y));
                connect(f, getField(x - 1, y));
                connect(f, getField(x, y + 1));
                connect(f, getField(x, y - 1));
            }
        }
    }

    /**
     * connects to fields
     */
    public void connect(Field a, Field b) {
        if (a == null || b == null) return;
        a.addEdge(b);
        b.addEdge(a);
    }

    /**
     * Removes connection between two fields
     */
    public void disconnect(Field a, Field b) {
        a.removeEdge(b);
        b.removeEdge(a);
    }

    /**
     * returns field at a desired position
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public Field getField(int x, int y) {
        return getField(coordinatesToId(x, y));
    }

    /**
     * returns field with an id
     */
    public Field getField(int id) {
        if (id < 0 || id >= sizeX * sizeY) {
            return null;
        }
        return fieldsById[id];
    }

    public List<Field> getAllFields() {
        return allFields;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public double getScalingFactor() {
        return scalingFactor;
    }


    private int coordinatesToId(int x, int y) {
        return x * (sizeY) + y;
    }
}
