import java.util.HashMap;
import java.util.Map.Entry;
import java.util.List;
import java.util.ArrayList;

public abstract class AST{
    public void error(String msg){
        System.err.println(msg);
        System.exit(-1);
    }
};

/* Expressions are similar to arithmetic expressions in the impl
   language: the atomic expressions are just Signal (similar to
   variables in expressions) and they can be composed to larger
   expressions with And (Conjunction), Or (Disjunction), and Not
   (Negation). Moreover, an expression can be using any of the
   functions defined in the definitions. */

abstract class Expr extends AST{
    public abstract Boolean eval(Environment env);
}

class Conjunction extends Expr{
    // Example: Signal1 * Signal2 
    Expr e1,e2;
    Conjunction(Expr e1,Expr e2) {
        this.e1=e1;
        this.e2=e2;
    }

    @Override
    public Boolean eval(Environment env) {
        return e1.eval(env) && e2.eval(env);
    }
}

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
        return null; // unreachable
    }
}

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

class Def extends AST{
    // Definition of a function
    // Example: def xor(A,B) = A * /B + /A * B
    String f; // function name, e.g. "xor"
    List<String> args;  // formal arguments, e.g. [A,B]
    Expr e;  // body of the definition, e.g. A * /B + /A * B
    Def(String f, List<String> args, Expr e){
        this.f=f; this.args=args; this.e=e;
    }
}

// An Update is any of the lines " signal = expression "
// in the update section

class Update extends AST{
    // Example Signal1 = /Signal2
    String name; // Signal being updated, e.g. "Signal1"
    Expr e; // The value it receives, e.g., "/Signal2"
    Update(String name, Expr e){
        this.e=e;
        this.name=name;
    }

    public void eval(Environment env) {
        env.setVariable(name, e.eval(env));
    }
}

/* A Trace is a signal and an array of Booleans, for instance each
   line of the .simulate section that specifies the traces for the
   input signals of the circuit. It is suggested to use this class
   also for the output signals of the circuit in the second
   assignment.
*/

class Trace extends AST{
    // Example Signal could be: 0101010
    String signal;
    Boolean[] values;
    Trace(String signal, Boolean[] values){
        this.signal=signal;
        this.values=values;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Boolean value : values) {
            sb.append(value ? "1" : "0");
        }
        sb.append(" ").append(signal);
        return sb.toString();
    }
}

/* The main data structure of this simulator: the entire circuit with
   its inputs, outputs, latches, definitions and updates. Additionally
   for each input signal, it has a Trace as simulation input.

   There are two variables that are not part of the abstract syntax
   and thus not initialized by the constructor (so far): simoutputs
   and simlength. It is suggested to use these two variables for
   assignment 2 as follows:

   1. all siminputs should have the same length (this is part of the
   checks that you should implement). set simlength to this length: it
   is the number of simulation cycles that the interpreter should run.

   2. use the simoutputs to store the value of the output signals in
   each simulation cycle, so they can be displayed at the end. These
   traces should also finally have the length simlength.
*/

class Circuit extends AST {
    String name;
    List<String> inputs;
    List<String> outputs;
    List<String> latches;
    List<Def> definitions;
    List<Update> updates;
    List<Trace> siminputs;
    int simlength;

    Circuit(String name,
            List<String> inputs,
            List<String> outputs,
            List<String> latches,
            List<Def> definitions,
            List<Update> updates,
            List<Trace> siminputs) {
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
        this.latches = latches;
        this.definitions = definitions;
        this.updates = updates;
        this.siminputs = siminputs;

        // Initialize sim length based on sim inputs
        if (!siminputs.isEmpty()) {
            simlength = siminputs.get(0).values.length; // Assuming all traces have the same length
        }
    }

    void initialize(Environment env) {
        // Step 1: Set initial input signals at time point 0
        for (String input : inputs) {
            Trace trace = findTrace(input);
            if (trace == null || trace.values.length == 0) {
                error("Siminput not defined or has length 0 for input signal: " + input);
            }
            env.setVariable(input, trace.values[0]);  // Set initial value for time point 0
        }

        // Step 2: Initialize latch outputs
        latchesInit(env);

        // Step 3: Initialize remaining signals by evaluating updates
        for (Update update : updates) {
            update.eval(env);  // Run eval method of each Update
        }

        // Step 4: Print the environment
        System.out.println(env);
    }

    void nextCycle(Environment env, int cycle) {
        // Step 1: Update input signals based on cycle number
        for (String input : inputs) {
            Trace trace = findTrace(input);
            if (trace == null || cycle >= trace.values.length) {
                error("Siminput not defined for input signal: " + input + " at cycle " + cycle);
            }
            env.setVariable(input, trace.values[cycle]);  // Update input for the current cycle
        }

        // Step 2: Update latches
        latchesUpdate(env);

        // Step 3: Update remaining signals by evaluating updates
        for (Update update : updates) {
            update.eval(env);  // Run eval method of each Update
        }

        // Step 4: Print the environment
        System.out.println(env);
    }

    // New method to run the simulator
    void runSimulator(Environment env) {
        // First initialize the environment
        initialize(env);

        // Then run nextCycle for each cycle up to simlength
        for (int cycle = 1; cycle < simlength; cycle++) {
            nextCycle(env, cycle);  // Cycle starts from 0 in initialize, so we start from 1 here
        }
    }

    // Helper function to find a Trace by signal name
    private Trace findTrace(String signalName) {
        for (Trace trace : siminputs) {
            if (trace.signal.equals(signalName)) {
                return trace;
            }
        }
        return null; // Not found
    }

    void latchesInit(Environment env) {
        // Initialize all latch outputs (e.g., A', B', C') to 0
        for (String latch : latches) {
            String latchOutput = latch + "'";  // Append prime to denote latch output
            env.setVariable(latchOutput, false);  // Initialize latch output to 0 (false)
        }
    }

    void latchesUpdate(Environment env) {
        // Update each latch output to the current value of its corresponding input
        for (String latch : latches) {
            String latchOutput = latch + "'";  // Latch output name with prime
            Boolean latchInputValue = env.getVariable(latch);  // Get current value of latch input
            env.setVariable(latchOutput, latchInputValue);  // Set output to input's current value
        }
    }
}

class EnvironmentTest {
    private HashMap<String, Boolean> variables = new HashMap<>();

    public void setVariable(String name, Boolean value) {
        variables.put(name, value);
    }

    public Boolean getVariable(String name) {
        return variables.getOrDefault(name, false);
    }
}