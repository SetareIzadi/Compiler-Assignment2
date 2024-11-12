import java.util.HashMap;
import java.util.Map.Entry;
import java.util.List;
import java.util.ArrayList;

public abstract class AST {
    public void error(String msg) {
        System.err.println(msg);
        System.exit(-1);
    }
}

// Abstract class for expressions
abstract class Expr extends AST {
    public abstract Boolean eval(Environment env);
}

// Logical AND operation
class Conjunction extends Expr {
    Expr e1, e2;

    Conjunction(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    @Override
    public Boolean eval(Environment env) {
        return e1.eval(env) && e2.eval(env);
    }
}

// Logical OR operation
class Disjunction extends Expr {
    Expr e1, e2;

    Disjunction(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    @Override
    public Boolean eval(Environment env) {
        return e1.eval(env) || e2.eval(env);
    }
}

// Logical NOT operation
class Negation extends Expr {
    Expr e;

    Negation(Expr e) {
        this.e = e;
    }

    @Override
    public Boolean eval(Environment env) {
        return !e.eval(env);
    }
}

// Using user-defined functions (not implemented yet)
class UseDef extends Expr {
    String f;
    List<Expr> args;

    UseDef(String f, List<Expr> args) {
        this.f = f;
        this.args = args;
    }

    @Override
    public Boolean eval(Environment env) {
        error("Use of user-defined function '" + f + "' is not implemented yet.");
        return null;
    }
}

// Signals represent atomic expressions
class Signal extends Expr {
    String varname;

    Signal(String varname) {
        this.varname = varname;
    }

    @Override
    public Boolean eval(Environment env) {
        return env.getVariable(varname);
    }
}

// Definitions for user-defined functions
class Def extends AST {
    String f; // Function name, e.g., "xor"
    List<String> args; // Formal arguments, e.g., [A, B]
    Expr e; // Function body, e.g., A * /B + /A * B

    Def(String f, List<String> args, Expr e) {
        this.f = f;
        this.args = args;
        this.e = e;
    }
}

// Update represents signal updates in the circuit
class Update extends AST {
    String name; // Signal being updated
    Expr e; // Value assigned to the signal

    Update(String name, Expr e) {
        this.name = name;
        this.e = e;
    }

    public void eval(Environment env) {
        Boolean value = e.eval(env); // Evaluate the expression
        env.setVariable(name, value); // Update the signal's value in the environment
    }
}

// Trace represents the value of a signal over time
class Trace extends AST {
    String signal;
    Boolean[] values;

    Trace(String signal, Boolean[] values) {
        this.signal = signal;
        this.values = values;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(signal).append(" = ");
        for (Boolean value : values) {
            sb.append(value ? "1" : "0");
        }
        return sb.toString();
    }
}

// Main circuit class
class Circuit extends AST {
    String name;
    List<String> inputs;
    List<String> outputs;
    List<String> latches;
    List<Def> definitions;
    List<Update> updates;
    List<Trace> siminputs;
    List<Trace> simoutputs;
    int simlength;

    Circuit(String name, List<String> inputs, List<String> outputs, List<String> latches,
            List<Def> definitions, List<Update> updates, List<Trace> siminputs) {
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
        this.latches = latches;
        this.definitions = definitions;
        this.updates = updates;
        this.siminputs = siminputs;

        // Validate siminputs length
        if (siminputs.isEmpty()) {
            error("No simulation inputs provided.");
        }

        simlength = siminputs.get(0).values.length;
        for (Trace trace : siminputs) {
            if (trace.values.length != simlength) {
                error("All simulation inputs must have the same length.");
            }
        }
    }

    // Initialize latch outputs to false
    public void latchesInit(Environment env) {
        for (String latch : latches) {
            String output = latch + "'"; // Generate the output name, e.g., A -> A'
            env.setVariable(output, false); // Initialize to false
        }
    }

    // Update latch outputs to current input values
    public void latchesUpdate(Environment env) {
        for (String latch : latches) {
            String output = latch + "'"; // Generate the output name, e.g., A -> A'
            Boolean value = env.getVariable(latch); // Get the current input signal
            env.setVariable(output, value); // Update the latch output
        }
    }

    // Initialize the circuit
    public void initialize(Environment env) {
        // Set input signals from siminputs
        for (Trace trace : siminputs) {
            String signal = trace.signal;
            if (trace.values.length == 0) {
                error("Siminput for signal " + signal + " is not defined.");
            }
            env.setVariable(signal, trace.values[0]); // Use first value at time 0
        }

        // Initialize latch inputs if not already set
        for (String latch : latches) {
            if (!env.hasVariable(latch)) {
                env.setVariable(latch, false); // Initialize to false
            }
        }

        // Initialize latch outputs
        latchesInit(env);

        // Evaluate all updates
        for (Update update : updates) {
            update.eval(env);
        }

        // Print the environment after initialization
        System.out.println("Environment after initialization:\n" + env.toString());
    }

    // Simulate one cycle
    public void nextCycle(Environment env, int i) {
        // Set input signals for the current cycle
        for (Trace trace : siminputs) {
            String signal = trace.signal;
            if (i >= trace.values.length) {
                error("Siminput for signal " + signal + " is not defined for time " + i + ".");
            }
            env.setVariable(signal, trace.values[i]); // Use i-th value for the current cycle
        }

        // Update latch outputs
        latchesUpdate(env);

        // Evaluate all updates
        for (Update update : updates) {
            update.eval(env);
        }

        // Print the environment for the current cycle
        System.out.println("Environment after cycle " + i + ":\n" + env.toString());
    }

    // Run the entire simulation
    public void runSimulator() {
        Environment env = new Environment();
        initialize(env); // Perform initialization

        for (int i = 1; i < simlength; i++) {
            nextCycle(env, i); // Simulate each cycle
        }
    }
}