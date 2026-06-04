package com.PubSub.System.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PubSub.System.Entity.Subscriber;

@Repository
public interface SubscriberRepo extends JpaRepository<Subscriber, UUID>  {
    boolean existsByemailId(String emailId);
    List<Subscriber> findByTopicsContaining(String topic);
}
