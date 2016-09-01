package org.grails.datastore.gorm.sparql.engine

import org.grails.datastore.gorm.sparql.SparqlTransaction
import org.grails.datastore.mapping.core.AbstractSession
import org.grails.datastore.mapping.engine.EntityPersister
import org.grails.datastore.mapping.engine.NonPersistentTypeException
import org.grails.datastore.mapping.engine.Persister;
import org.grails.datastore.mapping.model.MappingContext;
import org.grails.datastore.mapping.model.PersistentEntity
import org.openrdf.repository.Repository
import org.springframework.context.ApplicationEventPublisher;

/**
 * Created by didier on 03.05.16.
 */
class SparqlSession extends AbstractSession<Repository> {

    private Repository repository
    private SparqlDatastore datastore

    SparqlSession(SparqlDatastore datastore, MappingContext mappingContext, ApplicationEventPublisher publisher) {
        super(datastore, mappingContext, publisher)
        this.datastore = datastore
        this.repository = datastore.repository
    }

    @Override
    protected Persister createPersister(Class cls, MappingContext mappingContext) {
        PersistentEntity entity = mappingContext.getPersistentEntity(cls.getName());
        if (entity == null) {
            return null;
        }
        new SparqlEntityPersister(mappingContext, entity, this, datastore, publisher)
    }

    @Override
    public List retrieveAll(Class type, Iterable keys) {
        EntityPersister persister = (EntityPersister) getPersister(type);
        if (persister == null) {
            throw new NonPersistentTypeException("Cannot retrieve objects with keys [$keys]. The class [${type.getName()}] is not a known persistent type.");
        }
        if(persister instanceof SparqlEntityPersister){
            persister.retrieveAll(keys)
        } else {
            super.retrieve(type, keys)
        }
    }

    @Override
    protected SparqlTransaction beginTransactionInternal() {
        new SparqlTransaction(repository.getConnection())
    }

    /**
     * @return The native interface to the datastore
     */
    @Override
    public Object getNativeInterface() {
        repository
    }
}
