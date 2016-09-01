package test

import grails.gorm.tests.GormDatastoreSpec
import model.ListModel
import model.Skill
import org.grails.datastore.mapping.core.DatastoreUtils
import org.grails.datastore.mapping.core.Session
import org.openrdf.model.impl.SimpleValueFactory
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by mwildt on 30.06.16.
 */
abstract class BaseSpec extends Specification {

    static final CURRENT_TEST_NAME = "current.gorm.test"
    static final SETUP_CLASS_NAME = 'org.grails.datastore.gorm.Setup'
    static final TEST_CLASSES = []

    @Shared
    Class setupClass

    Session session

    def setupSpec() {
        ExpandoMetaClass.enableGlobally()
        setupClass = loadSetupClass()
    }

    def setup() {
        cleanRegistry()
        System.setProperty(CURRENT_TEST_NAME, this.getClass().simpleName - 'Spec')
        session = setupClass.setup(((TEST_CLASSES + getDomainClasses()) as Set) as List)
        DatastoreUtils.bindSession session
    }

    List getDomainClasses() {
        []
    }

    def cleanup() {
        if (session) {
            session.disconnect()
            DatastoreUtils.unbindSession session
        }
        try {
            setupClass.destroy()
        } catch (e) {
            println "ERROR: Exception during test cleanup: ${e.message}"
        }

        cleanRegistry()
    }

    private cleanRegistry() {
        for (clazz in (TEST_CLASSES + getDomainClasses())) {
            GroovySystem.metaClassRegistry.removeMetaClass(clazz)
        }
    }

    static Class loadSetupClass() {
        try {
            getClassLoader().loadClass(SETUP_CLASS_NAME)
        } catch (Throwable e) {
            throw new RuntimeException("Datastore setup class ($SETUP_CLASS_NAME) was not found", e)
        }
    }

}
