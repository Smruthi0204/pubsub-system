package com.PubSub.System.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PubSub.System.Entity.Event;

@Repository
public interface EventRepo extends JpaRepository<Event, UUID>  {

   

}
