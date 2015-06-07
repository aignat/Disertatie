package ngrams;

import com.mongodb.*;
import exception.CustomException;
import utils.Constants;

import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by aignat on 5/27/2015.
 */
public class MongoDBService {

    private DB db;

    public MongoDBService(MongoClient mongoClient, String database) {
        db = mongoClient.getDB(database);
    }

    public List<Float> getNGram(String ngram) throws CustomException {

        List<Float> data = new ArrayList<Float>(Collections.nCopies(Constants.NGRAM_END_YEAR - Constants.NGRAM_START_YEAR + 1, 0F));

        DBCollection collection = db.getCollection("grams");
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("ngram", ngram);

        DBCursor cursor = collection.find(searchQuery).sort(new BasicDBObject("year", 1));

        if (!cursor.hasNext()) {
            throw new CustomException(ngram + " not found", Thread.currentThread().getStackTrace()[1].getMethodName());
        }

        try {
            while (cursor.hasNext()) {
                DBObject object = cursor.next();
                data.set((Integer) (object.get("year")) - Constants.NGRAM_START_YEAR, ((Number) (object.get("match_count"))).floatValue());
            }
        } finally {
            cursor.close();
        }

        return data;
    }

}
