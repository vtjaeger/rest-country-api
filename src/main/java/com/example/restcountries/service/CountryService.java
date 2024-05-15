package com.example.restcountries.service;

import com.example.restcountries.model.Country;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CountryService {
    private final String URL = "https://restcountries.com/v3.1/";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResponseEntity<List<String>> getAll() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(URL + "all", String.class);

        if (response.getBody() != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                List<String> countryNames = new ArrayList<>();

                for (JsonNode node : root) {
                    JsonNode nameNode = node.path("name").path("common");
                    String name = nameNode.asText();
                    countryNames.add(name);
                }

                return ResponseEntity.ok(countryNames);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body(Collections.singletonList("Erro ao processar a resposta da API: " + e.getMessage()));
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity getByName(String name) {
        URI uri;
        try {
            uri = new URI(URL + "name/" + name);
        } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode rootNode = objectMapper.readTree(response.body());
                JsonNode countryNode = rootNode.get(0);

                Country countryDto = new Country();
                countryDto.setCommonName(countryNode.at("/name/common").asText());
                countryDto.setOfficialName(countryNode.at("/name/official").asText());
                countryDto.setCapital(countryNode.at("/capital/0").asText());
                countryDto.setRegion(countryNode.at("/region").asText());
                countryDto.setSubregion(countryNode.at("/subregion").asText());
                countryDto.setPopulation(countryNode.at("/population").asLong());
                countryDto.setFlagUrl(countryNode.at("/flags/png").asText());

                return ResponseEntity.ok(countryDto);
            } else {
                return ResponseEntity.status(response.statusCode()).body(response.body());
            }
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}