package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marwa Adel on 4/3/2021.
 */
public class Movies {

    @SerializedName("results")
    private List<Movie> movies = new ArrayList<>();

    public List<Movie> getMovies() {
        return movies;
    }
}

