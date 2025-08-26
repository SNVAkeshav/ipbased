package com.example.currencyconverter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.util.logging.Logger;

@Service
public class GeoLocationService {
    private static final Logger logger = Logger.getLogger(GeoLocationService.class.getName());
//    private static final String TEST_IP = "49.249.44.226"; // Example Indian IP for testing

    @Value("${ipgeolocation.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final HttpServletRequest request;

    public GeoLocationService(RestTemplate restTemplate, HttpServletRequest request) {
        this.restTemplate = restTemplate;
        this.request = request;
    }

    public String getCountryCodeFromIP() {
        String ipAddress = getClientIp();
        // Basic IP validation to avoid API calls for invalid IPs
        if (ipAddress == null || ipAddress.isEmpty() || isInvalidIp(ipAddress)) {
            logger.warning("Invalid or empty IP address detected: " + ipAddress + ". Falling back to US.");
            return "US";
        }

        // Temporary override for testing IP
//        if (ipAddress.equals(TEST_IP)) {
//            logger.info("Test IP detected: " + ipAddress + ". Forcing country code to IN for testing.");
//            return "IN";
//        }

        String url = "https://api.ipgeolocation.io/ipgeo?apiKey=" + apiKey + "&ip=" + ipAddress;
        try {
            GeoLocationResponse response = restTemplate.getForObject(url, GeoLocationResponse.class);
            String countryCode = response != null && response.getCountryCode2() != null ? response.getCountryCode2() : "US";
            logger.info("API response for IP " + ipAddress + ": countryCode=" + countryCode + ", fullResponse=" + response);
            return countryCode;
        } catch (Exception e) {
            logger.severe("Error fetching country code for IP " + ipAddress + ": " + e.getMessage());
            return "US"; // Fallback to US on error
        }
    }

    private String getClientIp() {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // Handle both IPv4 and IPv6 localhost addresses
//        if (ipAddress != null && (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1"))) {
//            logger.info("Localhost IP detected (" + ipAddress + "). Using test IP: " + TEST_IP);
//            ipAddress = TEST_IP;
//        }
        return ipAddress;
    }

    private boolean isInvalidIp(String ipAddress) {
        // Add basic validation for bogon or invalid IPs
        return ipAddress == null || ipAddress.matches("^(0\\.0\\.0\\.0|192\\.168\\..*|10\\..*|172\\.(1[6-9]|2[0-9]|3[0-1])\\..*)$");
    }

    // Inner class to map the API response
    private static class GeoLocationResponse {
        private String countryCode2;
        private String countryName; // Added to debug full response

        public String getCountryCode2() {
            return countryCode2;
        }

        public void setCountryCode2(String countryCode2) {
            this.countryCode2 = countryCode2;
        }

        public String getCountryName() {
            return countryName;
        }

        public void setCountryName(String countryName) {
            this.countryName = countryName;
        }

        @Override
        public String toString() {
            return "GeoLocationResponse{countryCode2='" + countryCode2 + "', countryName='" + countryName + "'}";
        }
    }
}