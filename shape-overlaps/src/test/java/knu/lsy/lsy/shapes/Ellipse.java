package knu.lsy.shapes;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;

public class Ellipse extends Shape {
    private double semiMajorAxis; // 장반경
    private double semiMinorAxis; // 단반경
    private double rotation; // 회전 각도 (라디안)
    private static final double EPSILON = 1e-9;
    private static final int APPROXIMATION_SEGMENTS = 16; // 타원 근사치를 위한 점의 수
    
    public Ellipse(Point center, double semiMajorAxis, double semiMinorAxis, double rotation) {
        super(center, Math.max(semiMajorAxis, semiMinorAxis));
        this.semiMajorAxis = semiMajorAxis;
        this.semiMinorAxis = semiMinorAxis;
        this.rotation = rotation;
    }
    
    public double getSemiMajorAxis() {
        return semiMajorAxis;
    }
    
    public double getSemiMinorAxis() {
        return semiMinorAxis;
    }
    
    public double getRotation() {
        return rotation;
    }
    
    public BoundingBox getBoundingBox() {
        // 회전된 타원의 경계 상자 계산
        double a = semiMajorAxis;
        double b = semiMinorAxis;
        double cosTheta = Math.cos(rotation);
        double sinTheta = Math.sin(rotation);
        
        // 타원의 회전을 고려한 경계 상자의 반경 계산
        double boundingWidth = Math.sqrt(a * a * cosTheta * cosTheta + b * b * sinTheta * sinTheta);
        double boundingHeight = Math.sqrt(a * a * sinTheta * sinTheta + b * b * cosTheta * cosTheta);
        
        return new BoundingBox(
            center.getX() - boundingWidth,
            center.getY() - boundingHeight,
            center.getX() + boundingWidth,
            center.getY() + boundingHeight
        );
    }
    
    @Override
    public boolean overlaps(Shape other) {
        // 경계 상자 충돌 검사를 먼저 수행 (최적화)
        BoundingBox thisBox = this.getBoundingBox();
        BoundingBox otherBox = null;
        
        if (other instanceof Circle) {
            Circle circle = (Circle) other;
            otherBox = circle.getBoundingBox();
        } else if (other instanceof AbstractPolygon) {
            AbstractPolygon polygon = (AbstractPolygon) other;
            otherBox = polygon.getBoundingBox();
        } else if (other instanceof Ellipse) {
            Ellipse otherEllipse = (Ellipse) other;
            otherBox = otherEllipse.getBoundingBox();
        }
        
        if (otherBox != null && !thisBox.overlaps(otherBox)) {
            return false;
        }
        
        // 타원-타원 겹침 검사
        if (other instanceof Ellipse) {
            Ellipse otherEllipse = (Ellipse) other;
            
            // 두 타원이 모두 회전이 없고 축이 정렬된 경우에 대한 간단한 테스트
            if (Math.abs(this.rotation) < EPSILON && Math.abs(otherEllipse.rotation) < EPSILON) {
                return simpleEllipseOverlap(otherEllipse);
            }
            
            // 하나 이상의 타원이 회전된 경우, 다각형 근사치 사용
            return ellipseOverlapUsingPolygonApproximation(otherEllipse);
        }
        
        // 타원-원 겹침 검사
        if (other instanceof Circle) {
            Circle circle = (Circle) other;
            
            // 원은 특수한 타원으로 볼 수 있음
            if (Math.abs(this.rotation) < EPSILON) {
                return circleEllipseOverlap(circle);
            }
            
            // 타원이 회전된 경우, 다각형 근사치 사용
            return ellipseCircleOverlapUsingPolygonApproximation(circle);
        }
        
        // 타원-다각형 겹침 검사
        if (other instanceof Polygon) {
            // 타원을 다각형으로 근사하여 다각형-다각형 충돌 감지 사용
            Polygon approximatedPolygon = approximateAsPolygon();
            return approximatedPolygon.overlaps(other);
        }
        
        // 미구현된 도형 타입과의 겹침 검사
        return false;
    }
    
    // 회전이 없는 타원 간의 간단한 겹침 테스트
    private boolean simpleEllipseOverlap(Ellipse other) {
        double dx = this.center.getX() - other.center.getX();
        double dy = this.center.getY() - other.center.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // 두 타원의 중심 거리가 두 타원의 반경 합보다 작으면 겹친다고 간주
        // 단순화된 테스트이므로 완벽하지 않지만 빠른 근사 테스트
        double sumOfSemiAxis = this.semiMajorAxis + other.semiMajorAxis;
        return distance < sumOfSemiAxis;
    }
    
    // 원과 회전되지 않은 타원의 겹침 테스트
    private boolean circleEllipseOverlap(Circle circle) {
        // 타원 좌표계에서 원의 중심 위치 계산
        double dx = circle.getCenter().getX() - this.center.getX();
        double dy = circle.getCenter().getY() - this.center.getY();
        
        // 타원의 자체 좌표계에서 거리 검사
        double normalizedDistance = 
            (dx * dx) / (this.semiMajorAxis * this.semiMajorAxis) + 
            (dy * dy) / (this.semiMinorAxis * this.semiMinorAxis);
            
        // 원의 중심이 타원 내부에 있는 경우
        if (normalizedDistance <= 1.0) {
            return true;
        }
        
        // 원이 타원을 완전히 감싸는 경우
        double circleRadius = circle.getRadius();
        if (circleRadius >= this.semiMajorAxis && circleRadius >= this.semiMinorAxis) {
            double centerDistance = Math.sqrt(dx * dx + dy * dy);
            return centerDistance <= circleRadius;
        }
        
        // 타원을 다각형으로 근사하여 검사
        return ellipseCircleOverlapUsingPolygonApproximation(circle);
    }
    
    // 다각형 근사치를 사용한 타원-타원 겹침 검사
    private boolean ellipseOverlapUsingPolygonApproximation(Ellipse other) {
        Polygon thisPolygon = this.approximateAsPolygon();
        Polygon otherPolygon = other.approximateAsPolygon();
        
        if (thisPolygon instanceof AbstractPolygon && otherPolygon instanceof Shape) {
            return ((AbstractPolygon) thisPolygon).overlaps((Shape) otherPolygon);
        }
        
        return false;
    }
    
    // 다각형 근사치를 사용한 타원-원 겹침 검사
    private boolean ellipseCircleOverlapUsingPolygonApproximation(Circle circle) {
        Polygon thisPolygon = this.approximateAsPolygon();
        
        if (thisPolygon instanceof AbstractPolygon) {
            return ((AbstractPolygon) thisPolygon).overlaps(circle);
        }
        
        return false;
    }
    
    // 타원을 다각형으로 근사
    public Polygon approximateAsPolygon() {
        List<Point> vertices = getApproximationVertices();
        return new IrregularPolygon(vertices);
    }
    
    // 타원의 근사 정점 목록 반환
    private List<Point> getApproximationVertices() {
        List<Point> vertices = new java.util.ArrayList<>();
        
        for (int i = 0; i < APPROXIMATION_SEGMENTS; i++) {
            double angle = 2.0 * Math.PI * i / APPROXIMATION_SEGMENTS;
            double x = semiMajorAxis * Math.cos(angle);
            double y = semiMinorAxis * Math.sin(angle);
            
            // 회전 적용
            double rotatedX = x * Math.cos(rotation) - y * Math.sin(rotation);
            double rotatedY = x * Math.sin(rotation) + y * Math.cos(rotation);
            
            vertices.add(new Point(center.getX() + rotatedX, center.getY() + rotatedY));
        }
        
        return vertices;
    }
    
    // 점이 타원 내부에 있는지 확인
    public boolean contains(Point point) {
        // 타원 좌표계로 변환
        double dx = point.getX() - center.getX();
        double dy = point.getY() - center.getY();
        
        // 회전 역변환
        double cosTheta = Math.cos(-rotation);
        double sinTheta = Math.sin(-rotation);
        double rotatedX = dx * cosTheta - dy * sinTheta;
        double rotatedY = dx * sinTheta + dy * cosTheta;
        
        // 타원 방정식을 사용한 내부 판정
        return (rotatedX * rotatedX) / (semiMajorAxis * semiMajorAxis) + 
               (rotatedY * rotatedY) / (semiMinorAxis * semiMinorAxis) <= 1.0;
    }
    
    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", "ellipse");
        json.put("id", id);
        json.put("center", center.toJSON());
        json.put("semiMajorAxis", semiMajorAxis);
        json.put("semiMinorAxis", semiMinorAxis);
        json.put("rotation", rotation);
        json.put("color", color);
        
        JSONArray verticesArray = new JSONArray();
        List<Point> vertices = getApproximationVertices();
        for (Point vertex : vertices) {
            verticesArray.put(vertex.toJSON());
        }
        json.put("vertices", verticesArray);
        
        return json;
    }
    
    @Override
    public String getShapeType() {
        return "ellipse";
    }
    
    @Override
    public List<Point> getVertices() {
        return getApproximationVertices();
    }
}