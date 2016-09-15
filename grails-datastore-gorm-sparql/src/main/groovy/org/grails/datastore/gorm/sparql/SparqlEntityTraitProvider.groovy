package org.grails.datastore.gorm.sparql

import org.grails.datastore.gorm.sparql.entity.SparqlEntity
import groovy.transform.CompileStatic
import org.grails.compiler.gorm.GormEntityTraitProvider
/**
 * Created by mwildt on 30.06.16.
 */
@CompileStatic
class SparqlEntityTraitProvider implements GormEntityTraitProvider {

    public SparqlEntityTraitProvider(){}

    final Class entityTrait = SparqlEntity

}
