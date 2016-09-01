package test

import model.LongIdEntity

/**
 * Created by mwildt on 07.07.16.
 */
class IdentifierSpec extends BaseSpec {

    @Override
    List getDomainClasses() {
        [LongIdEntity]
    }


    def "save entity with Long Id"(){
        when:
             def ntt = new LongIdEntity(name: "name 1").save(flush:true)
        then:
            ntt.id
            ntt.id == 1
//            ntt.iri != null
    }

    def "reload by id"(){
        given:
            def ntt = new LongIdEntity(name: "Bob").save(flush:true)
            session.clear()
        when:
            def nttRel = LongIdEntity.get(ntt.id)
        then:
            nttRel.id == ntt.id
//            nttRel.getIRI() == ntt.getIRI()
            nttRel.name == "Bob"
    }

    def "reload by property"(){
        given:
        def ntt = new LongIdEntity(name: "Maike").save(flush:true)
        session.clear()
        when:
        def nttRel = LongIdEntity.findByName("Maike")
        then:
        nttRel.id == ntt.id
//        nttRel.getIRI() == ntt.getIRI()
        nttRel.name == "Maike"
    }

}
