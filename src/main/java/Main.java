import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by bgl on 11.04.2017.
 */
public class Main {

    public static void main(String[] args) {

        try {
            MongoDBConnection.INSTANCE.init("mongo");
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
}