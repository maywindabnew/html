package knu.lsy.shapes;

import java.util.List;

public interface Polygon {
    List<Point> getVertices();
    boolean contains(Point point);
}