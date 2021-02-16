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


public class addActor implements HttpHandler {
	
	public addActor() {
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
		
		int statusCode = 200;
		String name = "";
		String actorId = "";

		// check if the body is correctly formatted
		if (deserialized.has("name") && deserialized.has("actorId")) {
			name = deserialized.getString("name");
			actorId = deserialized.getString("actorId");
		}
		else {
			statusCode = 400;
		}
		
		System.out.println(name);
		System.out.println(actorId);
		
		if (statusCode == 200) {
			
			// connect to db and insert actor is the actor doesn't already exist
			Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "1234"));
			try (Session session = driver.session()) {
				Result node_boolean = session.run("MATCH (n:actor {id: \"" + actorId + "\"}) RETURN n as bool;");
				if (!(node_boolean.hasNext())) {
					session.run("CREATE (n:actor {name:\"" + name + "\", id:\"" + actorId + "\"});");
					System.out.println("Transaction complete");
				}
				else {
					System.out.println("Actor already exists with that actorId");
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
