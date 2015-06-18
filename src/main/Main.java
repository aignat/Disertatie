package main;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import exception.CustomException;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;
import ngrams.NGramUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Constants;
import wordnet.WordNetUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author aignat
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        MongoClient mongoClient = null;
        BufferedWriter output = null;

        try {
            //create MongoDB client
            mongoClient = new MongoClient("localhost", 27017);
            DB db = mongoClient.getDB("test");

            output = new BufferedWriter(new FileWriter(Constants.RESULTS));

            //initialize WordNetUtils
            WordNetUtils.initializeWordNet();

            //process data
            for (Object pos : POS.getAllPOS()) {
                try {
                    Iterator iterator = Dictionary.getInstance().getIndexWordIterator((POS) pos);

                    while (iterator.hasNext()) {
                        IndexWord indexWord = (IndexWord) iterator.next();
                        String word = indexWord.getLemma();

                        if (!word.matches("([a-zA-Z])*")) {
                            continue;
                        }
                        LOGGER.info(word);

                        try {
                            List<Double> dataWord = NGramUtils.getProcessedData(db, word);
                            List<String> wordToPeaksList = new ArrayList<>(Collections.nCopies(dataWord.size(), ""));

                            for (String s : WordNetUtils.getHypernymsAndHyponyms(indexWord)) {
                                if (!s.matches("([a-zA-Z])*")) {
                                    continue;
                                }

                                List<Double> dataHypoOrHyper = NGramUtils.getProcessedData(db, s);
                                List<Integer> peakYears = NGramUtils.getPeakYears(dataHypoOrHyper);

                                for (int year : peakYears) {
                                    wordToPeaksList.set(year - Constants.NGRAM_START_YEAR, wordToPeaksList.get(year - Constants.NGRAM_START_YEAR) + s + '/');
                                }
                            }

                            for (String synonym : WordNetUtils.getSynonyms(indexWord)) {
                                if (!synonym.matches("([a-zA-Z])*")) {
                                    continue;
                                }

                                List<Double> dataSynonym = NGramUtils.getProcessedData(db, synonym);
                                for (int year : NGramUtils.getIntersectionYears(dataWord, dataSynonym)) {
                                    for (int i = 1; i < 10 && year - Constants.NGRAM_START_YEAR - i >= 0; i++) {
                                        if (!wordToPeaksList.get(year - Constants.NGRAM_START_YEAR - i).equals("")) {
                                            System.out.println(word + "," + synonym + "," + wordToPeaksList.get(year - Constants.NGRAM_START_YEAR - i) + "," + year + "," + (year - i));
//                                            output.write(word + "," + synonym + "," + wordToPeaksList.get(year - Constants.NGRAM_START_YEAR - i) + "," + year + "," + (year - i));
//                                            output.newLine();
                                        }
                                    }
                                }
                            }
                        } catch (CustomException e) {
                            continue;
                        }
                    }
                } catch (JWNLException ex) {}
            }
        } catch (CustomException e) {
            LOGGER.error(e.getMessage() + ":" + e.getOriginatingMethodName());
        } catch (UnknownHostException e) {
            LOGGER.error(e.getMessage() + ":" + e.getMessage());
        } catch (IOException e) {
            LOGGER.error(e.getMessage() + ":" + e.getMessage());
        } finally {
            mongoClient.close();
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage() + ":" + e.getMessage());
                }
            }
        }

        System.out.println("Done processing.");
    }
}