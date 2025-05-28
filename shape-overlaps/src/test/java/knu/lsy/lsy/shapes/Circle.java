package knu.lsy.shapes;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;

public class Circle extends Shape {
    
    public Circle(Point center, double radius) {
        super(center, radius);
    }
    
    // TODO: 학생 과제 - 원의 겹침 감지 알고리즘 구현
    @Override
    public boolean overlaps(Shape other) {
        // 임시 구현: 랜덤하게 true/false 반환
        return Math.random() < 0.3;
        
        // 힌트: 
        // 1. 다른 도형이 원인 경우: 두 원의 중심 거리가 반지름의 합보다 작은지 확인
        // 2. 다른 도형이 다각형인 경우: 다각형의 모든 정점이 원 안에 있는지 확인
        // 3. 또는 다각형의 모든 변이 원과 교차하는지 확인
    }
    
    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", "circle");
        json.put("id", id);
        json.put("center", center.toJSON());
        json.put("radius", radius);
        json.put("color", color);
        return json;
    }
    
    @Override
    public String getShapeType() {
        return "circle";
    }
    
    @Override
    public List<Point> getVertices() {
        // 원의 경계를 근사하는 점들 생성
        List<Point> vertices = new ArrayList<>();
        int numPoints = 32;
        for (int i = 0; i < numPoints; i++) {
            double angle = 2 * Math.PI * i / numPoints;
            double x = center.getX() + radius * Math.cos(angle);
            double y = center.getY() + radius * Math.sin(angle);
            vertices.add(new Point(x, y));
        }
        return vertices;
    }
}
