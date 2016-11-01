package gorm.sparql.test

import grails.gorm.tests.GormDatastoreSpec
import gorm.sparql.model.ListModel
import gorm.sparql.model.Skill

/**
 * Created by mwildt on 30.06.16.
 */
class RelationalQuerySpec extends GormDatastoreSpec{

    List getDomainClasses() {
        [Skill, ListModel]
    }

    def "query associations with Entity"(){
        given:
            def pl = new Skill(name :"Programming Language").save()
            def jvm = new Skill(name :"JVM Language", parent: pl).save()
            def java = new Skill(name:"Java", parent:  jvm).save()
            def groovy = new Skill(name:"Groovy", parent:  jvm).save()
            def javascript = new Skill(name:"JavaScript", parent:  pl).save()
        when:
            def s = Skill.findAllByParent(jvm);
        then:
            s*.name == ["Java", "Groovy"]
    }

    def "query associations by entities value deep where"(){
        given:
        def pl = new Skill(name :"Programming Language").save()
        def jvm = new Skill(name :"JVM Language", parent: pl).save()
        def java = new Skill(name:"Java", parent:  jvm).save()
        def groovy = new Skill(name:"Groovy", parent:  jvm).save()
        def javascript = new Skill(name:"JavaScript", parent:  pl).save()
        when:
        def s = Skill.where {
            parent {
                parent {
                    name == "Programming Language"
                }
            }
        }
        then:
        s.collect{
            it.name
        } == ["Java", "Groovy"]
    }

    def "query associations by entities value deep criteria"(){
        given:
        def pl = new Skill(name :"Programming Language").save()
        def jvm = new Skill(name :"JVM Language", parent: pl).save()
        def java = new Skill(name:"Java", parent:  jvm).save()
        def groovy = new Skill(name:"Groovy", parent:  jvm).save()
        def javascript = new Skill(name:"JavaScript", parent:  pl).save()
        when:
        def s = Skill.withCriteria {
            parent {
                parent {
                    eq("name", "JVM Language")
                }
            }
        }
        then:
        s.collect{
            it.name
        } == ["Java", "Groovy"]
    }

    def "query associations by entities value variable deep criteria"(){
        given:
        def fw = new Skill(name :"Framework").save()
        def pl = new Skill(name :"Programming Language").save()
        def jvm = new Skill(name :"JVM Language", parent: pl).save()
        def java = new Skill(name:"Java", parent:  jvm).save()
        def groovy = new Skill(name:"Groovy", parent:  jvm).save()
        def javascript = new Skill(name:"JavaScript", parent:  pl).save()
        def grails = new Skill(name:"Grails", parent:  fw).save()
        when:
        def s = Skill.withCriteria {
            find "parent+", { ->
                eq("name", "Programming Language")
            }
        }
        then:
        s.collect{
            it.name
        }.sort() == ["Java", "Groovy", "JVM Language", "JavaScript"].sort()
    }

}
