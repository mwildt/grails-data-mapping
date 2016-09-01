package org.grails.datastore.gorm.sparql.mapping.config;

import org.grails.datastore.mapping.config.AbstractGormMappingFactory
import org.grails.datastore.mapping.config.Property
import org.grails.datastore.gorm.sparql.entity.SparqlEntity
import org.grails.datastore.mapping.config.groovy.MappingConfigurationBuilder
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.config.GormProperties
import org.grails.datastore.mapping.reflect.ClassPropertyFetcher
import org.springframework.beans.BeanUtils;

/**
 * Created by didier on 03.05.16.
 */
public class SparqlMappingFactory extends AbstractGormMappingFactory<SparqlMappingEntity, SparqlMappingProperty> {

    @Override
    protected Class getPropertyMappedFormType() {
        SparqlMappingProperty
    }

    @Override
    protected Class getEntityMappedFormType() {
        SparqlMappingEntity
    }
}
