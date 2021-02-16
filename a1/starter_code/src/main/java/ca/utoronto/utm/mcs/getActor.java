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

public class getActor implements HttpHandler {
    public getActor() {
    }

    @Override
    public void handle(com.sun.net.httpserver.HttpExchange r) throws IOException {
        try {
            handleGet(r);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void handleGet(com.sun.net.httpserver.HttpExchange r) throws IOException, JSONException {
        Scanner s = new Scanner(r.getRequestBody()).useDelimiter("\\A");
        String body = s.hasNext() ? s.next() : "";

        // get json object
        JSONObject deserialized = new JSONObject(body);

        // check if the body is correctly formatted
        int statusCode = 200;
        String actorId = "";

        if (deserialized.has("actorId")) {
            actorId = deserialized.getString("actorId");
        } else {
            statusCode = 400;
        }

        System.out.println(actorId);
        String name;
        List<Object> movies = new ArrayList<>();

        if (statusCode == 200) {
            Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "1234"));
            try (Session session = driver.session()) {
                Result node_boolean = session.run("MATCH (n:actor {id: " + actorId + "}) RETURN n;");

                if (node_boolean.hasNext()) {
                    Result node_name = session.run("MATCH (n:actor {id: " + actorId + "}) RETURN n.name;");
                    Result node_movieIds = session.run("MATCH (a:actor),(m:movie) WHERE (a.id =" + actorId + ") AND (a)-[:ACTED_IN]->(m) RETURN collect(m.id);");
                    movies = node_movieIds.list().get(0).get(0).asList();
                    name = node_name.next().get(0).asNode().get("name").toString().replaceAll("\"", "");

                } else {
                    statusCode = 404;
                    System.out.println("Actor does not exist.\n");
                }
            } catch(Exception e) {
                System.out.println(e.getMessage());
                statusCode = 500;
            }
        }

        JSONObject json = new JSONObject();
        JSONArray movieIds = new JSONArray();
        json.put("actorId", actorId);
        json.put("name", name);
        for (int i = 0; i < actorIds.size(); i++) {
            movieIds.put(movies.get(i).toString());
        }
        json.put("movies", movieIds);

        r.sendResponseHeaders(statusCode, jsonObject.toString().length());
        OutputStream os = r.getResponseBody();
        os.write(jsonObject.toString().getBytes());
        os.close();
    }

}