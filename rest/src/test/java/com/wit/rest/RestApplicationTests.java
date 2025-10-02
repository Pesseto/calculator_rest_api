package com.wit.rest;

import com.wit.rest.dto.CalculatorRequest;
import com.wit.rest.dto.CalculatorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class RestApplicationTests {

    @MockBean
    private ReplyingKafkaTemplate<String, CalculatorRequest, CalculatorResponse> rrTemplate;

    @Test
    void contextLoads() { }
}
