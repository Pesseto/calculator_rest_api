package com.wit.calculator.dto;

import java.math.BigDecimal;

public class CalculatorRequest {
    private String id;
    private BigDecimal a;
    private BigDecimal b;
    private String operation;

    public CalculatorRequest() { }

    public CalculatorRequest(String id, BigDecimal a, BigDecimal b, String operation) {
        this.id = id;
        this.a = a;
        this.b = b;
        this.operation = operation;
    }

    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getA() {
        return this.a;
    }
    public void setA(BigDecimal a) {
        this.a = a;
    }

    public BigDecimal getB() {
        return this.b;
    }
    public void setB(BigDecimal b) {
        this.b = b;
    }

    public String getOperation() {
        return this.operation;
    }
    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "CalculatorRequest{id='" + this.id + 
                    "', a=" + this.a + 
                    ", b=" + this.b + 
                    ", operation='" + this.operation + 
                    "'}";
    }
}
