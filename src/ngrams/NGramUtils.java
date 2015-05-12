package ngrams;

import exception.WordNotFoundException;
import utils.Constants;

import java.io.*;
import java.util.*;

public class NGramUtils {

    public static ArrayList<Integer> getIntersectionYears(TreeMap<Integer, Float> data1, TreeMap<Integer, Float> data2) {
        ArrayList<Integer> intersectionYears = new ArrayList<Integer>();

        Set<Map.Entry<Integer, Float>> entrySet1 = data1.entrySet();
        Set<Map.Entry<Integer, Float>> entrySet2 = data2.entrySet();

        Iterator<Map.Entry<Integer, Float>> iterator1 = entrySet1.iterator();
        Iterator<Map.Entry<Integer, Float>> iterator2 = entrySet2.iterator();

        float previousValue1 = iterator1.next().getValue();
        float previousValue2 = iterator2.next().getValue();

        while (iterator1.hasNext() && iterator2.hasNext()) {

            Map.Entry<Integer, Float> entry1 = iterator1.next();
            int actualKey = entry1.getKey();
            float actualValue1 = entry1.getValue();
            float actualValue2 = iterator2.next().getValue();

            if ((previousValue1 - previousValue2) * (actualValue1 - actualValue2) <= 0) {       // check the intersection
                if ((previousValue1 - actualValue1) * (previousValue2 - actualValue2) <= 0) {   // check tendencies to be in opposition
                    intersectionYears.add(actualKey);
                }
            }

            previousValue1 = actualValue1;
            previousValue2 = actualValue2;
        }

        return intersectionYears;
    }

    public static ArrayList<Integer> getPeakYears(TreeMap<Integer, Float> data, int plateau) {

        ArrayList<Integer> peakYearsList = new ArrayList<Integer>();
        ArrayList<Integer> years = new ArrayList<Integer>();
        ArrayList<Float> frequences = new ArrayList<Float>();

        Set<Map.Entry<Integer, Float>> entrySet = data.entrySet();
        Iterator<Map.Entry<Integer, Float>> iterator = entrySet.iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, Float> entry = iterator.next();
            years.add(entry.getKey());
            frequences.add(entry.getValue());
        }

        for (int i = 0; i < years.size(); i++) {
            float value = frequences.get(i);
            int left = i - plateau >= 0 ? i - plateau : 0;
            int right = i + plateau < years.size() ? i + plateau : years.size() - 1;
            boolean isPeak = true;

            for (int j = left; j < i; j++) {
                if (frequences.get(j) > value) {
                    isPeak = false;
                    break;
                }
            }

            for (int j = i + 1; j < right; j++) {
                if (frequences.get(j) > value) {
                    isPeak = false;
                    break;
                }
            }

            if (isPeak) {
                peakYearsList.add(years.get(i));
                i++;
            }
        }

        return peakYearsList;
    }

    public static void writePeaksForAllWordNetWordsToFile() {

        List<Integer> years = new ArrayList<Integer>();
        List<String> words = new ArrayList<String>();

        for (int i = Constants.STARTING_YEAR; i <= Constants.END_YEAR; i++) {
            years.add(i);
            words.add("");
        }

        BufferedReader br = null;
        String line;
        NGramCSVReader nGramReader = new NGramCSVReader();

        try {
            br = new BufferedReader(new FileReader(Constants.WORDNET_WORDS_SYNONYMS_FILE));
            while ((line = br.readLine()) != null) {
                String word = line.split(":")[0];

                try {
                    TreeMap<Integer, Float> data = nGramReader.readCSV("English", word, false);
                    for (int i : NGramUtils.getPeakYears(data, 10)) {
                        String newWord = words.get(i - Constants.STARTING_YEAR) + word + ",";
                        words.set(i - Constants.STARTING_YEAR, newWord);
                    }
                } catch (WordNotFoundException e) {
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Can't find WordNet synonyms input file");
        } catch (IOException ex) {
            System.out.println("Error reading WordNet file");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    System.out.println("Can't close file");
                }
            }
        }
        
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(Constants.WORDNET_WORDS_PEAKYEARS_FILE));
            for (int i = 0; i < years.size(); i++) {
                bw.write(years.get(i) + ":" + words.get(i));
                bw.newLine();
            }
        } catch (IOException ex) {
            System.out.println("Error writing to peak years file");
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    System.out.println("Error closing peaks file");
                }
            }
        }

    }

}
