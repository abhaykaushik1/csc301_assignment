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


public class addRelationship implements HttpHandler {
	
	public addRelationship() {
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
		
		// convert input stream to string
		Scanner s = new Scanner(r.getRequestBody()).useDelimiter("\\A");
		String body = s.hasNext() ? s.next() : "";
		
		// get json object
		JSONObject deserialized = new JSONObject(body);
		
		// check if the body is correctly formatted
		int statusCode = 200;
		String actorId = "";
		String movieId = "";

		// check if body is formatted correctly
		if (deserialized.has("actorId") && deserialized.has("movieId")) {
			actorId = deserialized.getString("actorId");
			movieId = deserialized.getString("movieId");
		}
		else {
			statusCode = 400;
		}
		
		System.out.println(actorId);
		System.out.println(movieId);
		
		if (statusCode == 200) {
			
			// connect to db and insert relationship if doesn't already exist
			Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "1234"));
			try (Session session = driver.session()) {
				
				// check if actor or movie doesn't exist and check if relationship already exists 
				Result node_boolean_actorId = session.run("MATCH (n:actor {id: \"" + actorId + "\"}) RETURN n as bool;");
				Result node_boolean_movieId = session.run("MATCH (n:movie {id: \"" + movieId + "\"}) RETURN n as bool;");
				Result node_boolean_relation_exists = session.run("RETURN exists((:actor {id: \"" + actorId + "\"})-[:ACTED_IN]->(:movie {id: \"" + movieId + "\"})) as bool;");
				if (node_boolean_actorId.hasNext() && node_boolean_movieId.hasNext()) {
					
					// create relationship
					if (node_boolean_relation_exists.next().get(0).asBoolean() == false) {
						session.run("MATCH (a:actor),(m:movie) WHERE a.id = \"" + actorId + "\" AND m.id = \"" + movieId + "\" CREATE (a)-[r:ACTED_IN]->(m);");
						System.out.println("Transaction complete");
					}
					else {
						System.out.println("Error, relationship already exists");
						statusCode = 400;
					}
				}
				else {
					System.out.println("Error, either actorId or movieId does not exist");
					statusCode = 404;
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