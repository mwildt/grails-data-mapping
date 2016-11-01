package gorm.sparql.test

import grails.gorm.tests.GormDatastoreSpec
import gorm.sparql.model.NorCandidate
import gorm.sparql.model.NorUser
import gorm.sparql.model.Person
import gorm.sparql.model.Profile
import gorm.sparql.model.User
import spock.lang.Stepwise

/**
 * Created by mwildt on 30.06.16.
 */
@Stepwise
class OneToOneSpec extends GormDatastoreSpec{

    List getDomainClasses() {
        [User, Profile, Person, NorCandidate, NorUser]
    }

    def "test bidirectional OntToOne update"(){
        given:
            User u1 = new User(label: "User 1").save()
            User u2 = new User(label: "User 2").save()
            Profile p1 = new Profile(label: "Profil 1", user: u1).save()
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

    def "mapped by none "(){
        given:
            Person p1 = new Person().save()
            Person p2 = new Person(parent: p1).save();
            session.flush()
            session.clear()
        when:
            Person l1 = Person.get(p1.id)
            Person l2 = Person.get(p2.id)
        then:
            l1.parent == null;
            l1.supervisor == null;
            l2.parent == l1;
            l2.supervisor == null;
    }

    def "unidirectional test"(){
        given:
            NorCandidate c = new NorCandidate(name: "Malte Wildt").save()
            NorUser mwildt = new NorUser(username: "mwildt", profile: c).save();
            session.flush()
            session.clear()
        when:
            NorCandidate cl = NorCandidate.get(c.id);
        then:
            cl.getLastEditedBy() == null;
            cl.getOwner() == null;
    }
}

