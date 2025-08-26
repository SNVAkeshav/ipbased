package com.example.currencyconverter.repository;

import com.example.currencyconverter.model.Currency;
//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CurrencyRepository extends MongoRepository<Currency, String> {
}