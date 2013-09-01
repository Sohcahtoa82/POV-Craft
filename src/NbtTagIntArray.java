
import java.io.DataInputStream;


public class NbtTagIntArray extends NbtTag {

	public int[] value;
	public boolean readTag(DataInputStream stream) throws Exception{
		int length = stream.readInt();
		value = new int[length];
		for (int i = 0; i < length; i++){
			value[i] = stream.readInt();
		}
		return true;
	}
	
	public String toString(){
		String indent = this.getIndentString();
		String ret = indent.concat("IntArray Name: ").concat(this.name).concat("\n");
		ret = ret.concat(indent).concat("Values: [ ");
		for (int i = 0; i < value.length; i++){
			ret = ret.concat(Integer.toString(value[i])).concat(" ");
		}
		ret.concat("]\n");
		return ret;
	}
}
