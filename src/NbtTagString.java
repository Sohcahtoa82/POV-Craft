
import java.io.DataInputStream;


public class NbtTagString extends NbtTag {

	public String value;
	public boolean readTag(DataInputStream stream) throws Exception {
		short length = stream.readShort();
		byte[] str = new byte[length];
		stream.read(str);
		value = new String(str);
		return true;
	}
	
	public String toString(){
		String indent = this.getIndentString();
		return String.format("%sString Name: %s\n%sValue: %s\n", indent, this.name, indent, this.value);
		
	}
}
