package org.grails.datastore.gorm

import org.junit.runner.RunWith
import org.junit.runners.Suite
import grails.gorm.tests.*

/**
 * Created by mwildt on 28.06.16.
 * Is used to run the provied tck tests
 */
@RunWith(Suite)
@Suite.SuiteClasses([
      CrudOperationsSpec,
      OneToManySpec,
      OneToOneSpec,
      OptimisticLockingSpec
])
class SparqlSuite {}

