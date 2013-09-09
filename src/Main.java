
public class Main {

	public final static String FILE_NAME = "region\\";
	public final static int MAX_Y = 255;
	public final static Point3D camera = 
		//new Point3D(50, 81, -123);  
		//new Point3D(384, 80, 352);
		//new Point3D(130, 77, 145);
		//new Point3D(98, 76, 467);
		new Point3D(195, 72, 568);
	public final static int dist = 50;
	public final static int minY = 0;
	public static boolean useLimit = false;
	
	public static void main(String[] args) throws Exception {
		World world = new World();
		world.loadRegionDirectory(FILE_NAME);
		long start = System.currentTimeMillis();
		PrintWriterGroup pw = new PrintWriterGroup();
		world.writePOVString(pw);
		pw.closeFiles();
		System.out.printf("Finished - Execution time %d ms\n", System.currentTimeMillis() - start);
		return;

	}

}
