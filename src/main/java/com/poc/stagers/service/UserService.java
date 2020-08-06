package com.poc.stagers.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.poc.stagers.models.Role;
import com.poc.stagers.models.User;
import com.poc.stagers.repositories.RoleRepository;
import com.poc.stagers.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public void createUser(User newUser) {
        newUser.setPassword(bCryptPasswordEncoder.encode((CharSequence) newUser.getPassword()));
        newUser.setEnabled(true);
        Role userRole = roleRepo.findByRole("ADMIN");
        newUser.setRoles(new HashSet<Role>(Arrays.asList(userRole)));
        userRepo.save(newUser);
    }

    public User findByUsername(final String username) {
        return userRepo.findByUsername(username);
    }

    public User findById(final long id) {
        return userRepo.findById(id);
    }
    
    public List<User> findByEmail(final String email) {
        return userRepo.findByEmail(email);
    }
    
    public List<User> findByLastName(final String lastName) {
        return userRepo.findByLastName(lastName);
    }
    
    public List<User> findByFirstName(final String firstName) {
        return userRepo.findByFirstName(firstName);
    }
    
    public List<User> findAllUsers() {
        return userRepo.findAll();
    }
    
    public User update(final User user, final Long l) {
        return userRepo.save(user);
    }
    
    public User changeUserStatus(final User user, final Long id) {
        User usr = findById(id);
        usr.setEnabled(user.isEnabled());
        return (User)this.userRepo.save(usr);
    }
}