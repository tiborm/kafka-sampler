package com.cloudera.kafkasampler.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class KafkaSampleConsumer {

    private final Logger logger = LoggerFactory.getLogger(KafkaSampleConsumer.class);

    // TODO how to consume dynamic topic
    // TODO what is groupId
    @KafkaListener(topics = "users", groupId = "group_id")
    public void consume(String message) throws IOException {
        logger.info(String.format("#### -> Consumed message -> %s", message));
    }
}
