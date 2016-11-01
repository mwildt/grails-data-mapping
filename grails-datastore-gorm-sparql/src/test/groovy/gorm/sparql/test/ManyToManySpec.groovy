package gorm.sparql.test

import gorm.sparql.model.Author
import gorm.sparql.model.Book
import gorm.sparql.model.WithListAttribute
import grails.gorm.tests.GormDatastoreSpec

/**
 * Created by mwildt on 01.11.16.
 */
class ManyToManySpec  extends GormDatastoreSpec{

    List getDomainClasses() {
        [Author, Book]
    }

    def "many to many assoc is saved" (){
        given:
        Author a = new Author(name:"Stephen King")
                .addToBooks(new Book(title:"The Stand"))
                .addToBooks(new Book(title:"The Shining"))
                .save(flush:true)
        session.clear()
        when:
        Author king = Author.get(a.id);
        def books = Book.list()
        then:
        books.size() == 2;
        books.each{
            it.authors.size() == 1
        }
    }

    def "many to many assoc is updated" (){
        given:
        Book b1 = new Book(title:"The Stand").save(flush:true)
        Book b2 = new Book(title:"The Shining").save(flush:true)
        Author a = new Author(name:"Stephen King").save(flush:true)
        a.books = []
        a.books << b1
        a.books << b2
        a.save(flush:true)
        session.clear()
        when:
        Author king = Author.get(a.id);
        def books = Book.list()
        then:
        books.size() == 2;
        books.each {
            assert it.authors.size() == 1
            assert it.authors.first().name == "Stephen King"
        }
    }
}
