package com.PubSub.System.Consumer;
import java.util.List;

import org.springframework.stereotype.Component;

import com.PubSub.System.Entity.DeliveryTracking;
import com.PubSub.System.Entity.Event;
import com.PubSub.System.Entity.Subscriber;
import com.PubSub.System.Repository.DeliveryTrackingRepo;
import com.PubSub.System.Repository.SubscriberRepo;
import com.PubSub.System.Service.DeliveryService;
import com.PubSub.System.Service.WebSocketHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventConsumer {
    private final SubscriberRepo subscriberRepo;
    private final DeliveryService deliveryService;
    private final WebSocketHandler webSocketHandler;
    private final DeliveryTrackingRepo deliveryTrackingRepo;
    private final ObjectMapper objectMapper;
    
    @SqsListener("pubsub-events")
    public void consume(String message) throws InterruptedException {
        try {
            Event event = objectMapper.readValue(message, Event.class);
            List<Subscriber> sub = subscriberRepo.findByTopicsContaining(event.getTopic());
            for (Subscriber s : sub) {

                switch (s.getDeliverytype()) {
                    case "webhook" -> deliveryService.deliverWebhook(s, event);
                    case "email" -> deliveryService.deliverEmail(s, event);
                    default -> {
                        
                        DeliveryTracking tracking = new DeliveryTracking();
                        tracking.setEvent(event);
                        tracking.setSubscriber(s);
                        for(int i=0;i<3;i++){
                        try {
                            webSocketHandler.sendMessage(s.getName(), event.getPayload());
                            tracking.setStatus("Delivered");
                            deliveryTrackingRepo.save(tracking);
                            break;
                        } catch (Exception e) {
                            tracking.setRetryCount(i+1);
                            Thread.sleep(2000);
                            if(i==2){
                            tracking.setStatus("Permanently Failed");
                            deliveryTrackingRepo.save(tracking);
                            break;
                        }
                            tracking.setStatus("Failed");
                            deliveryTrackingRepo.save(tracking);
                        }
                    }
    
                    }
                }
            }
        } catch (JsonProcessingException e) {
        }
    }
}


        