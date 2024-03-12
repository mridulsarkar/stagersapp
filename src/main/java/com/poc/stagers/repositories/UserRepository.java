package com.poc.stagers.repositories;

import com.poc.stagers.models.User;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserRepository extends MongoRepository<User, Long>
{
    @Query("{'username': {$regex: ?0 }})")
    User findByUsername(final String username);
    
    User findById(long Id);
    
    @Query("{ 'email' : { '$regex' : ?0 , $options: 'i'}}")
    List<User> findByEmail(final String email);
    
    @Query("{ 'lastname' : { '$regex' : ?0 , $options: 'i'}}")
    List<User> findByLastName(final String lastName);
    
    @Query("{ 'firstname' : { '$regex' : ?0 , $options: 'i'}}")
    List<User> findByFirstName(final String firstName);
}