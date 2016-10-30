package org.grails.datastore.gorm.sparql

import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.gorm.sparql.engine.SparqlDatastore
import org.springframework.transaction.PlatformTransactionManager

/**
 * Created by mwildt on 02.07.16.
 */
class SparqlGormEnhancer extends GormEnhancer {

    public SparqlGormEnhancer(SparqlDatastore datastore) {
        super(datastore)
    }

    public SparqlGormEnhancer(SparqlDatastore datastore, PlatformTransactionManager transactionManager) {
        super(datastore, transactionManager)
    }

    @CompileStatic
    protected <D> GormStaticApi<D> getStaticApi(Class<D> cls) {
        new SparqlGormStaticApi<D>(cls, datastore as SparqlDatastore, getFinders(), transactionManager)
    }



}
