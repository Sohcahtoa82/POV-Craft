
import java.io.DataInputStream;



public class NbtTagInt extends NbtTag {

	public int value;
	public boolean readTag(DataInputStream stream) throws Exception{
		value = stream.readInt();
		return true;
	}
	
	public String toString(){
		String indent = this.getIndentString();
		return String.format("%sInt Name: %s\n%sValue: %d\n", indent, this.name, indent, this.value);
		
	}
}
