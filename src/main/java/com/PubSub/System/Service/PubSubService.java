package com.PubSub.System.Service;

import org.springframework.stereotype.Service;

import com.PubSub.System.Entity.Event;
import com.PubSub.System.Entity.Publisher;
import com.PubSub.System.Entity.Subscriber;
import com.PubSub.System.Queue.EventQueue;
import com.PubSub.System.Repository.EventRepo;
import com.PubSub.System.Repository.PublisherRepo;
import com.PubSub.System.Repository.SubscriberRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PubSubService {
    private final PublisherRepo publisherRepo;
    private final SubscriberRepo subscriberRepo;
    private final EventRepo eventRepo;
    private final EventQueue eveq;
    

     
    public String registerPublisher(Publisher pub){
        if(!publisherRepo.existsByemailId(pub.getEmailId())){
            publisherRepo.save(pub);
            return ("""
                    Registered Successfully
                    publisher Id:"""+pub.getId());
        }

        return ("""
                    Publisher already Registeredd
                    publisher Id:"""+pub.getId());
    }

    public String registerSubscriber(Subscriber sub){
        if(!subscriberRepo.existsByemailId(sub.getEmailId())){
            subscriberRepo.save(sub);
            return ("Registered Successfully");
        }

        return ("Subscriber already registered");
    }

     public String registerEvent(Event event){
        eventRepo.save(event);
        eveq.push(event);
        return ("Event Added to Queue");
    }


}
