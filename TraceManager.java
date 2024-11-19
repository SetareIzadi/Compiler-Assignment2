
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class TraceManager {

    private HashMap<String, List<Boolean>> traces = new HashMap<>();

    // Tilføj en værdi til et signals historik
    public void addValue(String signal, Boolean value) {
        traces.computeIfAbsent(signal, k -> new ArrayList<>()).add(value);
    }

    // Hent historikken for et signal
    public List<Boolean> getTrace(String signal) {
        return traces.getOrDefault(signal, new ArrayList<>());
    }

    // Hent alle signalnavne
    public List<String> getSignals() {
        return new ArrayList<>(traces.keySet());
    }

    // Udskriv alle traces
    public void printTraces() {
        System.out.println("Traces:");
        for (String signal : getSignals()) {
            String traceString = traces.get(signal).stream()
                    .map(b -> b ? "1" : "0") // Konverter Boolean til "1" eller "0"
                    .reduce("", (a, b) -> a + b); // Saml værdierne til én streng
            System.out.printf("%-10s: %s%n", signal, traceString);
        }
    }
}
