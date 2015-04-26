package ngrams;

import utils.Constants;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aignat
 */
public class NGramCSVReader {

    private static HashMap<Integer, Long> yearToBookTotalCounts;

    public NGramCSVReader() {
        yearToBookTotalCounts = new HashMap<Integer, Long>();
        readTotalCounts();
    }

    public static void readTotalCounts() {
        BufferedReader br = null;
        String line;
        String splitBy = ",";

        try {
            br = new BufferedReader(new FileReader(Constants.TOTAL_COUNTS_FILENAME));
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(splitBy);
                yearToBookTotalCounts.put(Integer.parseInt(tokens[0]), Long.parseLong(tokens[1]));
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error reading CSV file, FileNotFoundException.");
        } catch (IOException e) {
            System.out.println("Error reading CSV file, IOException.");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static TreeMap<Integer, Float> smoothData(TreeMap<Integer, Float> yearToFrequency, int smoothing) {
        TreeMap<Integer, Float> smoothData = new TreeMap<Integer, Float>();

        Set<Integer> keySet = yearToFrequency.keySet();
        Object[] keys = keySet.toArray();

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

    public static String getCSVFilePathAndName(String corpus, String word) {
        return Constants.CORPUS_PATH + corpus + "\\" + Constants.ENGLISH_1GRAM_CSV_PREFIX + word.substring(0, 1).toLowerCase();
    }

    public static TreeMap<Integer, Float> readCSV(String corpus, String word, boolean writeToFile) {
        //String csvFileToRead = "C:\\GoogleNgrams\\1grams\\googlebooks-eng-us-all-1gram-20120701-a";
        String csvFileToRead = getCSVFilePathAndName(corpus, word);
        BufferedReader br = null;
        PrintWriter pw = null;
        String line;
        String splitBy = "\t";
        boolean alreadyRead = false;
        int startYear = Constants.STARTING_YEAR;
        int endYear = Constants.END_YEAR;
        TreeMap<Integer, Float> yearToFrequency = new TreeMap<Integer, Float>();

        for (int i = startYear; i <= endYear; i++) {
            yearToFrequency.put(i, 0.0F);
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
                    if (key < Constants.STARTING_YEAR) {
                        continue;
                    }
                    if (key > Constants.END_YEAR) {
                        break;
                    }
                    yearToFrequency.put(key, Float.parseFloat(tokens[2]) / yearToBookTotalCounts.get(key));
                } else {
                    if (alreadyRead) {
                        break;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error reading CSV file, FileNotFoundException.");
        } catch (IOException e) {
            System.out.println("Error reading CSV file, IOException.");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }

        yearToFrequency = smoothData(yearToFrequency, Constants.SMOOTHING);

        if (writeToFile) {
            try {
                pw = new PrintWriter(word + ".txt", "UTF-8");
                for (Map.Entry<Integer, Float> yearFrequencyEntry : yearToFrequency.entrySet()) {
                    pw.println(yearFrequencyEntry.getKey() + " " + yearFrequencyEntry.getValue());
                }
            } catch (IOException e) {
                System.out.println("Error writing to file, IOException.");
            } finally {
                if (pw != null) {
                    pw.close();
                }
            }
        }

        System.out.println("Done with reading CSV");
        
        return yearToFrequency;
    }

    public HashSet<String> getAllWordsFromWordnetSynonymsFile() {
        HashSet<String> allWords = new HashSet<String>();

        BufferedReader br = null;
        String line;

        try {
            br = new BufferedReader(new FileReader(Constants.WORDNET_WORDS_FILE));
            while ((line = br.readLine()) != null) {
                String synonyms = line.split(":")[1];
                if (synonyms != null) {
                    for (String s : synonyms.split(",")) {
                        allWords.add(s);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Can't find Wordnet synonyms input file");
        } catch (IOException ex) {
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

    public void readAllWordsFromCSVToFile(String corpus) {

        BufferedReader br = null;
        PrintWriter pw = null;
        String csvFileToRead;
        String line;
        String splitBy = "\t";
        boolean alreadyRead;
        int startYear = Constants.STARTING_YEAR;
        int endYear = Constants.END_YEAR;
        TreeMap<Integer, Float> yearToFrequency = new TreeMap<Integer, Float>();

        try {
            pw = new PrintWriter(Constants.NGRAM_DATA_FOR_ALL_WORDS);

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
                    System.out.println("Error reading CSV file, FileNotFoundException.");
                } catch (IOException e) {
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
        } finally {
            if (pw != null) {
                pw.close();
            }
        }

        System.out.println("Done with reading CSV");
    }

}
