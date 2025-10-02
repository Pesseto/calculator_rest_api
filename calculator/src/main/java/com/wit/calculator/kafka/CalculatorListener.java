package com.wit.calculator.kafka;

import com.wit.calculator.dto.CalculatorRequest;
import com.wit.calculator.dto.CalculatorResponse;
import com.wit.calculator.config.KafkaTopicsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CalculatorListener {

  private static final Logger log = LoggerFactory.getLogger(CalculatorListener.class);

  @KafkaListener(topics = KafkaTopicsConfig.REQ, groupId = "calc-worker")
  @SendTo(KafkaTopicsConfig.RES)
  public CalculatorResponse handle(CalculatorRequest req, @Header(name="Request-Id") String id) {
    MDC.put("requestId", id);
    
    if (req == null) {
      log.warn("Received null request, requestId={}", id);
      return new CalculatorResponse(id, "null request");
    }

    log.info("Received calculation request: operation={}, requestId={}", 
              req.getOperation(), id);

    try {
      final int SCALE = 10;
      String op = req.getOperation();
      BigDecimal a = req.getA();
      BigDecimal b = req.getB();

      log.debug("Processing operation: {} with values a={}, b={}, requestId={}", op, a, b, id);

      if (a == null) {
        log.warn("Missing operand 'a', requestId={}", id);
        return new CalculatorResponse(id, "missing a");
      }
      if (b == null) {
        log.warn("Missing operand 'b', requestId={}", id);
        return new CalculatorResponse(id, "missing b");
      }
      if (op == null) {
        log.warn("Missing operation, requestId={}", id);
        return new CalculatorResponse(id, "missing operation");
      }

      CalculatorResponse result = switch (op) {
        case "sum" -> {
          BigDecimal res = a.add(b);
          log.info("Sum calculation: {} + {} = {}, requestId={}", a, b, res, id);
          yield new CalculatorResponse(id, res);
        }
        case "sub" -> {
          BigDecimal res = a.subtract(b);
          log.info("Subtraction calculation: {} - {} = {}, requestId={}", a, b, res, id);
          yield new CalculatorResponse(id, res);
        }
        case "mul" -> {
          BigDecimal res = a.multiply(b);
          log.info("Multiplication calculation: {} * {} = {}, requestId={}", a, b, res, id);
          yield new CalculatorResponse(id, res);
        }
        case "div" -> {
          if (b.compareTo(BigDecimal.ZERO) == 0){
            log.warn("Division by zero attempted: {} / {}, requestId={}", a, b, id);
            yield new CalculatorResponse(id, "division by zero attempted");
          } else {
            BigDecimal res = a.divide(b, SCALE, RoundingMode.HALF_UP);
            log.info("Division calculation: {} / {} = {}, requestId={}", a, b, res, id);
            yield new CalculatorResponse(id, res);
          }
        }
        default -> {
          log.warn("Invalid operation requested: {}, requestId={}", op, id);
          yield new CalculatorResponse(id, "invalid operation: " + op);
        }
      };
      
      
      log.info("Calculation completed successfully, requestId={}", id);
      return result;

    } catch (Exception e) {
      log.error("Error processing calculation: {}, requestId={}", e.getMessage(), id);
      return new CalculatorResponse(id, "error: " + e.getMessage());
    } finally {
      MDC.clear();
    } 
  }
}
