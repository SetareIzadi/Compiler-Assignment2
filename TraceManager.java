
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class TraceManager {
    private HashMap<String, List<Boolean>> traces = new HashMap<>();

    // Add a value to a signal's trace
    public void addValue(String signal, Boolean value) {
        traces.computeIfAbsent(signal, k -> new ArrayList<>()).add(value);
    }

    // Get the trace for a specific signal
    public List<Boolean> getTrace(String signal) {
        return traces.getOrDefault(signal, new ArrayList<>());
    }

    // Print all traces after the simulation
    public void printTraces() {
        System.out.println("Traces:");
        for (String signal : traces.keySet()) {
            String traceString = traces.get(signal).stream()
                    .map(b -> b ? "1" : "0") // Convert Boolean to "1" or "0"
                    .reduce("", (a, b) -> a + b); // Combine all values into a string
            System.out.printf("%-10s: %s%n", signal, traceString);
        }
    }
}

