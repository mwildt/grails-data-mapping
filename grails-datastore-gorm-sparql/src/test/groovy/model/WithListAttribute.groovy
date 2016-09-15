package model

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

    def static hasOne = [
        profile: Profile
    ]
}

@Entity
class Profile {
    String label
    User user

    def static hasOne = [
        user: User
    ]
}
