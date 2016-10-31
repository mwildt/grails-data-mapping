package gorm.sparql.test

import grails.gorm.tests.GormDatastoreSpec
import gorm.sparql.model.ListModel
import gorm.sparql.model.Skill

/**
 * Created by mwildt on 30.06.16.
 */
class BasicQuerySpec extends GormDatastoreSpec{

    List getDomainClasses() {
        [Skill, ListModel]
    }

    def "test find only triples with the correct subject type"(){
        given:
            new Skill(name: "Name1").save();
            new ListModel(items: ["A", "B"]).save();
        when:
            def s = Skill.findByName("Name1");
        then:
            s.name == "Name1"
    }

    def "test find multiple an correct triples with the correct subject type"(){
        given:
        new Skill(name: "Name1").save();
        new Skill(name: "Name2").save();
        new Skill(name: "Name2").save();
        new Skill(name: "Name3").save();
        new Skill(name: "Name4").save();
        new ListModel(items: ["A", "B"]).save();
        when:
        def s = Skill.findAllByName("Name2");
        then:
        s.size() == 2
        s*.name == ["Name2", "Name2"]
    }

    def "test find multiple an correct triples with and "(){
        given:
        new Skill(name: "Name1").save();
        new Skill(name: "Name2", approved: true).save();
        def toFind = new Skill(name: "Name2", approved: false).save(flush:true);
        when:
        def s = Skill.findAllByNameAndApproved("Name2", false);
        then:
        s.size() == 1
        !s.first().approved
        s.first().name == "Name2"
        s.first().id == toFind.id
    }

    def "test find by single property with dynamic Finder"(){
        given:
            new Skill(name: "Name1").save();
        when:
            def s = Skill.findByName("Name1");
        then:
            s
    }

    def "test find by single property with query"(){
        given:
            new Skill(name: "Name1").save();
        when:
            def query = Skill.where {
                name == "Name1"
            }
            def s = query.find()
        then:
            s
    }

}
