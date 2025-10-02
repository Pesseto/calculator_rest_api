package com.wit.rest.controller;

import com.wit.rest.dto.CalculatorRequest;
import com.wit.rest.dto.CalculatorResponse;
import com.wit.rest.config.KafkaProducerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("")
public class CalculatorController {

    private static final Logger log = LoggerFactory.getLogger(CalculatorController.class);

    private final ReplyingKafkaTemplate<String, CalculatorRequest, CalculatorResponse> rrTemplate;

    public CalculatorController(ReplyingKafkaTemplate<String, CalculatorRequest, CalculatorResponse> rrTemplate) {
        this.rrTemplate = rrTemplate;
    }

    @GetMapping("/sum")
    public ResponseEntity<?> sum(@RequestParam String a, @RequestParam String b) throws Exception {
        return handle("sum", a, b);
    }

    @GetMapping({"/sub", "/subtraction"})
    public ResponseEntity<?> sub(@RequestParam String a, @RequestParam String b) throws Exception {
        return handle("sub", a, b);
    }

    @GetMapping({"/mul", "/multiplication"})
    public ResponseEntity<?> mul(@RequestParam String a, @RequestParam String b) throws Exception {
        return handle("mul", a, b);
    }

    @GetMapping({"/div", "/division"})
    public ResponseEntity<?> div(@RequestParam String a, @RequestParam String b) throws Exception {
        return handle("div", a, b);
    }

    private ResponseEntity<?> handle(String op, String a, String b) throws Exception {
        String id = java.util.UUID.randomUUID().toString();
        MDC.put("requestId", id);
        
        final BigDecimal aBD;
        final BigDecimal bBD;

        try {
            aBD = new BigDecimal(a);
            bBD = new BigDecimal(b);
        } catch (NumberFormatException nfe) {
            log.error("Invalid numeric input: a='{}', b='{}', requestId={}", a, b, id);
            CalculatorResponse err = new CalculatorResponse();
            err.setId(id);
            err.setError("invalid number format" + nfe.getMessage());
            return ResponseEntity.badRequest()
                    .header("Request-Id", id)
                    .body(err);
        }

        CalculatorRequest req = new CalculatorRequest();
        req.setId(id);
        req.setOperation(op);
        req.setA(aBD);
        req.setB(bBD);

        log.debug("Created CalculatorRequest: {}, requestId={}", req, id);

        try {
            log.info("Sending request to Kafka, requestId={}", id);
            RequestReplyFuture<String, CalculatorRequest, CalculatorResponse> future =
                rrTemplate.sendAndReceive(KafkaProducerConfig.buildRecord(req, id));

            ConsumerRecord<String, CalculatorResponse> reply = future.get(5, java.util.concurrent.TimeUnit.SECONDS);

            CalculatorResponse response = reply.value();
            log.info("Received response from Kafka: {}, requestId={}", response, id);

            if (response.getError() != null) {
                return ResponseEntity.status(400) // Bad Request
                    .header("Request-Id", id)
                    .body(Map.of("error", response.getError()));
            }
            
            return ResponseEntity.ok()
                .header("Request-Id", id)
                .body(Map.of("result", response.getResult()));

        } catch (TimeoutException e) {
            log.error("Timeout waiting for calculator response: {}, requestId={}", e.getMessage(), id);
            return ResponseEntity.status(503)  // Service Unavailable
                .header("Request-Id", id)
                .body(Map.of("error", "Calculator didn't answer in 5 seconds"));

        } catch (Exception e) {
            log.error("Error processing request: {}, requestId={}", e.getMessage(), id);
            CalculatorResponse errorResponse = new CalculatorResponse();
            errorResponse.setId(id);
            errorResponse.setError("timeout/error: " + e.getMessage());
            return ResponseEntity.status(500) // Internal Server Error
                .header("Request-Id", id)
                .body(errorResponse);
                
        } finally {
            MDC.clear();
        }
    }
}

