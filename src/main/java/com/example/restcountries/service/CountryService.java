package com.example.restcountries.service;

import com.example.restcountries.dtos.CountryDDD;
import com.example.restcountries.dtos.MoneyInfoCountry;
import com.example.restcountries.model.Country;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.*;

@Service
public class CountryService {
    private final String BASE_URL = "https://restcountries.com/v3.1/";
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public ResponseEntity<List<String>> getAll() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> apiResponse = restTemplate.getForEntity(BASE_URL + "all", String.class);

        if (apiResponse.getBody() != null) {
            try {
                JsonNode jsonResponse = jsonMapper.readTree(apiResponse.getBody());
                List<String> countryNames = new ArrayList<>();

                for (JsonNode countryNode : jsonResponse) {
                    JsonNode commonNameNode = countryNode.path("name").path("common");
                    String commonName = commonNameNode.asText();
                    countryNames.add(commonName);
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

    public ResponseEntity getByName(String countryName) {
        URI apiUri;
        try {
            apiUri = new URI(BASE_URL + "name/" + countryName);
        } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest apiRequest = HttpRequest.newBuilder()
                .uri(apiUri)
                .GET()
                .build();

        try {
            HttpResponse<String> apiResponse = httpClient.send(apiRequest, HttpResponse.BodyHandlers.ofString());

            if (apiResponse.statusCode() == 200) {
                JsonNode jsonResponse = jsonMapper.readTree(apiResponse.body());
                JsonNode countryNode = jsonResponse.get(0);

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
                return ResponseEntity.status(apiResponse.statusCode()).body(apiResponse.body());
            }
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    public ResponseEntity<List<CountryDDD>> getCountriesSuffixes() throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> apiResponse = restTemplate.getForEntity(BASE_URL + "all", String.class);

        if(apiResponse.getBody() == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        JsonNode jsonResponse = jsonMapper.readTree(apiResponse.getBody());
        List<CountryDDD> countrySuffixesList = new ArrayList<>();

        for(JsonNode countryNode : jsonResponse) {
            String countryName = countryNode.path("name").path("common").asText();
            JsonNode iddNode = countryNode.path("idd").path("suffixes");

            List<String> suffixes = new ArrayList<>();

            if(iddNode.isArray()){
                for(JsonNode suffix : iddNode){
                    suffixes.add(suffix.asText());
                }
            }

            CountryDDD countryDDD = new CountryDDD(countryName, suffixes);
            countrySuffixesList.add(countryDDD);
        }
        return ResponseEntity.ok().body(countrySuffixesList);
    }

    public ResponseEntity<List<MoneyInfoCountry>> getMoneyInfos() throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> apiResponse = restTemplate.getForEntity(BASE_URL + "all", String.class);

        if (apiResponse.getBody() == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        JsonNode jsonResponse = jsonMapper.readTree(apiResponse.getBody());
        List<MoneyInfoCountry> moneyInfoCountries = new ArrayList<>();

        for (JsonNode country : jsonResponse) {
            String countryName = country.path("name").path("common").asText();

            String capital = "";
            if (country.path("capital").isArray() && country.path("capital").size() > 0) {
                capital = country.path("capital").get(0).asText();
            }

            List<String> languages = new ArrayList<>();
            country.path("languages").fields().forEachRemaining(entry -> languages.add(entry.getValue().asText()));

            int population = country.path("population").asInt();

            String moneyName = "";
            String moneySymbol = "";
            Iterator<Map.Entry<String, JsonNode>> currenciesIterator = country.path("currencies").fields();
            if (currenciesIterator.hasNext()) {
                Map.Entry<String, JsonNode> currency = currenciesIterator.next();
                moneyName = currency.getValue().path("name").asText();
                moneySymbol = currency.getValue().path("symbol").asText();
            }

            MoneyInfoCountry moneyInfoCountry = new MoneyInfoCountry(
                    countryName,
                    capital,
                    languages,
                    population,
                    moneyName,
                    moneySymbol
            );
            moneyInfoCountries.add(moneyInfoCountry);
        }

        return ResponseEntity.ok().body(moneyInfoCountries);
    }

}
