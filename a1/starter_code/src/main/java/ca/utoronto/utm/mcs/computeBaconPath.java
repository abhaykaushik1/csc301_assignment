package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import org.json.*;
import org.neo4j.driver.*;
import java.util.*;


public class computeBaconPath implements HttpHandler {
	
	public computeBaconPath() {
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
		
		// convert input stream to string
		Scanner s = new Scanner(r.getRequestBody()).useDelimiter("\\A");
		String body = s.hasNext() ? s.next() : "";
		
		// get json object
		JSONObject deserialized = new JSONObject(body);
		
		// check if the body is correctly formatted
		int statusCode = 200;
		String actorId = "";

		// get movieId from request
		if (deserialized.has("actorId")) {
			actorId = deserialized.getString("actorId");
		}
		else {
			statusCode = 400;
		}
		
		System.out.println(actorId);
		String baconNumber = null;
		Value path = null;
		List<String> pathActorIds = new ArrayList<>();
		List<String> pathMovieIds = new ArrayList<>();
		
		if (statusCode == 200 && actorId != "nm0000102") {
			
			// connect check if node exists with movieId and get the node if it does exist
			Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "1234"));
			try (Session session = driver.session()) {
				Result node_actor = session.run("MATCH (n:actor {id: \"" + actorId + "\"}) RETURN n;");
				
				// check if movie exists, get actors if it does exist
				if (node_actor.hasNext()) {
					
					// get bacon number
					Result bacon = session.run("MATCH (n:actor {id: \"" + actorId + "\"}), (m:actor {id: \"nm0000102\"}), p = shortestPath( (n)-[*]-(m) ) RETURN nodes(p);");
					if (bacon.hasNext()) {
						path = bacon.list().get(0).get(0);
					}
					else {
						System.out.println("Error, Bacon path not found");
						statusCode = 404;
					}
				}
				else {
					System.out.println("Error, actorId does not exist");
					statusCode = 400;
				}
			}
			catch(Exception e) {
				System.out.println(e.getMessage());
				statusCode = 500;
			}
		}
		
		// seperate the actorIds and movieIds from the path into different lists
		System.out.println(path);
		/*
		// base case
		if (actorId == "nm0000102") {
			baconNumber = "0";
		}
		
		// make json response
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("baconNumber", baconNumber);
		
		// convert json to string and send 
		r.sendResponseHeaders(statusCode, jsonObject.toString().length());
		OutputStream os = r.getResponseBody();
		os.write(jsonObject.toString().getBytes());
		os.close();
		*/
	} 
}