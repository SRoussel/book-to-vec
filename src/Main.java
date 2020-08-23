import java.io.File;
import java.util.*;

import Jama.Matrix;

public class Main {
    private static String TestDir = "data";
    private static Matrix x;
    private static Matrix y;

    public static void main(String[] args) {
        // Get filenames of test files
        String[] filenames = new File(TestDir).list();

        // Generate frequency map for our test data
        ArrayList<FrequencyMap> frequencyMaps = FrequencyMap.GenerateFrequencyMaps(filenames);

        // Aggregate maps into a single map
        FrequencyMap aggregateMap = FrequencyMap.AggregateMaps(frequencyMaps);

        // Sort words by frequency
        Map<String, Double> sorted = sortByValue(aggregateMap.getFreqMap());

        List<HashMap<String, Double>> list = new ArrayList<>();

        for(FrequencyMap map : frequencyMaps) {
            HashMap<String, Double> newMap = new HashMap<>();

            for(Object key : Arrays.copyOfRange(sorted.keySet().toArray(), 2, 99)) {
                newMap.put((String)key, (map.getFreqMap().get(key) == null) ? 0.0 : map.getFreqMap().get(key));
            }

            list.add(normalize(newMap));
        }


        double[][] array = new double[100][filenames.length];

        for (int i = 0; i < list.size(); ++i) {
            Collection values = list.get(i).values();
            for (int j = 0; j < values.size(); ++j) {
                array[i][j] = (double)values.toArray()[j];
            }
        }

        Matrix m = new Matrix(array);
        Matrix v = m.svd().getV();
        Matrix s = m.svd().getS();

        Matrix fin = s.times(v.transpose());

        x = fin.getMatrix(0, 0, 0, filenames.length - 1);
        y = fin.getMatrix(1, 1, 0, filenames.length - 1);

        // Graph x and y values
    }

    private static HashMap<String, Double> normalize(HashMap<String, Double> map) {
        Double total = Double.valueOf(0);
        HashMap<String, Double> frequencies = new HashMap<>();

        for (Double l : map.values()) {
            total += l;
        }

        for (String e : map.keySet()) {
            frequencies.put(e, Double.valueOf(map.get(e)).doubleValue()/total.doubleValue());
        }

        return frequencies;
    }

    // Allows for the sorting of a map by value
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, (e1, e2) -> -(e1.getValue()).compareTo(e2.getValue()));
        Map<K, V> result = new LinkedHashMap<>();

        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}