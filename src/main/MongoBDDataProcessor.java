package main;

import com.mongodb.MongoClient;
import exception.CustomException;
import ngrams.MongoDBService;

import java.net.UnknownHostException;

/**
 * Created by aignat on 5/28/2015.
 */
public class MongoBDDataProcessor {

    public static void main(String[] args) {

        try {
            MongoClient mongoClient = new MongoClient("localhost", 27017);
            MongoDBService mongoDBService = new MongoDBService(mongoClient, "test");

            //TODO: filter, normalize, apply logaritm
            mongoDBService.normalizeData();

            mongoClient.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (CustomException e) {
            e.printStackTrace();
        }
    }
}
