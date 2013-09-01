
import java.io.DataInputStream;



public class NbtTagDouble extends NbtTag {

	public double value;
	public boolean readTag(DataInputStream stream) throws Exception{
		value = stream.readDouble();
		return true;
	}
	
	public String toString(){
		String indent = this.getIndentString();
		return String.format("%sDouble Name: %s\n%sValue: %f\n", indent, this.name, indent, this.value);
		
	}
}
