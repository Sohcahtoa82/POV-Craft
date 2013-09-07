
public class Main {

	public final static String FILE_NAME = "region\\";
	public final static int MAX_Y = 255;
	public final static Point3D camera = new Point3D(130, 77, 145);
	public final static int dist = 100;
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
