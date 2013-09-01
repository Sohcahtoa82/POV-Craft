


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.io.InputStreamReader;

public class Main {

	public final static String FILE_NAME = "region\\";
	public final static int camerax = 7;
	public final static int cameraz = 10;
	public final static int dist = 5;
	public final static int minY = 0;
	public final static boolean useLimit = true;
	
	public Main(String fileName) throws Exception{
		
		World world = new World();
		world.loadRegionDirectory(FILE_NAME);
		long start = System.currentTimeMillis();
		PrintWriterGroup pw = new PrintWriterGroup();
		world.writePOVString(pw);
		pw.closeFiles();
		System.out.printf("Finished - Execution time %d ms\n", System.currentTimeMillis() - start);
		return;
	}
	
	public static void main(String[] args) throws Exception {
		new Main(FILE_NAME);

	}
	

}
