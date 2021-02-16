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


public class getMovie implements HttpHandler {
	
	public getMovie() {
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
		String movieId = "";

		// get movieId from request
		if (deserialized.has("movieId")) {
			movieId = deserialized.getString("movieId");
		}
		else {
			statusCode = 400;
		}
		
		System.out.println(movieId);
		Result node_movie = null;
		Result node_actors = null;
		List<Object> actorIds = new ArrayList<>();
		if (statusCode == 200) {
			
			// connect check if node exists with movieId and get the node if it does exist
			Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "1234"));
			try (Session session = driver.session()) {
				node_movie = session.run("MATCH (n:movie {movieId: " + movieId + "}) RETURN n;");
				
				// check if movie exists, get actors if it does exist
				if (node_movie.hasNext()) {
					node_actors = session.run("MATCH (a:actor),(m:movie) WHERE (m.movieId = " + movieId + ") AND (a)-[:ACTED_IN]->(m) RETURN collect(a.actorId);");
					actorIds = node_actors.list().get(0).get(0).asList();
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
		
		// make json response
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", node_movie.next().get(0).asNode().get("name").toString().replaceAll("\"", ""));
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < actorIds.size(); i++) {
			jsonArray.put(actorIds.get(i).toString());
		}
		jsonObject.put("movieId", movieId);
		jsonObject.put("actors", jsonArray);
		
		// convert json to string and send 
		r.sendResponseHeaders(statusCode, jsonObject.toString().length());
		OutputStream os = r.getResponseBody();
		os.write(jsonObject.toString().getBytes());
		os.close();
	} 
}