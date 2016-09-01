package test

import grails.gorm.tests.GormDatastoreSpec
import model.Categorized
import model.Category
import model.ListModel
import model.Skill
import spock.lang.Unroll

/**
 * Created by mwildt on 28.06.16.
 */
class RefernceSpec extends GormDatastoreSpec {

    List getDomainClasses() {
        [Category, Categorized]
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
            category1.id.toString() == "http://norris.flavia-it.de/model.Category/1"
            category2.id.toString() == "http://norris.flavia-it.de/model.Category/2"
            r.id.toString() == "http://norris.flavia-it.de/model.Categorized/1"
            r.name == "Item 1"
            r.categories.size() == 2
            r.mainCategory
            r.mainCategory.name == "CAT 1"
            item.mainCategoryId == category1.id;
    }

}