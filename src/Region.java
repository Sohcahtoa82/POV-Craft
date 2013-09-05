
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;


public class Region {
	
	public Chunk[][] chunk;
	public int posX;
	public int posZ;
	public int numChunks = 0;
	public int lastChunkX;
	public int lastChunkZ;
	
	private byte[][][] visited = new byte[512][256][512];
	private LinkedList<Point> queue = new LinkedList<Point>();
	
	private static final int NOT_VISITED = 0;
	private static final int QUEUED = 1;
	private static final int VISITED = 2;
	
	
	public void readRegionFile(String fileName) throws Exception{
		chunk = new Chunk[32][32];
		int offset, sectorCount, readInt;
		//InputStream fileStream = new FileInputStream(fileName);
		RandomAccessFile dataStream = new RandomAccessFile(fileName, "r");
		//InputStream gzipStream = new GZIPInputStream(fileStream);
		for (int z = 0; z < 32; z++){
			for (int x = 0; x < 32; x++){
				readInt = dataStream.readInt();
				if (readInt != 0){
					offset = (readInt & 0xFFFFFF00) >> 8;
					sectorCount = readInt & 0x000000FF;
					//System.out.printf("Chunk at x=%d, z=%d has data %d offset %d and count %d\n", x, z, readInt, offset, sectorCount);
					chunk[x][z] = new Chunk();
					chunk[x][z].offset = offset;
					chunk[x][z].sectorCount = sectorCount;
					lastChunkX = x;
					lastChunkZ = z;
				}
			}
		}
		for (int x = 0; x < 32; x++){
			for (int z = 0; z < 32; z++){
				if (chunk[x][z] == null){
					continue;
				}
				numChunks++;
				dataStream.seek(chunk[x][z].offset * 4096);
				int length = dataStream.readInt();
				@SuppressWarnings("unused")
				byte compType = dataStream.readByte();
				//System.out.printf("\nChunk at x=%d, z=%d has length %d and compType %d Data:\n", x, z, length, compType);
				byte[] data = new byte[length];
				dataStream.read(data);
				//System.out.print(chunk[x][z].data);
				Inflater inflator = new Inflater();
				inflator.setInput(data);
			    ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
			    byte[] buf = new byte[1024];
			    while (!inflator.finished()) {
			        try {
			            int count = inflator.inflate(buf);
			            bos.write(buf, 0, count);
			        } catch (DataFormatException e) {
			        }
			    }
			    try {
			        bos.close();
			    } catch (IOException e) {
			    }
			    
			    // Get the decompressed data
			    chunk[x][z].decompressedData = bos.toByteArray();
			    chunk[x][z].loadTag();
			    NbtTagCompound levelTag = (NbtTagCompound)chunk[x][z].tag.tags.get("Level");
			    NbtTagInt pos = (NbtTagInt)levelTag.tags.get("xPos");
			    this.posX = pos.value / 32;
			    if (pos.value < 0)
			    	this.posX--;
			    chunk[x][z].posX = pos.value;
			    pos = (NbtTagInt)levelTag.tags.get("zPos");
			    this.posZ = pos.value / 32;
			    if (pos.value < 0)
			    	this.posZ--;
			    chunk[x][z].posZ = pos.value;
			    chunk[x][z].decompressedData = null;
			}
		}
		
	}
	class Point {
		public int x; public int y; public int z;
		public Point(int newX, int newY, int newZ) {
			this.x = newX; this.y = newY; this.z = newZ;
		}
	};
	
	public int writeWaterBlocks(PrintWriterGroup pw) {
		visited = new byte[512][256][512];

		int blocksWritten = 0;
		boolean hasCreatedMesh = false;
		
		// TODO: Place a starting point based on camera position instead of trying to add each chunk from the sky
		for (int x = 0; x < 32; x++) {
			for (int y = 0; y < 32; y++){
				if (chunk[x][y] != null){
					visited[x*16][255][y*16] = QUEUED;
					queue.add(new Point(x*16, 255, y*16));
				}
			}
		}
		
		while (!queue.isEmpty()){
			//System.out.printf("Queue size: %d\n", queue.size());
			Point point = queue.poll();
			int chunkX = point.x / 16;
			int chunkZ = point.z / 16;
			if (chunk[chunkX][chunkZ] == null)
				continue;
			// TODO: Fix this math.  It doesn't account for distance properly
			if (Main.useLimit && ((Math.pow(chunk[chunkX][chunkZ].posX - Main.camerax,2) + Math.pow(chunk[chunkX][chunkZ].posZ - Main.cameraz, 2)) > (Main.dist * Main.dist)))
				continue;
			int x = point.x;
			int y = point.y;
			int z = point.z;
			BlockType blockType = chunk[chunkX][chunkZ].blockType[x%16][y][z%16];
			if (visited[x][y][z] == VISITED && !blockType.isTransparent())
				continue;
			addNeighbors(x, y, z, blockType);
			if (blockType.isWater() && visited[x][y][z] != VISITED){
				if (!hasCreatedMesh){
					pw.water.write("mesh {\n");
					hasCreatedMesh = true;
				}
				addWaterBlock(pw.water, x, y, z);
				blocksWritten++;
			}
		}

		if (hasCreatedMesh)
			pw.water.printf("hollow translate <%d, -0.2, %d>\n material { waterMaterial } }\n ", this.posX * 512, this.posZ * 512);
		return blocksWritten;
	}
		
	
	public int writePOVString(PrintWriterGroup pw) {
		
		visited = new byte[512][256][512];

		int blocksWritten = 0;
		
		// TODO: Place a starting point based on camera position instead of trying to add each chunk from the sky
		for (int x = 0; x < 32; x++) {
			for (int y = 0; y < 32; y++){
				if (chunk[x][y] != null){
					visited[x*16][255][y*16] = QUEUED;
					queue.add(new Point(x*16, 255, y*16));
				}
			}
		}
		pw.write("union {\n");
		while (!queue.isEmpty()){
			//System.out.printf("Queue size: %d\n", queue.size());
			Point point = queue.poll();
			int chunkX = point.x / 16;
			int chunkZ = point.z / 16;
			if (chunk[chunkX][chunkZ] == null)
				continue;
			// TODO: Fix this math.  It doesn't account for distance properly
			if (Main.useLimit && ((Math.pow(chunk[chunkX][chunkZ].posX - Main.camerax,2) + Math.pow(chunk[chunkX][chunkZ].posZ - Main.cameraz, 2)) > (Main.dist * Main.dist)))
				continue;
			int x = point.x;
			int y = point.y;
			int z = point.z;
			BlockType blockType = chunk[chunkX][chunkZ].blockType[x%16][y][z%16];
			if (visited[point.x][point.y][point.z] == VISITED && !blockType.isTransparent())
				continue;
			
			/*if (blockType == 9 || blockType == 8) {
				System.out.println();
			}*/
			addNeighbors(x, y, z, blockType);
			if (blockType != BlockType.AIR && !blockType.isWater() && visited[x][y][z] != VISITED){
				// Find the beginning of the run
				while (x > 0 && blockType == this.getBlockType(x - 1, y, z))
					x--;
				int run = 1;
				while (x + run < 512 && blockType == this.getBlockType(x + run, y, z)){
					visited[x + run][y][z] = VISITED;
					addNeighbors(x + run, y, z, blockType);
					run++;
				}

				this.writePOVBlock(blockType, run, pw, x, y, z);
				
				blocksWritten++;
				if (blocksWritten % 10000 == 0) {
					pw.printf("#debug \"%d\\n\"\n", blocksWritten);
				}
			}
		}

		pw.printf("\n translate <%d, 0, %d> } \n", this.posX * 512, this.posZ * 512);
		System.out.printf("\n translate <%d, 0, %d> } \n", this.posX * 512, this.posZ * 512);
		return blocksWritten;
	}
	
	public BlockType getBlockType(int x, int y, int z){
		int chunkX = x / 16;
		int chunkZ = z / 16;
		if (chunk[chunkX][chunkZ] != null){
			return chunk[chunkX][chunkZ].blockType[x%16][y][z%16];
		} else {
			return BlockType.AIR;
		}
	}
	
	public void addWaterBlock(PrintWriter pw, int x, int y, int z){

		if (x > 0) {
			if (!getBlockType(x-1, y, z).isWater()){
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
						x - 0.00001, y + 1, z - 0.00001,		x - 0.00001, y + 1, z + 1.00001, 		x - 0.00001, y, z - 0.00001);
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
						x - 0.00001, y, z - 0.00001,			x - 0.00001, y, z + 1.00001, 			x - 0.00001, y + 1, z + 1.00001);
			}
		}
		if (x < 510) {
			if (!getBlockType(x+1, y, z).isWater()){
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
						x + 1.00001, y + 1, z - 0.00001,			x + 1.00001, y + 1, z + 1.00001, 			x + 1.00001, y, z - 0.00001);
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
						x + 1.00001, y + 1, z + 1.00001,			x + 1.00001, y, z - 0.00001, 			x + 1.00001, y, z + 1.00001);
			}
		}
		if (y > Main.minY) {
			if (!getBlockType(x, y-1, z).isWater()){
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
						x - 0.00001, y, z - 0.00001,			x + 1.00001, y, z - 0.00001, 			x - 0.00001, y, z + 1.00001);
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
						x + 1.00001, y, z - 0.00001,			x - 0.00001, y, z + 1.00001, 			x + 1.00001, y, z + 1.00001);
			}
		}
		if (y < 254) {
			if (!getBlockType(x, y+1, z).isWater()){
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
						x - 0.00001, y+1, z - 0.00001,			x + 1.00001, y+1, z - 0.00001, 			x - 0.00001, y+1, z + 1.00001);
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n" ,
						x + 1.00001, y+1, z - 0.00001,			x - 0.00001, y+1, z + 1.00001, 			x + 1.00001, y+1, z + 1.00001);
			}
		}
		if (z > 0) {
			if (!getBlockType(x, y, z-1).isWater()){
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
						x-0.00001, y, z - 0.00001,			x + 1.00001, y, z- 0.00001, 			x - 0.00001, y+1, z- 0.00001);
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
						x + 1.00001 , y, z- 0.00001,			x - 0.00001, y + 1, z- 0.00001, 			x + 1.00001, y + 1, z- 0.00001);
			}
		}
		if (z < 510) {
			if (!getBlockType(x, y, z+1).isWater()){
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
						x-0.00001, y, z + 1.00001,			x + 1.00001, y, z + 1.00001, 			x - 0.00001, y+1, z + 1.00001);
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> }\n",
						x + 1.00001 , y, z + 1.00001,			x - 0.00001, y + 1, z + 1.00001, 			x + 1.00001, y + 1, z + 1.00001);
			}
		}
	}
	
	public void addNeighbors(int x, int y, int z, BlockType blockType){
		if (blockType.isTransparent()) { 
			if (x > 0) {
				if (visited[x - 1][y][z] == NOT_VISITED) {
					queue.add(new Point(x - 1,y, z));
					visited[x-1][y][z] = QUEUED;
				}
			}
			if (x < 511) {
				if (visited[x + 1][y][z] == NOT_VISITED) {
					queue.add(new Point(x + 1,y, z));
					visited[x+1][y][z] = QUEUED;
				}
			}
			if (y > Main.minY) {
				if (visited[x][y - 1][z] == NOT_VISITED) {
					queue.add(new Point(x, y - 1, z));
					visited[x][y-1][z] = QUEUED;
				}
			}
			if (y < 255) {
				if (visited[x][y + 1][z] == NOT_VISITED) {
					queue.add(new Point(x, y + 1, z));
					visited[x][y+1][z] = QUEUED;
				}
			}
			if (z > 0) {
				if (visited[x][y][z - 1] == NOT_VISITED) {
					queue.add(new Point(x,y, z - 1));
					visited[x][y][z-1] = QUEUED;
				}
			}
			if (z < 511) {
				if (visited[x][y][z + 1] == NOT_VISITED) {
					queue.add(new Point(x, y, z + 1));
					visited[x][y][z+1] = QUEUED;
				}
			}
		}
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
				System.out.printf("Unimplemented block: %d (%s)\n", type.blockType, type.toString());
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
