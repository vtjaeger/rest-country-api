package com.example.restcountries.controller;

import com.example.restcountries.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CountryController {
    @Autowired
    private CountryService countryService;
    @GetMapping("/all")
    public ResponseEntity getAll(){
        return countryService.getAll();
    }

    @GetMapping("/{name}")
    public ResponseEntity getByName(@PathVariable String name) {
        return countryService.getByName(name);
    }

}
