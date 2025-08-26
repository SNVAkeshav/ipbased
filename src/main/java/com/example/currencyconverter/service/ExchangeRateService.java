package com.example.currencyconverter.service;

import com.example.currencyconverter.model.Currency;
import com.example.currencyconverter.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class ExchangeRateService {
    private static final Logger logger = Logger.getLogger(ExchangeRateService.class.getName());

    @Value("${exchangerate.api.key}")
    private String apiKey;

    private final CurrencyRepository currencyRepository;
    private final RestTemplate restTemplate;

    public ExchangeRateService(CurrencyRepository currencyRepository, RestTemplate restTemplate) {
        this.currencyRepository = currencyRepository;
        this.restTemplate = restTemplate;
    }

    // Update exchange rates daily
    @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
    public void updateExchangeRates() {
        String url = "https://api.exchangerate-api.com/v4/latest/USD?access_key=" + apiKey;
        try {
            ExchangeRateResponse response = restTemplate.getForObject(url, ExchangeRateResponse.class);
            if (response != null && response.getRates() != null) {
                response.getRates().forEach((code, rate) -> {
                    Currency currency = new Currency(code, code, BigDecimal.valueOf(rate));
                    currencyRepository.save(currency);
                    logger.info("Updated exchange rate for " + code + ": " + rate);
                });
            } else {
                logger.warning("No exchange rates received from API");
            }
        } catch (Exception e) {
            logger.severe("Error updating exchange rates: " + e.getMessage());
        }
    }

    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        Currency from = currencyRepository.findById(fromCurrency)
                .orElseThrow(() -> new IllegalArgumentException("Currency not found: " + fromCurrency));
        Currency to = currencyRepository.findById(toCurrency)
                .orElseThrow(() -> new IllegalArgumentException("Currency not found: " + toCurrency));
        // Convert to USD first, then to target currency
        BigDecimal usdAmount = amount.divide(from.getExchangeRate(), 2, BigDecimal.ROUND_HALF_UP);
        return usdAmount.multiply(to.getExchangeRate()).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    // Inner class to map the API response
    private static class ExchangeRateResponse {
        private Map<String, Double> rates;

        public Map<String, Double> getRates() {
            return rates;
        }

        public void setRates(Map<String, Double> rates) {
            this.rates = rates;
        }
    }
}