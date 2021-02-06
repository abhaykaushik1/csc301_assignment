package ca.utoronto.utm.mcs;

import java.io.Exception;

public class Add {
    
    public void addMovie(String name, String movieId) {
        if (name.equals(null) || name.equals("") || movieId.equals(null) || movieId.equals("")) {
            System.out.println("400 BAD REQUEST.");
            return;
        }
        
        
    }
    
    public static void main(String args[]) throws Exception {
        return;
    }
}