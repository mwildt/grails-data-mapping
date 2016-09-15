package org.grails.datastore.gorm

import org.grails.datastore.gorm.events.AutoTimestampEventListener
import org.grails.datastore.gorm.events.DomainEventListener
import org.grails.datastore.gorm.sparql.SparqlGormEnhancer
import org.grails.datastore.gorm.sparql.engine.SparqlDatastore
import org.grails.datastore.mapping.core.Session
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.transactions.DatastoreTransactionManager
import org.openrdf.model.IRI
import org.openrdf.repository.Repository
import org.openrdf.repository.http.HTTPRepository
import org.springframework.context.support.GenericApplicationContext

/**
 * Created by didier on 09.05.16.
 */
class Setup {

    static SparqlDatastore datastore;

    static destroy() {
    }

    static Session setup(classes) {
//        Repository repository = new SailRepository(new MemoryStore())

        String rdf4jServer = "http://localhost:8081/rdf4j-server/";
        String repositoryID = "test";
        Repository repository = new HTTPRepository(rdf4jServer, repositoryID);

        repository.connection.remove((IRI) null, (IRI) null, null)

        repository.initialize();

        def ctx = new GenericApplicationContext()

        ctx.refresh()
        datastore = new SparqlDatastore(ctx, repository)

        for (cls in classes) {
            def mappingContext = datastore.mappingContext
            mappingContext.addPersistentEntity(cls)
        }

        def enhancer = new SparqlGormEnhancer(datastore, new DatastoreTransactionManager(datastore: datastore))
        enhancer.enhance()

        datastore.mappingContext.addMappingContextListener({ e -> enhancer.enhance e } as MappingContext.Listener)
        datastore.applicationContext.addApplicationListener new DomainEventListener(datastore)
        datastore.applicationContext.addApplicationListener new AutoTimestampEventListener(datastore)

        datastore.createSession()
    }
}
