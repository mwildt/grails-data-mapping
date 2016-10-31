package gorm.sparql.model

import grails.gorm.annotation.Entity
import org.openrdf.model.vocabulary.FOAF

/**
 * Created by mwildt on 15.09.16.
 */
@Entity
class WithListAttribute {

    List<String> labels;

    def static mapping = {
        labels predicate: FOAF.LABEL_PROPERTY
    }
}

@Entity
class User {
    String label

    Profile profile

    def static mappedBy = [
        profile : 'user'
    ]
}

@Entity
class Profile {
    String label

    User user
    User owner

    def static mappedBy = [
        user : 'profile'
    ]
}

@Entity
class Person {
    String name
    Person parent
    Person supervisor
//    static belongsTo = [ supervisor: Person ]

    static mappedBy = [ supervisor: "none", parent: "none" ]

}

@Entity class NorUser {
    String username

    static belongsTo = [
        profile: NorCandidate
    ]
    static mappedBy = [
        profile: "none",
        //owner: null
    ]
}

@Entity class NorCandidate {
    String name

//    NorUser lastEditedBy
    NorUser owner

}

