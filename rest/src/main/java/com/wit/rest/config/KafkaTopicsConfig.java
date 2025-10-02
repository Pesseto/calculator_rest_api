package com.wit.rest.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicsConfig {
  public static final String REQ = "calculator.requests";
  public static final String RES = "calculator.replies";

  @Bean 
  public NewTopic calculatorRequestsTopic(){ 
    return new NewTopic(REQ, 1, (short)1); 
  }

  @Bean 
  public NewTopic calculatorRepliesTopic(){ 
    return new NewTopic(RES, 1, (short)1); 
  }
}
