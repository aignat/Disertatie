package ngrams;

import exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Constants;

import java.io.*;
import java.util.*;

public class NGramUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(NGramUtils.class);

    public static ArrayList<Integer> getIntersectionYears(TreeMap<Integer, Float> data1, TreeMap<Integer, Float> data2) {
        ArrayList<Integer> intersectionYears = new ArrayList<Integer>();

        Iterator<Map.Entry<Integer, Float>> iterator1 = data1.entrySet().iterator();
        Iterator<Map.Entry<Integer, Float>> iterator2 = data2.entrySet().iterator();

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

    public static ArrayList<Integer> getPeakYears(TreeMap<Integer, Float> data) {

        int plateau = Constants.NGRAM_PEAK_PLATEAU;
        ArrayList<Integer> peakYearsList = new ArrayList<Integer>();
        ArrayList<Integer> years = new ArrayList<Integer>();
        ArrayList<Float> frequencies = new ArrayList<Float>();

        for (Map.Entry<Integer, Float> entry : data.entrySet()) {
            years.add(entry.getKey());
            frequencies.add(entry.getValue());
        }

        outerloop:
        for (int i = plateau; i < years.size() - plateau; i++) {
            float value = frequencies.get(i);

            for (int j = 1; j < plateau; j++) {
                if (frequencies.get(i - j) > value || frequencies.get(i + j) > value) {
                    continue outerloop;
                }
            }

            peakYearsList.add(years.get(i));
            i+= plateau - 1;
        }

        return peakYearsList;
    }

    public static void writePeaksForAllWordNetWordsToFile() throws CustomException {

        List<Integer> years = new ArrayList<Integer>();
        List<String> words = new ArrayList<String>();

        for (int i = Constants.NGRAM_STARTING_YEAR; i <= Constants.NGRAM_END_YEAR; i++) {
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
                LOGGER.info(word);

                try {
                    TreeMap<Integer, Float> data = nGramReader.readWordFromCSV(Constants.NGRAM_ENGLISH_CORPUS_NAME, word, false);

                    for (int i : NGramUtils.getPeakYears(data)) {
                        String newWord = words.get(i - Constants.NGRAM_STARTING_YEAR) + word + ",";
                        words.set(i - Constants.NGRAM_STARTING_YEAR, newWord);
                    }
                } catch(CustomException e) {
                    //do nothing, no peaks for the word
                }
            }
        } catch (FileNotFoundException e) {
            throw new CustomException("Constants.WORDNET_WORDS_SYNONYMS_FILE not found", Thread.currentThread().getStackTrace()[1].getMethodName());
        } catch (IOException ex) {
            throw new CustomException("Error reading Constants.WORDNET_WORDS_SYNONYMS_FILE", Thread.currentThread().getStackTrace()[1].getMethodName());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    throw new CustomException("Error closing Constants.WORDNET_WORDS_SYNONYMS_FILE", Thread.currentThread().getStackTrace()[1].getMethodName());
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
            throw new CustomException("Error writing to Constants.WORDNET_WORDS_PEAKYEARS_FILE", Thread.currentThread().getStackTrace()[1].getMethodName());
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    throw new CustomException("Error closing to Constants.WORDNET_WORDS_PEAKYEARS_FILE", Thread.currentThread().getStackTrace()[1].getMethodName());
                }
            }
        }

    }

}
