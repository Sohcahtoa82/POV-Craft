
import java.io.DataInputStream;


public class NbtTagLong extends NbtTag {
	
	public long value;
	public boolean readTag(DataInputStream stream) throws Exception{
		value = stream.readLong();
		return true;
	}
	
	public String toString(){
		String indent = this.getIndentString();
		return String.format("%sLong Name: %s\n%sValue: %d\n", indent, this.name, indent, this.value);
		
	}
}
