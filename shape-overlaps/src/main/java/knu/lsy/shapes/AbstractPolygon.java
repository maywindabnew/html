package knu.lsy.shapes;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPolygon extends Shape implements Polygon {
    
    public AbstractPolygon(Point center, double radius) {
        super(center, radius);
    }
    
    // 경계 상자 반환 메서드
    public BoundingBox getBoundingBox() {
        List<Point> vertices = getVertices();
        if (vertices.isEmpty()) {
            return new BoundingBox(0, 0, 0, 0);
        }
        
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        
        for (Point vertex : vertices) {
            minX = Math.min(minX, vertex.getX());
            minY = Math.min(minY, vertex.getY());
            maxX = Math.max(maxX, vertex.getX());
            maxY = Math.max(maxY, vertex.getY());
        }
        
        return new BoundingBox(minX, minY, maxX, maxY);
    }
    
    @Override
    public boolean contains(Point point) {
        // 최적화: 경계 상자 검사를 먼저 수행
        BoundingBox box = getBoundingBox();
        if (!box.contains(point)) {
            return false;
        }
        
        // Ray casting 알고리즘
        boolean inside = false;
        List<Point> vertices = getVertices();
        
        for (int i = 0, j = vertices.size() - 1; i < vertices.size(); j = i++) {
            Point vi = vertices.get(i);
            Point vj = vertices.get(j);
            
            if (((vi.getY() > point.getY()) != (vj.getY() > point.getY())) &&
                (point.getX() < (vj.getX() - vi.getX()) * (point.getY() - vi.getY()) / 
                (vj.getY() - vi.getY()) + vi.getX())) {
                inside = !inside;
            }
        }
        
        return inside;
    }
    
    @Override
    public boolean overlaps(Shape other) {
        // 경계 상자 충돌 검사를 먼저 수행 (최적화)
        if (other instanceof Circle) {
            return other.overlaps(this); // Circle 클래스에 위임
        }
        
        // 다각형-다각형 겹침 검사 (SAT 알고리즘)
        if (other instanceof Polygon) {
            Polygon otherPolygon = (Polygon) other;
            
            // 1. 이 다각형의 모든 변에 대한 법선 벡터를 축으로 사용
            List<Point> axes = getAxes();
            
            // 2. 다른 다각형의 모든 변에 대한 법선 벡터를 축으로 사용
            axes.addAll(getAxesFromPolygon(otherPolygon));
            
            // 3. 모든 축에 대해 두 다각형을 투영하고 겹침 여부 확인
            for (Point axis : axes) {
                ProjectionResult proj1 = projectOntoAxis(this.getVertices(), axis);
                ProjectionResult proj2 = projectOntoAxis(otherPolygon.getVertices(), axis);
                
                // 둘 중 하나라도 투영이 겹치지 않으면 겹치지 않음
                if (!projectionOverlap(proj1, proj2)) {
                    return false;
                }
            }
            
            // 모든 축에서 투영이 겹치면 도형이 겹침
            return true;
        }
        
        // 타원과의 겹침 검사
        if (other instanceof Ellipse) {
            return other.overlaps(this);
        }
        
        // 미구현된 도형 타입과의 겹침 검사
        return false;
    }
    
    // 다각형의 모든 변에 대한 법선 벡터 반환
    private List<Point> getAxes() {
        return getAxesFromPolygon(this);
    }
    
    private List<Point> getAxesFromPolygon(Polygon polygon) {
        List<Point> axes = new ArrayList<>();
        List<Point> vertices = polygon.getVertices();
        
        for (int i = 0; i < vertices.size(); i++) {
            Point p1 = vertices.get(i);
            Point p2 = vertices.get((i + 1) % vertices.size());
            
            // 변의 벡터
            double edgeX = p2.getX() - p1.getX();
            double edgeY = p2.getY() - p1.getY();
            
            // 법선 벡터 (시계 방향으로 90도 회전)
            double normalX = -edgeY;
            double normalY = edgeX;
            
            // 정규화
            double length = Math.sqrt(normalX * normalX + normalY * normalY);
            if (length > 1e-9) { // 0으로 나누기 방지
                normalX /= length;
                normalY /= length;
                
                axes.add(new Point(normalX, normalY));
            }
        }
        
        return axes;
    }
    
    // 다각형을 축에 투영하기
    private ProjectionResult projectOntoAxis(List<Point> vertices, Point axis) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        for (Point vertex : vertices) {
            // 점을 축에 투영
            double projection = vertex.getX() * axis.getX() + vertex.getY() * axis.getY();
            
            min = Math.min(min, projection);
            max = Math.max(max, projection);
        }
        
        return new ProjectionResult(min, max);
    }
    
    // 투영이 겹치는지 확인
    private boolean projectionOverlap(ProjectionResult proj1, ProjectionResult proj2) {
        return !(proj1.getMax() < proj2.getMin() || proj2.getMax() < proj1.getMin());
    }
    
    // 투영 결과를 저장하는 내부 클래스
    private static class ProjectionResult {
        private final double min;
        private final double max;
        
        public ProjectionResult(double min, double max) {
            this.min = min;
            this.max = max;
        }
        
        public double getMin() {
            return min;
        }
        
        public double getMax() {
            return max;
        }
    }
}