package com.adfonic.datacollector.kafka;

public class KafkaTopicStream {
    
    private String topic;
    private int streams;
    
    public KafkaTopicStream(String topic, int streams){
        this.topic = topic;
        this.streams = streams;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getStreams() {
        return streams;
    }

    public void setStreams(int streams) {
        this.streams = streams;
    }
    
    
}