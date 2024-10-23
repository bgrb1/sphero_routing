package sphero.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single cell in the maze grid
 * (= node in a grid graph)
 */
public class Field {
    private final int id;
    private final int x;
    private final int y;
    private final List<Field> neighbors;

    /**
     * @param id field id
     * @param x  x coordinate
     * @param y  y coordinate
     */
    public Field(int id, int x, int y) {
        assert id >= 0;
        assert x >= 0;
        assert y >= 0;
        neighbors = new ArrayList<>();
        this.id = id;
        this.x = x;
        this.y = y;
    }

    /**
     * @return field id
     */
    public int getId() {
        return id;
    }

    /**
     * @return  x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * @return  y coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * @return neighboring fields
     */
    public List<Field> getNeighbors() {
        return neighbors;
    }

    protected void addEdge(Field b) {
        assert b != null;
        if (!neighbors.contains(b))
            neighbors.add(b);
    }

    protected void removeEdge(Field b) {
        neighbors.remove(b);
    }

    @Override
    public String toString() {
        return "Field{" +
                "id=" + id +
                ", x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Field field = (Field) o;
        return id == field.id && x == field.x && y == field.y;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
