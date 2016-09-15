package test

import grails.gorm.tests.GormDatastoreSpec
import model.Profile
import model.User
import model.WithListAttribute
import org.grails.datastore.mapping.dirty.checking.DirtyCheckable

/**
 * Created by mwildt on 30.06.16.
 */
class OneToOneSpec extends GormDatastoreSpec{

    List getDomainClasses() {
        [User, Profile]
    }

    def "test bidirectional OntToOne update"(){
        given:
            User u1 = new User(label: "User 1").save()
            User u2 = new User(label: "User 2").save()
            Profile p1 = new Profile(label: "Priofil 1", user: u1).save()
            session.flush()
            session.clear()
        when:
            u1 = User.get(u1.id);
            p1 = Profile.get(p1.id);
        then:
            u1.version == 1
            p1.version == 0
        when:
            p1.setUser(u2);
            p1.setLabel("New Label")
            p1.save();
            session.flush()
            session.clear()
        and:
            u1 = User.get(u1.id);
            u2 = User.get(u2.id);
            p1 = Profile.get(p1.id);
        then:
            u1.profile == null
            u2.profile == p1
    }

}

