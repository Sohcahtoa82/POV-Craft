
import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;

public class World {
	private static final byte NOT_VISITED = 0;
	private static final byte QUEUED = 1;
	private static final byte VISITED = 2;	
	
	enum Direction {
		TOP("Top", "object{ Face scale <%d,1,1> pigment { image_map {png \"%s\"}} rotate 90*x translate <%d, %d + 1, %d> }\n"),
		BOTTOM("Bottom", "object{ Face scale <%d,1,1> pigment { image_map {png \"%s\"}} rotate 90*x translate <%d, %d, %d>}\n"),
		NORTH("North", "object{ Face scale <%d,1,1> pigment { image_map {png \"%s\"}} translate <%d, %d, %d> }\n"),
		SOUTH("South", "object{ Face scale <%d,1,1> pigment { image_map {png \"%s\"}} translate <%d, %d, %d + 1>}\n"),
		EAST("East", "object{ Face scale <%d,1,1> pigment { image_map {png \"%s\"}} rotate -90*y translate <%d + 1, %d, %d> }\n"),
		WEST("West", "object{ Face scale <%d,1,1> pigment { image_map {png \"%s\"}} rotate -90*y translate <%d, %d, %d> }\n");
		
		String value;
		String POVString;
		Direction(String newValue, String newPOVString){
			value = newValue;
			POVString = newPOVString;
		}
	}
	
	ArrayList<Region> regions = new ArrayList<Region>();
	private LinkedList<Point3D> queue = new LinkedList<Point3D>();
	
	public void loadRegionDirectory(String dir) throws Exception{
		String[] files = (new File(dir)).list(new FilenameFilter(){
			@Override
			public boolean accept(File file, String filename) {
				return filename.toLowerCase().endsWith(".mca");
			}
		});
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
		
		//public int writeFaces(PrintWriterGroup pw, int adjX, int adjY, int adjZ, int runX, int runY, int runZ, Direction direction) {
		
		blocksWritten = writeFaces(pw, 0, 1, 0, 1, 0, Direction.TOP); //writeTopFaces(pw);
		System.out.printf("Top faces: %d\n", blocksWritten);
		totalBlocks += blocksWritten;
		
		blocksWritten = writeFaces(pw, 0, -1, 0, 1, 0, Direction.BOTTOM); //writeTopFaces(pw);
		System.out.printf("Bottom faces: %d\n", blocksWritten);
		totalBlocks += blocksWritten;
		
		blocksWritten = writeFaces(pw, 0, 0, -1, 1, 0, Direction.NORTH);
		System.out.printf("North faces: %d\n", blocksWritten);
		totalBlocks += blocksWritten;
		
		blocksWritten = writeFaces(pw, 0, 0, 1, 1, 0, Direction.SOUTH);
		System.out.printf("South faces: %d\n", blocksWritten);
		totalBlocks += blocksWritten;
		
		blocksWritten = writeFaces(pw, 1, 0, 0, 0, 1, Direction.EAST); //writeTopFaces(pw);
		System.out.printf("East faces: %d\n", blocksWritten);
		totalBlocks += blocksWritten;
		
		blocksWritten = writeFaces(pw, -1, 0, 0, 0, 1, Direction.WEST); //writeTopFaces(pw);
		System.out.printf("West faces: %d\n", blocksWritten);
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
			pw.water.printf("hollow translate <0, -0.2, 0> material { waterMaterial } }\n ");
		return blocksWritten;
	}
		
	public int writeFaces(PrintWriterGroup pw, int adjX, int adjY, int adjZ, int runX, int runZ, Direction direction) {
		
		queue.add(Main.camera);
		resetVisitedArrays();

		int blocksWritten = 0;
		
		System.out.printf("Writing %s faces...", direction.value.toLowerCase());
		while (!queue.isEmpty()){
			//System.out.printf("Queue size: %d\n", queue.size());
			Point3D point = queue.poll();
			if (Main.useLimit && point.distanceFrom(Main.camera) > Main.dist)
				continue;
			int x = point.x;
			int y = point.y;
			int z = point.z;
			if (getVisited(x, y, z) == VISITED)
				continue;
			BlockType blockType = getBlockType(x, y, z);
			if (blockType == null)
				continue;
			
			BlockType adjacent = getBlockType(x + adjX, y + adjY, z + adjZ);	
			if (adjacent != null && !adjacent.isTransparent())
				continue;
			
			addNeighbors(x, y, z, blockType);
			if (blockType != BlockType.AIR && !blockType.isWater()){
				// Find the beginning of the run
				while (blockType == this.getBlockType(x - runX, y, z - runZ)){
					x -= runX;
					z -= runZ;
				}
				int run = 0;
				while (blockType == this.getBlockType(x + run * runX, y, z + run * runZ)){
					setVisited(x + run * runX, y, z + run * runZ, VISITED);
					addNeighbors(x + run * runX, y, z + run * runZ, blockType);
					run++;
				}
				//setVisited(x + run * runX, y, z + run * runZ, VISITED);
				
				if (blockType.isTransparent() && blockType != BlockType.LEAVES)
					continue;
				
				String texture = "";
				switch(direction){
				case NORTH:
				case SOUTH:
					texture = getNorthSouthTexture(blockType);
					break;
				case WEST:
				case EAST:
					texture = getEastWestTexture(blockType);
					break;
				case TOP:
					texture = getTopTexture(blockType);
					break;
				case BOTTOM:
					texture = getBottomTexture(blockType);
					break;
				}
				//pw.printf("object { %sFace(\"%s\", %d, <%d, %d, %d>) }\n", direction.value, texture, run, x, y, z);
				pw.printf(direction.POVString, run, texture, x, y, z);
				
				//this.writeFace(blockType, run, pw, x, y, z);
				
				blocksWritten++;
				if (blocksWritten % 100000 == 0)
					System.out.print(".");
				if (blocksWritten % 10000 == 0) {
					pw.printf("#debug \"%d\\n\"\n", blocksWritten);
				}
			}
		}
		
		System.out.printf("done\n");
		return blocksWritten;
	}
	
	public BlockType getBlockType(int x, int y, int z){		
		Region region = getContainingRegion(x, z);
		if (region == null || y > Main.MAX_Y)
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
	
	public String getNorthSouthTexture(BlockType type){
		// TODO: Make these use official Minecraft texture file names
		switch (type){
		case STONE:
			return "stone.png";
		case GRASS_BLOCK:
			// TODO: Make grass properly use different colors
			return "grass_side.png";
		case DIRT:
			return "dirt.png";
		case COBBLESTONE:
			return "cobblestone.png";
		case BEDROCK:
			return "bedrock.png";
		case SAND: // sand
			return "sand.png";
		case WOOD:  // wood
			return "wood_side.png";
		case LEAVES: // leaves
			return "leaves.png";
		default: 
			if (!BlockType.unimplemented[type.blockType]){
				System.out.printf("Unimplemented block: %d (%s)\n", type.blockType, type.toString());
				BlockType.unimplemented[type.blockType] = true;
			}
			return "stone.png";
		}
	}
	
	public String getEastWestTexture(BlockType type){
		// TODO: Change this
		return getNorthSouthTexture(type);
	}
	
	public String getTopTexture(BlockType type){
		// TODO: Make these use official Minecraft texture file names
		switch (type){
		case STONE:
			return "stone.png";
		case GRASS_BLOCK:
			// TODO: Make grass properly use different colors
			return "grass_top.png";
		case DIRT:
			return "dirt.png";
		case COBBLESTONE:
			return "cobblestone.png";
		case BEDROCK:
			return "bedrock.png";
		case SAND: // sand
			return "sand.png";
		case WOOD:  // wood
			return "wood_top.png";
		case LEAVES: // leaves
			return "leaves.png";
		default: 
			if (!BlockType.unimplemented[type.blockType]){
				System.out.printf("Unimplemented block: %d (%s)\n", type.blockType, type.toString());
				BlockType.unimplemented[type.blockType] = true;
			}
			return "stone.png";
		}
	}
	
	public String getBottomTexture(BlockType type){
		// TODO: Change this to use the actual texture
		return getTopTexture(type);
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
