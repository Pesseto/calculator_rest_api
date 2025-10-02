package com.wit.rest.dto;

import java.math.BigDecimal;


public class CalculatorResponse {
    private String id;
    private BigDecimal result;
    private String error;

    public CalculatorResponse() { }

    public CalculatorResponse(String id, BigDecimal result) {
        this.id = id;
        this.result = result;
    }

    public CalculatorResponse(String error) {
        this.error = error;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }       

    public BigDecimal getResult() {
        return this.result;
    }
    public void setResult(BigDecimal result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "CalculatorResponse{id='" + id + 
                    "', result=" + result + 
                    ", error='" + error + 
                    "'}";
    }
}
