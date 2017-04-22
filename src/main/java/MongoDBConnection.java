import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

/**
 * Created by bgl on 19.04.2017.
 */
public enum MongoDBConnection {

    INSTANCE;

    private MongoClient mongo;
    private DB db;
    private DBCollection trxCollection;

    MongoDBConnection() {
        mongo = new MongoClient("localhost", 27017);
        db = mongo.getDB("webserver");
        trxCollection = db.getCollection("transactions");

        BasicDBObject senderIndex = new BasicDBObject("sender", "hashed");
        BasicDBObject receiverIndex = new BasicDBObject("receiver", "hashed");
        BasicDBObject tsIndex = new BasicDBObject("timestamp", 1);
        BasicDBObject sumIndex = new BasicDBObject("sum", 1);

        trxCollection.createIndex(senderIndex);
        trxCollection.createIndex(receiverIndex);
        trxCollection.createIndex(tsIndex);
        trxCollection.createIndex(sumIndex);
    }

    public MongoClient getMongo() {
        return mongo;
    }

    public DB getDb() {
        return db;
    }

    public DBCollection getTrxCollection() {
        return trxCollection;
    }
}
