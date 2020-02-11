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

import java.util.*;
import java.util.stream.Collectors;

@Service
@Profile("dev")
public class FakeProducer {

    private static final Logger logger = LoggerFactory.getLogger(FakeProducer.class);
    private final List<String> messageTemplates;

    private KafkaTemplate<String, String> kafkaTemplate;
    private String topic;
    private String bootstrapServers;

    private int increment = 0;

    @Autowired
    public FakeProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${kafka-sampler.fake-producer.default-topic:}") String topic,
            @Value("${kafka-sampler.fake-producer.fake-messages:}") String fakeMessages,
            @Value("${kafka-sampler.fake-producer.delimiter:}") String delimiter,
            @Value("${kafka-sampler.fake-producer.message-per-ms:}") int messagePerMS,
            @Value("${spring.kafka.producer.bootstrap-servers:}") String bootstrapServers
            ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.bootstrapServers = bootstrapServers;

        this.messageTemplates = this.processFakeMessages(fakeMessages, delimiter);

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendMessage(getActualizedMessage());
                increment++;
            }
        }, 0, messagePerMS / 1000);
    }

    private List<String> processFakeMessages(String messageSource, String delimiter) {
        return Arrays.asList(messageSource.split(delimiter));
    }

    private String getActualizedMessage() {
        return String.format(
            this.messageTemplates.get(new Random().nextInt(this.messageTemplates.size())),
            this.increment, "[date]"
        );
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
