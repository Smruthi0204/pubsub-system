package com.PubSub.System.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.PubSub.System.Entity.Event;
import com.PubSub.System.Entity.Publisher;
import com.PubSub.System.Entity.Subscriber;
import com.PubSub.System.Repository.DeliveryTrackingRepo;
import com.PubSub.System.Repository.EventRepo;
import com.PubSub.System.Service.PubSubService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PubSubController {
    private final PubSubService servObj;
    private final DeliveryTrackingRepo deliveryTrackingRepo;
    private final EventRepo eventRepo;
    
    @PostMapping("/api/pubregister")
    public String registerPublisher(@RequestBody Publisher publisher){
            return servObj.registerPublisher(publisher);
    }

    @PostMapping("/api/subregister")
    public String registerSuscriber(@RequestBody Subscriber subscriber){
            return servObj.registerSubscriber(subscriber);
    }

    @PostMapping("/api/events")
    public String registerSuscriber(@RequestBody Event event){
            return servObj.registerEvent(event);
    }
 
    @GetMapping("/dashboard")
    public Map<String, Long> getDashboard() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalEvents", eventRepo.count());
        stats.put("totalDelivered", deliveryTrackingRepo.countByStatus("Delivered"));
        stats.put("totalPermanentlyFailed", deliveryTrackingRepo.countByStatus("Permanently Failed"));
        return stats;
    }


}
