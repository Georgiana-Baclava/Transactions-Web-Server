import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

;

/**
 * Created by bgl on 23.04.2017.
 */
public class TransactionsTests {

    @BeforeClass
    public static void initDatabase() {
        MongoDBConnection.INSTANCE.init("localhost");
        MongoDBConnection.INSTANCE.getTrxCollection().drop();
    }

    @BeforeClass
    public static void startServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(5000), 0);
            server.createContext("/", new ServerHomeHandler());
            server.createContext("/transactions/", new TransactionsHandler());
            server.createContext("/balance/", new BalanceHandler());
            server.setExecutor(null);
            server.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testServer() throws IOException {
        URL url = new URL("http://127.0.0.1:5000/");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setRequestMethod("GET");
        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String line;
        StringBuilder result = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        Assert.assertEquals(200, urlConnection.getResponseCode());
        Assert.assertEquals("Hello stranger!", result.toString());
        urlConnection.disconnect();
    }

    @Test
    public void postTransactionSimpleTest() throws IOException {
        String response = addTransaction(1, 2, 1492916399, 34);
        Assert.assertEquals("Transaction successfully added!", response);
    }

    @Test
    public void addMultipleTransactionsTest() throws IOException {
        addTransaction(5, 2, 1492907399, 543);
        addTransaction(5, 1, 1492909399, 231);
        addTransaction(8, 6, 1492916399, 34);
    }

    @Test
    public void getTransactionsThresholdSimpleTest() throws IOException {
        URL url = new URL("http://127.0.0.1:5000/transactions/?user=1&day=1492916399&threshold=23");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setDoInput(true);
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Content-Type", "application/json");

        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String line;
        List<String> actual = new ArrayList<String>();
        while ((line = reader.readLine()) != null) {
            actual.add(line);
        }
        Assert.assertEquals(200, urlConnection.getResponseCode());
        List<String> expected = new ArrayList<String>();
        expected.add("{ \"sender\" : 1 , \"receiver\" : 2 , \"timestamp\" : 1492916399 , \"sum\" : 34}");
        expected.add("{ \"sender\" : 5 , \"receiver\" : 1 , \"timestamp\" : 1492909399 , \"sum\" : 231}");

        Assert.assertTrue(compareJSON(expected, actual));
        urlConnection.disconnect();
    }

    private boolean compareJSON(List<String> expected, List<String> actual) throws IOException {
        if (actual == null || expected.size() != actual.size()) {
            return false;
        }

        ObjectMapper om = new ObjectMapper();
        for (int i = 0; i < expected.size(); i++) {
            Map<String, Object> m1 = (Map<String, Object>)(om.readValue(expected.get(i), Map.class));
            Map<String, Object> m2 = (Map<String, Object>)(om.readValue(actual.get(i), Map.class));
            if (!m1.equals(m2)) {
                return false;
            }
        }
        return true;
    }

    private String addTransaction(int sender, int receiver, long timestamp, int sum) throws IOException {
        URL url = new URL("http://127.0.0.1:5000/transactions/");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("POST");
        urlConnection.setUseCaches(false);
        urlConnection.setRequestProperty("Content-Type", "application/json");

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("sender", sender);
        jsonBody.put("receiver", receiver);
        jsonBody.put("timestamp", timestamp);
        jsonBody.put("sum", sum);


        DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
        printout.write(jsonBody.toJSONString().getBytes("utf-8"));
        printout.flush();
        printout.close();

        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String line;
        StringBuilder result = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        Assert.assertEquals(200, urlConnection.getResponseCode());
        urlConnection.disconnect();

        return result.toString();
    }

    @Test
    public void getBalanceSimpleTest() throws IOException {
        URL url = new URL("http://127.0.0.1:5000/balance/?user=1&since=1492005144&until=1493128344");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Content-Type", "application/json");

        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String line;
        StringBuilder result = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        Assert.assertEquals(200, urlConnection.getResponseCode());
        Assert.assertEquals("197", result.toString());
        urlConnection.disconnect();
    }

    @Test
    public void getBalanceReversedInterval() throws IOException {
        URL url = new URL("http://127.0.0.1:5000/balance/?user=1&since=1493128344&until=1492005144");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Content-Type", "application/json");

        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String line;
        StringBuilder result = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        Assert.assertEquals(200, urlConnection.getResponseCode());
        Assert.assertEquals("0", result.toString());
        urlConnection.disconnect();
    }
}