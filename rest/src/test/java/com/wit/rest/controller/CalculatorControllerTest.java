package com.wit.rest.controller;

import com.wit.rest.config.KafkaTopicsConfig;
import com.wit.rest.dto.CalculatorRequest;
import com.wit.rest.dto.CalculatorResponse;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@WebMvcTest(CalculatorController.class)
class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReplyingKafkaTemplate<String, CalculatorRequest, CalculatorResponse> rrTemplate;

    @Test
    void invalid_number_returns_400_and_request_id() throws Exception {
        mockMvc.perform(get("/sum")
                .param("a", "abc")
                .param("b", "3"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("Request-Id"))
                .andExpect(jsonPath("$.error", Matchers.containsString("invalid number format")));
    }

    @Test
    void kafka_timeout_returns_503_and_request_id() throws Exception {
        @SuppressWarnings("unchecked")
        RequestReplyFuture<String, CalculatorRequest, CalculatorResponse> future =
                Mockito.mock(RequestReplyFuture.class);

        when(rrTemplate.sendAndReceive(Mockito.<ProducerRecord<String, CalculatorRequest>>any()))
                .thenReturn(future);

        when(future.get(anyLong(), any(TimeUnit.class)))
                .thenThrow(new TimeoutException("Kafka timeout"));

        mockMvc.perform(get("/sum").param("a", "1").param("b", "2"))
                .andExpect(status().isServiceUnavailable()) // 503
                .andExpect(header().exists("Request-Id"))
                .andExpect(jsonPath("$.error").value("Calculator didn't answer in 5 seconds"));
    }

    @Test
    void sum_ok_returns_200_and_result() throws Exception {
        CalculatorResponse mockResponse = new CalculatorResponse();
        mockResponse.setResult(new BigDecimal("15"));

        ConsumerRecord<String, CalculatorResponse> record =
                new ConsumerRecord<>(KafkaTopicsConfig.RES, 0, 0L, null, mockResponse);

        @SuppressWarnings("unchecked")
        RequestReplyFuture<String, CalculatorRequest, CalculatorResponse> future =
                Mockito.mock(RequestReplyFuture.class);

        when(rrTemplate.sendAndReceive(Mockito.<ProducerRecord<String, CalculatorRequest>>any()))
                .thenReturn(future);
        when(future.get(anyLong(), any(TimeUnit.class)))
                .thenReturn(record);

        mockMvc.perform(get("/sum").param("a", "10").param("b", "5"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Request-Id"))
                .andExpect(jsonPath("$.result").value(15));
    }

    @Test  
    void div_ok_returns_200_and_result() throws Exception {
        CalculatorResponse mockResponse = new CalculatorResponse();
        mockResponse.setResult(new BigDecimal("2"));

        ConsumerRecord<String, CalculatorResponse> record =
                new ConsumerRecord<>(KafkaTopicsConfig.RES, 0, 0L, null, mockResponse);

        @SuppressWarnings("unchecked")
        RequestReplyFuture<String, CalculatorRequest, CalculatorResponse> future =
                Mockito.mock(RequestReplyFuture.class);

        when(rrTemplate.sendAndReceive(Mockito.<ProducerRecord<String, CalculatorRequest>>any()))
                .thenReturn(future);
        when(future.get(anyLong(), any(TimeUnit.class)))
                .thenReturn(record);

        mockMvc.perform(get("/div").param("a", "10").param("b", "5"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Request-Id"))
                .andExpect(jsonPath("$.result").value(2));
    }

    @Test
    void division_by_zero_returns_400_and_error() throws Exception {
        CalculatorResponse mockResponse = new CalculatorResponse();
        mockResponse.setError("division by zero attempted");

        ConsumerRecord<String, CalculatorResponse> record =
                new ConsumerRecord<>(KafkaTopicsConfig.RES, 0, 0L, null, mockResponse);

        @SuppressWarnings("unchecked")
        RequestReplyFuture<String, CalculatorRequest, CalculatorResponse> future =
                Mockito.mock(RequestReplyFuture.class);

        when(rrTemplate.sendAndReceive(Mockito.<ProducerRecord<String, CalculatorRequest>>any()))
                .thenReturn(future);
        when(future.get(anyLong(), any(TimeUnit.class)))
                .thenReturn(record);

        mockMvc.perform(get("/div").param("a", "10").param("b", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("Request-Id"))
                .andExpect(jsonPath("$.error").value("division by zero attempted"));
    }

    @Test
    void mul_ok_returns_200_and_result() throws Exception {
        CalculatorResponse mockResponse = new CalculatorResponse();
        mockResponse.setResult(new BigDecimal("50"));

        ConsumerRecord<String, CalculatorResponse> record =
                new ConsumerRecord<>(KafkaTopicsConfig.RES, 0, 0L, null, mockResponse);

        @SuppressWarnings("unchecked")
        RequestReplyFuture<String, CalculatorRequest, CalculatorResponse> future =
                Mockito.mock(RequestReplyFuture.class);

        when(rrTemplate.sendAndReceive(Mockito.<ProducerRecord<String, CalculatorRequest>>any()))
                .thenReturn(future);
        when(future.get(anyLong(), any(TimeUnit.class)))
                .thenReturn(record);

        mockMvc.perform(get("/mul").param("a", "10").param("b", "5"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Request-Id"))
                .andExpect(jsonPath("$.result").value(50));
    }
}
