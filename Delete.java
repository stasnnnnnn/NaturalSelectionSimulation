import java.io.*; 
public class Delete {
    public static void main(String[] args) {
        File file = new File("Bacterium.class");          
		file.delete(); 
		file = new File("Food.class");          
		file.delete(); 
		file = new File("Form.class");          
		file.delete(); 
		file = new File("Main.class");          
		file.delete(); 
    }
}
