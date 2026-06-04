package com.PubSub.System.Queue;

import org.springframework.stereotype.Component;

import com.PubSub.System.Entity.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventQueue {
    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper;

    public void push(Event event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            sqsTemplate.send("pubsub-events", json);
        } catch (JsonProcessingException e) {
        }
    }
}