package ngrams;

import exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Constants;

import java.io.*;
import java.util.*;

public class NGramUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(NGramUtils.class);

    private static HashMap<Integer, Long> totalCounts;

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

    public static List<Integer> getIntersectionYears(List<Float> data1, List<Float> data2) {

        List<Integer> intersectionYears = new ArrayList<Integer>();

        for (int i = 1; i < data1.size() - 1; i++) {

            float previousValue1 = data1.get(i-1);
            float previousValue2 = data2.get(i-1);
            float actualValue1 = data1.get(i);
            float actualValue2 = data2.get(i);

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

                List<Float> wordData;

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

                    List<Float> synonymData;
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

    public static void writePeaksForAllWordNetWordsToFile() throws CustomException {

//        List<Integer> years = new ArrayList<Integer>();
//        List<String> words = new ArrayList<String>();
//        BufferedReader br = null;
//        String line;
//
//        for (int i = Constants.NGRAM_STARTING_YEAR; i <= Constants.NGRAM_END_YEAR; i++) {
//            years.add(i);
//            words.add("");
//        }
//
//        try {
//            br = new BufferedReader(new FileReader(Constants.WORDNET_WORDS_SYNONYMS_FILE));
//            while ((line = br.readLine()) != null) {
//
//                String word = line.split(":")[0];
//                LOGGER.info(word);
//
//                try {
//                    TreeMap<Integer, Float> data = NGramCSVReader.readWordFromCSV(Constants.NGRAM_ENGLISH_CORPUS_NAME, word, false);
//
//                    for (int i : NGramUtils.getPeakYears2(data)) {
//                        String newWord = words.get(i - Constants.NGRAM_STARTING_YEAR) + word + ",";
//                        words.set(i - Constants.NGRAM_STARTING_YEAR, newWord);
//                    }
//                } catch(CustomException e) {
//                    //do nothing, no peaks for the word
//                }
//            }
//        } catch (FileNotFoundException e) {
//            throw new CustomException("Constants.WORDNET_WORDS_SYNONYMS_FILE not found", Thread.currentThread().getStackTrace()[1].getMethodName());
//        } catch (IOException ex) {
//            throw new CustomException("Error reading Constants.WORDNET_WORDS_SYNONYMS_FILE", Thread.currentThread().getStackTrace()[1].getMethodName());
//        } finally {
//            if (br != null) {
//                try {
//                    br.close();
//                } catch (IOException ex) {
//                    throw new CustomException("Error closing Constants.WORDNET_WORDS_SYNONYMS_FILE", Thread.currentThread().getStackTrace()[1].getMethodName());
//                }
//            }
//        }
//
//        BufferedWriter bw = null;
//        try {
//            bw = new BufferedWriter(new FileWriter(Constants.WORDNET_WORDS_PEAKYEARS_FILE));
//            for (int i = 0; i < years.size(); i++) {
//                bw.write(years.get(i) + ":" + words.get(i));
//                bw.newLine();
//            }
//        } catch (IOException ex) {
//            throw new CustomException("Error writing to Constants.WORDNET_WORDS_PEAKYEARS_FILE", Thread.currentThread().getStackTrace()[1].getMethodName());
//        } finally {
//            if (bw != null) {
//                try {
//                    bw.close();
//                } catch (IOException e) {
//                    throw new CustomException("Error closing to Constants.WORDNET_WORDS_PEAKYEARS_FILE", Thread.currentThread().getStackTrace()[1].getMethodName());
//                }
//            }
//        }

    }

    public static List<Float> normalizeData(List<Float> data) throws CustomException {

        List<Float> normalizedData = new ArrayList<Float>(Constants.NGRAM_END_YEAR - Constants.NGRAM_START_YEAR + 1);

        HashMap<Integer, Long> totalCountsMap = getTotalCounts();

        for (int i = 0; i < data.size(); i++) {
            normalizedData.add(data.get(i) * 1.0F / totalCountsMap.get(i + Constants.NGRAM_START_YEAR));
        }

        return normalizedData;
    }

    public static List<Float> smoothData(List<Float> data) {

        List<Float> smoothedData = new ArrayList<Float>(Constants.NGRAM_END_YEAR - Constants.NGRAM_START_YEAR + 1);

        for (int i = 0; i < data.size(); i++) {
            float sum = 0;
            int left = i - Constants.NGRAM_SMOOTHING >= 0 ? i - Constants.NGRAM_SMOOTHING : 0;
            int right = i + Constants.NGRAM_SMOOTHING < data.size() ? i + Constants.NGRAM_SMOOTHING : data.size() - 1;

            for (int j = left; j <= right; j++) {
                sum += data.get(j);
            }
            smoothedData.add(1.0F * sum / (right - left + 1));
        }

        return smoothedData;
    }

    public TreeMap<Integer, Float> logarithmizeData(TreeMap<Integer, Float> data) {

//        DBCollection ngramsCollection = db.getCollection("total_counts");

//        DBObject query = new BasicDBObject();
//        DBObject update = new BasicDBObject();
//        update.put("$mul", new BasicDBObject("match_count", 1.0F/2));

//        ngramsCollection.update(query, update, false, true);
        return null;
    }

}
