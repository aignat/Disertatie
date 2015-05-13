package ngrams;

import exception.CustomException;
import utils.Constants;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


/**
 *
 * @author aignat
 */
public class NGramCSVReader {

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

    public static TreeMap<Integer, Float> smoothData(TreeMap<Integer, Float> yearToFrequency, int smoothing) {

        TreeMap<Integer, Float> smoothData = new TreeMap<Integer, Float>();
        Object[] keys = yearToFrequency.keySet().toArray();

        for (int i = 0; i < yearToFrequency.size(); i++) {
            float sum = 0;
            int left = i - smoothing >= 0 ? i - smoothing : 0;
            int right = i + smoothing < yearToFrequency.size() ? i + smoothing : yearToFrequency.size() - 1;
            for (int j = left; j <= right; j++) {
                sum += yearToFrequency.get((Integer) keys[j]);
            }

            smoothData.put((Integer) keys[i], 1.0F * sum / (right - left + 1));
        }

        return smoothData;
    }

    public static TreeMap<Integer, Float> readWordFromCSV(String corpus, String word, boolean writeToFile) throws CustomException {

        String csvFileToRead = Constants.NGRAM_CORPUS_PATH + File.separator + corpus + File.separator
                             + Constants.NGRAM_ENGLISH_CSV_PREFIX + word.substring(0, 1).toLowerCase();
        String line;
        BufferedReader br = null;
        PrintWriter pw = null;
        boolean alreadyRead = false;
        TreeMap<Integer, Float> yearToFrequency = new TreeMap<Integer, Float>();
        HashMap<Integer, Long> yearToBookTotalCounts = readTotalCounts();

        for (int i = Constants.NGRAM_STARTING_YEAR; i <= Constants.NGRAM_END_YEAR; i++) {
            yearToFrequency.put(i, 0.0F);
        }

        try {
            br = new BufferedReader(new FileReader(csvFileToRead));
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\t");

                if (!tokens[0].equals(word)) {
                    if (alreadyRead) {
                        break;
                    } else {
                        continue;
                    }
                }

                int year = Integer.parseInt(tokens[1]);
                if (year < Constants.NGRAM_STARTING_YEAR) {
                    continue;
                } else if (year > Constants.NGRAM_END_YEAR) {
                    break;
                }
                yearToFrequency.put(year, Float.parseFloat(tokens[2]) / yearToBookTotalCounts.get(year));
                alreadyRead = true;
            }
        } catch (FileNotFoundException e) {
            throw new CustomException("CSV file not found", Thread.currentThread().getStackTrace()[1].getMethodName());
        } catch (IOException e) {
            throw new CustomException("Error reading CSV file", Thread.currentThread().getStackTrace()[1].getMethodName());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    throw new CustomException("Error closing CSV file", Thread.currentThread().getStackTrace()[1].getMethodName());
                }
            }
        }

        if (!alreadyRead) {
            throw new CustomException(word + " doesn't exist in the CSV files", Thread.currentThread().getStackTrace()[1].getMethodName());
        }

        yearToFrequency = smoothData(yearToFrequency, Constants.NGRAM_SMOOTHING);

        if (writeToFile) {
            try {
                pw = new PrintWriter(word + ".txt", "UTF-8");
                for (Map.Entry<Integer, Float> yearFrequencyEntry : yearToFrequency.entrySet()) {
                    pw.println(yearFrequencyEntry.getKey() + " " + yearFrequencyEntry.getValue());
                }
            } catch (IOException e) {
                throw new CustomException("Error writing to file", Thread.currentThread().getStackTrace()[1].getMethodName());
            } finally {
                if (pw != null) {
                    pw.close();
                }
            }
        }

        return yearToFrequency;
    }

/*
    //TODO: do we still need this?
    public HashSet<String> getAllWordsFromWordnetSynonymsFile() {
        HashSet<String> allWords = new HashSet<String>();

        BufferedReader br = null;
        String line;

        try {
            br = new BufferedReader(new FileReader(Constants.WORDNET_WORDS_SYNONYMS_FILE));
            while ((line = br.readLine()) != null) {
                String synonyms = line.split(":")[1];
                if (synonyms != null) {
                    for (String s : synonyms.split(",")) {
                        allWords.add(s);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("In method " + Thread.currentThread().getStackTrace()[1].getMethodName());
            System.out.println("Can't find Wordnet synonyms input file");
        } catch (IOException ex) {
            System.out.println("In method " + Thread.currentThread().getStackTrace()[1].getMethodName());
            System.out.println("Error reading wordnet file");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    System.out.println("Can't close file");
                }
            }
        }

        return allWords;
    }
*/

/*
    //TODO: optimize repeated reading from CVS, by doing initial reading of words from WordNet into a file.
    public void readAllWordsFromCSVToFile(String corpus) {

        BufferedReader br = null;
        PrintWriter pw = null;
        String csvFileToRead;
        String line;
        String splitBy = "\t";
        boolean alreadyRead;
        int startYear = Constants.NGRAM_STARTING_YEAR;
        int endYear = Constants.NGRAM_END_YEAR;
        TreeMap<Integer, Float> yearToFrequency = new TreeMap<Integer, Float>();
        HashMap<Integer, Long> yearToBookTotalCounts = readTotalCounts();

        try {
            pw = new PrintWriter(Constants.NGRAM_ENGLISH_DATA_FOR_WORDNET);

            HashSet<String> allWords = getAllWordsFromWordnetSynonymsFile();
            //for each word read data from csv
            for (String word : allWords) {
                System.out.println(word);
                alreadyRead = false;
                for (int i = startYear; i <= endYear; i++) {
                    yearToFrequency.put(i, 0.0F);
                }
                try {
                    csvFileToRead = getCSVFilePathAndName(corpus, word);
                } catch (NullPointerException e) {
                    continue;
                }
                try {
                    br = new BufferedReader(new FileReader(csvFileToRead));
                    while ((line = br.readLine()) != null) {
                        String[] tokens = line.split(splitBy);
                        if (tokens[0].equals(word)) {
                            if (!alreadyRead) {
                                alreadyRead = true;
                            }
                            int key = Integer.parseInt(tokens[1]);
                            yearToFrequency.put(key, Float.parseFloat(tokens[2]) / yearToBookTotalCounts.get(key));
                        } else {
                            if (alreadyRead) {
                                break;
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("In method " + Thread.currentThread().getStackTrace()[1].getMethodName());
                    System.out.println("Error reading CSV file, FileNotFoundException.");
                } catch (IOException e) {
                    System.out.println("In method " + Thread.currentThread().getStackTrace()[1].getMethodName());
                    System.out.println("Error reading CSV file, IOException.");
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                        }
                    }
                }
                //write data to output file
                pw.print(word + ":");
                for (Map.Entry<Integer, Float> yearFrequencyEntry : yearToFrequency.entrySet()) {
                    pw.print(yearFrequencyEntry.getValue());
                }
                pw.println();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NGramCSVReader.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("In method " + Thread.currentThread().getStackTrace()[1].getMethodName());
        } finally {
            if (pw != null) {
                pw.close();
            }
        }

        System.out.println("Done with reading CSV");
    }
*/

}
