package com.cloudera.kafkasampler.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class KafkaSamplerController {

    @RequestMapping("/api/v1/samples/kafka")
    public String getKafkaSample() {
        return "Greetings from Spring Boot!";
    }

}