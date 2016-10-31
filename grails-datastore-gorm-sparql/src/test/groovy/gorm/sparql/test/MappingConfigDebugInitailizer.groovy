package gorm.sparql.test

import grails.persistence.Entity

/**
 * Created by mwildt on 17.09.16.
 */
class MappingConfigDebugInitailizer extends BaseSpec {

    List getDomainClasses() {
        [User, Profile]
    }

    def "test "() {
        expect:
            true
    }

}

@Entity class User {
    String label;
    Profile profile;

    static hasOne = [
        profile: Profile
    ]

}

@Entity class Profile {
    String label;
}
