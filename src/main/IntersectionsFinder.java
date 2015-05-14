package main;

import exception.CustomException;
import ngrams.NGramCSVReader;
import ngrams.NGramUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Constants;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by aignat on 5/14/2015.
 */
public class IntersectionsFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntersectionsFinder.class);

    public static void main(String[] args) {

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
