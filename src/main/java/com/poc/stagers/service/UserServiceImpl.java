package com.poc.stagers.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.poc.stagers.exception.CustomException;
import com.poc.stagers.jwt.JwtTokenProvider;
import com.poc.stagers.models.Role;
import com.poc.stagers.models.User;
import com.poc.stagers.repositories.RoleRepository;
import com.poc.stagers.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    public String login(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            return jwtTokenProvider.createToken(username, userRepo.findByUsername(username).getRoles());
        } catch (AuthenticationException e) {
            throw new CustomException("Invalid username/password supplied", HttpStatus.UNPROCESSABLE_ENTITY);
        }
      }
    
    public String signup(User newUser) {
        if (null == userRepo.findByUsername(newUser.getUsername())) {
            createUser(newUser); // create new user
            return jwtTokenProvider.createToken(newUser.getUsername(), newUser.getRoles());
        } else {
            throw new CustomException("Username is already in use", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }
    
    public String refresh(String username) {
        return jwtTokenProvider.createToken(username, userRepo.findByUsername(username).getRoles());
    }
    
    public void createUser(User newUser) {
        newUser.setPassword(bCryptPasswordEncoder.encode((CharSequence) newUser.getPassword()));
        newUser.setEnabled(true);
        Role userRole = roleRepo.findByRole("ADMIN");
        newUser.setRoles(new HashSet<Role>(Arrays.asList(userRole)));
        userRepo.save(newUser);
    }

    public User findByUsername(final String username) throws CustomException {
        User user = userRepo.findByUsername(username);
        if(null == user) {
            throw new CustomException("The user doesn't exist - Invalid user", HttpStatus.NOT_FOUND);
         }
        return user;
    }

    public User findById(final long id) throws CustomException {
        User user = userRepo.findById(id);
        if(null == user) {
            throw new CustomException("The user doesn't exist - search by id", HttpStatus.NOT_FOUND);
         }
        return user;
    }
    
    public List<User> findByEmail(final String email)  throws CustomException {
        List<User> users = userRepo.findByEmail(email);
        if(null == users || users.isEmpty()) {
            throw new CustomException("The user doesn't exist - search by email", HttpStatus.NOT_FOUND);
         }
        return users;
    }
    
    public List<User> findByLastName(final String lastName) throws CustomException {
        List<User> users = userRepo.findByLastName(lastName);
        if(null == users || users.isEmpty()) {
            throw new CustomException("The user doesn't exist - search by lastname", HttpStatus.NOT_FOUND);
         }
        return users;
    }
    
    public List<User> findByFirstName(final String firstName ) throws CustomException {
        List<User> users = userRepo.findByFirstName(firstName);
        if(null == users || users.isEmpty()) {
            throw new CustomException("The user doesn't exist - search by firstname", HttpStatus.NOT_FOUND);
         }
        return users;
    }
    
    public List<User> findAllUsers() {
        return userRepo.findAll();
    }
    
    public User update(final User user, final Long l) {
        return userRepo.save(user);
    }
    
    public User changeUserStatus(final User user, final Long id) throws CustomException {
        User usr = findById(id);
        usr.setEnabled(user.isEnabled());
        return (User)this.userRepo.save(usr);
    }

    public void delete(User user2Delete) {
        userRepo.delete(user2Delete);
    }
}