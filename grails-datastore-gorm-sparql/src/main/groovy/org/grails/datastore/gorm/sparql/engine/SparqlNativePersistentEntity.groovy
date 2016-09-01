package org.grails.datastore.gorm.sparql.engine

import org.grails.datastore.mapping.model.PersistentProperty
import org.openrdf.model.IRI
import org.openrdf.model.Literal
import org.openrdf.model.Model
import org.openrdf.model.Value
import org.openrdf.model.impl.LinkedHashModel
import org.openrdf.model.util.RDFCollections
import org.openrdf.model.vocabulary.XMLSchema
import org.openrdf.repository.util.Connections

/**
 * Created by mwildt on 24.08.16.
 */
public class SparqlNativePersistentEntity {

    private LinkedList<SparqlNativePersistentEntityProperty> data = new LinkedList<SparqlNativePersistentEntityProperty>();

    def String family;
    def SparqlEntityPersister persister;
    def Model model

    SparqlNativePersistentEntity(SparqlEntityPersister persister){
        this.family = persister.getEntityFamily()
        this.persister = persister
    }

    def getIdentifier(){
        SparqlNativePersistentEntityProperty prop = getOne(persister.getIRI('identifier'));
        if(IRI.isAssignableFrom(prop.value.class)){
            return prop.value
        } else {
            return prop.value.stringValue();
        }
    }

//    def setProperty(IRI predicate, Collection values){
//        this.addAll(predicate, values)
//    }

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
        def res = null;
        PersistentProperty persistentProperty = persister.persistentEntity.getPropertyByName(propertyKey);
        IRI predicate = persister.getPredicateForProperty(persistentProperty);

        if(Collection.isAssignableFrom(persistentProperty.type)){
            def head = getOne(predicate);
            if(head){
                Model rdfList = Connections.getRDFCollection(persister.datastore.repository.connection, head.value, new LinkedHashModel());
                res = RDFCollections.asValues(rdfList, head.value, new ArrayList<Value>()).collect{ value ->
                    convert(value)
                }
            } else {
                res = createEmptyCollection(persistentProperty.type)
            }
        } else {
            SparqlNativePersistentEntityProperty item = getOne(predicate);
            if(item) {
                res = convert(item.value)
            }
        }
        return res;
    }

    def convert(Value value){
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
        this.data.add(SparqlNativePersistentEntityProperty.withData(predicate, values))
    }

    private void addAll(IRI predicate, Value ... values){
        values.each { value ->
            add(predicate, value)
        }
    }

    private void addAll(IRI predicate, List<Value> values){
        values.each { value ->
            add(predicate, value)
        }
    }

    private void add(IRI predicate, Value value){
        this.data.add(SparqlNativePersistentEntityProperty.withData(predicate, value))
    }

    public SparqlNativePersistentEntityProperty getOne(IRI predicate){
        this.data.find { SparqlNativePersistentEntityProperty property ->
            property.predicate == predicate
        }
    }

    public List<SparqlNativePersistentEntityProperty>  getAll(){
        return this.data;
    }

    public List<SparqlNativePersistentEntityProperty> getAll(IRI predicate){
        this.data.findAll { SparqlNativePersistentEntityProperty property ->
            property.predicate == predicate
        }
    }

}

