import java.util.List;

public class EvaluatorTest {
    public static void main(String[] args) {
        // Testing basic Expr evaluation
        System.out.println("Testing individual expressions:");
        Environment env = new Environment();
        env.setVariable("A", true);
        env.setVariable("B", false);

        // Test for A * B (AND-operation)
        Expr conjunction = new Conjunction(new Signal("A"), new Signal("B"));
        System.out.println("Conjunction (A * B): " + conjunction.eval(env)); // Expected: false

        // Test for A + B (OR-operation)
        Expr disjunction = new Disjunction(new Signal("A"), new Signal("B"));
        System.out.println("Disjunction (A + B): " + disjunction.eval(env)); // Expected: true

        // Test for !A (NOT-operation)
        Expr negation = new Negation(new Signal("A"));
        System.out.println("Negation (!A): " + negation.eval(env)); // Expected: false

        // Testing the full circuit simulation
        System.out.println("\nTesting full circuit simulation:");

        // Example circuit setup
        List<String> inputs = List.of("A", "B"); // Input signals
        List<String> outputs = List.of("C"); // Output signals
        List<String> latches = List.of("D"); // Latch signals

        // Updates: specify how signals are updated in each cycle
        List<Update> updates = List.of(
                new Update("C", new Conjunction(new Signal("A"), new Signal("B"))), // C = A * B
                new Update("D", new Disjunction(new Signal("C"), new Signal("D")))  // D = C + D
        );

        // Simulation inputs for A and B over 3 cycles
        List<Trace> siminputs = List.of(
                new Trace("A", new Boolean[] {true, false, true}), // A = 1, 0, 1
                new Trace("B", new Boolean[] {false, true, true}) // B = 0, 1, 1
        );

        // Create a circuit
        Circuit circuit = new Circuit("TestCircuit", inputs, outputs, latches, List.of(), updates, siminputs);

        // Run the simulator
        circuit.runSimulator();
    }
}
