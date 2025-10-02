package com.wit.calculator.kafka;

import com.wit.calculator.dto.CalculatorRequest;
import com.wit.calculator.dto.CalculatorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class CalculatorListenerTest {

    private CalculatorListener listener;

    @BeforeEach
    void setUp() {
        listener = new CalculatorListener();
    }

    @Test
    void sum_ok() {
        var req = new CalculatorRequest("id-1", new BigDecimal("2"), new BigDecimal("3"), "sum");
        CalculatorResponse res = listener.handle(req, req.getId());
        assertNull(res.getError());
        assertEquals(new BigDecimal("5"), res.getResult());
        assertEquals(req.getId(), res.getId());
    }

    @Test
    void sub_ok() {
        var req = new CalculatorRequest("id-2", new BigDecimal("10"), new BigDecimal("3"), "sub");
        var res = listener.handle(req, req.getId());
        assertNull(res.getError());
        assertEquals(new BigDecimal("7"), res.getResult());
        assertEquals(req.getId(), res.getId());
    }

    @Test
    void mul_ok() {
        var req = new CalculatorRequest("id-3", new BigDecimal("4"), new BigDecimal("5"), "mul");
        var res = listener.handle(req, req.getId());
        assertNull(res.getError());
        assertEquals(new BigDecimal("20"), res.getResult());
        assertEquals(req.getId(), res.getId());
    }

    @Test
    void div_ok() {
        var req = new CalculatorRequest("id-4", new BigDecimal("15"), new BigDecimal("3"), "div");
        var res = listener.handle(req, req.getId());
        assertNull(res.getError());
        assertEquals(new BigDecimal("5.0000000000"), res.getResult());
        assertEquals(req.getId(), res.getId());
    }

    @Test
    void division_by_zero() {
        var req = new CalculatorRequest("id-5", new BigDecimal("10"), BigDecimal.ZERO, "div");
        var res = listener.handle(req, req.getId());
        assertNotNull(res.getError());
        assertEquals("division by zero attempted", res.getError());
        assertNull(res.getResult());
    }

    @Test
    void invalid_operation() {
        var req = new CalculatorRequest("id-6", new BigDecimal("5"), new BigDecimal("3"), "invalid");
        var res = listener.handle(req, req.getId());
        assertNotNull(res.getError());
        assertEquals("invalid operation: invalid", res.getError());
        assertNull(res.getResult());
    }

    @Test
    void null_request() {
        var res = listener.handle(null, "x");
        assertEquals("null request", res.getError());
        assertNull(res.getResult());
    }

    @Test
    void missing_operands_and_operation() {
        var r1 = new CalculatorRequest("id-7", null, new BigDecimal("1"), "sum");
        var r2 = new CalculatorRequest("id-8", new BigDecimal("1"), null, "sum");
        var r3 = new CalculatorRequest("id-9", new BigDecimal("1"), new BigDecimal("1"), null);

        assertEquals("missing a", listener.handle(r1, r1.getId()).getError());
        assertEquals("missing b", listener.handle(r2, r2.getId()).getError());
        assertEquals("missing operation", listener.handle(r3, r3.getId()).getError());
    }

    @Test
    void high_precision_division() {
        var req = new CalculatorRequest("id-10", new BigDecimal("1"), new BigDecimal("3"), "div");
        var res = listener.handle(req, req.getId());
        assertNull(res.getError());
        assertEquals(new BigDecimal("0.3333333333"), res.getResult());
    }
}
