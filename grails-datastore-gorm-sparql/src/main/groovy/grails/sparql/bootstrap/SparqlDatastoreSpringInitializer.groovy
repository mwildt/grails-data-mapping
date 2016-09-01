package grails.sparql.bootstrap

import groovy.transform.InheritConstructors
import org.grails.datastore.gorm.bootstrap.AbstractDatastoreInitializer
import org.grails.datastore.gorm.sparql.SparqlGormEnhancer
import org.grails.datastore.gorm.sparql.bean.factory.SparqlDatastoreFactoryBean
import org.grails.datastore.gorm.sparql.bean.factory.SparqlMappingContextFactoryBean
import org.grails.datastore.gorm.sparql.engine.SparqlMappingContext
import org.grails.datastore.gorm.sparql.entity.SparqlEntity
import org.grails.datastore.gorm.support.AbstractDatastorePersistenceContextInterceptor
import org.grails.datastore.gorm.support.DatastorePersistenceContextInterceptor
import org.grails.datastore.mapping.transactions.DatastoreTransactionManager
import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.springframework.beans.factory.support.BeanDefinitionRegistry

/**
 * Created by mwildt on 15.08.16.
 */
@InheritConstructors
public class SparqlDatastoreSpringInitializer extends AbstractDatastoreInitializer {

    @Override
    Closure getBeanDefinitions(BeanDefinitionRegistry beanDefinitionRegistry) {
        return { ->

            // common configuration
            def callable = getCommonConfiguration(beanDefinitionRegistry, "sparql")
            callable.delegate = delegate
            callable.call()

            // repository
            sparql(SailRepository, new MemoryStore()){ bean ->
                bean.initMethod = 'initialize'
            }

            sparqlMappingContext(SparqlMappingContextFactoryBean) {
                grailsApplication = ref('grailsApplication')
                defaultExternal = secondaryDatastore
            }

            // datastore factory
            sparqlDatastore(SparqlDatastoreFactoryBean){
                mappingContext = sparqlMappingContext
                repository = ref('sparql')
            }

            callable = getAdditionalBeansConfiguration(beanDefinitionRegistry, "sparql")
            callable.delegate = delegate
            callable.call()

            sparqlDatastoreTransactionManager(DatastoreTransactionManager){
                datastore = ref('sparqlDatastore')
            }

            // gorm enhancer
            "org.grails.gorm.sparql.internal.GORM_ENHANCER_BEAN-sparql"(SparqlGormEnhancer, ref("sparqlDatastore"), ref('sparqlDatastoreTransactionManager')) { bean ->
                bean.initMethod = 'enhance'
                bean.destroyMethod = 'close'
                bean.lazyInit = false
                includeExternal = !secondaryDatastore
            }

        }
    }

    @Override
    protected Class<AbstractDatastorePersistenceContextInterceptor> getPersistenceInterceptorClass() {
        DatastorePersistenceContextInterceptor
    }
}
