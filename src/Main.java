
public class Main {

	public final static String FILE_NAME = "region\\";
	public final static int camerax = 7;
	public final static int cameraz = 10;
	public final static int dist = 5;
	public final static int minY = 0;
	public final static boolean useLimit = true;
	
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
