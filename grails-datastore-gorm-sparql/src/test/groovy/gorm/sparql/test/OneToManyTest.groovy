package gorm.sparql.test

import grails.gorm.tests.GormDatastoreSpec
import gorm.sparql.model.Skill

/**
 * Created by mwildt on 01.11.16.
 */
class OneToManyTest extends GormDatastoreSpec {

    List getDomainClasses() {
        [Skill]
    }

    def "when a Skill with multiple implications is saved all references are stored correctly" (){
        given:
        Skill groovy = new Skill(name: "groovy").save(flush:true)
        Skill spring = new Skill(name: "spring").save(flush:true)
        Skill grails = new Skill(name: "grails", implicits: [groovy, spring]).save(flush:true)
        session.clear();
        when:
        def found = Skill.withCriteria {
            find "implicits", {
                eq("name", "groovy");
            }
        }
        then:
        found*.name == ["grails"]
        found.first().implicits*.name.sort() == ["groovy", "spring"].sort()
    }


    def "recursive queries over List Elements are evaluated correctly" (){
        given:
        Skill java = new Skill(name: "java").save(flush:true)
        Skill groovy = new Skill(name: "groovy").save(flush:true)
        Skill spring = new Skill(name: "spring", implicits: [java]).save(flush:true)
        Skill grails = new Skill(name: "grails", implicits: [groovy, spring]).save(flush:true)
        session.clear();
        when:
        def found = Skill.withCriteria {
            find "implicits+", {
                eq("name", "java");
            }
        }
        then:
        found*.name.sort() == ["spring", "grails"].sort()
    }

    def "find all skills implicated by grails" (){
        given:
        Skill java = new Skill(name: "java").save(flush:true)
        Skill groovy = new Skill(name: "groovy").save(flush:true)
        Skill spring = new Skill(name: "spring", implicits: [java]).save(flush:true)
        Skill grails = new Skill(name: "grails", implicits: [groovy, spring]).save(flush:true)
        session.clear();
        when:
        def found = Skill.withCriteria {
            find "-implicits+", {
                eq("name", "grails");
            }
        }
        then:
        found*.name.sort() == ["spring", "groovy", "java"].sort()
    }

}
