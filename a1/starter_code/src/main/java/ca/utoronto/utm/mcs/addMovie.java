package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import org.json.*;
import org.neo4j.driver.*;
import java.util.*;

public class addMovie implements HttpHandler{
    
    public addMovie() {
    }
    
    @Override
    public void handle(com.sun.net.httpserver.HttpExchange r) throws IOException {
        try {
            handlePut(r);
        } 
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    public void handlePut(com.sun.net.httpserver.HttpExchange r) throws IOException, JSONException {

        Scanner s = new Scanner(r.getRequestBody()).useDelimiter("\\A");
        String body = s.hasNext() ? s.next() : "";
        
        // get json object
        JSONObject deserialized = new JSONObject(body);
        
        // check if the body is correctly formatted
        int statusCode = 200;
        String name = "";
        String movieId = "";
        
        if (deserialized.has("name") && deserialized.has("movieId")) {
            name = deserialized.getString("name");
            movieId = deserialized.getString("movieId");
        } else {
            statusCode = 400;
        }
        
        System.out.println(name);
        System.out.println(movieId);
        
        if (statusCode == 200) {
            
            Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "1234"));
            try (Session session = driver.session()) {
                Result node_boolean = session.run("MATCH (n:movie {name: \"" + name + "\"}) RETURN n as bool;");
                if (!(node_boolean.hasNext())) {
                    session.run("CREATE (n:movie {name:\"" + name + "\", id:\"" + movieId + "\"});");
                }
                else {
                    System.out.println("Movie already exists with that movieId");
                    statusCode = 400;
                } 
            }
            catch(Exception e) {
                System.out.println(e.getMessage());
                statusCode = 500;
            }
        }
        
        // send response code (no body)
        r.sendResponseHeaders(statusCode, 0);
        OutputStream os = r.getResponseBody();
        os.close();
    }
}