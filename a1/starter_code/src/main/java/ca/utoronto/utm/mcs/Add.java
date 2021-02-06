package ca.utoronto.utm.mcs;

import java.io.Exception;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

public class Add {
    
    public void addMovie(String name, String movieId) {
        if (name.equals(null) || name.equals("") || movieId.equals(null) || movieId.equals("")) {
            System.out.println("400 BAD REQUEST.");
            return;
        }
        String tsvfile = "../../../../../../../../starter_files/movies.tsv";
        String newMovieId = "tt"+movieId;
        BufferedReader movies = new BufferedReader(new FileReader(tsvfile));
    }
    
    public static void main(String args[]) throws Exception {
        return;
    }
}