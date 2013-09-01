
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
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
	
	@SuppressWarnings("unused")
	public int writePOVString(PrintWriterGroup pw) {
		
		for (int x = 0; x < 512; x++){
			for (int y = 0; y < 256; y++){
				for (int z = 0; z < 512; z++){
					visited[x][y][z] = 0;
				}
			}
		}

		int blocksWritten = 0;
		boolean hasCreatedMesh = false;
		for (int x = 0; x < 32; x++) {
			for (int y = 0; y < 32; y++){
				if (chunk[x][y] != null){
					visited[x*16][255][y*16]= 1;
					queue.add(new Point(x*16, 255, y*16));
				}
			}
		}
		pw.write("union {\n");
		//pw.water.write("mesh {\n");
		while (!queue.isEmpty()){
			//System.out.printf("Queue size: %d\n", queue.size());
			Point point = queue.poll();
			int chunkX = point.x / 16;
			int chunkZ = point.z / 16;
			if (chunk[chunkX][chunkZ] == null)
				continue;
			if (Main.useLimit && ((Math.pow(chunk[chunkX][chunkZ].posX - Main.camerax,2) + Math.pow(chunk[chunkX][chunkZ].posZ - Main.cameraz, 2)) > (Main.dist * Main.dist)))
				continue;
			int x = point.x;
			int y = point.y;
			int z = point.z;
			int blockType = chunk[chunkX][chunkZ].blockType[x%16][y][z%16];
			if (visited[point.x][point.y][point.z] == 2 && !typeIsTransparent(blockType))
				continue;
			/*if (blockType == 9 || blockType == 8) {
				System.out.println();
			}*/
			addNeighbors(x, y, z, blockType);
			if (blockType != 0 && visited[x][y][z] != 2){
				if (blockType == 8 || blockType == 9) { // If its water
					if (!hasCreatedMesh){
						pw.water.write("mesh {\n");
						hasCreatedMesh = true;
					}
					addWaterBlock(pw.water, x, y, z);
				} else {
					// Find the beginning of the run
					while (x > 0 && blockType == this.getBlockType(x - 1, y, z))
						x--;
					int run = 1;
					while (x + run < 512 && blockType == this.getBlockType(x + run, y, z)){
						visited[x + run][y][z] = 2;
						addNeighbors(x + run, y, z, blockType);
						run++;
					}
					//if (blockType == 8 || blockType == 9) { // Water block
					//	waterBlocks.add(new WaterBlock(x, y, z, run));
					//} else {
					this.writePOVBlock(blockType, run, pw, x, y, z);
					//}
				}
				blocksWritten++;
				if (blocksWritten % 10000 == 0) {
					pw.printf("#debug \"%d\\n\"\n", blocksWritten);
				}
			}
			
			
			
		}

		pw.printf("\n translate <%d, 0, %d> } \n", this.posX * 512, this.posZ * 512);
		System.out.printf("\n translate <%d, 0, %d> } \n", this.posX * 512, this.posZ * 512);
		if (hasCreatedMesh)
			pw.water.printf("hollow translate <%d, -0.2, %d>\n material { waterMaterial } }\n ", this.posX * 512, this.posZ * 512);
		return blocksWritten;
	}
	
	public int getBlockType(int x, int y, int z){
		int chunkX = x / 16;
		int chunkZ = z / 16;
		if (chunk[chunkX][chunkZ] != null){
			return chunk[chunkX][chunkZ].blockType[x%16][y][z%16];
		} else {
			return 0;
		}
	}
	
	public boolean isWater(int blockType) {
		return (blockType == 8 || blockType == 9);
	}
	
	public void addWaterBlock(PrintWriter pw, int x, int y, int z){
		//int blockType;
		if (x > 0) {
			if (!isWater(getBlockType(x-1, y, z))){
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
						x - 0.00001, y + 1, z - 0.00001,		x - 0.00001, y + 1, z + 1.00001, 		x - 0.00001, y, z - 0.00001);
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
						x - 0.00001, y, z - 0.00001,			x - 0.00001, y, z + 1.00001, 			x - 0.00001, y + 1, z + 1.00001);
			}
		}
		if (x < 510) {
			if (!isWater(getBlockType(x+1, y, z))){
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
						x + 1.00001, y + 1, z - 0.00001,			x + 1.00001, y + 1, z + 1.00001, 			x + 1.00001, y, z - 0.00001);
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
						x + 1.00001, y + 1, z + 1.00001,			x + 1.00001, y, z - 0.00001, 			x + 1.00001, y, z + 1.00001);
			}
		}
		if (y > Main.minY) {
			if (!isWater(getBlockType(x, y-1, z))){
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
						x - 0.00001, y, z - 0.00001,			x + 1.00001, y, z - 0.00001, 			x - 0.00001, y, z + 1.00001);
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
						x + 1.00001, y, z - 0.00001,			x - 0.00001, y, z + 1.00001, 			x + 1.00001, y, z + 1.00001);
			}
		}
		if (y < 254) {
			if (!isWater(getBlockType(x, y+1, z))){
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
						x - 0.00001, y+1, z - 0.00001,			x + 1.00001, y+1, z - 0.00001, 			x - 0.00001, y+1, z + 1.00001);
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n" ,
						x + 1.00001, y+1, z - 0.00001,			x - 0.00001, y+1, z + 1.00001, 			x + 1.00001, y+1, z + 1.00001);
			}
		}
		if (z > 0) {
			if (!isWater(getBlockType(x, y, z-1))){
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
						x-0.00001, y, z - 0.00001,			x + 1.00001, y, z- 0.00001, 			x - 0.00001, y+1, z- 0.00001);
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
						x + 1.00001 , y, z- 0.00001,			x - 0.00001, y + 1, z- 0.00001, 			x + 1.00001, y + 1, z- 0.00001);
			}
		}
		if (z < 510) {
			if (!isWater(getBlockType(x, y, z+1))){
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> } \n",
						x-0.00001, y, z + 1.00001,			x + 1.00001, y, z + 1.00001, 			x - 0.00001, y+1, z + 1.00001);
				pw.printf("triangle { <%f, %d, %f>, <%f, %d, %f>, <%f, %d, %f> }\n",
						x + 1.00001 , y, z + 1.00001,			x - 0.00001, y + 1, z + 1.00001, 			x + 1.00001, y + 1, z + 1.00001);
			}
		}
	}
	
	public boolean typeIsTransparent(int type) {
		switch (type){
		case 0:
		case 6:
		case 8:
		case 9:
		case 18:
		case 20:
		case 31:
		case 37:
		case 38:
		case 39:
		case 106:
			return true;
		default:
			return false;
		}
	}
	
	public void addNeighbors(int x, int y, int z, int blockType){
		if (typeIsTransparent(blockType)) { 
			if (x > 0) {
				if (visited[x - 1][y][z] == 0) {
					queue.add(new Point(x - 1,y, z));
					visited[x-1][y][z]= 1;
				}
			}
			if (x < 511) {
				if (visited[x + 1][y][z] == 0) {
					queue.add(new Point(x + 1,y, z));
					visited[x+1][y][z]= 1;
				}
			}
			if (y > Main.minY) {
				if (visited[x][y - 1][z] == 0) {
					queue.add(new Point(x, y - 1, z));
					visited[x][y-1][z]= 1;
				}
			}
			if (y < 255) {
				if (visited[x][y + 1][z] == 0) {
					queue.add(new Point(x, y + 1, z));
					visited[x][y+1][z]= 1;
				}
			}
			if (z > 0) {
				if (visited[x][y][z - 1] == 0) {
					queue.add(new Point(x,y, z - 1));
					visited[x][y][z-1]= 1;
				}
			}
			if (z < 511) {
				if (visited[x][y][z + 1] == 0) {
					queue.add(new Point(x, y, z + 1));
					visited[x][y][z+1]= 1;
				}
			}
		}
	}
	
	
	public void writePOVBlock(int type, int run, PrintWriterGroup pw, int x, int y, int z){
		switch (type){
		case 0:  // Air
			return;
		case 1:  // stone
			pw.printf("object { MyBoxSimple(\"stone.png\", %d) ", run);
			printTranslate(pw, x, y, z, run);
			break;
		case 2:  // grass
			pw.printf("object { MyBoxComplex(\"grass_top.png\", \"grass_side.png\", \"dirt.png\", %d) ", run);
			printTranslate(pw, x, y, z, run);
			break;
		case 3:  // dirt
			pw.printf("object { MyBoxSimple(\"dirt.png\", %d) ", run);
			printTranslate(pw, x, y, z, run);
			break;
		case 4:  // cobblestone
			printBox(pw, run);
			pw.print("pigment {color rgb <0.25, 0.25, 0.25> } ");
			printTranslate(pw, x, y, z, run);
			break;
		case 8:  // Moving water
			//printBox(pw, run);
			//pw.print("pigment {color rgb <0.0, 0.0, 1> } ");
			//printTranslate(pw, x, y, z, run);
			//break;
		case 9:  // water
			//pw.printf("object { Water(%d, <%d, %d, %d>) ", run, x + this.posX*16, y-run + 1, z+this.posZ*16);
			pw.water.printf("object { Water(%d, <%d, %d, %d>) ", run, x, y, z);
			printTranslate(pw.water, x, y, z, run);
			break;
		case 12: // sand
			pw.printf("object { MyBoxSimple(\"sand.png\", %d) ", run);
			printTranslate(pw, x, y, z, run);
			break;
		case 17:  // wood
			pw.printf("object { MyBoxComplex(\"wood_top.png\", \"wood_side.png\", \"wood_top.png\", %d) ", run);
			printTranslate(pw, x, y, z, run);
			break;
		case 18: // leaves
			pw.printf("object { MyBoxSimple(\"leaves.png\", %d) ", run);
			printTranslate(pw, x, y, z, run);
			break;
		case 31: // tall grass
			return;
		case 106: // vines 
			return;
		default: 
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
	
	/*public int writePOVStringOld(PrintWriter pw){
		//pw.println("#include \"minecraft.inc\"");
		int chunksWritten = 0;
		for (int x = 0; x < 32; x++){
			for (int y = 0; y < 32; y++){
				//System.out.printf("XX: %d YY: %d\n", x, y);
				if (chunk[x][y] != null) {
					if ((Math.pow(chunk[x][y].posX - Main.camerax,2) + Math.pow(chunk[x][y].posZ - Main.cameraz, 2)) > Main.dist)
						continue;
					pw.println("union {");
					//pw.print(chunk[x][y].getPOVString());
					chunk[x][y].WritePOVString(pw);
					pw.printf("\ntranslate <%d,0,%d>} \n", x*16+this.posX*256, y*16+this.posZ*256);
					//pw.println("#debug \".\"");
					System.out.print(".");
					chunksWritten++;
					if (chunksWritten % 50 == 0){
						System.out.printf("%d\n",chunksWritten);
						pw.printf("#debug \"%d\\n\"\n", chunksWritten);
					}
				}
			}
		}
		return chunksWritten;
	}*/
}
