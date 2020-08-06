package com.poc.stagers.models;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

@Document(collection = "EVENT")
public class Event
{
    @Transient
    public static final String SEQUENCE_NAME = "event_sequence";
    
    @Id
    private long id;
    
    @Indexed(unique = true, direction = IndexDirection.DESCENDING, dropDups = true)
    private String eventid;
    
    @NotNull
    @Size(max = 30, message = "title need to have only 30 characters")
    private String title;
    
    @NotNull
    private BigDecimal amount;
    
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date created;
    
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date deadline;
    
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date lastupdated;
    
    @NotNull
    @Size(max = 256, message = "description need to have only 256 characters")
    private String description;
    
    @DBRef(lazy = false)
    private User creator;
    
    @DBRef(lazy = false)
    private EventStatus status;
    
    public long getId() {
        return id;
    }
    
    public void setId(final long id) {
        this.id = id;
    }
    
    public String getEventid() {
        return eventid;
    }

    public void setEventid(String eventid) {
        this.eventid = eventid;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(final String title) {
        this.title = title;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }
    
    public Date getCreated() {
        return created;
    }
    
    public void setCreatedDate(final Date created) {
        this.created = created;
    }
    
    public Date getDeadline() {
        return deadline;
    }
    
    public void setDeadline(final Date deadline) {
        this.deadline = deadline;
    }
    
    public Date getLastupdated() {
        return lastupdated;
    }
    
    public void setLastupdated(final Date lastupdated) {
        this.lastupdated = lastupdated;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(final String description) {
        this.description = description;
    }
    
    public User getCreator() {
        return creator;
    }
    
    public void setCreator(final User creator) {
        this.creator = creator;
    }
    
    public EventStatus getStatus() {
        return status;
    }
    
    public void setStatus(final EventStatus status) {
        this.status = status;
    }
    
    public Event() {
        this.created = new Date();
        this.lastupdated = new Date();
    }
    
    public Event(final String eventid, final String title, final String description, 
                    final BigDecimal amount, final EventStatus status, final User creator, final Date deadline) {
        this.eventid = eventid;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.status = status;
        this.creator = creator;
        this.deadline = deadline;
        this.created = new Date();
        this.lastupdated = new Date();
    }
    
    public Event(final String eventid, final String title, final String description, 
                    final BigDecimal amount, final EventStatus status, final User creator) {
        this.eventid = eventid;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.status = status;
        this.creator = creator;
        this.created = new Date();
        this.lastupdated = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.lastupdated);
        cal.add(5, 8);
        this.deadline = cal.getTime();
    }
}