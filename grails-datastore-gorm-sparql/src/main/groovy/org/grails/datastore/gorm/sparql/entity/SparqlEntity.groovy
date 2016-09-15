package org.grails.datastore.gorm.sparql.entity

import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.gorm.sparql.engine.SparqlDatastore
import org.grails.datastore.gorm.sparql.engine.SparqlEntityPersister
import org.grails.datastore.gorm.sparql.engine.SparqlNativeInterface
import org.grails.datastore.mapping.core.AbstractDatastore
import org.openrdf.model.IRI

/**
 * This trait is used to add additional Methods do Domain Classes and Instances
 * @param <D>
 */
@CompileStatic
trait SparqlEntity<D> extends GormEntity<D> {

    def IRI iri;

    public static SparqlNativeInterface sparql(){
        def session = AbstractDatastore.retrieveSession(SparqlDatastore)
        SparqlEntityPersister persister = session.getPersister(this) as SparqlEntityPersister;
        return new SparqlNativeInterface(persister);
    }

    void setIRI(IRI iri){
        this.iri = iri
    }

    def IRI getIRI(){
        return iri
    }

}