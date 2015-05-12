package main;

import exception.WordNotFoundException;
import net.didion.jwnl.JWNLException;
import ngrams.NGramCSVReader;
import ngrams.NGramUtils;
import utils.Constants;
import wordnet.WordNet;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 *
 * @author aignat
 */
public class Main {

    public static void main(String[] args) {

        //if not already done, write all the WordNet words and their synonyms to Constants.WORDNET_WORDS_SYNONYMS_FILE
        if (!new File(Constants.WORDNET_WORDS_SYNONYMS_FILE).isFile()) {
            //initialize WordNet
            try {
                WordNet.initializeWordNet();
            } catch (JWNLException ex) {
                System.out.println("Can not initialize WordNet");
                ex.printStackTrace();
                return;
            } catch (FileNotFoundException ex) {
                System.out.println("Can not initialize WordNet");
                ex.printStackTrace();
                return;
            }

            WordNet.writeAllWordsWithSynonymsToFile();
        }
        
        //if not already done, write peak years for all WordNet words to Constants.WORDNET_WORDS_PEAKYEARS_FILE
        if (!new File(Constants.WORDNET_WORDS_PEAKYEARS_FILE).isFile()) {
            NGramUtils.writePeaksForAllWordNetWordsToFile();
        }

        try {
            NGramCSVReader nGramCSVReader = new NGramCSVReader();
            TreeMap<Integer, Float> data1 = nGramCSVReader.readCSV("English", "house", false);
            TreeMap<Integer, Float> data2 = nGramCSVReader.readCSV("English", "home", false);
            System.out.println("=========");
            for (int i : NGramUtils.getPeakYears(data1, 10)) {
                System.out.println(i);
            }
            System.out.println("=========");
            for (int i : NGramUtils.getPeakYears(data2, 10)) {
                System.out.println(i);
            }

            //China, India : 1934 1941 1999 2002 2007
            //China, computer : 1982 2004
            //India, computer : 1995 2004

            System.out.println("=========");
            ArrayList<Integer> lista = NGramUtils.getIntersectionYears(data1, data2);
            for (int i : lista) {
                System.out.println(i);
            }
        } catch (WordNotFoundException e) {
            System.out.println("Word not found in Google NGrams");
        }

//        nGramReader.readAllWordsFromCSVToFile("English");
        
    }
}

