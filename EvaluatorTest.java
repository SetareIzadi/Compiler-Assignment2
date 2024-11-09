public class EvaluatorTest {
    public static void main(String[] args) {
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


    }
}
