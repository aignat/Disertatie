package main;

import com.mongodb.MongoClient;
import exception.CustomException;
import ngrams.MongoDBService;
import ngrams.NGramUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

/**
 * Created by aignat on 5/14/2015.
 */
public class PeaksFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeaksFinder.class);

    public static void main(String[] args) {

        //write peak years for all WordNet words to Constants.WORDNET_WORDS_PEAKYEARS_FILE
        try {
            MongoClient mongoClient = new MongoClient("localhost", 27017);
            MongoDBService mongoDBService = new MongoDBService(mongoClient, "test");

            NGramUtils.writePeaksForAllWordNetWordsToFile(mongoDBService);
        } catch (CustomException e) {
            LOGGER.error(e.getMessage() + ":" + e.getOriginatingMethodName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


        System.out.println("Done finding peaks");
    }
}
