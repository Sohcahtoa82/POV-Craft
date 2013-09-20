
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;


public class Region {
	
	public Chunk[][] chunk = new Chunk[32][32];
	public int posX;
	public int posZ;
	public int numChunks = 0;
	
	public void readRegionFile(String fileName) throws Exception{
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
		//System.out.printf("Region coordinates: %d, %d\n", posX, posZ);
	}
}
