import java.util.HashMap;
import java.util.Map;

/**
 * Created by bgl on 23.04.2017.
 */
public class URLQueryParser {

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
