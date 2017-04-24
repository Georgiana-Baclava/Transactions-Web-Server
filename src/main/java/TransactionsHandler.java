import com.mongodb.*;
import com.mongodb.util.JSON;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

/**
 * Created by bgl on 19.04.2017.
 */
public class TransactionsHandler implements HttpHandler {

    static final long SECONDS_IN_A_DAY = 86400;

    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();

        if ("GET".equals(method)) {
            getTransactionsThresholdHandler(httpExchange);
        } else if ("POST".equals(method)) {
            postTransactionsHandler(httpExchange);
        }
    }


    private void getTransactionsThresholdHandler(HttpExchange httpExchange) throws IOException {
        URI uri = httpExchange.getRequestURI();
        String query = uri.getRawQuery();
        Map<String, String> params = URLQueryParser.parseQuery(query);

        Integer user = Integer.valueOf(params.get("user"));
        Long day = Long.valueOf(params.get("day"));
        Integer threshold = Integer.valueOf(params.get("threshold"));

        //get transactions that meet the constraints
        StringBuilder sb = getTransactionsThreshold(user, day, threshold);

        String response = sb.toString();
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());

        os.close();
    }

    private StringBuilder getTransactionsThreshold(Integer user, Long day, Integer threshold) {
        DBCollection dbCollection = MongoDBConnection.INSTANCE.getTrxCollection();

        DBObject searchObject = new BasicDBObject();
        DBObject senderClause = new BasicDBObject("sender", user);
        DBObject receiverClause = new BasicDBObject("receiver", user);
        BasicDBList or = new BasicDBList();
        or.add(senderClause);
        or.add(receiverClause);
        searchObject.put("$or", or);

        //day
        Long startDay = (day / SECONDS_IN_A_DAY) * SECONDS_IN_A_DAY;
        Long endDay = startDay + SECONDS_IN_A_DAY;

        searchObject.put("timestamp",new BasicDBObject("$gte", startDay).append("$lt", endDay));
        searchObject.put("sum", new BasicDBObject().append("$gt", threshold));

        DBObject sortCriteria = new BasicDBObject();
        sortCriteria.put("sum", 1);

        DBObject explainObject = dbCollection.find(searchObject).sort(sortCriteria).explain();
        System.out.println("sorting with Index--->"+explainObject);

        DBObject removeIdProjection = new BasicDBObject("_id", 0);

        DBCursor dbCursor = dbCollection.find(searchObject, removeIdProjection).sort(sortCriteria);
        StringBuilder sb  = new StringBuilder();
        while (dbCursor.hasNext()) {
            sb.append(dbCursor.next().toString() + "\n");
        }
        return sb;
    }


    private void postTransactionsHandler(HttpExchange httpExchange) throws IOException {

        //read JSON
        InputStreamReader input = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
        BufferedReader reader = new BufferedReader(input);

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String json = sb.toString();

        //save JSON data to mongoDB
        DBCollection dbCollection = MongoDBConnection.INSTANCE.getTrxCollection();
        DBObject dbObject = (DBObject) JSON.parse(json);
        dbCollection.insert(dbObject);

        String response = "Transaction successfully added!";
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();

    }
}
