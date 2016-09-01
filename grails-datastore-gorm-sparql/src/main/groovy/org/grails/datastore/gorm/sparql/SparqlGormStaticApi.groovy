package org.grails.datastore.gorm.sparql

import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.gorm.sparql.engine.SparqlDatastore
import org.springframework.transaction.PlatformTransactionManager

/**
 * Created by mwildt on 02.07.16.
 */
class SparqlGormStaticApi<D> extends GormStaticApi<D> {

    SparqlGormStaticApi(Class persistentClass, SparqlDatastore datastore, List finders) {
        super(persistentClass, datastore, finders)
    }

    SparqlGormStaticApi(Class persistentClass, SparqlDatastore datastore, List finders, PlatformTransactionManager transactionManager) {
        super(persistentClass, datastore, finders, transactionManager)
    }

}
