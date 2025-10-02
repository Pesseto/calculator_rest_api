package com.wit.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ProducerFactory;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import com.wit.rest.dto.CalculatorRequest;
import com.wit.rest.dto.CalculatorResponse;

import java.nio.charset.StandardCharsets;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ReplyingKafkaTemplate<String, CalculatorRequest, CalculatorResponse> replyingKafkaTemplate(
                            ProducerFactory<String, CalculatorRequest> producerFactory,
                            ConsumerFactory<String, CalculatorResponse> consumerFactory) {
        var containerProps = new ContainerProperties(KafkaTopicsConfig.RES);
        var repliesContainer = new ConcurrentMessageListenerContainer<>(consumerFactory, containerProps);

        repliesContainer.setAutoStartup(true);
        return new ReplyingKafkaTemplate<>(producerFactory, repliesContainer);
    }

    public static ProducerRecord<String, CalculatorRequest> buildRecord(CalculatorRequest req, String id){
        if (req == null) {
            throw new IllegalArgumentException("CalculatorRequest cannot be null");
        }

        ProducerRecord<String, CalculatorRequest> record = new ProducerRecord<>(KafkaTopicsConfig.REQ, req);

        record.headers().add(new RecordHeader( 
            KafkaHeaders.REPLY_TOPIC, KafkaTopicsConfig.RES.getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader(
            "Request-Id", id.getBytes(StandardCharsets.UTF_8)));
        return record;
    }

}
