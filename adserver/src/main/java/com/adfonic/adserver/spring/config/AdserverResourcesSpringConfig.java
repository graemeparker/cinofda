package com.adfonic.adserver.spring.config;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.adfonic.geo.AustrianPostalCodeManager;
import com.adfonic.geo.CanadianPostalCodeManager;
import com.adfonic.geo.ChinesePostalCodeManager;
import com.adfonic.geo.DmaManager;
import com.adfonic.geo.GBPostalCodeManager;
import com.adfonic.geo.PostalCodeIdManager;
import com.adfonic.geo.SpanishPostalCodeManager;
import com.adfonic.geo.USZipCodeManager;

/**
 * 
 * @author mvanek
 *
 */
@Configuration
public class AdserverResourcesSpringConfig {

    @Bean
    public GBPostalCodeManager GBPostalCodeManager(@Value("${PostalCodeManager.GB.dataFile}") File dataFile,
            @Value("${PostalCodeManager.GB.checkForUpdatesPeriodSec}") Integer checkPeriod) {
        return new GBPostalCodeManager(dataFile, checkPeriod);
    }

    @Bean
    public USZipCodeManager USZipCodeManager(@Value("${USZipCodeManager.dataFile}") File dataFile, @Value("${USZipCodeManager.checkForUpdatesPeriodSec}") Integer checkPeriod) {
        USZipCodeManager manager = new USZipCodeManager(dataFile, checkPeriod);
        return manager;
    }

    @Bean
    public DmaManager DmaManager(@Value("${DmaManager.dataFile}") File dataFile, @Value("${DmaManager.checkForUpdatesPeriodSec}") Integer checkPeriod) {
        return new DmaManager(dataFile, checkPeriod);
    }

    @Bean
    public CanadianPostalCodeManager CanadianPostalCodeManager(@Value("${CanadianPostalCodeManager.dataFile}") File dataFile,
            @Value("${CanadianPostalCodeManager.checkForUpdatesPeriodSec}") Integer checkPeriod) {
        return new CanadianPostalCodeManager(dataFile, checkPeriod);
    }

    @Bean
    public ChinesePostalCodeManager ChinesePostalCodeManager(@Value("${ChinesePostalCodeManager.dataFile}") File dataFile,
            @Value("${ChinesePostalCodeManager.checkForUpdatesPeriodSec}") Integer checkPeriod) {
        return new ChinesePostalCodeManager(dataFile, checkPeriod);
    }

    @Bean
    public AustrianPostalCodeManager AustrianPostalCodeManager(@Value("${AustrianPostalCodeManager.dataFile}") File dataFile,
            @Value("${AustrianPostalCodeManager.checkForUpdatesPeriodSec}") Integer checkPeriod) {
        return new AustrianPostalCodeManager(dataFile, checkPeriod);
    }

    @Bean
    public SpanishPostalCodeManager SpanishPostalCodeManager(@Value("${SpanishPostalCodeManager.dataFile}") File dataFile,
            @Value("${SpanishPostalCodeManager.checkForUpdatesPeriodSec}") Integer checkPeriod) {
        return new SpanishPostalCodeManager(dataFile, checkPeriod);
    }

    @Bean
    public PostalCodeIdManager PostalCodeIdManager(@Value("${PostalCodeIdManager.dataFile}") File dataFile,
            @Value("${PostalCodeIdManager.checkForUpdatesPeriodSec}") Integer checkPeriod) {
        return new PostalCodeIdManager(dataFile, checkPeriod);
    }
}
