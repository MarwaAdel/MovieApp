
package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marwa Adel on 4/3/2021.
 */
public class Trailers {

    @SerializedName("results")
    private List<Trailer> trailers = new ArrayList<>();

    public List<Trailer> getTrailers() {
        return trailers;
    }
}
