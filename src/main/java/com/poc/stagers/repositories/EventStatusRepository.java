package com.poc.stagers.repositories;

import com.poc.stagers.models.EventStatus;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EventStatusRepository extends MongoRepository<EventStatus, Long>
{
    @Query("{ 'status' : { '$regex' : ?0 , $options: 'i'}}")
    EventStatus findByEventStatus(final String status);
}