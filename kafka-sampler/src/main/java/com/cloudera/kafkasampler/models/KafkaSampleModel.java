package com.cloudera.kafkasampler.models;

public class KafkaSampleModel {

    private static int LIMIT = 100;

    enum PickingMethod {
        RANDOM,
        LAST_N;
    }

    private String topic;
    private int itemNum = 10;
    private PickingMethod pickingMethod = PickingMethod.LAST_N;
    private String[] results;

    public String[] getResults() {
        return results;
    }

    public void setResults(String[] results) {
        this.results = results;
    }

    public KafkaSampleModel(String topic, int itemNum, PickingMethod pickingMethod) {
        this.topic = topic;
        this.itemNum = itemNum;
        this.pickingMethod = pickingMethod;
    }

}
