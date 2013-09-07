
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;

public class World {
	private static final byte NOT_VISITED = 0;
	private static final byte QUEUED = 1;
	private static final byte VISITED = 2;	
	
	ArrayList<Region> regions = new ArrayList<Region>();
	private LinkedList<Point3D> queue = new LinkedList<Point3D>();
	
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
		blocksWritten = writeWaterBlocks(pw);
		System.out.printf("Water blocks: %d\n", blocksWritten);
		totalBlocks += blocksWritten;
		blocksWritten = writeRegularBlocks(pw);
		System.out.printf("Regular blocks: %d\n", blocksWritten);
		totalBlocks += blocksWritten;
		System.out.printf("Total Blocks: %d\n", totalBlocks);
	}
	
	private void resetVisitedArrays(){
		for (Region region : regions){
			region.visited = new byte[512][256][512];
		}
	}
	
	public int writeWaterBlocks(PrintWriterGroup pw) {

		int blocksWritten = 0;
		boolean hasCreatedMesh = false;
		
		queue.add(Main.camera);
		resetVisitedArrays();
		
		System.out.printf("Writing water blocks...");
		while (!queue.isEmpty()){
			//System.out.printf("Queue size: %d\n", queue.size());
			Point3D point = queue.poll();
			if (Main.useLimit && point.distanceFrom(Main.camera) > Main.dist)
				continue;
			int x = point.x;
			int y = point.y;
			int z = point.z;
			BlockType blockType = getBlockType(x, y, z);
			if (blockType == null)
				continue;
			if (getVisited(x, y, z) == VISITED && !blockType.isTransparent())
				continue;
			addNeighbors(x, y, z, blockType);
			if (blockType.isWater() && getVisited(x, y, z) != VISITED){
				if (!hasCreatedMesh){
					pw.water.write("mesh {\n");
					hasCreatedMesh = true;
				}
				addWaterBlock(pw.water, x, y, z);
				blocksWritten++;
				if (blocksWritten % 100000 == 0)
					System.out.print(".");
			}
		}

		System.out.printf("done\n");
		if (hasCreatedMesh)
			pw.water.printf("hollow \n material { waterMaterial } }\n ");
		return blocksWritten;
	}
		
	
	public int writeRegularBlocks(PrintWriterGroup pw) {
		
		queue.add(Main.camera);
		resetVisitedArrays();

		int blocksWritten = 0;
		
		System.out.printf("Writing regular blocks...");
		pw.write("union {\n");
		while (!queue.isEmpty()){
			//System.out.printf("Queue size: %d\n", queue.size());
			Point3D point = queue.poll();
			if (Main.useLimit && point.distanceFrom(Main.camera) > Main.dist)
				continue;
			int x = point.x;
			int y = point.y;
			int z = point.z;
			BlockType blockType = getBlockType(x, y, z);
			if (blockType == null)
				continue;
			if (getVisited(x, y, z) == VISITED && !blockType.isTransparent())
				continue;
			addNeighbors(x, y, z, blockType);
			if (blockType != BlockType.AIR && !blockType.isWater() && getVisited(x, y, z) != VISITED){
				// Find the beginning of the run
				while (blockType == this.getBlockType(x - 1, y, z))
					x--;
				int run = 1;
				while (blockType == this.getBlockType(x + run, y, z)){
					setVisited(x + run, y, z, VISITED);
					addNeighbors(x + run, y, z, blockType);
					run++;
				}

				this.writePOVBlock(blockType, run, pw, x, y, z);
				
				blocksWritten++;
				if (blocksWritten % 100000 == 0)
					System.out.print(".");
				if (blocksWritten % 10000 == 0) {
					pw.printf("#debug \"%d\\n\"\n", blocksWritten);
				}
			}
		}
		
		System.out.printf("done\n");
		pw.printf("\n  } \n");
		return blocksWritten;
	}
	
	public BlockType getBlockType(int x, int y, int z){		
		Region region = getContainingRegion(x, z);
		if (region == null)
			return null;
		
		int chunkX = (int)Math.floor(x / 16.0);
		int chunkZ = (int)Math.floor(z / 16.0);
		chunkX = chunkX - (region.posX * 32);
		chunkZ = chunkZ - (region.posZ * 32);
		
		if (x < 0)
			x = x % 16 + 16;
		if (z < 0)
			z = z % 16 + 16;
		
		if (region.chunk[chunkX][chunkZ] != null){
			return region.chunk[chunkX][chunkZ].blockType[x%16][y][z%16];
		} else {
			return null;
		}
	}
	
	private Region getContainingRegion(int x, int z){
		int regionX = x / 512;
		int regionZ = z / 512;
		
		if (x < 0)
			regionX--;
		if (z < 0)
			regionZ--;
		return getRegion(regionX, regionZ);
	}
	
	private Region getRegion(int x, int z){
		for (Region region : regions){
			if (region.posX == x && region.posZ == z)
				return region;
		}
		return null;
	}
	
	public void addWaterBlock(PrintWriter pw, int x, int y, int z){
		BlockType block;

		block = getBlockType(x-1, y, z);
		if (block != null && !block.isWater()){
			pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
					x - 0.00001, y + 1, z - 0.00001,		x - 0.00001, y + 1, z + 1.00001, 		x - 0.00001, y, z - 0.00001);
			pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
					x - 0.00001, y, z - 0.00001,			x - 0.00001, y, z + 1.00001, 			x - 0.00001, y + 1, z + 1.00001);
		}
	

		block = getBlockType(x+1, y, z);
		if (block != null && !block.isWater()){
			pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
					x + 1.00001, y + 1, z - 0.00001,			x + 1.00001, y + 1, z + 1.00001, 			x + 1.00001, y, z - 0.00001);
			pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
					x + 1.00001, y + 1, z + 1.00001,			x + 1.00001, y, z - 0.00001, 			x + 1.00001, y, z + 1.00001);
		}

		block = getBlockType(x, y-1, z);
		if (block != null && !block.isWater()){
			pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
					x - 0.00001, y, z - 0.00001,			x + 1.00001, y, z - 0.00001, 			x - 0.00001, y, z + 1.00001);
			pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
					x + 1.00001, y, z - 0.00001,			x - 0.00001, y, z + 1.00001, 			x + 1.00001, y, z + 1.00001);
		}
	

		block = getBlockType(x, y+1, z);
		if (block != null && !block.isWater()){
			pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
					x - 0.00001, y+1, z - 0.00001,			x + 1.00001, y+1, z - 0.00001, 			x - 0.00001, y+1, z + 1.00001);
			pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n" ,
					x + 1.00001, y+1, z - 0.00001,			x - 0.00001, y+1, z + 1.00001, 			x + 1.00001, y+1, z + 1.00001);
		}
	

		block = getBlockType(x, y, z-1);
		if (block != null && !block.isWater()){
			pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
					x-0.00001, y, z - 0.00001,			x + 1.00001, y, z- 0.00001, 			x - 0.00001, y+1, z- 0.00001);
			pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
					x + 1.00001 , y, z- 0.00001,			x - 0.00001, y + 1, z- 0.00001, 			x + 1.00001, y + 1, z- 0.00001);
		}
	

		block = getBlockType(x, y, z+1);
		if (block != null && !block.isWater()){
			pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
					x-0.00001, y, z + 1.00001,			x + 1.00001, y, z + 1.00001, 			x - 0.00001, y+1, z + 1.00001);
			pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> }\n",
					x + 1.00001 , y, z + 1.00001,			x - 0.00001, y + 1, z + 1.00001, 			x + 1.00001, y + 1, z + 1.00001);
		}
		
	}
	
	public void addNeighbors(int x, int y, int z, BlockType blockType){
		if (!blockType.isTransparent())
			return;

		if (getVisited(x - 1, y, z) == NOT_VISITED) {
			queue.add(new Point3D(x - 1,y, z));
			setVisited(x-1, y, z, QUEUED);
		}
	

		if (getVisited(x + 1, y, z) == NOT_VISITED) {
			queue.add(new Point3D(x + 1,y, z));
			setVisited(x+1, y, z, QUEUED);
		}
	

		if (getVisited(x, y - 1, z) == NOT_VISITED) {
			queue.add(new Point3D(x, y - 1, z));
			setVisited(x, y-1, z, QUEUED);
		}
	

		if (getVisited(x, y + 1, z) == NOT_VISITED) {
			queue.add(new Point3D(x, y + 1, z));
			setVisited(x, y+1, z, QUEUED);
		}
	

		if (getVisited(x, y, z - 1) == NOT_VISITED) {
			queue.add(new Point3D(x,y, z - 1));
			setVisited(x, y, z-1, QUEUED);
		}
	

		if (getVisited(x, y, z + 1) == NOT_VISITED) {
			queue.add(new Point3D(x, y, z + 1));
			setVisited(x, y, z+1, QUEUED);
		}
	}
	
	private int getVisited(int x, int y, int z){
		Region region = getContainingRegion(x, z);
		if (region == null || y > Main.MAX_Y)
			return VISITED;
		return region.visited[Math.abs(x % 512)][y][Math.abs(z % 512)];
	}
	
	private void setVisited(int x, int y, int z, byte visited){
		Region region = getContainingRegion(x, z);
		if (region != null || y > Main.MAX_Y)
			region.visited[Math.abs(x % 512)][y][Math.abs(z % 512)] = visited;
	}
	
	public void writePOVBlock(BlockType type, int run, PrintWriterGroup pw, int x, int y, int z){
		switch (type){
		case AIR:  // Air
			return;
		case STONE:  // stone
			pw.printf("object { MyBoxSimple(\"stone.png\", %d) ", run);
			printTranslate(pw, x, y, z, run);
			break;
		case GRASS_BLOCK:  // grass
			pw.printf("object { MyBoxComplex(\"grass_top.png\", \"grass_side.png\", \"dirt.png\", %d) ", run);
			printTranslate(pw, x, y, z, run);
			break;
		case DIRT:  // dirt
			pw.printf("object { MyBoxSimple(\"dirt.png\", %d) ", run);
			printTranslate(pw, x, y, z, run);
			break;
		case COBBLESTONE:  // cobblestone
			printBox(pw, run);
			pw.print("pigment {color rgb <0.25, 0.25, 0.25> } ");
			printTranslate(pw, x, y, z, run);
			break;
		case BEDROCK:
			pw.printf("object { MyBoxSimple(\"bedrock.png\", %d) ", run);
			printTranslate(pw, x, y, z, run);
			break;
		// TODO: Properly handle moving water
		case WATER_MOVING:  // Moving water
			//printBox(pw, run);
			//pw.print("pigment {color rgb <0.0, 0.0, 1> } ");
			//printTranslate(pw, x, y, z, run);
			//break;
		case WATER_STATIONARY:  // water
			//pw.printf("object { Water(%d, <%d, %d, %d>) ", run, x + this.posX*16, y-run + 1, z+this.posZ*16);
			pw.water.printf("object { Water(%d, <%d, %d, %d>) ", run, x, y, z);
			printTranslate(pw.water, x, y, z, run);
			break;
		case SAND: // sand
			pw.printf("object { MyBoxSimple(\"sand.png\", %d) ", run);
			printTranslate(pw, x, y, z, run);
			break;
		case WOOD:  // wood
			pw.printf("object { MyBoxComplex(\"wood_top.png\", \"wood_side.png\", \"wood_top.png\", %d) ", run);
			printTranslate(pw, x, y, z, run);
			break;
		case LEAVES: // leaves
			pw.printf("object { MyBoxSimple(\"leaves.png\", %d) ", run);
			printTranslate(pw, x, y, z, run);
			break;
		default: 
			if (!BlockType.unimplemented[type.blockType]){
				//System.out.printf("Unimplemented block: %d (%s)\n", type.blockType, type.toString());
				BlockType.unimplemented[type.blockType] = true;
			}
			return;
			//printBox(pw, run);
			//pw.print("pigment {color rgb 1 } ");
			//break;
		}
	}
	private void printTranslate(PrintWriterGroup pw, int x, int y, int z, int run) {
		pw.printf("translate <%d, %d, %d> }\n", x, y, z);
	}
	private void printTranslate(PrintWriter pw, int x, int y, int z, int run) {
		pw.printf("translate <%d, %d, %d> }\n", x, y, z);
	}
	private void printBox(PrintWriterGroup pw, int run){
		pw.printf("box { 0, <1, %d, 1> ", run);
	}
}
