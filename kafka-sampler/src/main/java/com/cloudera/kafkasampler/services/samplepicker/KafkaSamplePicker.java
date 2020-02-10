package com.cloudera.kafkasampler.services.samplepicker;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class KafkaSamplePicker {

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
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        DefaultKafkaConsumerFactory<String, String> kafkaConsumerFactory =
            new DefaultKafkaConsumerFactory<>(
                consumerConfig,
                new StringDeserializer(),
                new StringDeserializer());

        ContainerProperties containerProperties = new ContainerProperties(topic);
        containerProperties.setMessageListener(
                (MessageListener<String, String>) message -> {
                    logger.info(String.format("#### -> Consumed message -> %s", message));
                    consumedMessages.add(message.value());
                    latch.countDown();
                });

        KafkaMessageListenerContainer container =
                new KafkaMessageListenerContainer<>(
                        kafkaConsumerFactory,
                        containerProperties);

        container.start();
        latch.await(60, TimeUnit.SECONDS);
        container.stop();
        return consumedMessages;
    }
}
