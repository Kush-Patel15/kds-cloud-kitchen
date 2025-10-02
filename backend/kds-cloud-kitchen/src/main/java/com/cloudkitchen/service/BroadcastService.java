package com.cloudkitchen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BroadcastService {
    private final SimpMessagingTemplate template;
    public void send(String topic, Object payload) {
        template.convertAndSend(topic, payload);
    }
}