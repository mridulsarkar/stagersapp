package com.poc.stagers.service;

import java.util.List;

import com.poc.stagers.models.User;
import com.poc.stagers.exception.CustomException;

import org.springframework.stereotype.Service;

@Service
public interface UserService {
    public void createUser(User newUser);
    public User findByUsername(final String username) throws CustomException;
    public User findById(final long id) throws CustomException;
    public List<User> findByEmail(final String email) throws CustomException;
    public List<User> findByLastName(final String lastName) throws CustomException;    
    public List<User> findByFirstName(final String firstName) throws CustomException;
    public List<User> findAllUsers();
    public User update(final User user, final Long l);
    public User changeUserStatus(final User user, final Long id) throws CustomException;
}
