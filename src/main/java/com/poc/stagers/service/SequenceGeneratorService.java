package com.poc.stagers.service;

import java.util.Objects;

import com.poc.stagers.models.DbSequence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.stereotype.Service;

@Service
public class SequenceGeneratorService
{
    private MongoOperations mongoOperations;
    
    @Autowired
    public SequenceGeneratorService(final MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }
    
    public long generateSequence(final String seqName) {
        DbSequence counter = (DbSequence) mongoOperations.findAndModify(
                                Query.query((CriteriaDefinition) Criteria.where("_id").is((Object) seqName)),
                                (UpdateDefinition) new Update().inc("seq", (Number) 1),
                                FindAndModifyOptions.options().returnNew(true).upsert(true), (Class<?>)DbSequence.class);
        
        return Objects.isNull(counter) ? 1L : counter.getSeq();
    }
}