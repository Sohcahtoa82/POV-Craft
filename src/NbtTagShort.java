
import java.io.DataInputStream;


public class NbtTagShort extends NbtTag {

	public short value;
	public boolean readTag(DataInputStream stream) throws Exception{
		value = stream.readShort();
		return true;
	}
	
	public String toString(){
		String indent = this.getIndentString();
		return String.format("%sShort Name: %s\n%sValue: %d\n", indent, this.name, indent, this.value);
		
	}
}
