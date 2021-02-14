package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

public class App 
{
    static int PORT = 8080;
    public static void main(String[] args) throws IOException
    {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        server.createContext("/api/v1/addActor", new addActor());
        //server.createContext("/api/v1/addMovie", new addMovie());
        server.createContext("/api/v1/addRelationship", new addRelationship());
        //server.createContext("/api/v1/getActor", new getActor());
        //server.createContext("/api/v1/getMovie", new getMovie());
        //server.createContext("/api/v1/hasRelationship", new hasRelationship());
        //server.createContext("/api/v1/computeBaconNumber", new computeBaconNumber());
        //server.createContext("/api/v1/computeBaconPath", new computeBaconPath());
        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
