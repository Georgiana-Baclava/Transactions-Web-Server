import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by bgl on 19.04.2017.
 */
public class ServerHomeHandler implements HttpHandler {

    public void handle(HttpExchange httpExchange) throws IOException {
        String response = "Hello stranger!";
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
