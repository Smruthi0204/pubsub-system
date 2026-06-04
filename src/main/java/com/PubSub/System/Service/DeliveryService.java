package com.PubSub.System.Service;

import java.time.LocalDateTime;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.PubSub.System.Entity.DeliveryTracking;
import com.PubSub.System.Entity.Event;
import com.PubSub.System.Entity.Subscriber;
import com.PubSub.System.Repository.DeliveryTrackingRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryService {
    private final RestTemplate restTemplate;
    private final JavaMailSender mailSender;
    private final DeliveryTrackingRepo deliveryTrackingRepo;

    public void  deliverWebhook(Subscriber subscriber, Event event) throws InterruptedException{

        DeliveryTracking deliveryTracking = new DeliveryTracking();
        deliveryTracking.setEvent(event);
        deliveryTracking.setSubscriber(subscriber);
        deliveryTracking.setAttemptedAt(LocalDateTime.now());
        for(int i=0;i<3;i++){
            try {
                restTemplate.postForObject(subscriber.getEndpoint(), event.getPayload(), String.class);
                deliveryTracking.setStatus("Delivered");
                deliveryTrackingRepo.save(deliveryTracking);
                return;

            } catch (Exception e) {
                System.out.println("Delivery failed: " + e.getClass().getName() + " - " + e.getMessage());
                Thread.sleep(2000);
                deliveryTracking.setRetryCount(i+1);
                if(i==2){
                    deliveryTracking.setStatus("Permanently Failed");
                    deliveryTrackingRepo.save(deliveryTracking);
                    return;
                }
                deliveryTracking.setStatus("Delivery Failed: ReAttempting");
                deliveryTrackingRepo.save(deliveryTracking);
            }
        }
       
    }


    public void  deliverEmail(Subscriber subscriber, Event event) throws InterruptedException {
        DeliveryTracking deliveryTracking = new DeliveryTracking();
        deliveryTracking.setEvent(event);
        deliveryTracking.setSubscriber(subscriber);
        deliveryTracking.setAttemptedAt(LocalDateTime.now());
        for(int i=0;i<3;i++){
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(subscriber.getEndpoint());
            message.setSubject("New event on topic: " + event.getTopic());
            message.setText(event.getPayload());
            mailSender.send(message);
            deliveryTracking.setStatus("Delivered");
            deliveryTrackingRepo.save(deliveryTracking);
            return;
            
        } catch (MailException e) {
            deliveryTracking.setRetryCount(i+1);
            Thread.sleep(2000);
            if(i==2){
                deliveryTracking.setStatus("Permanently Failed");
                deliveryTrackingRepo.save(deliveryTracking);
                return;
            }
            deliveryTracking.setStatus("Delivery Failed: ReAttempting");
            deliveryTrackingRepo.save(deliveryTracking);
        }
    }
}

}


