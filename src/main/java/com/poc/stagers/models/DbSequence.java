package com.poc.stagers.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "db_sequence")
public class DbSequence
{
    @Id
    private String id;
    private long seq;
    
    public String getId() {
        return id;
    }
    
    public void setId(final String id) {
        this.id = id;
    }
    
    public long getSeq() {
        return seq;
    }
    
    public void setSeq(final long seq) {
        this.seq = seq;
    }
}