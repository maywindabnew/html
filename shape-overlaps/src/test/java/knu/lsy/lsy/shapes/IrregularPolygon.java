package knu.lsy.shapes;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class IrregularPolygon extends AbstractPolygon {
    private List<Point> vertices;
    
    public IrregularPolygon(Point center, double radius, int numVertices) {
        super(center, radius);
        this.vertices = generateIrregularVertices(numVertices);
    }
    
    public IrregularPolygon(List<Point> vertices) {
        super(calculateCenter(vertices), calculateRadius(vertices, calculateCenter(vertices)));
        this.vertices = new ArrayList<>(vertices);
    }
    
    private static Point calculateCenter(List<Point> vertices) {
        if (vertices == null || vertices.isEmpty()) {
            return new Point(0, 0);
        }
        
        double sumX = 0;
        double sumY = 0;
        
        for (Point vertex : vertices) {
            sumX += vertex.getX();
            sumY += vertex.getY();
        }
        
        return new Point(sumX / vertices.size(), sumY / vertices.size());
    }
    
    private static double calculateRadius(List<Point> vertices, Point center) {
        if (vertices == null || vertices.isEmpty()) {
            return 0;
        }
        
        double maxDistSq = 0;
        
        for (Point vertex : vertices) {
            double dx = vertex.getX() - center.getX();
            double dy = vertex.getY() - center.getY();
            double distSq = dx * dx + dy * dy;
            maxDistSq = Math.max(maxDistSq, distSq);
        }
        
        return Math.sqrt(maxDistSq);
    }
    
    private List<Point> generateIrregularVertices(int numVertices) {
        List<Point> points = new ArrayList<>();
        
        // 1. 무작위 각도로 점들 생성
        List<Double> angles = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            angles.add(Math.random() * 2 * Math.PI);
        }
        Collections.sort(angles); // 각도 순으로 정렬
        
        // 2. 각 점에 대해 무작위 반경 적용
        for (int i = 0; i < numVertices; i++) {
            double angle = angles.get(i);
            double r = radius * (0.5 + Math.random() * 0.5);
            double x = center.getX() + r * Math.cos(angle);
            double y = center.getY() + r * Math.sin(angle);
            points.add(new Point(x, y));
        }
        
        // 간단한 컨벡스 헐 생성 (선분 교차 방지)
        return createSimpleConvexHull(points);
    }
    
    private List<Point> createSimpleConvexHull(List<Point> points) {
        // 간단한 컨벡스 헐 구현
        if (points.size() <= 3) return points;
        
        // x 좌표로 정렬
        points.sort(Comparator.comparingDouble(Point::getX));
        
        List<Point> hull = new ArrayList<>();
        
        // 하부 헐
        for (Point p : points) {
            while (hull.size() >= 2 && orientation(hull.get(hull.size() - 2), 
                                                   hull.get(hull.size() - 1), p) <= 0) {
                hull.remove(hull.size() - 1);
            }
            hull.add(p);
        }
        
        // 상부 헐
        int lowerSize = hull.size();
        for (int i = points.size() - 2; i >= 0; i--) {
            Point p = points.get(i);
            while (hull.size() > lowerSize && orientation(hull.get(hull.size() - 2), 
                                                          hull.get(hull.size() - 1), p) <= 0) {
                hull.remove(hull.size() - 1);
            }
            hull.add(p);
        }
        
        // 마지막 점 제거 (중복)
        if (hull.size() > 1) hull.remove(hull.size() - 1);
        
        return hull;
    }
    
    private double orientation(Point p, Point q, Point r) {
        return (q.getX() - p.getX()) * (r.getY() - p.getY()) - 
               (q.getY() - p.getY()) * (r.getX() - p.getX());
    }
    
    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", "irregularPolygon");
        json.put("id", id);
        json.put("center", center.toJSON());
        json.put("radius", radius);
        json.put("color", color);
        
        JSONArray verticesArray = new JSONArray();
        for (Point vertex : vertices) {
            verticesArray.put(vertex.toJSON());
        }
        json.put("vertices", verticesArray);
        
        return json;
    }
    
    @Override
    public String getShapeType() {
        return "irregularPolygon";
    }
    
    @Override
    public List<Point> getVertices() {
        return new ArrayList<>(vertices);
    }
}