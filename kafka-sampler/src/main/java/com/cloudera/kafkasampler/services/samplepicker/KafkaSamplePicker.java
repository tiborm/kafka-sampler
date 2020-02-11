package com.cloudera.kafkasampler.services.samplepicker;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class KafkaSamplePicker {
    private static final String CONSUMER_AND_GROUP_ID_BASE = "kafka-sampler-";
    private final Logger logger = LoggerFactory.getLogger(KafkaSamplePicker.class);

    @Value("${spring.kafka.consumer.bootstrap-servers:}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:}")
    private String groupId;

    public List<String> takeSample(String topic, int itemNo, PickingMethods method) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(itemNo);
        List<String> consumedMessages = new ArrayList<>();

        Map<String, Object> consumerConfig = new HashMap<String, Object>();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_AND_GROUP_ID_BASE + UUID.randomUUID());
        consumerConfig.put(ConsumerConfig.CLIENT_ID_CONFIG, CONSUMER_AND_GROUP_ID_BASE + UUID.randomUUID());

        DefaultKafkaConsumerFactory<String, String> kafkaConsumerFactory =
            new DefaultKafkaConsumerFactory<>(
                consumerConfig,
                new StringDeserializer(),
                new StringDeserializer());

        ContainerProperties containerProperties = new ContainerProperties(topic);
        containerProperties.setMessageListener(
                (MessageListener<String, String>) message -> {
                    logger.info(String.format("Consumed message: %s; countDown: %s", message, latch.getCount()));
                    consumedMessages.add(message.value());
                    latch.countDown();
                });

        KafkaMessageListenerContainer listenerContainer =
                new KafkaMessageListenerContainer<>(
                        kafkaConsumerFactory,
                        containerProperties);

        listenerContainer.start();
        latch.await(60, TimeUnit.SECONDS);
        listenerContainer.stop();
        return consumedMessages;
    }
}
