package org.grails.datastore.gorm.sparql.bean.factory

import org.grails.datastore.gorm.bean.factory.AbstractMappingContextFactoryBean
import org.grails.datastore.gorm.sparql.engine.SparqlMappingContext
import org.grails.datastore.mapping.model.MappingContext

/**
 * Created by mwildt on 18.08.16.
 */
class SparqlMappingContextFactoryBean extends AbstractMappingContextFactoryBean {

    protected MappingContext createMappingContext() {
        new SparqlMappingContext();
    }

}
