package com.example.restcountries.dtos;

import java.util.List;

public record MoneyInfoCountry(String countryName, String capital, List<String> languages, int population, String money,
                               String moneySymbol) {
}
