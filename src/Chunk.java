
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.PrintWriter;
import java.util.LinkedList;


public class Chunk {
	public int offset;
	public int sectorCount;
	public int timeStamp;
	public byte[] decompressedData;
	public NbtTagCompound tag;
	public int[][][] blockType = new int[16][256][16];
	public int posX;
	public int posZ;
	
	public Chunk(){
		for (int x = 0; x < 16; x++){
			for (int y = 0; y < 256; y++){
				for (int z = 0; z < 16; z++){
					blockType[x][y][z] = 0;
				}
			}
		}
	}
	
	public void loadTag() throws Exception{
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(decompressedData));
		byte tagType = stream.readByte();
		if (tagType != NbtTag.TAG_COMPOUND) {
			throw new Exception("Top tag wasn't a compound!");
		}
		tag = new NbtTagCompound();
		tag.readName(stream);
		tag.readTag(stream);
		//System.out.println(tag.toString());
		//PrintWriter pw = new PrintWriter("chunk.txt");
		//pw.print(tag.toString());
		//pw.close();
		NbtTagCompound levelTag = (NbtTagCompound) tag.tags.get("Level");
		NbtTagList sections = (NbtTagList) levelTag.tags.get("Sections");
		for (int i = 0; i < sections.tags.size(); i++) {
			NbtTagCompound item = (NbtTagCompound)sections.tags.get(i);
			byte sectionNum = ((NbtTagByte)item.tags.get("Y")).value;
			byte[] list = ((NbtTagByteArray)item.tags.get("Blocks")).value;
			int l = 0; //sectionNum * 16 * 16;
			for (int y = sectionNum * 16; y < sectionNum * 16 + 16; y++){
				for (int z = 0; z < 16; z++){
					for (int x = 0; x < 16; x++){
						blockType[x][y][z] = list[l];
						l++;
					}
				}
			}
		}
		// Optimize to remove blocks that are completely surrounded
		/*byte[][][] newBlockTypeList = blockType.clone();
		for (int x = 1; x < 15; x++){
			for (int y = 1; y < 64; y++){
				for (int z = 1; z < 15; z++){
					if (blockType[x+1][y][z] != 0 && blockType[x-1][y][z] != 0 &&
					    blockType[x][y+1][z] != 0 && blockType[x][y-1][z] != 0 &&
					    blockType[x][y][z+1] != 0 && blockType[x][y][z-1] != 0){
						newBlockTypeList[x][y][z] = 0;
					}
						
				}
			}
		}
		blockType = newBlockTypeList;*/
	}
	
	public String getPOVString() {
		String str = "";
		for (int x = 0; x < 16; x++) {
			//System.out.printf("X: %d\n", x);
			for (int y = 0; y < 256; y++){
				for (int z = 0; z < 16; z++){
					int type = blockType[x][y][z];
					if (type > 0)
						str = str.concat(String.format("object { blocks[%d] translate <%d, %d, %d> }\n", type, x, y, z));
				}
			}
		}
		return str;
	}
	

	
	/*public void WritePOVString(PrintWriterGroup pw) {
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++){
				int lastType = blockType[x][0][z];
				int type = lastType;
				int run = 0;
				for (int y = Main.minY; y < 256; y++){
					
					run++;
					lastType = type;
					type = blockType[x][y][z];
					
					if (type < 0){
						//System.out.printf("WTF?  Had a Type = %d @ [%d,%d,%d] ",type,x,y,z);
						type = 2 - type;
						//System.out.printf("Setting to %d\n", type);
					}
					if (type != lastType) {
						if (lastType != 0) {
							writePOVBlock(lastType, run, pw, x, y, z);
							
						}
						run = 0;
					}
				}
			}
		}
		return;
	}*/


}
