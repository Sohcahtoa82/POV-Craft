
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public class Chunk {
	public int offset;
	public int sectorCount;
	public int timeStamp;
	public byte[] decompressedData;
	public NbtTagCompound tag;
	public BlockType[][][] blockType = new BlockType[16][256][16];
	public byte[][][] visited = new byte[16][Main.MAX_Y + 1][16];
	public int posX;
	public int posZ;
	
	public Chunk(){
		for (int x = 0; x < 16; x++){
			for (int y = 0; y < 256; y++){
				for (int z = 0; z < 16; z++){
					blockType[x][y][z] = BlockType.AIR;
				}
			}
		}
		resetVisited();
	}
	
	public void resetVisited(){
		for (int x = 0; x < 16; x++){
			for (int y = 0; y <= Main.MAX_Y; y++){
				for (int z = 0; z < 16; z++){
					visited[x][y][z] = World.NOT_VISITED;
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
						blockType[x][y][z] = BlockType.fromInt(list[l]);
						l++;
					}
				}
			}
		}
	}
}
