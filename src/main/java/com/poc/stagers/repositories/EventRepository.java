package com.poc.stagers.repositories;

import com.poc.stagers.models.Event;
import com.poc.stagers.models.EventStatus;
import com.poc.stagers.models.User;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends MongoRepository<Event, Long>
{
    @Query("{'eventid': {$regex: ?0 }})")
    Event findByEventId(final String eventid);
    
    Event findById(final long Id);
    
    @Query("{ 'description' : { '$regex' : ?0 , $options: 'i'}}")
    List<Event> findByDescription(final String description);
    
    @Query("{ 'title' : { '$regex' : ?0 , $options: 'i'}}")
    List<Event> findByTitle(final String title);
    
    List<Event> findByStatus(final EventStatus status);
    
    List<Event> findByCreator(final User creator);
    
    List<Event> findByCreatorAndStatus(final User creator, final EventStatus status);
    
    List<Event> findByCreatorAndEventid(final User creator, final String eventid);

    Event findByCreatorAndId(final User creator, final long id);
}