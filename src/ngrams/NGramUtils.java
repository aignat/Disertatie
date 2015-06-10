package ngrams;

import exception.CustomException;
import math.MathUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Constants;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class NGramUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(NGramUtils.class);

    private static HashMap<Integer, Long> totalCounts;
    private static double log2 = Math.log(2);

    public static HashMap<Integer, Long> getTotalCounts() throws CustomException {
        if (totalCounts == null) {
            totalCounts = readTotalCounts();
        }
        return totalCounts;
    }

    public static HashMap<Integer, Long> readTotalCounts() throws CustomException {

        HashMap<Integer, Long> yearToBookTotalCounts = new HashMap<Integer, Long>();
        BufferedReader br = null;
        String line;

        try {
            br = new BufferedReader(new FileReader(Constants.NGRAM_ENGLISH_TOTALCOUNTS_FILE));
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                yearToBookTotalCounts.put(Integer.parseInt(tokens[0]), Long.parseLong(tokens[1]));
            }
        } catch (FileNotFoundException e) {
            throw new CustomException("Constants.NGRAM_ENGLISH_TOTALCOUNTS_FILE not found", Thread.currentThread().getStackTrace()[1].getMethodName());
        } catch (IOException e) {
            throw new CustomException("Error reading from CSV file", Thread.currentThread().getStackTrace()[1].getMethodName());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    throw new CustomException("Error closing Constants.NGRAM_ENGLISH_TOTALCOUNTS_FILE", Thread.currentThread().getStackTrace()[1].getMethodName());
                }
            }
        }

        return yearToBookTotalCounts;
    }

    public static List<Integer> getIntersectionYears(List<Double> data1, List<Double> data2) {

        List<Integer> intersectionYears = new ArrayList<Integer>();

        for (int i = 1; i < data1.size() - 1; i++) {

            double previousValue1 = data1.get(i-1);
            double previousValue2 = data2.get(i-1);
            double actualValue1 = data1.get(i);
            double actualValue2 = data2.get(i);

            if ((previousValue1 - previousValue2) * (actualValue1 - actualValue2) < 0) {       // check the intersection
                if ((previousValue1 - actualValue1) * (previousValue2 - actualValue2) <= 0) {   // check tendencies to be in opposition
                    intersectionYears.add(Constants.NGRAM_START_YEAR + i);
                }
            }
        }

        return intersectionYears;
    }

    public static void writeIntersectionForAllWordsToFile(MongoDBService mongoDBService) throws CustomException {

        String line;
        BufferedReader br = null;
        BufferedWriter bw = null;

        try {
            br = new BufferedReader(new FileReader(Constants.WORDNET_WORDS_SYNONYMS_FILE));
            bw = new BufferedWriter(new FileWriter(Constants.WORDNET_WORDS_INTERSECTIONS));
            Set<String> pairSet = new HashSet<String>();

            while ((line = br.readLine()) != null) {

                String[] synonyms = line.split(":|,");
                LOGGER.info(synonyms[0]);

                List<Double> wordData;

                try {
                    wordData = NGramUtils.smoothData(NGramUtils.normalizeData(mongoDBService.getNGram(synonyms[0])));
                } catch (CustomException e) {
                    continue;
                }

                for (int i = 1; i < synonyms.length; i++) {

                    if (synonyms[i].equals(synonyms[0])) {
                        continue;
                    }

                    if (pairSet.contains(synonyms[i] + "+" + synonyms[0])) {
                        continue;
                    } else {
                        pairSet.add(synonyms[0] + "+" + synonyms[i]);
                    }

                    List<Double> synonymData;
                    try {
                        synonymData = NGramUtils.smoothData(NGramUtils.normalizeData(mongoDBService.getNGram(synonyms[i])));
                    } catch (CustomException e) {
                        continue;
                    }

                    List<Integer> intersections = getIntersectionYears(wordData, synonymData);

                    if (intersections.size() > 0) {
                        bw.write(synonyms[0] + "," + synonyms[i] + ":");
                        for (int intersection : intersections) {
                            bw.write(String.valueOf(intersection) + ",");
                        }
                        bw.newLine();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new CustomException("Constants.WORDNET_WORDS_SYNONYMS_FILE not found", Thread.currentThread().getStackTrace()[1].getMethodName());
        } catch (IOException e) {
            throw new CustomException("Error reading Constants.WORDNET_WORDS_SYNONYMS_FILE or writing to Constants.WORDNET_WORDS_INTERSECTIONS", Thread.currentThread().getStackTrace()[1].getMethodName());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    throw new CustomException("Error closing Constants.WORDNET_WORDS_SYNONYMS_FILE", Thread.currentThread().getStackTrace()[1].getMethodName());
                }
            }
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                    throw new CustomException("Error closing Constants.WORDNET_WORDS_INTERSECTIONS", Thread.currentThread().getStackTrace()[1].getMethodName());
                }
            }
        }
    }

    public static List<Integer> getPeakYears(List<Double> data) {

        int windowSize = 5;
        double h = 1.5;

        List<Integer> peakYearsList = new ArrayList<Integer>();
        List<Double> peakFuncValues = new ArrayList<Double>();
        List<Double> peakFuncValuesAux = new ArrayList<Double>(Collections.nCopies(data.size(), 0.0));

        for (int i = 0; i < data.size(); i++) {
            peakFuncValues.add(S1(windowSize, i, data));
        }

        for (int i = 0; i < peakFuncValues.size(); i++) {
            double localAverage = MathUtils.getLocalAverage(i, windowSize, peakFuncValues);
            double localStandardDeviation = MathUtils.getLocalStandardDeviation(i, windowSize, peakFuncValues);
            System.out.println(localStandardDeviation);

            if ((peakFuncValues.get(i) > 0) && (peakFuncValues.get(i) - localAverage > h * localStandardDeviation) && checkIfMax(i, data)) {
                peakFuncValuesAux.set(i, peakFuncValues.get(i));
            }
        }

        for (int i = 0; i < peakFuncValuesAux.size(); i++) {
            for (int j = 1; j < windowSize; j++) {
                if (peakFuncValuesAux.get(i) < peakFuncValuesAux.get(j)) {
                    peakFuncValuesAux.set(i, 0.0);
                } else {
                    peakFuncValuesAux.set(j, 0.0);
                }
            }
        }

        for (int i = 0; i < peakFuncValuesAux.size(); i++) {
            if (peakFuncValuesAux.get(i) > 0) {
                peakYearsList.add(Constants.NGRAM_START_YEAR + i);
            }
        }

        return peakYearsList;
    }

    @Test
    public void testGetPeakYears() {
        List<Double> list = new ArrayList<Double>();
        list.add(44.0); list.add(56.0); list.add(6.0); list.add(45.0); list.add(2.0); list.add(66.0); list.add(7.0);

        assertEquals(getPeakYears(list), Arrays.asList(1805));

    }

    private static boolean checkIfMax(int currIndex, List<Double> peak_func_values) {

        return (!(((currIndex > 0) && (peak_func_values.get(currIndex) < peak_func_values.get(currIndex - 1))) ||
                ((currIndex < peak_func_values.size() - 1) && (peak_func_values.get(currIndex) < peak_func_values.get(currIndex + 1)))));
    }

    @Test
    public void testCheckIfMax() {
        List<Double> list = new ArrayList<Double>();
        list.add(4.0); list.add(56.0); list.add(6.0); list.add(45.0);
        assertEquals(checkIfMax(0,list), false);
        assertEquals(checkIfMax(1,list), true);
        assertEquals(checkIfMax(2,list), false);
        assertEquals(checkIfMax(3,list), true);
    }

    private static double S1(int window_size, int current_index, List<Double> data_set) {
        List<Double> left = new ArrayList<Double>();
        List<Double> right = new ArrayList<Double>();

        left.add(0.0);
        right.add(0.0);

        for (int i = 1; i < window_size; i++) {
            if (current_index - i >= 0) {
                if (data_set.get(current_index - i) <= data_set.get(current_index - i + 1)) {
                    left.add(data_set.get(current_index) - data_set.get(current_index - i));
                } else {
                    break;
                }
            }
        }

        for (int i = 1; i < window_size; i++) {
            if (current_index + i < data_set.size()) {
                if (data_set.get(current_index + i) >= data_set.get(current_index + i - 1)) {
                    right.add(data_set.get(current_index) - data_set.get(current_index + i));
                } else {
                    break;
                }
            }
        }

        return (Collections.max(left) + Collections.max(right)) / 2;
    }

    @Test
    public void testS1() {
        List<Double> list = new ArrayList<Double>();
        list.add(44.0); list.add(56.0); list.add(6.0); list.add(45.0); list.add(2.0); list.add(66.0); list.add(7.0);

        assertEquals(S1(0, 0, list), 0F);
        assertEquals(S1(1, 1, list), 0F);
        assertEquals(S1(2, 2, list), 0F);
        assertEquals(S1(3, 3, list), 19.5F);
        assertEquals(S1(4, 4, list), 0F);
        assertEquals(S1(5, 5, list), 32F);
        assertEquals(S1(6, 6, list), 0F);
    }

    public static void writePeaksForAllWordNetWordsToFile(MongoDBService service) throws CustomException {

        List<Integer> years = new ArrayList<Integer>();
        List<String> words = new ArrayList<String>();
        BufferedReader br = null;
        String line;

        for (int i = Constants.NGRAM_START_YEAR; i <= Constants.NGRAM_END_YEAR; i++) {
            years.add(i);
            words.add("");
        }

        try {
            br = new BufferedReader(new FileReader(Constants.WORDNET_WORDS_SYNONYMS_FILE));
            while ((line = br.readLine()) != null) {

                String word = line.split(":")[0];
                LOGGER.info(word);

                try {
                    List<Double> datag = service.getNGram(word);
                    List<Double> datan = normalizeData(datag);
                    //List<Double> datas = smoothData(datan);
                    List<Double> data = logarithmizeData(datag);

                    for (int year : getPeakYears(data)) {
                        String newWord = words.get(year - Constants.NGRAM_START_YEAR) + word + ",";
                        words.set(year - Constants.NGRAM_START_YEAR, newWord);
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

    private static List<Double> normalizeData(List<Double> data) throws CustomException {

        List<Double> normalizedData = new ArrayList<Double>(Constants.NGRAM_END_YEAR - Constants.NGRAM_START_YEAR + 1);

        HashMap<Integer, Long> totalCountsMap = getTotalCounts();

        for (int i = 0; i < data.size(); i++) {
            normalizedData.add(data.get(i) * 1.0D / totalCountsMap.get(i + Constants.NGRAM_START_YEAR));
        }

        return normalizedData;
    }

    private static List<Double> smoothData(List<Double> data) {

        List<Double> smoothedData = new ArrayList<Double>(Constants.NGRAM_END_YEAR - Constants.NGRAM_START_YEAR + 1);

        for (int i = 0; i < data.size(); i++) {
            double sum = 0;
            int left = i - Constants.NGRAM_SMOOTHING >= 0 ? i - Constants.NGRAM_SMOOTHING : 0;
            int right = i + Constants.NGRAM_SMOOTHING < data.size() ? i + Constants.NGRAM_SMOOTHING : data.size() - 1;

            for (int j = left; j <= right; j++) {
                sum += data.get(j);
            }
            smoothedData.add(1.0D * sum / (right - left + 1));
        }

        return smoothedData;
    }

    private static List<Double> logarithmizeData(List<Double> data) {

        List<Double> logarithmicData = new ArrayList<Double>();

        for (int i = 0; i < data.size(); i++) {
            logarithmicData.add(Math.log(data.get(i) / log2));
        }

        return logarithmicData;
    }

}
