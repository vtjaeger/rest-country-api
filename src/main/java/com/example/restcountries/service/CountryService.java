package com.example.restcountries.service;

import io.micrometer.common.util.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class CountryService {
    private final String URL = "https://restcountries.com/v3.1/";
    public ResponseEntity getAll() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(URL + "all", String.class);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    public ResponseEntity getByName(String name) {
        // Verificar se o nome não está vazio
        if (StringUtils.isEmpty(name)) {
            return ResponseEntity.badRequest().body("O nome do país não pode estar vazio.");
        }

        // Construir a URL para buscar por nome
        URI uri;
        try {
            uri = new URI(URL + "name/" + name);
        } catch (URISyntaxException e) {
            return ResponseEntity.status(500).body("Erro ao construir a URI: " + e.getMessage());
        }

        // Criar cliente HTTP
        HttpClient client = HttpClient.newHttpClient();

        // Construir a solicitação GET
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        // Enviar a solicitação e processar a resposta
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return ResponseEntity.status(response.statusCode()).body(response.body());
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(500).body("Erro ao fazer a solicitação HTTP: " + e.getMessage());
        }
    }
}
