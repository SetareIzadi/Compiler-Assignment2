import java.util.ArrayList;
import java.util.List;

public class TestEnvironment {
	public static void main(String[] args) {
		Environment env = new Environment();

		// Set and get signal values
		env.setVariable("A", true);
		env.setVariable("B", false);

		System.out.println("A: " + env.getVariable("A")); // Should print: A: true
		System.out.println("B: " + env.getVariable("B")); // Should print: B: false

		// Testing function definitions (assuming `Def` class is defined)
		List<Def> definitions = new ArrayList<>(); // Assume Def objects are populated
		Environment envWithDefs = new Environment(definitions);

		// Display environment values
		System.out.println(env);
	}
}
