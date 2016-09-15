package org.grails.datastore.gorm.sparql.mapping.config;

import org.grails.datastore.mapping.config.AbstractGormMappingFactory
import org.grails.datastore.mapping.model.PersistentEntity

/**
 * Created by didier on 03.05.16.
 */
public class SparqlMappingFactory extends AbstractGormMappingFactory<RDFEntity, RDFProperty> {


    SparqlMappingFactory(){

    }


    @Override
    protected Class getPropertyMappedFormType() {
        RDFProperty
    }

    @Override
    protected Class getEntityMappedFormType() {
        RDFEntity
    }
}
