package org.grails.datastore.gorm.sparql.engine

import org.grails.datastore.gorm.sparql.SparqlPersistentEntity
import org.grails.datastore.gorm.sparql.entity.SparqlEntity
import org.grails.datastore.gorm.sparql.mapping.config.RDFEntity
import org.grails.datastore.gorm.sparql.mapping.config.RDFProperty
import org.grails.datastore.gorm.sparql.query.SparqlQuery
import org.grails.datastore.mapping.collection.PersistentList
import org.grails.datastore.mapping.collection.PersistentSet
import org.grails.datastore.mapping.collection.PersistentSortedSet
import org.grails.datastore.mapping.config.Property
import org.grails.datastore.mapping.core.OptimisticLockingException
import org.grails.datastore.mapping.core.SessionImplementor
import org.grails.datastore.mapping.core.impl.PendingInsert
import org.grails.datastore.mapping.core.impl.PendingInsertAdapter
import org.grails.datastore.mapping.core.impl.PendingOperation
import org.grails.datastore.mapping.core.impl.PendingUpdate
import org.grails.datastore.mapping.core.impl.PendingUpdateAdapter
import org.grails.datastore.mapping.engine.EntityAccess
import org.grails.datastore.mapping.engine.EntityPersister
import org.grails.datastore.mapping.engine.Persister
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.PropertyMapping
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.model.types.Basic
import org.grails.datastore.mapping.model.types.Custom
import org.grails.datastore.mapping.model.types.Simple
import org.grails.datastore.mapping.model.types.ToMany
import org.grails.datastore.mapping.model.types.ToOne
import org.grails.datastore.mapping.proxy.ProxyFactory
import org.grails.datastore.mapping.query.Query
import org.openrdf.model.IRI
import org.openrdf.model.Model
import org.openrdf.model.Resource
import org.openrdf.model.Statement
import org.openrdf.model.Value
import org.openrdf.model.ValueFactory
import org.openrdf.model.impl.LinkedHashModel
import org.openrdf.model.impl.TreeModelFactory
import org.openrdf.model.util.Models
import org.openrdf.model.util.RDFCollections
import org.openrdf.query.QueryResults
import org.openrdf.repository.RepositoryResult
import org.openrdf.repository.util.Connections
import org.springframework.context.ApplicationEventPublisher

import javax.persistence.FetchType

/**
 * Created by mwildt on 24.08.16.
 */
class SparqlEntityPersister extends EntityPersister {

    final static String DEFAULT_PREFIX = "http://norris.flavia-it.de/"

    final static Closure NOT_NULL = { it -> null != it}

    IRI getDefaultPredicateByName(String key) {
        valueFactory.createIRI(DEFAULT_PREFIX + key)
    }

    SparqlDatastore datastore
    ValueFactory valueFactory
    RDFEntity mappedForm

    public SparqlEntityPersister(SparqlMappingContext mappingContext, SparqlPersistentEntity entity, SparqlSession session, SparqlDatastore datastore, ApplicationEventPublisher publisher) {
        super(mappingContext, entity, session, publisher)
        this.datastore = datastore
        this.valueFactory = datastore.repository.valueFactory
        initalize()
    }

    private initalize(){
        this.mappedForm = getMappingContext().getMappingFactory().createMappedForm(persistentEntity);
    }

    public SparqlEntityPersister(SparqlMappingContext mappingContext, SparqlPersistentEntity entity, SparqlSession session, ApplicationEventPublisher publisher) {
        super(mappingContext, entity, session, publisher)
        initalize()
    }

    String getEntityFamily() {
        persistentEntity.name
    }

    @Override
    protected List<Object> retrieveAllEntities(PersistentEntity pe, Serializable[] keys) {
        keys.collect { key ->
            retrieveEntity(pe, key)
        }
    }

    public getVersionFromDataStore(identifier){
        def iri = getIRIFromIdentifier(identifier);
        def property = getPredicateForProperty(this.persistentEntity.getVersion());
        RepositoryResult<Statement> statements = datastore.repository.connection.getStatements(iri, property, null)
        if(statements.hasNext()){
            return SparqlNativePersistentEntity.convert(statements.next().object)
        } else {
            return null;
        }
    }

    @Override
    protected List<Object> retrieveAllEntities(PersistentEntity pe, Iterable<Serializable> keys) {
        keys.collect { key ->
            retrieveEntity(pe, key)
        }
    }

    @Override
    protected Object retrieveEntity(PersistentEntity pe, Serializable identifier) {
        IRI iri = getIRIFromIdentifier(identifier);
        RepositoryResult<Statement> statements = datastore.repository.connection.getStatements(iri, null, null);
        if(!statements.hasNext()){
            return null;
        }

        def nativeEntry = new SparqlNativePersistentEntity(this);
        while (statements.hasNext()) {
            Statement st = statements.next();
            nativeEntry.add(st.predicate, st.object)
            SparqlNativePersistentEntity

        }
        return createObjectFromNativeEntry(pe, identifier, nativeEntry);
    }

    IRI getPredicateForProperty(PersistentProperty prop){
        final RDFProperty mappedProperty = prop.getMapping().getMappedForm();
        if(!mappedProperty){
            return getDefaultPredicateByName(prop.name)
        } else if(mappedProperty.predicate) {
            return mappedProperty.predicate
        } else {
            return getDefaultPredicateByName(prop.name)
        }
    }

    public IRI getIRI(value, String prefix = DEFAULT_PREFIX){
        valueFactory.createIRI(prefix, value);
    }

    int lastId = 0;

    public IRI getFamilyIRI(String prefix = DEFAULT_PREFIX){
        IRI iri = valueFactory.createIRI(prefix + persistentEntity.name);
        return iri
    }

    protected Serializable generateIdentifier() {
        def nextValue = ++lastId;
        def identifier;
        if(IRI.isAssignableFrom(persistentEntity.getIdentity().type)) {
            identifier = getIRIFromIdentifier(nextValue);
        } else {
            identifier =  nextValue;
        }
        return identifier
    }

    private convertToIdentifier(value){
        if(this.persistentEntity.getIdentity().getType().isInstance(value)) {
            return value
        } else {
            def res = value.toString().replace(DEFAULT_PREFIX + getEntityFamily() + "/", '');
            return res;
        }
    }

    private getIRIFromIdentifier(id){
        if(null == id || IRI.isAssignableFrom(id.class)){
            return id
        }
        valueFactory.createIRI(DEFAULT_PREFIX + getEntityFamily() + "/${id}");
    }

    public Value getLiteral(object){
        if(Value.isAssignableFrom(object.class)){
            return object
        }
        valueFactory.createLiteral(object);
    }

    private getPendingPersistOperation(boolean isInsert, PersistentEntity persistentEntity, Serializable identifier, SparqlNativePersistentEntity entry, EntityAccess entityAccess){
        if(isInsert){
            return  new PendingInsertAdapter<SparqlNativePersistentEntity,Serializable>(persistentEntity, identifier, entry, entityAccess) {
                public void run() {
                    storeEntry(entityAccess, identifier, entry);
                    firePostInsertEvent(persistentEntity, entityAccess);
                }
            };
        } else {
            return new PendingUpdateAdapter<SparqlNativePersistentEntity,Serializable>(persistentEntity, identifier, entry, entityAccess) {
                public void run() {
                    updateEntry(persistentEntity, entityAccess, getNativeKey(), getNativeEntry());
                    firePostUpdateEvent(persistentEntity, entityAccess);
                }
            };
        }
    }

    @Override
    protected Serializable persistEntity(PersistentEntity pe, Object obj) {
        ProxyFactory proxyFactory = getProxyFactory();
        obj = proxyFactory.unwrap(obj);

        final EntityAccess entityAccess = createEntityAccess(persistentEntity, obj);

        SparqlNativePersistentEntity entry = new SparqlNativePersistentEntity(this)
        Serializable identifier = entityAccess.getIdentifier();

        boolean isInsert = false;
        if(null == identifier){
            isInsert = true;
            identifier = generateIdentifier()
            entityAccess.setIdentifier(identifier);
        }

        // Register operation to session
        if((session as SessionImplementor).isPendingAlready(obj)) {
            return (Serializable) identifier;
        }
        if(isInsert && isVersioned(entityAccess) ) {
            super.setVersion(entityAccess);
        }
        (session as SessionImplementor).registerPending(obj);

        final PendingOperation<SparqlNativePersistentEntity, IRI> currentOperation = getPendingPersistOperation(isInsert, persistentEntity, identifier, entry, entityAccess)

        final List<PersistentProperty> properties = persistentEntity.getPersistentProperties();

        def mappingFactory = getMappingContext().getMappingFactory()

        entry.setProperty(this.mappedForm.predicate, this.getFamilyIRI());
        entry.setProperty(getIRI('identifier'), identifier)

        for(PersistentProperty prop : properties) {
            def predicate = getPredicateForProperty(prop)
            println prop.getName() + " " + predicate;
            if(prop instanceof Simple){
                Object propValue = entityAccess.getProperty(prop.getName());
                entry.setProperty(predicate, propValue);
            } else if(prop instanceof Basic) {
                Object propValue = entityAccess.getProperty(prop.getName());
                // TODO: Das hiermuss nochmal angepasst werden
                entry.setProperty(predicate, propValue)
            } else if(prop instanceof ToOne) {
                ToOne toOne = prop as ToOne
                Object associatedObject = entityAccess.getProperty(prop.getName());
                if(associatedObject != null) {
                    Persister associationPersister = session.getPersister(associatedObject);
                    Serializable associationId
                    if (proxyFactory.isInitialized(associatedObject) && !session.contains(associatedObject) ) {
                        Serializable tempId = associationPersister.getObjectIdentifier(associatedObject);
                        if (tempId == null) {
                            tempId = session.persist(associatedObject);
                        }
                        associationId = tempId;
                    } else {
                        associationId = associationPersister.getObjectIdentifier(associatedObject)
                    }
                    if(SparqlEntityPersister.isInstance(associationPersister)){
                        associationId = (associationPersister as SparqlEntityPersister).getIRIFromIdentifier(associationId);
                    }
                    entry.setProperty(predicate, associationId)


                    // hier Cascading-Operation, falls auch die Rückseite gesetzt werden muss...
                    if(toOne.isBidirectional()){
                         /*
                          * wenn es sich um eine bidirektionale Verbinung handelt muss auch die inverse Seite gesetzt werden.
                          *
                          * 1) setzen von self in der inversen assiziation
                          */

                        Association inverseSide = toOne.getInverseSide()
                        if(inverseSide instanceof ToMany){ // ManyToMany
                            // Hier muss jetzt das listengeraffel eingebaut werden

                        } else if(inverseSide instanceof ToOne) { // One to One
                            EntityAccess inverseAccess = createEntityAccess(inverseSide.getOwner(), associatedObject);
                            def inversePropertyValue = inverseAccess.getProperty(inverseSide.getName());
                            if(inversePropertyValue && inversePropertyValue.id == identifier){
                                // allready set nothing to do here
                            } else {
                                inverseAccess.setProperty(inverseSide.getName(), obj);
                                def inverseIRI = (associationPersister as SparqlEntityPersister).getIRIFromIdentifier(inverseAccess.getIdentifier())
                                def inversePredicate = (associationPersister as SparqlEntityPersister).getPredicateForProperty(inverseSide);
                                // TODO: Das hier sollte als Cascade-Operation erfolgen
                                println "remove($inverseIRI, $inversePredicate, null) "
                                datastore.repository.connection.remove(inverseIRI, inversePredicate, null);
                                println "add($inverseIRI, $inversePredicate, ${getIRIFromIdentifier(identifier)})"
                                datastore.repository.connection.add(inverseIRI, inversePredicate, getIRIFromIdentifier(identifier));
                            }
                        }
                    }
                }

            } else if(prop instanceof ToMany) {
                final ToMany toMany = prop as ToMany
                final Object propValue = entityAccess.getProperty(toMany.getName());
                if (propValue instanceof Collection) {
                    Collection associatedObjects = (Collection) propValue;
                    PersistentEntity associatedEntity = toMany.getAssociatedEntity();
                    if(associatedEntity != null) {
                        Persister associationPersister = session.getPersister(associatedEntity)
                        def foreignIdentifiers =  associatedObjects.collect{ associatedObject ->
                            if(associatedObject != null) {
                                if (proxyFactory.isInitialized(associatedObject) && !session.contains(associatedObject) ) {
                                    Serializable tempId = associationPersister.getObjectIdentifier(associatedObject);
                                    if (tempId == null) {
                                        tempId = associationPersister.persist(associatedObject);
                                    }
                                    if(associationPersister instanceof  SparqlEntityPersister){
                                        tempId = (associationPersister as SparqlEntityPersister).getIRIFromIdentifier(tempId)
                                    }
                                    return tempId;
                                } else {
                                    Serializable tmpid = associationPersister.getObjectIdentifier(associatedObject)
                                    if(associationPersister instanceof  SparqlEntityPersister){
                                        tmpid = (associationPersister as SparqlEntityPersister).getIRIFromIdentifier(tmpid)
                                    }
                                    return tmpid;
                                }
                            }
                            return null;
                        }
                        entry.setProperty(predicate, foreignIdentifiers);
                    }
                }
            } else {
                println "Unsupported "
            }
        }

        (session as SessionImplementor).with{
            if(isInsert) {
                addPendingInsert(currentOperation as PendingInsert);
            } else {
                addPendingUpdate(currentOperation as PendingUpdate);
            }
        }

        return identifier
    }

    /**
     * Stores the native form of a Key/value datastore to the actual data store
     *
     * @param persistentEntity The persistent entity
     * @param entityAccess The EntityAccess
     * @param storeId
     * @param nativeEntry The native form. Could be a a ColumnFamily, BigTable Entity etc.
     * @return The native key
     */
    protected Serializable storeEntry(EntityAccess entityAccess, Serializable storeId, SparqlNativePersistentEntity nativeEntry) {
        println " >> storeEntry (identifier = $storeId)"

        IRI iri = getIRIFromIdentifier(storeId);
        (entityAccess.entity as SparqlEntity).setIRI(iri);

        Model model = new TreeModelFactory().createEmptyModel()
        nativeEntry.getAll().each { SparqlNativePersistentEntityProperty property ->
            println "add to Model $property.predicate, $property.value"
            if(property.value){
                model.add(iri, property.predicate, property.value)
            } else if(property.values){
                def head = valueFactory.createBNode()
                def list = RDFCollections.asRDF(property.values, head, new LinkedHashModel())
                model.addAll(list)
                model.add(iri, property.predicate, head)
            }
        }
        datastore.repository.connection.add(model)

        return storeId;
    }

    @Override
    protected List<Serializable> persistEntities(PersistentEntity pe, Iterable objs) {
        return objs.collect {
            this.persistentEntity(pe, it)
        }
    }

    protected void updateEntry(PersistentEntity persistentEntity, EntityAccess entityAccess, Serializable storeId, SparqlNativePersistentEntity nativeEntry) {
        println " >> updateEntry (identifier = $storeId)"

        checkOptimisticLock(persistentEntity, storeId, entityAccess, nativeEntry)
        IRI iri = getIRIFromIdentifier(storeId)

        datastore.repository.connection.remove(iri, (IRI) null, null)
        storeEntry(entityAccess, storeId, nativeEntry);
    }

    private void checkOptimisticLock(PersistentEntity persistentEntity, Serializable storeId, EntityAccess entityAccess, SparqlNativePersistentEntity nativeEntry) {
        if (this.mappedForm.isVersioned()) {
            def dataStoreVersion = getVersionFromDataStore(storeId)
            def currentVersion = super.getCurrentVersion(entityAccess)
            if (currentVersion < dataStoreVersion) {
                throw new OptimisticLockingException(persistentEntity, storeId)
            }
            super.incrementVersion(entityAccess)
            currentVersion = super.getCurrentVersion(entityAccess)
            nativeEntry.setProperty(getPredicateForProperty(persistentEntity.getVersion()), currentVersion)
        }
    }


    public Object createObjectFromNativeEntry(PersistentEntity persistentEntity, Serializable nativeKey, SparqlNativePersistentEntity nativeEntry) {
        // persistentEntity = discriminatePersistentEntity(persistentEntity, nativeEntry);

        // cacheNativeEntry(persistentEntity, nativeKey, nativeEntry);

        Object obj = newEntityInstance(persistentEntity);
        refreshObjectStateFromNativeEntry(persistentEntity, obj, nativeKey, nativeEntry, false);
        return obj;
    }


    @Override
    protected void deleteEntity(PersistentEntity pe, Object obj) {
        final EntityAccess entityAccess = createEntityAccess(persistentEntity, obj);
        Serializable identifier = entityAccess.getIdentifier();
        IRI iri = getIRIFromIdentifier(identifier);
        // delete Collections
        RepositoryResult<Statement> statements = datastore.repository.connection.getStatements(iri, null, null);
        Model model = QueryResults.asModel(statements);

        pe.persistentProperties.findAll {PersistentProperty pp -> Collection.isAssignableFrom(pp.type) }
            .each {PersistentProperty persistentProperty ->
                IRI predicate = getPredicateForProperty(persistentProperty);
                Resource head = Models.objectResource(model.filter(iri, predicate, null)).orElse(null);
                if(head){
                    Model rdfList = Connections.getRDFCollection(datastore.repository.connection, head, new LinkedHashModel());
                    datastore.repository.connection.remove(rdfList)
                }
            }
        datastore.repository.connection.remove(model);

    }

    @Override
    protected void deleteEntities(PersistentEntity pe, @SuppressWarnings("rawtypes") Iterable objects) {
        objects.each {
            deleteEntity(pe, it);
        }
    }

    @Override
    Query createQuery() {
        return new SparqlQuery(session, getPersistentEntity(), this);
    }

    @Override
    Serializable refresh(Object o) {
        return null
    }

    protected void refreshObjectStateFromNativeEntry(PersistentEntity persistentEntity, Object obj,
                                                     Serializable nativeKey, SparqlNativePersistentEntity nativeEntry, boolean isEmbedded = false) {
        EntityAccess ea = createEntityAccess(persistentEntity, obj)
        def identifier = nativeEntry.getIdentifier()
        ea.setIdentifier(identifier)

        IRI iri = getIRIFromIdentifier(nativeKey)
        (ea.entity as SparqlEntity).setIRI(iri)

        final List<PersistentProperty> props = persistentEntity.getPersistentProperties()
        for (final PersistentProperty prop : props) {
            String propKey = prop.getName();
            print "read property $propKey ";
            if (prop instanceof Simple) {
                // this magically converts most types to the correct property type, using bean converters.
                ea.setProperty(prop.getName(), nativeEntry.getValue(propKey));
                print "Simple " + nativeEntry.getValue(propKey)
            } else if (prop instanceof Basic) { // Basic Collection Type
                def values = nativeEntry.getValue(propKey);
                ea.setProperty(prop.getName(), values);
                print "Simple " + values
            } else if (prop instanceof Custom) {
                // handle Custom
            } else if (prop instanceof ToOne) {
                ToOne toOne = prop as ToOne;
                final Serializable associationId = nativeEntry.getValue(propKey);
                print "ToOne " + associationId
                if(null != associationId){
                    PropertyMapping<Property> associationPropertyMapping = toOne.getMapping();
                    boolean isLazy = isLazyAssociation(associationPropertyMapping);
                    Persister associationPersister = session.getPersister(prop.getAssociatedEntity())
                    if(SparqlEntityPersister.isInstance(associationPersister)){ // leider können wir den Proxy nur mit ID und nicht mit IRI erzeugen
                        associationId =  (associationPersister as SparqlEntityPersister).convertToIdentifier(associationId)
                    }
                    Object value = isLazy ? session.proxy(toOne.getType(), associationId)
                        : session.retrieve(toOne.getType(), associationId)
                    ea.setProperty(toOne.getName(), value);
                }
            } else if (prop instanceof ToMany) {
                Association association = (Association) prop;
                PropertyMapping<Property> associationPropertyMapping = association.getMapping();
                def values = nativeEntry.getValue(propKey);
                print "ToMany " + values
                boolean isLazy = isLazyAssociation(associationPropertyMapping);
                if (isLazy) {
                    if (List.class.isAssignableFrom(association.getType())) {
                        PersistentList list = new PersistentList(values, association.getAssociatedEntity().getJavaClass(), session);
                        ea.setPropertyNoConversion(association.getName(), list)
                    }
                    else if (SortedSet.class.isAssignableFrom(association.getType())) {
                        SortedSet sortedSet = new PersistentSortedSet(values, association.getAssociatedEntity().getJavaClass(), session);
                        ea.setPropertyNoConversion(association.getName(), sortedSet);
                    }
                    else if (Set.class.isAssignableFrom(association.getType())) {
                        Set set = new PersistentSet(values, association.getAssociatedEntity().getJavaClass(), session)
                        ea.setPropertyNoConversion(association.getName(), set);
                    }
                } else {
                    ea.setProperty(association.getName(), session.retrieveAll(association.getAssociatedEntity().getJavaClass(), keys));
                }
            } else {
                throw new RuntimeException("the property type $prop is not supported");
            }

            println ""
        }
    }

    private boolean isLazyAssociation(PropertyMapping<Property> associationPropertyMapping) {
        if (associationPropertyMapping == null) {
            return true;
        }
        Property kv = associationPropertyMapping.getMappedForm();
        return kv.getFetchStrategy() == FetchType.LAZY;
    }
}
