package com.adfonic.beans;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Test;

public class AccountTransactionMathsTest {
    
    private BigDecimal zeroToTwoDecimalPlaces = BigDecimal.valueOf(0.00).setScale(2);

    @Test
    public void testBigDecimalRoundingUpMultipleDecimalPlaces() {
        
        BigDecimal balance1 = BigDecimal.valueOf(0.0025000000);
        BigDecimal balance2 = BigDecimal.valueOf(0.0720000000).setScale(2, RoundingMode.CEILING);
        BigDecimal balance3 = BigDecimal.valueOf(0.0434040000).setScale(2, RoundingMode.CEILING);
        BigDecimal balance4 = BigDecimal.valueOf(0.0180000000).setScale(2, RoundingMode.CEILING);
        BigDecimal balance5 = BigDecimal.valueOf(10.0881910000).setScale(2, RoundingMode.CEILING);
        BigDecimal balance6 = BigDecimal.valueOf(0.0015000000).setScale(2, RoundingMode.CEILING);
        BigDecimal balance7 = BigDecimal.valueOf(0.0900003000).setScale(2, RoundingMode.CEILING);
        BigDecimal balance8 = BigDecimal.valueOf(0.0189720000).setScale(2, RoundingMode.CEILING);
        BigDecimal balance9 = BigDecimal.valueOf(0.0012120000).setScale(2, RoundingMode.CEILING);
        BigDecimal balance10 = BigDecimal.valueOf(1000.0000000).setScale(2, RoundingMode.CEILING);
        
        BigDecimal roundedBalance1 = balance1.setScale(2, RoundingMode.CEILING);
        assertThat(roundedBalance1, equalTo(BigDecimal.valueOf(0.01)));
        // >= 0 is the actual test performed in AccountTransactionBean.doCreateTransaction()
        assertThat(balance1.subtract(roundedBalance1).compareTo(zeroToTwoDecimalPlaces), not(greaterThanOrEqualTo(0))); 
        assertThat(balance1.setScale(2, RoundingMode.CEILING).compareTo(roundedBalance1), equalTo(0));
        assertThat(roundedBalance1.subtract(roundedBalance1), equalTo(zeroToTwoDecimalPlaces)); // actually gets to zero
        
        assertThat(balance2, equalTo(BigDecimal.valueOf(0.08)));
        assertThat(balance2.subtract(balance2), equalTo(zeroToTwoDecimalPlaces)); // actually gets to zero
        
        assertThat(balance3, equalTo(BigDecimal.valueOf(0.05)));
        assertThat(balance4, equalTo(BigDecimal.valueOf(0.02)));
        assertThat(balance5, equalTo(BigDecimal.valueOf(10.09)));
        assertThat(balance6, equalTo(BigDecimal.valueOf(0.01)));
        assertThat(balance7, equalTo(BigDecimal.valueOf(0.10).setScale(2)));
        assertThat(balance8, equalTo(BigDecimal.valueOf(0.02)));
        assertThat(balance9, equalTo(BigDecimal.valueOf(0.01)));
        assertThat(balance9.subtract(balance9), equalTo(zeroToTwoDecimalPlaces));
        assertThat(balance10, equalTo(BigDecimal.valueOf(1000.00).setScale(2)));
        assertThat(balance10.subtract(balance10), equalTo(zeroToTwoDecimalPlaces));
        
        
    }



}
