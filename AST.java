import java.util.List;
import java.util.ArrayList;

// Abstract Syntax Tree Base Class
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

// Using user-defined functions
class UseDef extends Expr {
    String f;
    List<Expr> args;

    UseDef(String f, List<Expr> args) {
        this.f = f;
        this.args = args;
    }

    @Override
    public Boolean eval(Environment env) {
        // Step 1: Retrieve the function definition
        Def functionDef = env.getDef(f);

        // Step 2: Create a new environment specifically for this function call
        Environment functionEnv = new Environment(env);

        // Step 3: Assign values to the function's parameters
        for (int i = 0; i < functionDef.args.size(); i++) {
            String param = functionDef.args.get(i);
            Boolean argValue = args.get(i).eval(env); // Evaluate argument in the original environment
            functionEnv.setVariable(param, argValue); // Set the parameter in the function's environment
        }

        // Step 4: Evaluate the function body in the new environment
        return functionDef.e.eval(functionEnv);
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

// Main Circuit class
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

        if (siminputs.isEmpty()) {
            error("No simulation inputs provided.");
        }

        simlength = siminputs.get(0).values.length;
        for (Trace trace : siminputs) {
            if (trace.values.length != simlength) {
                error("All simulation inputs must have the same length.");
            }
        }

        // Initialize simoutputs for output and latch signals
        this.simoutputs = new ArrayList<>();
        for (String output : outputs) {
            simoutputs.add(new Trace(output, new Boolean[simlength]));
        }
        for (String latch : latches) {
            simoutputs.add(new Trace(latch + "'", new Boolean[simlength]));
        }
    }

    // Initialize latch outputs to false
    public void latchesInit(Environment env) {
        for (String latch : latches) {
            env.setVariable(latch + "'", false);
        }
    }

    // Update latch outputs to current input values
    public void latchesUpdate(Environment env) {
        for (String latch : latches) {
            env.setVariable(latch + "'", env.getVariable(latch));
        }
    }

    // Initialize the circuit
    public void initialize(Environment env) {
        for (Trace trace : siminputs) {
            env.setVariable(trace.signal, trace.values[0]);
        }

        for (String latch : latches) {
            if (!env.hasVariable(latch)) {
                env.setVariable(latch, false);
            }
        }

        latchesInit(env);

        for (Update update : updates) {
            update.eval(env);
        }

        recordOutputs(env, 0);
        System.out.println("Environment after initialization:\n" + env.toString());
    }

    // Simulate one cycle
    public void nextCycle(Environment env, int i) {
        for (Trace trace : siminputs) {
            env.setVariable(trace.signal, trace.values[i]);
        }

        latchesUpdate(env);

        for (Update update : updates) {
            update.eval(env);
        }

        recordOutputs(env, i);
        System.out.println("Environment after cycle " + i + ":\n" + env.toString());
    }

    // Record output values into simoutputs for each cycle
    private void recordOutputs(Environment env, int cycle) {
        for (Trace trace : simoutputs) {
            trace.values[cycle] = env.getVariable(trace.signal);
        }
    }

    // Print all traces after the simulation
    public void printTraces() {
        for (Trace trace : simoutputs) {
            System.out.println(trace);
        }
    }

    // Run the entire simulation
    public void runSimulator() {
        Environment env = new Environment(this.definitions);
        initialize(env);

        for (int i = 1; i < simlength; i++) {
            nextCycle(env, i);
        }

        printTraces();
    }
}
