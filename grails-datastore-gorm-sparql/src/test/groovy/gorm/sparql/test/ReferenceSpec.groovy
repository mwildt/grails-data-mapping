package gorm.sparql.test

import gorm.sparql.model.Skill
import grails.gorm.tests.GormDatastoreSpec
import gorm.sparql.model.Categorized
import gorm.sparql.model.Category

/**
 * Created by mwildt on 28.06.16.
 */
class ReferenceSpec extends GormDatastoreSpec {

    List getDomainClasses() {
        [Category, Categorized, Skill]
    }

    def "a relation is saved an loaded afterwards"(){
        when:
            def category1 = new Category(name: "CAT 1").save();
            def category2 = new Category(name: "CAT 2").save();
        and:
            Categorized item = new Categorized(name: "Item 1",
                mainCategory: category1,
                categories : [
                      category1, category2
                ]
            ).save(flush:true);
            item.discard()

            category1.discard()
            category2.discard()

            println " \n\n END SAVE \n\n"

            Categorized r = Categorized.get(item.id)

        then:
            category1.id.toString() == "http://sparql.de/gorm.sparql.model.Category/1"
            category2.id.toString() == "http://sparql.de/gorm.sparql.model.Category/2"
            r.id.toString() == "http://sparql.de/gorm.sparql.model.Categorized/1"
            r.name == "Item 1"
            r.categories.size() == 2
            r.mainCategory
            r.mainCategory.name == "CAT 1"
            item.mainCategoryId == category1.id;
    }

    def "find() can be used with id()" (){
        given:
        Skill java = new Skill(name: "java").save(flush:true)
        Skill groovy = new Skill(name: "groovy").save(flush:true).save(flush:true)
        Skill gradle = new Skill(name: "gradle", implicits: [groovy]).save(flush:true)
        Skill spring = new Skill(name: "spring", implicits: [java]).save(flush:true)
        Skill grails = new Skill(name: "grails", implicits: [groovy, spring]).save(flush:true)
        session.clear();
        when:
        def found = Skill.withCriteria {
            find "implicits+", {
                id(java)
            }
        }
        then:
        found*.name.sort() == ["grails", "spring"].sort()
    }

}