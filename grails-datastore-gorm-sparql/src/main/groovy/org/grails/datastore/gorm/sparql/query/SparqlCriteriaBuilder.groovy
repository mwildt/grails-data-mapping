package org.grails.datastore.gorm.sparql.query

import grails.gorm.CriteriaBuilder
import org.grails.datastore.mapping.core.Session
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.query.AssociationQuery
import org.grails.datastore.mapping.query.Query
import org.grails.datastore.mapping.query.api.Criteria
import org.springframework.dao.InvalidDataAccessResourceUsageException

/**
 * Created by mwildt on 30.10.16.
 */
class SparqlCriteriaBuilder extends CriteriaBuilderAdapter {

    private List<Query.Junction> logicalExpressionStack = new ArrayList<Query.Junction>();

    SparqlCriteriaBuilder(Class targetClass, Session session) {
        super(targetClass, session)
    }

    SparqlCriteriaBuilder(Class targetClass, Session session, Query query) {
        super(targetClass, session, query)
    }

    public static class SparqlAssociationQuery extends AssociationQuery {
        String additionalOperator = null;

        protected SparqlAssociationQuery(Session session, PersistentEntity entity, Association association) {
            super(session, entity, association);
        }

    }

    public createQuery(propertyPath){
        String associationName;
        String additional;
        if(propertyPath.endsWith('+')){
            associationName = propertyPath.substring(0, propertyPath.length() - 1);
            additional = '+';
        } else if(propertyPath.endsWith('*')){
            associationName = propertyPath.substring(0, propertyPath.length() - 1);
            additional = '*';
        } else {
            associationName = propertyPath
            additional = null
        }
        final PersistentProperty property = query.entity.getPropertyByName(associationName);

        if (!Association.class.isInstance(property)) {
            throw new InvalidDataAccessResourceUsageException("Cannot query association [${associationName}] of class [${query.entity}]. The specified property is not an association.")
        }

        Association association = property as Association
        final PersistentEntity associatedEntity = association.getAssociatedEntity()

        SparqlAssociationQuery associationQuery = new SparqlAssociationQuery(query.session, associatedEntity, association)
        associationQuery.additionalOperator = additional;
        return associationQuery;
    }

    public SparqlAssociationQuery find(String propertyPath, Closure closure){
        Query previousQuery = query;
        PersistentEntity previousEntity = persistentEntity;
        List<Query.Junction> previousLogicalExpressionStack = logicalExpressionStack;
        try {
            SparqlAssociationQuery associationQuery = createQuery(propertyPath);
            if (associationQuery instanceof SparqlAssociationQuery) {
                previousQuery.add((Query.Criterion) associationQuery);
            }
            query = associationQuery as Query.Criterion;
            persistentEntity = associationQuery.getEntity();
            logicalExpressionStack = new ArrayList<Query.Junction>();
            if(closure instanceof Closure){
                invokeClosureNode(closure);
            }
            return query;
        }
        finally {
            logicalExpressionStack = previousLogicalExpressionStack;
            persistentEntity = previousEntity;
            query = previousQuery;
        }

    }

}

