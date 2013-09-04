
import java.io.File;
import java.util.ArrayList;

public class World {
	ArrayList<Region> regions = new ArrayList<Region>();
	
	public void loadRegionDirectory(String dir) throws Exception{
		String[] files = (new File(dir)).list();
		System.out.printf("Loading %d region files\n", files.length);
		for (int i = 0; i < files.length; i++){
			System.out.printf("Loading %s\n", files[i]);
			Region region = new Region();
			region.readRegionFile(dir.concat(files[i]));
			regions.add(region);
		}
		System.out.println("Done loading regions");
	}
	
	public void writePOVString(PrintWriterGroup pw) {
		int blocksWritten;
		int totalBlocks = 0;
		for (int i = 0; i < regions.size(); i++){
			pw.printf("#debug \"Region %d\\n\"\n", i);
			Region region = regions.get(i);
			blocksWritten = region.writePOVString(pw);
			blocksWritten += region.writeWaterBlocks(pw);
			totalBlocks += blocksWritten;
			System.out.printf("Region %d: %d blocks\n", i, blocksWritten);
		}
		System.out.printf("Total Blocks: %d\n", totalBlocks);
	}

	/*public void writePOVStringOld(PrintWriter pw) {
		for (int i = 0; i < regions.size(); i++){
			Region region = regions.get(i);
			int chunksWritten = region.writePOVString(pw);
			System.out.printf("Region %d/%d - %d chunks\n", i+1, regions.size(), chunksWritten);
			pw.printf("#debug \"Region %d - %d chunks\\n\" \n", i + 1, chunksWritten);
			//pw.println("union { ");
			
			//pw.printf("translate <%d, 0, %d> }\n", region.posX * 512, region.posZ * 512);
		}
		
	}*/
}
