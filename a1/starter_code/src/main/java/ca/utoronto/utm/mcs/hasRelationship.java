package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import org.json.*;
import org.neo4j.driver.*;
import java.util.*;

public class hasRelationship implements HttpHandler {
    public hasRelationship() {
    }

    @Override
    public void handle(com.sun.net.httpserver.HttpExchange r) throws IOException {
        try {
            handlePossession(r);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void handlePossession(com.sun.net.httpserver.HttpExchange r) throws IOException, JSONException {
        Scanner s = new Scanner(r.getRequestBody()).useDelimiter("\\A");
        String body = s.hasNext() ? s.next() : "";

        // get json object
        JSONObject deserialized = new JSONObject(body);

        // check if the body is correctly formatted
        int statusCode = 200;
        String actorId = "";
        String movieId = "";

        if (deserialized.has("actorId") && deserialized.has("movieId")) {
            actorId = deserialized.getString("actorId");
            movieId = deserialized.getString("movieId");
        } else {
            statusCode = 400;
        }

        System.out.println(actorId);
        System.out.println(movieId);

        boolean relationship = false;

        if (statusCode == 200) {

            Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "1234"));
            try (Session session = driver.session()) {
                Result node_boolean1 = session.run("MATCH (n:actor {id: \"" + actorId + "\"}) RETURN n as bool;");
                Result node_boolean2 = session.run("MATCH (n:movie {id: \"" + movieId + "\"}) RETURN n as bool;");
                System.out.println(node_boolean2);
                System.out.println(node_boolean1);
                if (node_boolean1.hasNext() && node_boolean2.hasNext()) {
                    Result node_relationship = session.run("MATCH (a:actor {id:\""+ actorId +"\"}),(m:movie {movieId:\"" + movieId + "\"}) RETURN EXISTS( (a)-[:ACTED_IN]-(m) )");
                    String relationship_ = node_relationship.next().get(0).asNode().get("hasRelationship").toString().replaceAll("\"", "");
                    if (relationship_ == "false") {relationship = false;} else {relationship = true;}
                }
                else {
                    System.out.println("Either movie or actor do not exist.");
                    statusCode = 404;
                }
            }
            catch(Exception e) {
                System.out.println(e.getMessage());
                statusCode = 500;
            }
        }
        JSONObject json = new JSONObject();
        json.put("actorId", actorId);
        json.put("movieId", movieId);
        json.put("hasRelationship", relationship);


        // send response code (no body)
        r.sendResponseHeaders(statusCode, json.toString().length());
        OutputStream os = r.getResponseBody();
        os.write(json.toString().getBytes());
        os.close();

    }


}