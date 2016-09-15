package org.grails.datastore.gorm.sparql

import org.grails.datastore.gorm.sparql.mapping.config.RDFEntity
import org.grails.datastore.mapping.model.AbstractPersistentEntity
import org.grails.datastore.mapping.model.MappingContext

/**
 *
 * Holds Information about a Persistent Entity
 */
class SparqlPersistentEntity extends AbstractPersistentEntity<RDFEntity>{

    SparqlPersistentEntity(Class javaClass, MappingContext context) {
        super(javaClass, context)
    }

    SparqlPersistentEntity(Class javaClass, MappingContext context, boolean external) {
        super(javaClass, context)
        this.external = external;
    }


}
