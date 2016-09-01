package org.grails.datastore.gorm.sparql.bean.factory

import groovy.transform.CompileStatic
import org.grails.datastore.gorm.sparql.engine.SparqlDatastore
import org.grails.datastore.gorm.sparql.engine.SparqlMappingContext
import org.openrdf.repository.Repository
import org.springframework.beans.BeansException
import org.springframework.beans.factory.FactoryBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 * Created by mwildt on 15.08.16.
 */
@CompileStatic
class SparqlDatastoreFactoryBean implements FactoryBean<SparqlDatastore>, ApplicationContextAware {

    ApplicationContext applicationContext
    Repository repository
    SparqlMappingContext mappingContext

    @Override
    void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    SparqlDatastore getObject() throws Exception {
       new SparqlDatastore(mappingContext, applicationContext, repository);
    }

    @Override
    Class<?> getObjectType() {
        return SparqlDatastore
    }

    @Override
    boolean isSingleton() {
        return true
    }
}
