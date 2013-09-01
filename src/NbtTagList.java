
import java.io.DataInputStream;
import java.util.ArrayList;



public class NbtTagList extends NbtTag {
	
	public byte listType;
	public ArrayList<NbtTag> tags = new ArrayList<NbtTag>();
	
	public boolean readTag(DataInputStream stream) throws Exception{
		listType = stream.readByte();
		int length = stream.readInt();
		
		for (int i = 0; i < length; i++){
			NbtTag newTag;
			switch (listType) {
			case NbtTag.TAG_BYTE:
				newTag = new NbtTagByte();
				break;
			case NbtTag.TAG_BYTE_ARRAY:
				newTag = new NbtTagByteArray();
				break;
			case NbtTag.TAG_COMPOUND:
				newTag = new NbtTagCompound();
				break;
			case NbtTag.TAG_DOUBLE:
				newTag = new NbtTagDouble();
				break;
			case NbtTag.TAG_FLOAT:
				newTag = new NbtTagFloat();
				break;
			case NbtTag.TAG_INT:
				newTag = new NbtTagInt();
				break;
			case NbtTag.TAG_INT_ARRAY:
				newTag = new NbtTagIntArray();
				break;
			case NbtTag.TAG_LIST:
				newTag = new NbtTagList();
				break;
			case NbtTag.TAG_LONG:
				newTag = new NbtTagLong();
				break;
			case NbtTag.TAG_SHORT:
				newTag = new NbtTagShort();
				break;
			case NbtTag.TAG_STRING:
				newTag = new NbtTagString();
				break;
			case NbtTag.TAG_END:
				return true;
			default:
				throw new Exception(String.format("Unknown tag type: %d", listType));
				//return false;
			}
			
			newTag.parent = this;
			//newTag.readName(stream);
			if (newTag.readTag(stream)){
				tags.add(newTag);
			}
			
		}
		return true;
	}
	
	public String toString(){
		if (this.name == null)
			this.name = "NULL";
		String indent = this.getIndentString();
		String ret = indent.concat("List Name = ").concat(this.name).concat("\n");
		for (int i = 0; i < tags.size(); i++){
		    ret = ret.concat(tags.get(i).toString());
		}
		return ret;		
	}
}
