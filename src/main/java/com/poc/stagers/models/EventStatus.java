package com.poc.stagers.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "EVENT_STATUS")
public class EventStatus
{
    @Transient
    public static final String SEQUENCE_NAME = "event_status_sequence";
    
    @Id
    private long id;
    
    @Indexed(unique = true, direction = IndexDirection.DESCENDING, dropDups = true)
    private String status;
    
    public long getId() {
        return id;
    }
    
    public void setId(final long id) {
        this.id = id;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(final String status) {
        this.status = status;
    }
    
    public enum EventStatusEnum {
        DRAFT,
        OPEN,
        HOLD,
        AWARDED,
        CANCELLED,
        CONCLUDED
    }
}