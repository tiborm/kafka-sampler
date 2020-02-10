package com.cloudera.kafkasampler.services;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@Service
@Profile("dev")
public class FakeProducer {

    private static final Logger logger = LoggerFactory.getLogger(FakeProducer.class);

    private KafkaTemplate<String, String> kafkaTemplate;
    private String topic;
    private String bootstrapServers;

    @Autowired
    public FakeProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${kafka-sampler.default-topic:}")String topic,
            @Value("${spring.kafka.producer.bootstrap-servers:}") String bootstrapServers
            ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.bootstrapServers = bootstrapServers;

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendMessage("Test MEssage...");
            }
        }, 0, 200);
    }

    @Bean
    public KafkaAdmin admin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic parserChainStream() {
        return TopicBuilder.name(this.topic)
                .partitions(1)
                .replicas(1)
                .compact()
                .build();
    }

    public void sendMessage(String message) {
        logger.info(String.format("#### -> Producing message -> %s", message));
        kafkaTemplate.send(this.topic, message);
    }
}