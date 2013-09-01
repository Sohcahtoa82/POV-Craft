
import java.io.DataInputStream;
import java.util.Enumeration;
import java.util.Hashtable;



public class NbtTagCompound extends NbtTag {

	public Hashtable<String, NbtTag> tags = new Hashtable<String, NbtTag>();
	
	public boolean readTag(DataInputStream stream) throws Exception{
		while(true) {
			byte nextTagType = stream.readByte();
			NbtTag newTag;
			switch (nextTagType) {
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
					throw new Exception("Unknown tag type");
					//return false;
			}
			newTag.parent = this;
			newTag.readName(stream);
			if (newTag.readTag(stream)){
				tags.put(newTag.name, newTag);
			}
		}
		//return true;
	}
	
	public String toString(){
		String indent = this.getIndentString();
		if (this.name == null)
			this.name = "NULL";
		String ret = indent.concat("Compound Name = ").concat(this.name).concat("\n");
		Enumeration<String> enumKey = tags.keys();
		while(enumKey.hasMoreElements()) {
		    String key = enumKey.nextElement();
		    NbtTag tag = tags.get(key);
		    ret = ret.concat(tag.toString());
		}
		return ret;		
	}
}
