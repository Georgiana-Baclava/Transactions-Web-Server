import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bgl on 19.04.2017.
 */
public class BalanceHandler implements HttpHandler {
    public void handle(HttpExchange httpExchange) throws IOException {

        DBCollection dbCollection = MongoDBConnection.INSTANCE.getTrxCollection();

        URI uri = httpExchange.getRequestURI();
        String query = uri.getRawQuery();

        Map<String, String> params = parseQuery(query);

        Integer user = Integer.valueOf(params.get("user"));
        Long since = Long.valueOf(params.get("since"));
        Long until = Long.valueOf(params.get("until"));

        System.out.println(user + since + until);

//        Date sinceDate = new Date(since);
//        Date untilDate = new Date(until);

        Double balance = 0.0;
        Double sended = 0.0;
        Double received = 0.0;

        DBObject match = new BasicDBObject("$match", new BasicDBObject("receiver", user).append("timestamp",
                new BasicDBObject("$gte", since).append("$lte", until)));

        DBObject group = new BasicDBObject("$group", new BasicDBObject("_id",
                new BasicDBObject("receiver", "$receiver").append("timestamp", "$timestamp")).append("receivedAmount",
                new BasicDBObject("$sum", "$sum")));

        System.out.println(group.toString());

        DBObject project = new BasicDBObject("$project", new BasicDBObject("_id", 0).append("receivedAmount", 1));

        System.out.println("ceva ok");

        AggregationOutput output = dbCollection.aggregate(match, group, project);
        System.out.println("ceva rau");

        for (DBObject result : output.results()) {
            received += ((Number)result.get("receivedAmount")).doubleValue();
            System.out.println(result);
            System.out.println(received);
        }

        DBObject match2 = new BasicDBObject("$match", new BasicDBObject("sender", user).append("timestamp",
                new BasicDBObject("$gte", since).append("$lte", until)));

        DBObject group2 = new BasicDBObject("$group", new BasicDBObject("_id",
                new BasicDBObject("sender", "$sender").append("timestamp", "$timestamp")).append("sendedAmount",
                new BasicDBObject("$sum", "$sum")));

        System.out.println(group.toString());

        DBObject project2 = new BasicDBObject("$project", new BasicDBObject("_id", 0).append("sendedAmount", 1));

        System.out.println("ceva ok");

        AggregationOutput output2 = dbCollection.aggregate(match2, group2, project2);
        System.out.println("ceva rau");

        for (DBObject result : output2.results()) {
            sended += ((Number)result.get("sendedAmount")).doubleValue();
            System.out.println(result);
            System.out.println(received);
        }

        balance = received - sended;

        String response = "Balance: " + balance;
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    public static Map<String, String> parseQuery(String query)
    {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params)
        {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
            System.out.println(name + value);
        }
        return map;
    }
}
