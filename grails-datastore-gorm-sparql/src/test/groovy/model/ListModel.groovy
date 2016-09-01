package model

import grails.persistence.Entity
import org.openrdf.model.IRI

/**
 * Created by mwildt on 28.06.16.
 */

@Entity
class ListModel {

    IRI id;
    List<String> items = []
}
