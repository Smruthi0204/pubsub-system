package com.PubSub.System.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PubSub.System.Entity.DeliveryTracking;

@Repository
public interface  DeliveryTrackingRepo extends JpaRepository <DeliveryTracking, UUID> {
   long countByStatus(String status);
}
