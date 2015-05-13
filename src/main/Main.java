package main;

import exception.CustomException;
import ngrams.NGramCSVReader;
import ngrams.NGramUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Constants;
import wordnet.WordNet;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 *
 * @author aignat
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        //if not already done, write all the WordNet words and their synonyms to Constants.WORDNET_WORDS_SYNONYMS_FILE
        if (!new File(Constants.WORDNET_WORDS_SYNONYMS_FILE).isFile()) {
            //initialize WordNet
            try {
                WordNet.initializeWordNet();
            } catch (CustomException e) {
                LOGGER.error(e.getMessage() + ":" + e.getOriginatingMethodName());
                return;
            }

            try {
                WordNet.writeAllWordsWithSynonymsToFile();
            } catch (CustomException e) {
                LOGGER.error(e.getMessage() + ":" + e.getOriginatingMethodName());
            }
        }
        
        //if not already done, write peak years for all WordNet words to Constants.WORDNET_WORDS_PEAKYEARS_FILE
        if (!new File(Constants.WORDNET_WORDS_PEAKYEARS_FILE).isFile()) {
            try {
                NGramUtils.writePeaksForAllWordNetWordsToFile();
            } catch (CustomException e) {
                LOGGER.error(e.getMessage() + ":" + e.getOriginatingMethodName());
            }
        }

        try {
            TreeMap<Integer, Float> data1 = NGramCSVReader.readWordFromCSV(Constants.NGRAM_ENGLISH_CORPUS_NAME, "house", false);
            TreeMap<Integer, Float> data2 = NGramCSVReader.readWordFromCSV(Constants.NGRAM_ENGLISH_CORPUS_NAME, "home", false);
            System.out.println("=========");
            for (int i : NGramUtils.getPeakYears(data1)) {
                System.out.println(i);
            }
            System.out.println("=========");
            for (int i : NGramUtils.getPeakYears(data2)) {
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
        } catch (CustomException e) {
            LOGGER.error(e.getOriginatingMethodName() + ":" + e.getMessage());
        }

//        nGramReader.readAllWordsFromCSVToFile("English");
        
    }
}

