package test

import model.ListModel
import model.Skill
import org.openrdf.sail.memory.model.MemIRI
import spock.lang.Unroll

/**
 * Created by mwildt on 28.06.16.
 */
class CrudSpec extends BaseSpec {

    List getDomainClasses() {
        [Skill, ListModel]
    }

    def "a not existing Key leads to null"(){
        expect:
            null == Skill.get(new MemIRI(null, "http://norris.flavia-it.de/model.Skill/", "0") )
    }

    def "after a new entity is saves its id is set"(){
        when:
            def e = new Skill(name: "Java").save(flush:true, failOnError:true)
        then:
            e.id
    }

    @Unroll
    def "test the List impl (#items) is stored and loaded correctly"(){
        when:
            def e = new ListModel(
                    items: items,
                ).save(flush:true, failOnError:true)
        and:
            ListModel r = ListModel.get(e.id).refresh()
        then:
            r.items == items
        where:
        items << [["A"], ["A", "B"], []];
    }

    def "test list reference with null is returned as [] from datastore after persisting"(){
        given:
            def e = new ListModel(
                    items: null,
            ).save(flush:true, failOnError:true)

            session.clear()

        when:
            ListModel r = ListModel.get(e.id).refresh()
        then:
            r.items == []
    }

    def "test the Literal Basis Types are saved"(){
        when:
            def e = new Skill(
                name: "Java",
                approved: true,
                approvedNat: true,
                numA: 123,
                numB: 99
            ).save(flush:true, failOnError:true)
        and:
            Skill r = Skill.get(e.id).refresh()
        then:
            r.approvedNat
            r.approved
            r.numA == 123
            r.numB == 99
    }

    def "after a new entity is saved it can be read from the database"(){
        given:
            def e = new Skill(name: "Java").save(flush:true, failOnError:true)
        when:
            def r = Skill.get(e.id)
                    .refresh()
        then:
            r.name == "Java"
            r.id == e.id
    }

    def "after updateing an entity all necessary Values have changed"(){
        given:
            def e = new Skill(
                name: "Name1",
                aliases: ["ALIAS1", "ALIAS2"]
            ).save(flush:true, failOnError:true)
            def id = e.id
        when:
            e.name = "Name2"
            e.aliases.remove("ALIAS1");
            e.aliases << "ALIAS3";
            println "\n\n UPDATE \n "
            e.save(flush:true)
        and:
            e.discard();
            def r = Skill.get(id);
        then:
            r.name == "Name2"
            r.aliases == ["ALIAS2", "ALIAS3"]
    }

    def "delete an entry"(){
        given:
            def e = new Skill(name: "Name1",aliases: ["ALIAS1", "ALIAS2"]).save(flush:true, failOnError:true)
            def id = e.id
        when:
            e.delete()
        and:
            def r = Skill.get(id)
        then:
            r == null;
    }

}
