public class Point3D {
	public int x; 
	public int y; 
	public int z;
	
	public Point3D(int newX, int newY, int newZ) {
		this.x = newX; this.y = newY; this.z = newZ;
	}
	
	public double distanceFrom(Point3D other){
		return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2) + Math.pow(z - other.z, 2));
	}
}