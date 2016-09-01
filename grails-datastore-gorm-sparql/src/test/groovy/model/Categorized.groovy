package model

import grails.persistence.Entity
import org.openrdf.model.IRI

/**
 * Created by mwildt on 29.06.16.
 */
@Entity
class Categorized {

    IRI id
    String name
    Category mainCategory
    List<Category> categories = []

}
