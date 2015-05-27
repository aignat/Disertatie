package ngrams;

import com.mongodb.*;

import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by aignat on 5/27/2015.
 */
public class MongoDBService {

    public DBCollection getCollection(String collectionName) {

        DBCollection collection = null;

        try {
            MongoClient mongo = new MongoClient( "localhost" , 27017 );
            DB db = mongo.getDB("test");
            collection = db.getCollection(collectionName);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return collection;
    }

    public List<DBObject> getAllNGrams() {

        DBCollection collection = getCollection("all_ngrams");

        return collection.find().toArray();
    }

    public List<DBObject> getNGram(String ngram) {

        DBCollection collection = getCollection("e_1ngram");

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("ngram", ngram);

        return collection.find(searchQuery).sort(new BasicDBObject("year", 1)).toArray();

        //        while (cursor.hasNext()) {
//            System.out.println(cursor.next());
//        }
    }



}
