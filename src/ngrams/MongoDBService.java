package ngrams;

import com.mongodb.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by aignat on 5/27/2015.
 */
public class MongoDBService {

    private DB db;

    public MongoDBService(MongoClient mongoClient, String database) {
        db = mongoClient.getDB(database);
    }

    public List<DBObject> getAllNGrams() {

        DBCollection collection = db.getCollection("all_ngrams");

        return collection.find().toArray();
    }

    public List<DBObject> getNGram(String ngram) {

        DBCollection collection = db.getCollection("e_1ngram");
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("ngram", ngram);

        return collection.find(searchQuery).sort(new BasicDBObject("year", 1)).toArray();
    }

    public HashMap<Integer, Long> getTotalCounts() {

        HashMap<Integer, Long> totalCountMap = new HashMap<Integer, Long>();

        DBCollection collection = db.getCollection("total_counts");
        DBCursor cursor = collection.find();

        while (cursor.hasNext()) {
            DBObject object = cursor.next();
            totalCountMap.put((Integer) object.get("year"), ((Number) (object.get("match_count"))).longValue());
        }

        return totalCountMap;
    }

    public void filterData() {
        String regex = "";

        DBCollection ngramsCollection = db.getCollection("total_counts");
    }

    public void normalizeData() {

        DBCollection ngramsCollection = db.getCollection("total_counts");
        HashMap<Integer, Long> totalCountsMap = getTotalCounts();

        DBObject query = new BasicDBObject();
        DBObject update = new BasicDBObject();
        update.put("$mul", new BasicDBObject("match_count", 1.0F/2));

        ngramsCollection.update(query, update, false, true);
    }

    public void logarithmizeData() {

        DBCollection ngramsCollection = db.getCollection("total_counts");

        DBObject query = new BasicDBObject();
        DBObject update = new BasicDBObject();
        update.put("$mul", new BasicDBObject("match_count", 1.0F/2));

        ngramsCollection.update(query, update, false, true);
    }

}
