package org.grails.datastore.gorm.sparql.mapping.config

import org.grails.datastore.mapping.config.Property
import org.openrdf.model.IRI

/**
 * Created by mwildt on 30.06.16.
 */
class SparqlMappingProperty extends Property {


    /*
     * the predicate used to specify the semantic type of the property
     */
    IRI predicate = null;

}
