
import java.io.FileNotFoundException;
import java.io.PrintWriter;


public class PrintWriterGroup {

	public PrintWriter main;
	public PrintWriter water;
	public PrintWriter glass;
	
	public PrintWriterGroup() throws FileNotFoundException{
		main = new PrintWriter("minecraft.inc");
		water = new PrintWriter("minecraft_water.inc");
		glass = new PrintWriter("minecraft_glass.inc");
		//main.print("#include \"minecraft_water.inc\"\n#include \"minecraft_glass.inc\"\n");
		//water.print("union {\n");
		//glass.print("union {\n");
	}
	
	public void closeFiles(){
		//water.print("}");
		//glass.print("}");
		main.close();
		water.close();
		glass.close();
	}

	public void write(String string) {
		main.write(string);		
	}
	
	public void printf(String string, Object...objects){
		main.printf(string, objects);
	}

	public void print(String string) {
		main.print(string);
	}
}
