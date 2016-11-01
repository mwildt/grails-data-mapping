package gorm.sparql.model;

import grails.persistence.Entity;

@Entity
class Book {

    String title
    List<Author> authors

    static belongsTo = Author
    static hasMany = [authors:Author]

}

@Entity
class Author {

    String name
    Set<Book> books

    static hasMany = [books:Book]

}