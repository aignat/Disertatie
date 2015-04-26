package main;

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

        if (!new File(Constants.WORDNET_WORDS_FILE).isFile()) {
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

            //get all the words from NordNet + synonyms and write them to a file
            WordNet.writeAllWordsWithSynonymsToFile(Constants.WORDNET_WORDS_FILE);
        }
        
        //get peak years for all words in WordNet and write them to a file
//        NGramUtils.getPeakYearsForAllWordNetWords();

        NGramCSVReader nGramReader = new NGramCSVReader();
        TreeMap<Integer, Float> data1 = nGramReader.readCSV("English", "India", true);
        TreeMap<Integer, Float> data2 = nGramReader.readCSV("English", "computer", true);

        //China, India : 1934 1941 1999 2002 2007
        //China, computer : 1982 2004
        //India, computer : 1995 2004

        ArrayList<Integer> lista = NGramUtils.getIntersectionYears(data1, data2);
        for (int i : lista) {
            System.out.println(i);
        }
        
//        System.out.println("Peak years:");
//        ArrayList<Integer> peakYears = NGramUtils.getPeakYears(data2, 10);
//        for (int i : peakYears) {
//            System.out.println(i);
//        }
        
//        nGramReader.readAllWordsFromCSVToFile("English");
        
    }
}

