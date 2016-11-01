package org.grails.datastore.gorm.sparql.query

import org.grails.datastore.gorm.sparql.engine.SparqlEntityPersister
import org.grails.datastore.gorm.sparql.engine.SparqlNativePersistentEntity
import org.grails.datastore.gorm.sparql.mapping.config.RDFEntity
import org.grails.datastore.mapping.core.Session
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.ToMany
import org.grails.datastore.mapping.model.types.ToOne
import org.grails.datastore.mapping.query.AssociationQuery
import org.grails.datastore.mapping.query.Query
import org.grails.datastore.mapping.query.api.QueryArgumentsAware
import org.openrdf.model.IRI
import org.openrdf.model.Value
import org.openrdf.query.BindingSet
import org.openrdf.query.QueryLanguage
import org.openrdf.query.TupleQuery
import org.openrdf.query.TupleQueryResult

/**
 * Created by mwildt on 30.06.16.
 */
class SparqlQuery extends Query implements QueryArgumentsAware {

    SparqlEntityPersister persister = null

    def static labelCounter = 0;

    def static uniqueLabel(String label){
        return "?${label}_${++labelCounter}"
    }

    protected SparqlQuery(Session session, PersistentEntity entity, SparqlEntityPersister persister) {
        super(session, entity)
        this.persister = persister
    }

    public QueryTree.QueryTreeNode get(PersistentEntity entity, Query.Conjunction conjunction, subject = "?s"){
        new QueryTree.AndQueryTreeNode(
                inner: conjunction.criteria.collect { Query.Criterion criterion ->
                    get(entity, criterion, subject)
                }
        );
    }

    public QueryTree.QueryTreeNode get(PersistentEntity entity, AssociationQuery associationQuery, subject = "?s"){
        PersistentProperty property = entity.getPropertyByName(associationQuery.association.name)
        IRI predicate = persister.getPredicateForProperty(property)
        String object = uniqueLabel(associationQuery.association.name)
        new QueryTree.TripleOnlyQueryTreeRootNode(
            inner: get(associationQuery.association.associatedEntity, associationQuery.criteria, object),
            triples: [
                new Triples.Triple(subject, predicate, object),
            ]
        )
    }

    public QueryTree.QueryTreeNode get(PersistentEntity entity, SparqlCriteriaBuilder.SparqlAssociationQuery associationQuery, subject = "?s"){
        PersistentProperty property = entity.getPropertyByName(associationQuery.association.name)
        String predicate;
        IRI predicateIRI = persister.getPredicateForProperty(property)
        if(ToOne.isInstance(property)){
            predicate = "<${predicateIRI}>${associationQuery.additionalOperator?:''}"
            String object = uniqueLabel(associationQuery.association.name)
            new QueryTree.TripleOnlyQueryTreeRootNode(
                    inner: get(associationQuery.association.associatedEntity, associationQuery.criteria, object),
                    triples: [
                            new Triples.Triple().withSubject(subject)
                                    .withPredicate(predicate)
                                    .withObject(object)
                    ]
            )
        } else if(ToMany.isInstance(property)){
            predicate = "<${predicateIRI}>/rdf:rest*/rdf:first"
            if(associationQuery.additionalOperator){
                predicate = "($predicate)${associationQuery.additionalOperator}"
            }
            String object = uniqueLabel(associationQuery.association.name)
            new QueryTree.TripleOnlyQueryTreeRootNode(
                    inner: get(associationQuery.association.associatedEntity, associationQuery.criteria, object),
                    triples: [
                            new Triples.Triple().withSubject(subject)
                                    .withPredicate(predicate)
                                    .withObject(object)
                    ]
            )
        }
    }


    public QueryTree.QueryTreeNode get(PersistentEntity entity, Query.Criterion criterion, subject = "?s"){
        throw new RuntimeException("Criterion $criterion is not supported");
    }

    public QueryTree.QueryTreeNode get(PersistentEntity entity, Query.Equals equals, subject = "?s"){
        PersistentProperty property = entity.getPropertyByName(equals.name)
        String label = uniqueLabel(equals.getProperty())
        IRI predicate = persister.getPredicateForProperty(property)
        Value object = persister.getLiteral(equals.value)
        new QueryTree.SimpleExpressionQueryTreeNode(
            expression: new Triples.SparqlSimpleFilter("$label = ${Triples.printValue(object)}"),
            triple: new Triples.Triple(subject, predicate, label)
        );
    }

    public QueryTree.QueryTreeNode get(PersistentEntity entity, Query.NotEquals equals, subject = "?s"){
        PersistentProperty property = entity.getPropertyByName(equals.name)
        String label = uniqueLabel(equals.getProperty())
        IRI predicate = persister.getPredicateForProperty(property)
        Value object = persister.getLiteral(equals.value)
        new QueryTree.SimpleExpressionQueryTreeNode(
                expression: new Triples.SparqlSimpleFilter("$label != ${Triples.printValue(object)}"),
                triple: new Triples.Triple(subject, predicate, label)
        );
    }

    public QueryTree.QueryTreeNode get(PersistentEntity entity, Query.In criteria, subject = "?s"){
        PersistentProperty property = entity.getPropertyByName(criteria.name)
        String label = uniqueLabel(criteria.getProperty())
        IRI predicate = persister.getPredicateForProperty(property)
        def objects = criteria.values.collect{
            persister.getLiteral(it)
        }.join(", ")
        new QueryTree.SimpleExpressionQueryTreeNode(
            expression: new Triples.SparqlSimpleFilter("$label IN ($objects)"),
            triple: new Triples.Triple(subject, predicate, label)
        );
    }

    public QueryTree.NotQueryTreeNode get(PersistentEntity entity, Query.Negation negation, subject = "?s"){
        def ands = negation.criteria.collect { Query.Criterion criterion ->
            get(entity, criterion, subject)
        }
        new QueryTree.NotQueryTreeNode(
                inner:  new QueryTree.AndQueryTreeNode(inner: ands)
        );
    }

    public QueryTree.OrQueryTreeNode get(PersistentEntity entity, Query.Disjunction disjunction, subject = "?s"){
        new QueryTree.OrQueryTreeNode (
            inner: disjunction.criteria.collect { Query.Criterion criterion ->
                get(entity, criterion, subject)
            }
        );
    }

    def printQuery(Query.Criterion criterion, int lvl = 0){
        String tab = "   " * lvl;
        println "$tab -- ${criterion.getClass().getSimpleName()}"
        if(criterion instanceof Query.Junction){
            (criterion as Query.Junction).criteria.each{ it ->
                printQuery it, (lvl + 1)
            }
        } else if(criterion instanceof AssociationQuery){
            (criterion as AssociationQuery).criteria.each{ it ->
                printQuery it, (lvl + 1)
            }
        }
    }

    @Override
    protected List executeQuery(PersistentEntity entity, Query.Junction criteria) {
        println " >> executeQuery on entity ${entity.getName()}"
        printQuery(criteria);
        RDFEntity mappedForm = persister
                .getMappingContext().getMappingFactory().createMappedForm(entity);
        QueryTree.QueryTreeNode treeRoot = new QueryTree.QueryTreeRootNode(
            triples: [
                new Triples.Triple(),
                new Triples.Triple().withPredicate(mappedForm.getPredicate()).withObject(persister.getFamilyIRI())
            ],
            inner: get(entity, criteria)
        )

        String queryString = QueryTree.getQuery(treeRoot)

        println queryString

        def con = persister.getDatastore().getRepository().connection;

        TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

        TupleQueryResult result = tupleQuery.evaluate()
        Map map = new LinkedHashMap<IRI, SparqlNativePersistentEntity>()
                .withDefault {IRI iri ->
                    new SparqlNativePersistentEntity(persister).withIRI(iri)
                }

        while (result.hasNext()) {  // iterate over the result
            BindingSet bindingSet = result.next();
            IRI subject = bindingSet.getValue("s");
            IRI predicate = bindingSet.getValue("p");
            Value object = bindingSet.getValue("o");
            map.get(subject).add(predicate, object);
        }

        map.entrySet().collect{entry ->
            SparqlNativePersistentEntity nativeEntry = entry.getValue()
            Serializable identifier = nativeEntry.getIdentifier();
            return persister.createObjectFromNativeEntry(entity, identifier, nativeEntry)
        }

    }

    @Override
    void setArguments(Map arguments) {

    }

 }
