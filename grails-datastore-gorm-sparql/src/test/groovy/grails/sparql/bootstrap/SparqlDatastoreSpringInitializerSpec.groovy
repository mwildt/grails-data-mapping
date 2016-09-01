package grails.sparql.bootstrap

import model.Skill
import spock.lang.Specification

/**
 * Created by mwildt on 15.08.16.
 */
class SparqlDatastoreSpringInitializerSpec extends Specification {

    void "Test that SparqlDatastoreSpringInitializer can setup GORM for Sparql from scratch"() {
        when:"the initializer used to setup GORM for Sparql"
        def initializer = new SparqlDatastoreSpringInitializer(Skill)
        def applicationContext = initializer.configure()

        then:"GORM for Sparql is initialized correctly"
        Skill.count() == 0

    }

//    void "Test specify mongo database name settings"() {
//        when:"the initializer used to setup GORM for Sparql"
//        def initializer = new SparqlDatastoreSpringInitializer(['grails.sparql.re':'foo'],Person)
//        def applicationContext = initializer.configure()
//        def mongoDatastore = applicationContext.getBean(MongoDatastore)
//
//        then:"GORM for Sparql is initialized correctly"
//        mongoDatastore.getDefaultDatabase() == 'foo'
//
//    }
    
}
