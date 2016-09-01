package org.grails.datastore.gorm.sparql.engine

import org.openrdf.model.IRI
import org.openrdf.model.Value

/**
 * Created by mwildt on 24.08.16.
 */
public  class SparqlNativePersistentEntityProperty {
    IRI predicate
    Value value
    List<Value> values

    public static SparqlNativePersistentEntityProperty withData(IRI name, Value value){
        return new SparqlNativePersistentEntityProperty (
                predicate: name, value:value
        )
    }

    public static SparqlNativePersistentEntityProperty withData(IRI name, Collection<Value> values){
        return new SparqlNativePersistentEntityProperty (
                predicate: name, values:values
        )
    }

    public String toString(){
        "SparqlNativePersistentEntityProperty: $predicate = $value"
    }
}
