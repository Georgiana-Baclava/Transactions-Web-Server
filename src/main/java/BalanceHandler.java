import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

/**
 * Created by bgl on 19.04.2017.
 */
public class BalanceHandler implements HttpHandler {

    public void handle(HttpExchange httpExchange) throws IOException {
        DBCollection dbCollection = MongoDBConnection.INSTANCE.getTrxCollection();

        URI uri = httpExchange.getRequestURI();
        String query = uri.getRawQuery();
        Map<String, String> params = URLQueryParser.parseQuery(query);

        Integer user = Integer.valueOf(params.get("user"));
        Long since = Long.valueOf(params.get("since"));
        Long until = Long.valueOf(params.get("until"));

        Integer balance = 0;
        Integer sent = getAmountSent(user, since, until, dbCollection);
        Integer received = getAmountReceived(user, since, until, dbCollection);

        balance = received - sent;

        String response = "" + balance;
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private Integer getAmountSent(Integer user, Long since, Long until, DBCollection dbCollection) {
        Integer sent = 0;

        DBObject match = new BasicDBObject("$match", new BasicDBObject("sender", user).append("timestamp",
                new BasicDBObject("$gte", since).append("$lte", until)));

        DBObject group = new BasicDBObject("$group", new BasicDBObject("_id",
                new BasicDBObject("sender", "$sender").append("timestamp", "$timestamp")).append("sentAmount",
                new BasicDBObject("$sum", "$sum")));

        DBObject project = new BasicDBObject("$project", new BasicDBObject("_id", 0).append("sentAmount", 1));

        AggregationOutput output2 = dbCollection.aggregate(match, group, project);

        for (DBObject result : output2.results()) {
            sent += ((Number)result.get("sentAmount")).intValue();
        }
        return sent;
    }

    private Integer getAmountReceived(Integer user, Long since, Long until, DBCollection dbCollection) {
        Integer received = 0;

        DBObject match = new BasicDBObject("$match", new BasicDBObject("receiver", user).append("timestamp",
                new BasicDBObject("$gte", since).append("$lte", until)));

        DBObject group = new BasicDBObject("$group", new BasicDBObject("_id",
                new BasicDBObject("receiver", "$receiver").append("timestamp", "$timestamp")).append("receivedAmount",
                new BasicDBObject("$sum", "$sum")));

        DBObject project = new BasicDBObject("$project", new BasicDBObject("_id", 0).append("receivedAmount", 1));

        AggregationOutput output = dbCollection.aggregate(match, group, project);

        for (DBObject result : output.results()) {
            received += ((Number)result.get("receivedAmount")).intValue();
        }
        return received;
    }
}
