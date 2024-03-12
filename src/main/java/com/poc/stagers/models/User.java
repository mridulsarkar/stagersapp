package com.poc.stagers.models;

import java.util.Date;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

@Document(collection = "USERS")
public class User
{
    @Transient
    public static final String SEQUENCE_NAME = "users_sequence";
    
    @Id
    private long id;
    
    @Indexed(unique = true, direction = IndexDirection.DESCENDING, dropDups = true)
    @Size(min = 4, max = 255, message = "Minimum username length: 4 characters")
    private String username;
    
    @Indexed
    private String email;
    
    @NotNull
    @Size(min = 8, message = "Minimum password length: 8 characters")
    private String password;
    
    @NotNull
    private String firstname;
    private String lastname;
    
    @NotNull
    private boolean enabled;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date createddate;
    
    @DBRef(lazy = false)
    private Set<Role> roles;
    
    public long getId() {
        return id;
    }
    
    public void setId(final long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(final String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(final String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(final String password) {
        this.password = password;
    }
    
    public String getFirstName() {
        return firstname;
    }
    
    public void setFirstName(final String firstname) {
        this.firstname = firstname;
    }
    
    public String getLastName() {
        return lastname;
    }
    
    public void setLastName(final String lastname) {
        this.lastname = lastname;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(final boolean status) {
        this.enabled = status;
    }
    
    public Date getCreatedDate() {
        return createddate;
    }
    
    public void setCreatedDate(final Date createddate) {
        this.createddate = createddate;
    }
    
    public Set<Role> getRoles() {
        return roles;
    }
    
    public void setRoles(final Set<Role> roles) {
        this.roles = roles;
    }
    
    public User() {
        this.createddate = new Date();
    }
    
    public User(final String username, final String email, final String firstname, 
                        final String lastname, final String password) {
        this.username = username;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
        this.createddate = new Date();
    }
}