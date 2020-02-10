package com.cloudera.kafkasampler.controllers;

import com.cloudera.kafkasampler.services.samplepicker.KafkaSamplePicker;
import com.cloudera.kafkasampler.services.samplepicker.PickingMethods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RestController
public class KafkaSamplerController {

    @Autowired
    private KafkaSamplePicker pickingService;

    @RequestMapping("/api/v1/kafka-sampler/topics/{topicId}")
    public List<String> getKafkaSample(
            @PathVariable(required = true) String topicId,
            @RequestParam int itemNo,
            @RequestParam PickingMethods method) throws Exception {

        return this.pickingService.takeSample(topicId, itemNo, method);
    }

}