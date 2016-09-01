package org.grails.datastore.gorm.sparql.engine

import org.openrdf.model.IRI
import org.openrdf.model.Value
import org.openrdf.query.BindingSet
import org.openrdf.query.QueryLanguage
import org.openrdf.query.TupleQuery
import org.openrdf.query.TupleQueryResult

/**
 * Created by mwildt on 30.06.16.
 */
class SparqlNativeInterface {

    SparqlEntityPersister persister

    SparqlNativeInterface(SparqlEntityPersister persister){
        this.persister = persister
    }

    List executeMapped(String queryString){

        def con = persister.getDatastore().getRepository().connection;
        TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

        TupleQueryResult result = tupleQuery.evaluate()
        Map map = new LinkedHashMap<IRI, SparqlNativePersistentEntity>()
                .withDefault {new SparqlNativePersistentEntity(persister)}

        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            IRI subject = bindingSet.getValue("s");
            IRI predicate = bindingSet.getValue("p");
            Value object = bindingSet.getValue("o");
            map.get(subject).add(predicate, object);
        }

        map.entrySet().collect{entry ->
            SparqlNativePersistentEntity nativeEntry = entry.getValue()
            Serializable identifier = nativeEntry.getIdentifier();
            return persister.createObjectFromNativeEntry(persister.getPersistentEntity(), identifier, nativeEntry)
        }

    }

}
