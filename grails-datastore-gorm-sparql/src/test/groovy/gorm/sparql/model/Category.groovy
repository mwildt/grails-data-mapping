package gorm.sparql.model;

import grails.persistence.Entity;
import org.openrdf.model.IRI;

/**
 * Created by mwildt on 29.06.16.
 */
@Entity
class Category {

    IRI id
    String name

}
