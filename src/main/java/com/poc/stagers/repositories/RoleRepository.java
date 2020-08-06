package com.poc.stagers.repositories;

import com.poc.stagers.models.Role;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends MongoRepository<Role, Long>
{
    @Query("{ 'role' : { '$regex' : ?0 , $options: 'i'}}")
    Role findByRole(final String role);
}