import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ShamirSecretSharing {

    public static BigInteger decodeValue(String value, int base) {
        return new BigInteger(value, base);
    }

    public static BigInteger lagrangeInterpolation(List<Pair<Integer, BigInteger>> points) {
        int numPoints = points.size();
        BigInteger secret = BigInteger.ZERO;

        for (int i = 0; i < numPoints; i++) {
            BigInteger y_i = points.get(i).getValue();
            BigInteger term = y_i;

            for (int j = 0; j < numPoints; j++) {
                if (i != j) {
                    BigInteger x_i = BigInteger.valueOf(points.get(i).getKey());
                    BigInteger x_j = BigInteger.valueOf(points.get(j).getKey());
                    BigInteger numerator = BigInteger.ZERO.subtract(x_j);
                    BigInteger denominator = x_i.subtract(x_j);

                    if (denominator.equals(BigInteger.ZERO)) {
                        throw new ArithmeticException("Division by zero.");
                    }

                    term = term.multiply(numerator).divide(denominator);
                }
            }
            secret = secret.add(term);
        }
        return secret;
    }

    public static BigInteger solveSecret(JSONObject data) {
        int totalPoints = data.getJSONObject("keys").getInt("n");
        int requiredPoints = data.getJSONObject("keys").getInt("k");
        List<Pair<Integer, BigInteger>> points = new ArrayList<>();

        for (int i = 1; i <= totalPoints; i++) {
            if (data.has(String.valueOf(i))) {
                JSONObject pointData = data.getJSONObject(String.valueOf(i));
                int base = pointData.getInt("base");
                String value = pointData.getString("value");
                int x = i;
                BigInteger y = decodeValue(value, base);
                points.add(new Pair<>(x, y));
            }
        }

        points = points.subList(0, Math.min(requiredPoints, points.size()));

        return lagrangeInterpolation(points);
    }

    public static void main(String[] args) throws IOException {
        String[] filenames = {"testcase1.json", "testcase2.json"};

        for (String filename : filenames) {
            try (FileReader reader = new FileReader(filename)) {
                JSONTokener tokener = new JSONTokener(reader);
                JSONObject json = new JSONObject(tokener);
                BigInteger secret = solveSecret(json);
                System.out.println(secret);
            } catch (Exception e) {
                System.err.println("Error processing " + filename + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static class Pair<K, V> {
        private final K key;
        private final V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}