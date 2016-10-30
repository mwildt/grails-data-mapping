package org.grails.datastore.gorm.sparql.engine

import org.grails.datastore.mapping.model.PersistentProperty
import org.openrdf.model.IRI
import org.openrdf.model.Literal
import org.openrdf.model.Model
import org.openrdf.model.Statement
import org.openrdf.model.Value
import org.openrdf.model.impl.LinkedHashModel
import org.openrdf.model.impl.TreeModelFactory
import org.openrdf.model.util.RDFCollections
import org.openrdf.model.vocabulary.XMLSchema
import org.openrdf.query.QueryResult
import org.openrdf.query.QueryResults
import org.openrdf.repository.RepositoryConnection
import org.openrdf.repository.RepositoryResult
import org.openrdf.repository.util.Connections

/**
 * Created by mwildt on 24.08.16.
 */
public class SparqlNativePersistentEntity {

    def modelFactory = new TreeModelFactory();

    /**
     * Weil der Zeitpunkt von Modelerstellung und FLush unterschiedliche sein kann,
     * müssen die Delete-Alationen Lazy implementiert sein.
     */
    public static class LazyDeleteAction {
        boolean collection = false;
        IRI subject
        IRI predicate
    }

    def String family;
    def SparqlEntityPersister persister;
    def RepositoryConnection connection;

    def Model model = modelFactory.createEmptyModel()
    def List<LazyDeleteAction> deletes

    def IRI iri

    def version;

    SparqlNativePersistentEntity(SparqlEntityPersister persister){
        this.family = persister.getEntityFamily()
        this.persister = persister
        this.connection = persister.datastore.repository.connection
    }

    def SparqlNativePersistentEntity withIRI(IRI iri){
        this.iri = iri;
        return this;
    }

    def init(IRI iri){
        this.iri = iri;
        this.model = modelFactory.createEmptyModel()
        this.deletes = new LinkedList<LazyDeleteAction>() // QueryResults.asModel(statements);
    }

    def getIdentifier(){
        Value value = getOne(persister.getIRI('identifier'));
        if(value == null){
            return null;
        }
        if(IRI.isAssignableFrom(value.class)){
            return value
        } else {
            return convert(value);
        }
    }

    def setProperty(IRI predicate, Object value) {
        if(null == value){
            return;
        } else if(Collection.isAssignableFrom(value.class)) {
            this.addCollection(predicate, value.collect { item ->
                persister.getLiteral(item)
            });
        } else if(IRI.isAssignableFrom(value.class)){
            this.add(predicate, value);
        } else { // Literal value
            Value object = persister.getLiteral(value)
            this.add(predicate, object);
        }
    }

    def createEmptyCollection(Class<?> type){
        if(Map.isAssignableFrom(type)){
            return new HashMap<Serializable, Value>()
        } else if(List.isAssignableFrom(type)){
            return new ArrayList<Value>()
        } else if(SortedSet.isAssignableFrom(type)){
            return new TreeSet<Value>()
        } else if(Set.isAssignableFrom(type)){
            return new HashSet<Value>()
        } else {
            return []
        }
    }

    def getValue(String propertyKey){
        println "get value for property ${propertyKey}"
        def res = null;
        PersistentProperty persistentProperty = persister.persistentEntity.getPropertyByName(propertyKey);
        IRI predicate = persister.getPredicateForProperty(persistentProperty);

        if(Collection.isAssignableFrom(persistentProperty.type)){
            def head = getOne(predicate);
            if(head){
                println "head: $head"
                Model rdfList = Connections.getRDFCollection(connection, head, new LinkedHashModel());
                res = RDFCollections.asValues(rdfList, head, new ArrayList<Value>()).collect{ value ->
                    convert(value)
                }
            } else {
                res = createEmptyCollection(persistentProperty.type)
            }
        } else {
            Value item = getOne(predicate);
            if(item) {
                res = convert(item);
            }
        }
        return res;
    }



    def toValue(Object value){
        if(null == value){
            return null
        } else if(Value.isAssignableFrom(value.class)){
            return value;
        } else {
            persister.getLiteral(value)
        }
    }


    def static convert(Value value){
        if(IRI.isAssignableFrom(value.class)){
            return value;
        } else  if(Literal.isAssignableFrom(value.class)){
            Literal literal = value as Literal
            switch(literal.datatype){
                case XMLSchema.DATETIME : return literal.calendarValue().toGregorianCalendar().getTime()
                case XMLSchema.STRING : return literal.stringValue()
                case XMLSchema.LONG: return literal.longValue()
                case XMLSchema.BOOLEAN : return literal.booleanValue()
                case XMLSchema.INT :
                case XMLSchema.INTEGER : return literal.integerValue()
                case XMLSchema.DECIMAL : return literal.decimalValue()
                case XMLSchema.SHORT : return literal.shortValue()
                case XMLSchema.FLOAT : return literal.floatValue()
                case XMLSchema.DOUBLE: return literal.doubleValue()
                case XMLSchema.BYTE: return literal.byteValue()
            }
        }
        return value.stringValue()
    }

    private addCollection(IRI predicate, Collection values){
        addCollection(this.iri, predicate, values);
    }

    private addCollection(IRI iri, IRI predicate, Collection values){
        if(values) {
            def head = persister.datastore.repository.valueFactory.createBNode()
            def list = RDFCollections.asRDF(values, head, new LinkedHashModel())
            model.addAll(list)
            model.add(iri, predicate, head)
        }
    }

    def updateCollection(IRI predicate, Collection values){
        updateCollection(this.iri, predicate, values);
    }

    def updateCollection(IRI iri, IRI predicate, Collection values){
        removeCollection(iri, predicate);
        addCollection(iri, predicate, values);
    }

    def removeCollection(IRI iri, IRI predicate){
        deletes << new LazyDeleteAction(subject: iri, predicate: predicate, collection: true);
    }

    def update(IRI iri, IRI predicate, value){
        update(iri, predicate, toValue(value))
    }

    def remove(IRI iri, IRI predicate){
        deletes << new LazyDeleteAction(subject: iri, predicate: predicate);
    }

    def update(IRI iri, IRI predicate, Value value){
        this.remove(iri, predicate)
        this.model.add(iri, predicate, value);
    }

    private void addAll(IRI predicate, List<Value> values){
        values.each { value ->
            add(predicate, value)
        }
    }

    private void add(IRI predicate, Value value){
        this.model.add(this.iri, predicate, value)
    }

    public Value getOne(IRI predicate){
        Model result = this.model.filter(iri, predicate, null as Value)
        result.isEmpty() ? null: result.first().object
    }

    public List<Value> getAll(IRI predicate){
        this.model.filter(iri, predicate, null as Value).objects();
    }

}

