
import java.io.DataInputStream;



public class NbtTagByteArray extends NbtTag {
	public byte[] value;
	
	public boolean readTag(DataInputStream stream) throws Exception{
		int length = stream.readInt();
        if( length < 0 ) {
            throw new Exception( "Negative length given in TAG_Byte_Array" );
        }
        value = new byte[length];
        stream.read(value);
		return true;
	}
	
	public String toString(){
		String indent = this.getIndentString();
		String ret = indent.concat("ByteArray Name: ").concat(this.name).concat("\n");
		ret = ret.concat(indent).concat("Values: [ ");
		for (int i = 0; i < value.length; i++){
			ret = ret.concat(Byte.toString(value[i])).concat(" ");
		}
		ret = ret.concat("]\n");
		return ret;
	}
}
