package gorm.sparql.test

import grails.gorm.tests.GormDatastoreSpec
import gorm.sparql.model.WithListAttribute

/**
 * Created by mwildt on 30.06.16.
 */
class ListQuerySpec extends GormDatastoreSpec{

    List getDomainClasses() {
        [WithListAttribute]
    }

    def "test find only triples with the correct subject type"(){
        given:
            new WithListAttribute(labels: ["A", "B"]).save(flush:true)
            session.clear()
        when:
            def res = WithListAttribute.get(1);
        then:
            res.labels == ["A", "B"]
            res.version == 0
        when:
            res.labels.add("C")
            res.save(flush:true);
            session.clear()
        and:
           def res2 = WithListAttribute.get(1);
        then:
            res2.version == 1
            res2.labels == ["A", "B", "C"]
    }


}

