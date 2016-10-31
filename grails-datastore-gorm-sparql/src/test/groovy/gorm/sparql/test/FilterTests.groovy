package gorm.sparql.test

import grails.gorm.tests.GormDatastoreSpec
import gorm.sparql.model.Skill

/**
 * Created by mwildt on 30.06.16.
 */
class FilterTests extends GormDatastoreSpec{

    List getDomainClasses() {
        [Skill]
    }

    def setup(){
        def s1 = new Skill(name :"S1", approved: true).save()
        def s2 = new Skill(name :"S2", approved: true).save()
        def s3 = new Skill(name :"S3", approved: true).save()
        def s4 = new Skill(name :"S4").save()
        def s5 = new Skill(name :"S5").save()
        def s6 = new Skill(name :"S6").save()
    }

    def "query equals"(){
        when:
            def s = Skill.findAllByName("S1")
        then:
            s.size() == 1
            s.first().name == "S1"
    }

    def "query not equals"(){
        given:

        when:
        def s = Skill.findAllByNameNotEqual("S1")
        then:
        s.size() == 5
        s*.name == (2..6).collect{"S$it"}
    }

    def "query InList"(){
        given:

        when:
        def s = Skill.findAllByNameInList(["S1", "S2", "S6"])
        then:
        s*.name == [1,2,6].collect{"S$it"}
    }

    def "query In List where"(){
        given:

        when:
        def s = Skill.where {
            inList( "name", ["S1", "S2", "S6"])
        }.list()
        then:
        s*.name == [1,2,6].collect{"S$it"}
    }

    def "query Not In List where"(){
        given:

        when:
        def s = Skill.findAllByNameNotInList(["S1", "S2", "S6"])
        then:
        s*.name == [1,2,6].collect{"S$it"}
    }

    def "query Not In List criteria"(){
        when:
        def s = Skill.withCriteria {
            not {
                inList( "name", ["S1", "S2", "S6"])
            }
        }
        then:
        s*.name == [3,4,5].collect{"S$it"}
    }

    def "query Not In List criteria with and"(){
        when:
        def s = Skill.withCriteria {
            not {
                inList( "name", ["S1", "S2", "S6"])
                eq("name", "S2")
            }
        }
        then:
        s*.name == ((1..6) - [2]).collect{"S$it"}
    }

    def "query and diff fields"(){
        expect:
        def s = Skill.withCriteria {
            not {
                inList( "name", ["S1", "S2", "S6"])
                eq("name", "S2")
            }
        }*.name == ["S2"]
    }

    def "query or diff fields"(){
        expect:
        def s = Skill.withCriteria {
            or {
                eq("name", "S6")
                eq("approved", true)
            }
        }*.name == [1,2,3,6].collect{"S$it"}
    }

}
