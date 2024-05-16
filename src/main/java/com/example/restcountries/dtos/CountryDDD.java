package com.example.restcountries.dtos;

import java.util.List;

public class CountryDDD {
    private String name;
    private List<String> DDDs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getDDDs() {
        return DDDs;
    }

    public void setDDDs(List<String> DDDs) {
        this.DDDs = DDDs;
    }

    public CountryDDD(String name, List<String> DDDs) {
        this.name = name;
        this.DDDs = DDDs;
    }
}
