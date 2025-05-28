package knu.lsy.shapes;

public class BoundingBox {
    private double minX;
    private double minY;
    private double maxX;
    private double maxY;
    
    public BoundingBox(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }
    
    public double getMinX() {
        return minX;
    }
    
    public double getMinY() {
        return minY;
    }
    
    public double getMaxX() {
        return maxX;
    }
    
    public double getMaxY() {
        return maxY;
    }
    
    public double getWidth() {
        return maxX - minX;
    }
    
    public double getHeight() {
        return maxY - minY;
    }
    
    // 두 경계 상자가 겹치는지 확인
    public boolean overlaps(BoundingBox other) {
        return !(this.maxX < other.minX || 
                 this.minX > other.maxX || 
                 this.maxY < other.minY || 
                 this.minY > other.maxY);
    }
    
    // 점이 경계 상자 내부에 있는지 확인
    public boolean contains(Point point) {
        return point.getX() >= minX && 
               point.getX() <= maxX && 
               point.getY() >= minY && 
               point.getY() <= maxY;
    }
    
    @Override
    public String toString() {
        return "BoundingBox{" +
                "minX=" + minX +
                ", minY=" + minY +
                ", maxX=" + maxX +
                ", maxY=" + maxY +
                '}';
    }
}