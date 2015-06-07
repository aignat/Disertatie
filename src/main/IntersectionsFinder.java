package main;

import com.mongodb.MongoClient;
import exception.CustomException;
import ngrams.MongoDBService;
import ngrams.NGramUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by aignat on 5/14/2015.
 */
public class IntersectionsFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntersectionsFinder.class);

    public static void main(String[] args) {

        try {
            MongoClient mongoClient = new MongoClient("localhost", 27017);
            MongoDBService mongoDBService = new MongoDBService(mongoClient, "test");

            NGramUtils.writeIntersectionForAllWordsToFile(mongoDBService);
        } catch (CustomException e) {
            LOGGER.error(e.getOriginatingMethodName() + ":" + e.getMessage());
        } catch (UnknownHostException e) {
            LOGGER.error(e.getMessage());
        }

        System.out.println("Done finding intersections");
    }

}
