
import java.io.DataInputStream;
import java.io.IOException;


public class NbtTagByte extends NbtTag {
	public byte value;
	
	public boolean readTag(DataInputStream stream) throws IOException{
		value = stream.readByte();
		return true;
	}
	
	public String toString(){
		String indent = this.getIndentString();
		return String.format("%sByte Name: %s\n%sValue: %d\n", indent, this.name, indent, this.value);
		
	}
}
