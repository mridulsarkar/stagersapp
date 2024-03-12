package com.poc.stagers.models;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;

@Document(collection = "ROLES")
public class Role implements GrantedAuthority
{
    @Transient
    public static final String SEQUENCE_NAME = "roles_sequence";
    @Id
    private long id;
    @Indexed(unique = true, direction = IndexDirection.DESCENDING, dropDups = true)
    private String role;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date createddate;
    
    public Role() {
        this.createddate = new Date();
    }
    
    public long getId() {
        return this.id;
    }
    
    public void setId(final long id) {
        this.id = id;
    }
    
    public String getRole() {
        return this.role;
    }
    
    public void setRole(final String role) {
        this.role = role;
    }
    
    public Date getCreatedDate() {
        return this.createddate;
    }
    
    public void setCreatedDate(final Date createddate) {
        this.createddate = createddate;
    }

    public String getAuthority() {
        return this.getRole();
    }
}