package gorm.sparql.model

import grails.persistence.Entity
import org.openrdf.model.IRI
import org.openrdf.model.impl.SimpleIRI
import org.openrdf.model.vocabulary.SKOS

/**
 * Created by mwildt on 28.06.16.
 */

@Entity
class Skill {

    static mapWith = "sparql"

    IRI id;
    boolean isTopLevel = false;
    String name;
    Boolean approved
    boolean approvedNat = false
    Integer numA
    int numB = 10
    List<String> aliases = []
    Skill parent
    List<Skill> implicits = []

    static mapping = {
        version false
        predicate (new SimpleIRI("http://test.de/type"))
        object (new SimpleIRI("http://test.de/Skill"))
        name predicate: new SimpleIRI("http://test.de/prefLabel")
    }

    static mappedBy = [
        implicits: "none",
        parent: "none",
    ]


}
