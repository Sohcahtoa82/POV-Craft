
import java.io.DataInputStream;



abstract public class NbtTag {
	
	public final static byte TAG_FIRST = 0;
	public final static byte TAG_END = 0;
	public final static byte TAG_BYTE = 1;
	public final static byte TAG_SHORT = 2;
	public final static byte TAG_INT = 3;
	public final static byte TAG_LONG = 4;
	public final static byte TAG_FLOAT = 5;
	public final static byte TAG_DOUBLE = 6;
	public final static byte TAG_BYTE_ARRAY = 7;
	public final static byte TAG_STRING = 8;
	public final static byte TAG_LIST = 9;
	public final static byte TAG_COMPOUND = 10;
	public final static byte TAG_INT_ARRAY = 11;
	public final static byte TAG_LAST = 11;
	
	public String name;
	public NbtTag parent;
	
	abstract public boolean readTag(DataInputStream stream) throws Exception;
	//abstract public String toString();
	
	public void readName(DataInputStream stream) throws Exception {
		name = "";
		short length = stream.readShort();
		byte[] str = new byte[length];
		stream.read(str);
		name = new String(str);
	}
	
	protected int getDepth() {
		NbtTag nextParent = this;
		int depth = 0;
		while (nextParent.parent != null){
			nextParent = nextParent.parent;
			depth++;
		}
		return depth;
	}

	protected String getIndentString() {
		if (this.name == null)
			this.name = "NULL";
		int depth = this.getDepth();
		String indent = "";
		for (int i = 0; i < depth; i++){
			indent = indent.concat("   ");
		}
		return indent;
	}
}
