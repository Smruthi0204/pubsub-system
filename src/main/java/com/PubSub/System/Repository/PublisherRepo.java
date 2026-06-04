package com.PubSub.System.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PubSub.System.Entity.Publisher;

@Repository
public interface PublisherRepo extends JpaRepository<Publisher, UUID>  {
    boolean existsByemailId(String emailId);
}
