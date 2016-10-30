package org.grails.datastore.gorm.sparql.engine

import org.grails.datastore.gorm.sparql.SparqlPersistentEntity
import org.grails.datastore.gorm.sparql.mapping.config.SparqlMappingConfigurationStrategy
import org.grails.datastore.gorm.sparql.mapping.config.SparqlMappingFactory
import org.grails.datastore.mapping.config.Entity
import org.grails.datastore.mapping.config.Property
import org.grails.datastore.mapping.model.AbstractMappingContext
import org.grails.datastore.mapping.model.MappingConfigurationStrategy
import org.grails.datastore.mapping.model.MappingFactory
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.config.GormMappingConfigurationStrategy
import org.openrdf.model.impl.SimpleValueFactory

import javax.xml.datatype.XMLGregorianCalendar

class SparqlMappingContext extends AbstractMappingContext {

    private static final Class<Long> LONG_TYPE = long.class;
    private static final Class<Double> DOUBLE_TYPE = double.class;
    private static final Class<String> STRING_TYPE = String.class;
    public static final Set<Class> BASIC_TYPES = Collections.unmodifiableSet( new HashSet<Class>( Arrays.asList(
            BigDecimal.class,
            BigInteger.class,
            boolean.class,
            byte.class,
            Date.class,
            double.class,
            float.class,
            int.class,
            short.class,
            XMLGregorianCalendar.class
    ) ) );

    SparqlMappingFactory mappingFactory
    MappingConfigurationStrategy syntaxStrategy

    SparqlMappingContext() {
        mappingFactory = new SparqlMappingFactory()
//        syntaxStrategy = new GormMappingConfigurationStrategy(mappingFactory)
        syntaxStrategy = new GormMappingConfigurationStrategy(mappingFactory);
    }

    /**
     * Returns the syntax reader used to interpret the entity
     * mapping syntax
     *
     * @return The SyntaxReader
     */
    @Override
    MappingConfigurationStrategy getMappingSyntaxStrategy() {
        syntaxStrategy
    }

    @Override
    MappingFactory getMappingFactory() {
        mappingFactory
    }


    @Override
    protected PersistentEntity createPersistentEntity(Class javaClass, boolean external) {
        SparqlPersistentEntity persistentEntity = new SparqlPersistentEntity(javaClass, this, external);
        mappingFactory.createMappedForm(persistentEntity)
        persistentEntity
    }

    @Override
    protected PersistentEntity createPersistentEntity(Class javaClass) {
        new SparqlPersistentEntity(javaClass, this)
    }

    public Object convertToNative(Object value) {
        if(value != null) {
            final Class<?> type = value.getClass();
            if(BASIC_TYPES.contains(type)) {
                return SimpleValueFactory.instance.createLiteral(value);
            }
            else if(value instanceof Collection) {
                return value;
            } else {
                return SimpleValueFactory.instance.createIRI((String)value);
            }
        }
        else {
            return value;
        }
    }

}
