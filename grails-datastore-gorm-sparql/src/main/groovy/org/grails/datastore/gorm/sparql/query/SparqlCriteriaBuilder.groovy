package org.grails.datastore.gorm.sparql.query

import grails.gorm.CriteriaBuilder
import org.grails.datastore.mapping.core.Session
import org.grails.datastore.mapping.query.Query

/**
 * Created by mwildt on 30.10.16.
 */
class SparqlCriteriaBuilder extends CriteriaBuilder {

    SparqlCriteriaBuilder(Class targetClass, Session session) {
        super(targetClass, session)
    }

    SparqlCriteriaBuilder(
            Class targetClass,
            Session session,
            Query query) {
        super(targetClass, session, query)
    }


}
