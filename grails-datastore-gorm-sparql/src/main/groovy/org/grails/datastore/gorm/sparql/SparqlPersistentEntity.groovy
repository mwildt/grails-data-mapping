package org.grails.datastore.gorm.sparql

import org.grails.datastore.gorm.sparql.mapping.config.RDFEntity
import org.grails.datastore.mapping.model.AbstractPersistentEntity
import org.grails.datastore.mapping.model.MappingContext

/**
 * Created by didier on 12.05.16.
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
