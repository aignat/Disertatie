package ngrams;

import com.mongodb.DB;
import exception.CustomException;
import math.MathUtils;
import mongo.MongoDBService;
import utils.Constants;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class NGramUtils {

    private static HashMap<Integer, Long> totalCounts;

    private static HashMap<Integer, Long> getTotalCounts() throws CustomException {
        if (totalCounts == null) {
            totalCounts = readTotalCounts();
        }
        return totalCounts;
    }

    private static HashMap<Integer, Long> readTotalCounts() throws CustomException {

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

    public static List<Integer> getPeakYears(List<Double> data) {

        int windowSize = 5;
        double h = 1.8;

        List<Integer> peakYearsList = new ArrayList<Integer>();
        List<Double> peakFuncValues = new ArrayList<Double>();
        List<Double> peakFuncValuesAux = new ArrayList<Double>(Collections.nCopies(data.size(), 0.0));

        for (int i = 0; i < data.size(); i++) {
            peakFuncValues.add(S1(windowSize, i, data));
        }

        for (int i = 0; i < peakFuncValues.size(); i++) {
            double localAverage = MathUtils.getLocalAverage(i, windowSize, peakFuncValues);
            double localStandardDeviation = MathUtils.getLocalStandardDeviation(i, windowSize, peakFuncValues);

            if ((peakFuncValues.get(i) > 0) && (peakFuncValues.get(i) - localAverage > h * localStandardDeviation) && checkIfMax(i, data)) {
                peakFuncValuesAux.set(i, peakFuncValues.get(i));
            }
        }

//        for (int i = windowSize; i < peakFuncValuesAux.size() - windowSize; i++) {
//            for (int j = 1; j < windowSize; j++) {
//                if (peakFuncValuesAux.get(i - j) < peakFuncValuesAux.get(i)) {
//                    peakFuncValuesAux.set(i - j, 0.0);
//                }
//                if (peakFuncValuesAux.get(i + j) < peakFuncValuesAux.get(i)) {
//                    peakFuncValuesAux.set(i + j, 0.0);
//                }
//            }
//        }

        for (int i = 0; i < peakFuncValuesAux.size(); i++) {
            if (peakFuncValuesAux.get(i) > 0) {
                peakYearsList.add(Constants.NGRAM_START_YEAR + i);
            }
        }

        return peakYearsList;
    }

    private static boolean checkIfMax(int currIndex, List<Double> peak_func_values) {

        return (!(((currIndex > 0) && (peak_func_values.get(currIndex) < peak_func_values.get(currIndex - 1))) ||
                ((currIndex < peak_func_values.size() - 1) && (peak_func_values.get(currIndex) < peak_func_values.get(currIndex + 1)))));
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

    public static List<Double> getProcessedData(DB db, String word) throws CustomException {
        return smoothData(normalizeData(MongoDBService.getNGram(db, word)));
    }
}
