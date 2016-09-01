package test

import model.ListModel
import model.Skill
import org.openrdf.model.impl.SimpleValueFactory
import spock.lang.Ignore

/**
 * Created by mwildt on 30.06.16.
 */
class NativeEvaulationSpec extends BaseSpec {

    List getDomainClasses() {
        [Skill, ListModel]
    }

    @Ignore
    def "execute a native query"(){
        given:

        def ps = new Skill(name: "Programmiersrachen").save()
        def oop = new Skill(name: "OOP", parent: ps).save()
        def java = new Skill(name: "Java", parent: oop, isTopLevel: true).save()
        def java7 = new Skill(name: "Java 7", parent: java).save()
        new Skill(name: "Java 1.7_81", parent: java7).save(flush:true)
        when:
        def skills = Skill.sparql().executeMapped("SELECT ?s ?p ?o WHERE {?s ?p ?o. " +
                "?s <http://norris.flavia-it.de/parent>* ?d ." +
                "?d <http://norris.flavia-it.de/isTopLevel> " + SimpleValueFactory.getInstance().createLiteral(true) + " ." +
                "}")
        then:
        skills*.name == ["Java", "Java 7", "Java 1.7_81"]

    }
}
