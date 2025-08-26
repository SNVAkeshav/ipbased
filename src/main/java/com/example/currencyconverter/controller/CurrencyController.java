package com.example.currencyconverter.controller;

import com.example.currencyconverter.service.ExchangeRateService;
import com.example.currencyconverter.service.GeoLocationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
public class CurrencyController {
    private static final Logger logger = Logger.getLogger(CurrencyController.class.getName());

    private final ExchangeRateService exchangeRateService;
    private final GeoLocationService geoLocationService;

    // Shared country-to-currency mapping
    private static final Map<String, String> COUNTRY_TO_CURRENCY = new HashMap<>();

    static {
        COUNTRY_TO_CURRENCY.put("US", "USD");
        COUNTRY_TO_CURRENCY.put("GB", "GBP");
        COUNTRY_TO_CURRENCY.put("EU", "EUR");
        COUNTRY_TO_CURRENCY.put("IN", "INR");
        // Add more mappings as needed
    }

    public CurrencyController(ExchangeRateService exchangeRateService, GeoLocationService geoLocationService) {
        this.exchangeRateService = exchangeRateService;
        this.geoLocationService = geoLocationService;
    }

    @GetMapping("/convert")
    public Map<String, Object> convertPrice(@RequestParam BigDecimal amount,
                                            @RequestParam String fromCurrency) {
        Map<String, Object> response = new HashMap<>();
        try {
            String countryCode = geoLocationService.getCountryCodeFromIP();
            String toCurrency = COUNTRY_TO_CURRENCY.getOrDefault(countryCode, "USD");
            BigDecimal convertedAmount = exchangeRateService.convert(amount, fromCurrency, toCurrency);

            response.put("originalAmount", amount);
            response.put("fromCurrency", fromCurrency);
            response.put("toCurrency", toCurrency);
            response.put("convertedAmount", convertedAmount);
            response.put("countryCode", countryCode);
            logger.info("Converted " + amount + " " + fromCurrency + " to " + convertedAmount + " " + toCurrency + " for country: " + countryCode);
        } catch (IllegalArgumentException e) {
            logger.severe("Currency conversion error: " + e.getMessage());
            response.put("error", "Invalid currency: " + e.getMessage());
            response.put("originalAmount", amount);
            response.put("fromCurrency", fromCurrency);
            response.put("toCurrency", "USD");
            response.put("convertedAmount", amount); // Fallback to original amount
            response.put("countryCode", "US");
        } catch (Exception e) {
            logger.severe("Unexpected error: " + e.getMessage());
            response.put("error", "Conversion failed: " + e.getMessage());
            response.put("originalAmount", amount);
            response.put("fromCurrency", fromCurrency);
            response.put("toCurrency", "USD");
            response.put("convertedAmount", amount); // Fallback to original amount
            response.put("countryCode", "US");
        }
        return response;
    }
}