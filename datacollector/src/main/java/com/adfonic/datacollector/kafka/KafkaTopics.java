package com.adfonic.datacollector.kafka;

import java.util.ArrayList;
import java.util.List;

public class KafkaTopics {
    
    private List<KafkaTopicStream> topics;
    
    public KafkaTopics(){
        this.topics = new ArrayList<KafkaTopicStream>();
    }

    public List<KafkaTopicStream> getTopics() {
        return topics;
    }

    public void setTopics(List<KafkaTopicStream> topics) {
        this.topics = topics;
    }
    
}