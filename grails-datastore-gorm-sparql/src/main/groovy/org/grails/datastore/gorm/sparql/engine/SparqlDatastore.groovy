package org.grails.datastore.gorm.sparql.engine

import org.grails.datastore.mapping.core.AbstractDatastore;
import org.grails.datastore.mapping.core.Session
import org.openrdf.repository.Repository
import org.springframework.context.ApplicationContext
import org.springframework.core.env.PropertyResolver;

/**
 * Created by didier on 03.05.16.
 */
public class SparqlDatastore extends AbstractDatastore {

    private Repository repository;

    public SparqlDatastore(SparqlMappingContext mappingContext, ApplicationContext ctx, Repository repository) {
        super(mappingContext, ctx.getEnvironment(), ctx);
        this.repository = repository
    }

    public SparqlDatastore(ApplicationContext ctx, Repository repository) {
        this(new SparqlMappingContext(), ctx, repository);
    }

    /**
     * Creates the native session
     *
     * @param connectionDetails The session details
     * @return The session object
     */
    @Override
    protected Session createSession(PropertyResolver connectionDetails) {
        return new SparqlSession(this, this.getMappingContext(), this.getApplicationEventPublisher());
    }

    Repository getRepository() {
        return repository
    }
}
