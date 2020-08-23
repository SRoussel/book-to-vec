import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class FrequencyMap {
    /**
     * The main data structure of the class; maps Strings to Integers.
     */
    private Map<String, Double> freqMap;

    /**
     * The constructor for the class; simply instantiates freqMap.
     */
    public FrequencyMap()
    {
        freqMap = new HashMap<>();
    }

    public Map<String, Double> getFreqMap()
    {
        return freqMap;
    }

    /**
     * The addWord() method allows for elements to be added to freqMap. The method utilizes .merge() from Map, allowing
     * for the given key to exist and not exist.
     *
     * @param word - the word to be added
     * @return the updated FrequencyMap
     */
    public void addWord(String word)
    {
        freqMap.merge(word, 1.0, Double::sum);
    }

    /**
     * Normalizes the frequency map by dividing all values by the map's overall size.
     */
    public void normalize() {
        int size = freqMap.size();

        for(String key : freqMap.keySet()) {
            freqMap.put(key, freqMap.get(key) / size);
        }
    }

    /**
     * Combines two frequency maps.
     *
     * @param first - the first map
     * @param second - the second map
     * @return the combined map
     */
    public static FrequencyMap combine(FrequencyMap first, FrequencyMap second) {
        first.freqMap.forEach((k, v) -> second.freqMap.putIfAbsent(k, v));

        return second;
    }

    /**
     * Generates a list of frequency maps given a list of input text file names.
     *
     * @param filenames - names of text files
     * @return the list of corresponding frequency maps
     */
    public static ArrayList<FrequencyMap> GenerateFrequencyMaps(String[] filenames) {
        ArrayList<FrequencyMap> maps = new ArrayList<>();

        for (String filename : filenames) {
            FrequencyMap freq = new FrequencyMap();
            String file;

            try {
                file = new String(Files.readAllBytes(Paths.get(filename))).replaceAll("\"\'`", "").toLowerCase();
            } catch (IOException e) {
                return null;
            }

            for (String word : Arrays.asList(file.split("[\\p{Punct}\\s]+"))) {
                freq.addWord(word);
            }

            freq.normalize();
            maps.add(freq);
        }

        return maps;
    }

    /**
     * Aggregates a list of maps into a single map.
     *
     * @param maps - the list of maps to be aggregated
     * @return the aggregate map
     */
    public static FrequencyMap AggregateMaps(ArrayList<FrequencyMap> maps) {
        FrequencyMap total = new FrequencyMap();

        for (FrequencyMap map : maps) {
            total = FrequencyMap.combine(map, total);
        }

        return total;
    }
}