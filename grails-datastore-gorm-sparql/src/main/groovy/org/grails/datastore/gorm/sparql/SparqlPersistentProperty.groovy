package org.grails.datastore.gorm.sparql

import org.grails.datastore.mapping.model.AbstractPersistentEntity
import org.grails.datastore.mapping.model.MappingContext
import org.openrdf.model.IRI

/**
 * Created by didier on 10.05.16.
 */
class SparqlPersistentProperty extends AbstractPersistentEntity<IRI>{

    SparqlPersistentProperty(Class javaClass, MappingContext context) {
        super(javaClass, context)
    }
}
