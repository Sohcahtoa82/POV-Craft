
import java.io.DataInputStream;
import java.io.IOException;


public class NbtTagFloat extends NbtTag {
	public float value;
	
	public boolean readTag(DataInputStream stream) throws IOException{
		value = stream.readFloat();
		return true;
	}
	
	public String toString(){
		String indent = this.getIndentString();
		return String.format("%sFloat Name: %s\n%sValue: %f\n", indent, this.name, indent, this.value);
		
	}
}
