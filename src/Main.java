import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Collections;
import org.knowm.xchart.*;
import org.knowm.xchart.demo.charts.ExampleChart;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import Jama.Matrix;

public class Main
{
    private static Matrix x;
    private static Matrix y;


    public static final String[] filenames =
    {
            "src/austen_emma.txt", "src/austen_lady.txt", "src/austen_mansfield.txt", "src/austen_pride.txt", "src/austen_sense.txt", "src/austen_north.txt", "src/austen_persuasion.txt",
            "src/dickens_copperfield.txt", "src/dickens_great.txt", "src/dickens_oliver.txt", "src/dickens_pickwick.txt", "src/dickens_tale.txt", "src/dickens_bleak.txt", "src/dickens_hard.txt",
            "src/hawthorne_blithedale.txt", "src/hawthorne_scarlet.txt", "src/hawthorne_fanshawe.txt", "src/hawthorne_gables.txt",
            "src/twain_connecticut.txt", "src/twain_huck.txt", "src/twain_innocents.txt", "src/twain_prince.txt", "src/twain_tom.txt",
            "src/wells_first.txt", "src/wells_kipps.txt", "src/wells_time.txt", "src/wells_war.txt", "src/wells_invisible.txt", "src/wells_island.txt",
            //"src/shakespeare_hamlet.txt", "src/shakespeare_macbeth.txt", "src/shakespeare_midsummer.txt", "src/shakespeare_othello.txt", "src/shakespeare_r&j.txt", "src/shakespeare_tempest.txt"
    };

    public static final String[] niceNames =
    {
            "Austen: Emma, 1815", "Austen: Lady Susan, 1818 (1878)", "Austen: Mansfield Park, 1814", "Austen: Pride and Prejudice, 1813", "Austen: Sense and Sensibility, 1811", "Austen: Northanger Abbey, 1818", "Austen: Persuasion, 1818",
            "Dickens: David Copperfield, 1849", "Dickens: Great Expectations, 1860", "Dickens: Oliver Twist, 1837", "Dickens: Pickwick Papers, 1836", "Dickens: Tale of Two Cities, 1859", "Dickens: Bleak House, 1852", "Dickens: Hard Times, 1854",
            "Hawthorne: Blithedale Romance, 1852", "Hawthorne: Scarlet Letter, 1850", "Hawthorne: Fanshawe, 1828", "Hawthorne: House of Seven Gables, 1851",
            "Twain: Connecticut Yankee, 1889", "Twain: Huckleberry Finn, 1884", "Twain: Innocents Abroad, 1869", "Twain: The Prince and the Pauper, 1881", "Twain: Tom Sawyer, 1876",
            "Wells: First Men in the Moon, 1900", "Wells: Kipps, 1905", "Wells: Time Machine, 1895", "Wells: War of the World, 1898", "Wells: Invisible Man, 1897", "Wells: Island of Dr. Moreau, 1896",
            //"Shakespeare: Hamlet", "Shakespeare: Macbeth", "Shakespeare: Midsummer Night's Dream", "Shakespeare: Othello", "Shakespeare: Romeo and Juliet", "Shakespeare: The Tempest"
    };

    private static final int NUMFILES = filenames.length;


    public static void main(String[] args)
    {
        ArrayList<FrequencyMap> maps = new ArrayList<>();

        for(String filename : filenames)
        {
            maps.add(frequence(filename));
        }

        FrequencyMap total = new FrequencyMap();

        for(FrequencyMap map : maps)
        {
            total = FrequencyMap.combine(map, total);
        }

        Map<String, Double> sorted = sortByValue(total.getFreqMap());

        System.out.println(sorted.keySet());

        List<HashMap<String, Double>> list = new ArrayList<>();

        for(FrequencyMap map : maps)
        {
            HashMap<String, Double> newMap = new HashMap<>();

            for(Object key : Arrays.copyOfRange(sorted.keySet().toArray(), 2, 99))
            {
                newMap.put((String)key, (map.getFreqMap().get(key) == null) ? 0.0 : map.getFreqMap().get(key));
            }

            list.add(normalize(newMap));
        }


        double[][] array = new double[100][NUMFILES];

        int i = 0;
        int j = 0;

        for(HashMap<String, Double> map : list)
        {
            for(Double value: map.values())
            {
                array[i][j] = value;
                ++i;
            }

            i = 0;
            ++j;
        }

        Matrix m = new Matrix(array);
        Matrix v = m.svd().getV();
        Matrix s = m.svd().getS();

        Matrix fin = s.times(v.transpose());

        x = fin.getMatrix(0, 0, 0, NUMFILES - 1);
        y = fin.getMatrix(1, 1, 0, NUMFILES - 1);

        ExampleChart<XYChart> scatter = new ScatterChart();
        XYChart chart = scatter.getChart();
        new SwingWrapper<>(chart).displayChart();

        try
        {
            BitmapEncoder.saveBitmap(chart, "output", BitmapEncoder.BitmapFormat.PNG);
        }
        catch(Exception e)
        {
            System.out.println("Something went wrong!");
        }
    }

    private static HashMap<String, Double> normalize(HashMap<String, Double> map)
    {
        Double total = Double.valueOf(0);
        HashMap<String, Double> frequencies = new HashMap<>();

        for(Double l : map.values())
        {
            total += l;
        }

        for(String e : map.keySet())
        {
            frequencies.put(e, Double.valueOf(map.get(e)).doubleValue()/total.doubleValue());
        }

        return frequencies;
    }

    private static String readFileAsString(String filename)
    {
        try
        {
            String original = new String(Files.readAllBytes(Paths.get(filename)));
            String noQuotes = original.replace("\"", "");
            String noApost = noQuotes.replace("\'", "");
            String none = noApost.replace("`", "");
            return none;
        }
        catch (IOException e)
        {
            return null;
        }
    }

    private static FrequencyMap frequence(String filename)
    {
        FrequencyMap freq = new FrequencyMap();
        String file = readFileAsString(filename).toLowerCase();

        for(String word : Arrays.asList(file.split("[\\p{Punct}\\s]+")))
        {
            freq.addWord(word);
        }

        freq.normalize();

        return freq;
    }

    private static class ScatterChart implements ExampleChart<XYChart>
    {
        @Override
        public XYChart getChart()
        {
            // Create Chart
            XYChart chart = new XYChartBuilder().width(1000).height(800).build();

            // Customize Chart
            chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);
            chart.getStyler().setChartTitleVisible(true);
            chart.getStyler().setMarkerSize(12);
            chart.getStyler().setHasAnnotations(true);

            // Series
            List<Double> xData = Arrays.stream(x.getArray()[0]).boxed().collect(Collectors.toList());
            List<Double> yData = Arrays.stream(y.getArray()[0]).boxed().collect(Collectors.toList());

            String titles[] = niceNames;

            for(int i = 0; i < 7; ++i)
            {
                XYSeries a = chart.addSeries(titles[i], Arrays.asList(xData.get(i)), Arrays.asList(yData.get(i)));
                a.setMarkerColor(Color.ORANGE);
            }

            for(int i = 7; i < 14; ++i)
            {
                XYSeries a = chart.addSeries(titles[i], Arrays.asList(xData.get(i)), Arrays.asList(yData.get(i)));
                a.setMarkerColor(Color.BLUE);
            }

            for(int i = 14; i < 18; ++i)
            {
                XYSeries a = chart.addSeries(titles[i], Arrays.asList(xData.get(i)), Arrays.asList(yData.get(i)));
                a.setMarkerColor(Color.GREEN);
            }

            for(int i = 18; i < 23; ++i)
            {
                XYSeries a = chart.addSeries(titles[i], Arrays.asList(xData.get(i)), Arrays.asList(yData.get(i)));
                a.setMarkerColor(Color.RED);
            }

            for(int i = 23; i < 29; ++i)
            {
                XYSeries a = chart.addSeries(titles[i], Arrays.asList(xData.get(i)), Arrays.asList(yData.get(i)));
                a.setMarkerColor(Color.CYAN);
            }

            //for(int i = 29; i < 35; ++i)
            //{
            //    XYSeries a = chart.addSeries(titles[i], Arrays.asList(xData.get(i)), Arrays.asList(yData.get(i)));
            //    a.setMarkerColor(Color.PINK);
            //}
            return chart;
        }
    }

    // Allows for the sorting of a map by value
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map)
    {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, (e1, e2) -> -(e1.getValue()).compareTo(e2.getValue()));
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}