import java.util.Map;
import java.util.HashMap;

public class FrequencyMap
{
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

    /**
     * The addWord() method allows for elements to be added to freqMap. The method utilized .merge() from Map, allowing
     * for the given key to exist and not exist.
     *
     * @param word - the word to be added
     * @return the updated FrequencyMap
     */
    public void addWord(String word)
    {
        freqMap.merge(word, 1.0, Double::sum);
    }

    public void normalize()
    {
        int size = freqMap.size();

        for(String key : freqMap.keySet())
        {
            freqMap.put(key, freqMap.get(key) / size);
        }
    }

    public static FrequencyMap combine(FrequencyMap one, FrequencyMap two)
    {
        one.freqMap.forEach((k, v) -> two.freqMap.putIfAbsent(k, v));

        return two;
    }

    public Map<String, Double> getFreqMap()
    {
        return freqMap;
    }
}