package org.grails.datastore.gorm.sparql.engine

import groovy.transform.InheritConstructors
import org.grails.datastore.gorm.sparql.entity.SparqlEntity
import org.grails.datastore.gorm.sparql.mapping.config.SparqlMappingEntity
import org.grails.datastore.gorm.sparql.mapping.config.SparqlMappingProperty
import org.grails.datastore.gorm.sparql.query.SparqlQuery
import org.grails.datastore.mapping.cache.TPCacheAdapterRepository
import org.grails.datastore.mapping.collection.PersistentList
import org.grails.datastore.mapping.collection.PersistentSet
import org.grails.datastore.mapping.collection.PersistentSortedSet
import org.grails.datastore.mapping.config.Property
import org.grails.datastore.mapping.core.SessionImplementor
import org.grails.datastore.mapping.core.impl.PendingInsert
import org.grails.datastore.mapping.core.impl.PendingInsertAdapter
import org.grails.datastore.mapping.core.impl.PendingOperation
import org.grails.datastore.mapping.core.impl.PendingOperationAdapter
import org.grails.datastore.mapping.core.impl.PendingUpdate
import org.grails.datastore.mapping.core.impl.PendingUpdateAdapter
import org.grails.datastore.mapping.engine.AssociationIndexer
import org.grails.datastore.mapping.engine.EntityAccess
import org.grails.datastore.mapping.engine.NativeEntryEntityPersister
import org.grails.datastore.mapping.engine.PropertyValueIndexer
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
import org.openrdf.model.Statement
import org.openrdf.model.Value
import org.openrdf.model.ValueFactory
import org.openrdf.model.impl.TreeModelFactory
import org.openrdf.repository.RepositoryResult
import org.springframework.context.ApplicationEventPublisher

import javax.persistence.FetchType

/**
 * Created by didier on 03.05.16.
 */
@InheritConstructors
class TmpSparqlEntityPersister extends NativeEntryEntityPersister<SparqlNativePersistentEntity, Serializable> {

    SparqlDatastore datastore
    ValueFactory valueFactory

    TmpSparqlEntityPersister(SparqlMappingContext mappingContext, org.grails.datastore.gorm.sparql.SparqlPersistentEntity entity, SparqlSession session, SparqlDatastore datastore, ApplicationEventPublisher publisher) {
        super(mappingContext, entity, session, publisher)
        this.datastore = datastore
        this.valueFactory = datastore.repository.valueFactory
    }

    TmpSparqlEntityPersister(SparqlMappingContext mappingContext, org.grails.datastore.gorm.sparql.SparqlPersistentEntity entity, SparqlSession session, ApplicationEventPublisher publisher, TPCacheAdapterRepository<SparqlNativePersistentEntity> cacheAdapterRepository) {
        super(mappingContext, entity, session, publisher, cacheAdapterRepository)
    }

    @Override
    String getEntityFamily() {
        persistentEntity.name
    }

    public IRI getIRI(value){
        valueFactory.createIRI("http://norris.flavia-it.de/${value}");
    }

    public IRI getFamilyIRI(){
        IRI iri = valueFactory.createIRI("http://norris.flavia-it.de/${persistentEntity.name}");
        return iri
    }

    private getIRIFromIdentifier(id){
        if(null == id || IRI.isAssignableFrom(id.class)){
            return id
        }
        valueFactory.createIRI("http://norris.flavia-it.de/${getEntityFamily()}/${id}");
    }

    /**
     * Deletes a single entry
     *
     * @param family The family
     * @param key The key
     * @param entry the entry
     */
    @Override
    protected void deleteEntry(String family, Serializable storeId, Object entry) {
        println " >>> delete $storeId"

        def iri = this.getIRIFromIdentifier(storeId);

        datastore.repository.connection.remove(iri, (IRI) null, null)
        /**
         * TODO: Hier müssen außerdem die eingehenden Referenzen zu dieser Entität gelöscht werden
         * Weiterhin muss der Chache entsprechend Invalidiert werden.
         * Problem: Welche Items müssen aus dem Cache gelöscht werden?
         * Hier kommt es auch auf den Typ der entsprchenden Entitäten an.
         *
         */
    }

    /**
     * @return The PersistentEntity instance
     */
    @Override
    PersistentEntity getPersistentEntity() {
        return super.getPersistentEntity()
    }
/**
 * Subclasses should override to provide id generation. If an identifier is only generated via an insert operation then this
 * method should return null
 *
 * @param persistentEntity The entity
 * @param entry The native entry
 * @return The identifier or null if an identifier is generated only on insert
 */

    int c = 1;

    @Override
    protected Serializable generateIdentifier(PersistentEntity persistentEntity, SparqlNativePersistentEntity entry) {
        def nextValue = ++c;
        def identifier;
        if(IRI.isAssignableFrom(persistentEntity.getIdentity().type)) {
            identifier = getIRIFromIdentifier(nextValue);
        } else {
            identifier =  nextValue;
        }
        println "generate IRI for familiy ${getEntityFamily()} : $identifier";
        return identifier
    }

    /**
     * Obtains an indexer for a particular property
     *
     * @param property The property to index
     * @return The indexer
     */
    @Override
    PropertyValueIndexer getPropertyIndexer(PersistentProperty property) {
        return null
    }

    /**
     * Obtains an indexer for the given association
     *
     *
     * @param nativeEntry The native entry
     * @param association The association
     * @return An indexer
     */
    @Override
    AssociationIndexer getAssociationIndexer(SparqlNativePersistentEntity nativeEntry, Association association) {
        return null
    }

    /**
     * Creates a new entry for the given family.
     *
     * @param family The family
     * @return An entry such as a BigTable Entity, ColumnFamily etc.
     */
    @Override
    protected SparqlNativePersistentEntity createNewEntry(String family) {
        println " >> createNewEntry($family)"
        return new SparqlNativePersistentEntity(this)
    }

    /**
     * Reads a value for the given key from the native entry
     *
     * @param nativeEntry The native entry. Could be a ColumnFamily, a BigTable entity, a Map etc.
     * @param property The property key
     * @return The value
     */
    @Override
    protected Object getEntryValue(SparqlNativePersistentEntity nativeEntry, String property) {
        println " >> getEntryValue(familiy $nativeEntry.family) --> $property"
        def res

        PersistentProperty persistentProperty = persistentEntity.getPropertyByName(property);

        IRI predicate = getPredicateForProperty(persistentProperty);

        println "persistentProperty.type $persistentProperty.type"

        if(Collection.isAssignableFrom(persistentProperty.type)){
            println "collection"
            def data = nativeEntry.getAll(predicate);
            res = data.collect { SparqlNativePersistentEntityProperty item ->
                IRI.isAssignableFrom(item.value.class) ? item.value : item.value.stringValue()
            }
        } else {
            println "no collection"
            SparqlNativePersistentEntityProperty item = nativeEntry.getOne(predicate);
            if(item) {
                res = IRI.isAssignableFrom(item.value.class) ? item.value : item.value.stringValue()
            }

        }

        println "<< getEntryValue $property: $res";
        return res;

    }


    void setEntryValue(SparqlNativePersistentEntity nativeEntry, IRI predicate, Object value) {
        if(null == value){
            return;
        } else if(Collection.isAssignableFrom(value.class)) {
            nativeEntry.addAll(predicate, value.collect { item ->
                getLiteral(item)
            });
        } else if(IRI.isAssignableFrom(value.class)){ // related Entity
            nativeEntry.add(predicate, value);
        } else { // Literal value
            Value object = getLiteral(value)
            nativeEntry.add(predicate, object);
        }
    }

    /**
     * Sets a value on an entry
     * @param nativeEntry The native entry such as a BigTable Entity, ColumnFamily etc.
     * @param key The key
     * @param value The value
     */
    @Override
    protected void setEntryValue(SparqlNativePersistentEntity nativeEntry, String key, Object value) {
        println " >> setEntryValue(familiy ${nativeEntry.family}) $key -> $value"

        IRI predicate = getDefaultPredicateByName(key)
        this.setEntryValue(nativeEntry, predicate, value);

    }



    private getPendingPersistOperation(boolean isUpdate, PersistentEntity persistentEntity, Serializable identifier, SparqlNativePersistentEntity entry, EntityAccess entityAccess){
        if(isUpdate){
            return new PendingUpdateAdapter<SparqlNativePersistentEntity,Serializable>(persistentEntity, identifier, entry, entityAccess) {
                public void run() {
                    updateEntry(persistentEntity, entityAccess, getNativeKey(), getNativeEntry());
                    updateTPCache(persistentEntity, entry, identifier);
                    firePostUpdateEvent(persistentEntity, entityAccess);
                }
            };
        } else {
            return  new PendingInsertAdapter<SparqlNativePersistentEntity,Serializable>(persistentEntity, identifier, entry, entityAccess) {
                public void run() {
                    Serializable insertResult = executeInsert(persistentEntity, entityAccess, getNativeKey(), getNativeEntry());
                    if(insertResult == null) {
                        setVetoed(true);
                    }
                }
            };
        }
    }


    @Override
    protected Serializable persistEntity(final PersistentEntity persistentEntity, Object obj, boolean isInsert) {

        ProxyFactory proxyFactory = getProxyFactory();
        obj = proxyFactory.unwrap(obj);

        SessionImplementor<Object> si = (SessionImplementor<Object>) session;

        SparqlNativePersistentEntity entry = createNewEntry(getEntityFamily());
        final EntityAccess entityAccess = createEntityAccess(persistentEntity, obj, entry);

        // Serializable
        Serializable identifier = readObjectIdentifier(entityAccess, persistentEntity.getMapping());

        def isUpdate = (null != identifier && !isInsert);

        if(si.isPendingAlready(obj)) {
            println " >>>> persist (isInsert : $isInsert, !PENDING) ${persistentEntity.name} $identifier "
            return (Serializable) identifier;
        } else {
            println " >>>> persist (isInsert : $isInsert) ${persistentEntity.name}"
            si.registerPending(obj);
        }
        PendingOperation<SparqlNativePersistentEntity, IRI> pendingOperation = getPendingPersistOperation(isUpdate, persistentEntity, identifier, entry, entityAccess)

        if(!isUpdate){
            identifier = generateIdentifier(persistentEntity, entry)
            entityAccess.setProperty(entityAccess.getIdentifierName(), identifier);
        }



        if (isUpdate && !getSession().isDirty(obj)) {
            println "return from Update cause not dirty"
            return identifier;
        }

        SparqlMappingEntity mappedForm = getMappingContext().getMappingFactory().createMappedForm(persistentEntity);

        setEntryValue(entry, mappedForm.predicate, this.getFamilyIRI());
        setEntryValue(entry, getIRI('identifier'), identifier)

        final List<PersistentProperty> props = persistentEntity.getPersistentProperties();
        for(PersistentProperty prop : props) {

            def key = getPredicateForProperty(prop)

            if(prop instanceof Simple){
                Object propValue = entityAccess.getProperty(prop.getName());
                setEntryValue(entry, key, propValue);
            } else if(prop instanceof Basic) { // Basic Collection Type
                Object propValue = entityAccess.getProperty(prop.getName());
                setEntryValue(entry, key, propValue);
            } else if(prop instanceof Custom){
                throw new RuntimeException("CustomDatatype not supprted")
            } else if (prop instanceof ToMany) {

                final ToMany toMany = (ToMany) prop;
                final Object propValue = entityAccess.getProperty(toMany.getName());

                if (propValue instanceof Collection) {
                    Collection associatedObjects = (Collection) propValue;
                    PersistentEntity associatedEntity = toMany.getAssociatedEntity();
                    if(associatedEntity != null) {
                        NativeEntryEntityPersister associationPersister = (NativeEntryEntityPersister) session.getPersister(associatedEntity);
                        for(Object associatedObject : associatedObjects){
                            if(associatedObject != null) {
                                Serializable associationId
                                if (proxyFactory.isInitialized(associatedObject) && !session.contains(associatedObject) ) {
                                    Serializable tempId = associationPersister.getObjectIdentifier(associatedObject);
                                    if (tempId == null) {
                                        tempId = associationPersister.persist(associatedObject);
                                        // hier brauchen wir eigentlich die IRI und nicht den identifier
                                        if(associationPersister instanceof  SparqlEntityPersister){
                                            tempId = (associationPersister as SparqlEntityPersister).getIRIFromIdentifier(tempId)
                                        }
                                    }
                                    associationId = tempId;
                                } else {
                                    associationId = associationPersister.getObjectIdentifier(associatedObject)
                                    if(!si.isPendingAlready(associatedObject) ){
                                        associationId = associationPersister.persist(associatedObject)
                                    }
                                }
                                println " --> value $associatedObject"
                                setEntryValue(entry, key, associationId);
                            }
                        }
                    }
                }

            } else if (prop instanceof ToOne) {
                ToOne toOne = prop as ToOne
                Object associatedObject = entityAccess.getProperty(prop.getName());
                if(associatedObject != null) {
                    NativeEntryEntityPersister associationPersister = (NativeEntryEntityPersister) session.getPersister(associatedObject);
                    Serializable associationId
                    if (proxyFactory.isInitialized(associatedObject) && !session.contains(associatedObject) ) {
                        Serializable tempId = associationPersister.getObjectIdentifier(associatedObject);
                        if (tempId == null) {
                            if (toOne.isOwningSide()) {
                                tempId = session.persist(associatedObject);
                            }
                        }
                        associationId = tempId;
                    } else {
                        associationId = associationPersister.getObjectIdentifier(associatedObject)
                    }
                    setEntryValue(entry, key, associationId);
                }

            } else {
//                throw new RuntimeException(");
                println "Unsupported Property type $prop on key $key"
            }

        }

        pendingOperation.addCascadeOperation(new PendingOperationAdapter<SparqlNativePersistentEntity,IRI>(persistentEntity, identifier, entry) {
            public void run() {
                // ADD POST-Operation here
            }
        });

        // If the key is still null at this point we have to execute the pending operation now to get the key
        if(isUpdate){
            si.addPendingUpdate((PendingUpdate) pendingOperation);
        } else {
            si.addPendingInsert((PendingInsert) pendingOperation);
        }
        return identifier
    }

    IRI getPredicateForProperty(PersistentProperty prop){
        final SparqlMappingProperty mappedProperty = prop.getMapping().getMappedForm();
        if(!mappedProperty){
            return getDefaultPredicateByName(prop.name)
        } else if(mappedProperty.predicate) {
            return mappedProperty.predicate
        } else {
            return getDefaultPredicateByName(prop.name)
        }
    }

    final static String DEFAULT_PREFIX = "http://norris.flavia-it.de/"

    IRI getDefaultPredicateByName(String key) {
        valueFactory.createIRI(DEFAULT_PREFIX + key)
    }


    Value getLiteral(object){
        if(Value.isAssignableFrom(object.class)){
            return object
        }
        valueFactory.createLiteral(object);
    }


    /**
     * Reads the native form of a Key/value datastore entry. This could be
     * a ColumnFamily, a BigTable Entity, a Map etc.
     *
     * @param persistentEntity The persistent entity
     * @param family The family
     * @param key The key
     * @return The native form
     */
    @Override
    protected SparqlNativePersistentEntity retrieveEntry(PersistentEntity persistentEntity, String family, Serializable key) {
        println ">> retrieveEntry with key $key and familiy $family"

        IRI iri = getIRIFromIdentifier(key);

        RepositoryResult<Statement> statements = datastore.repository.connection.getStatements(iri, null, null);

        if(!statements.hasNext()){
            return null;
        }

        SparqlNativePersistentEntity result = new SparqlNativePersistentEntity(this);
        while (statements.hasNext()) {
            Statement st = statements.next();
            println "retrieve $st.predicate === $st.object"
            result.add(st.predicate, st.object)
        }
        return result;
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
    @Override
    protected Serializable storeEntry(PersistentEntity persistentEntity, EntityAccess entityAccess, Serializable storeId, SparqlNativePersistentEntity nativeEntry) {
        println " >> storeEntry (identifier = $storeId)"

        IRI iri = getIRIFromIdentifier(storeId);
        (entityAccess.entity as SparqlEntity).setIRI(iri);

        Model model = new TreeModelFactory().createEmptyModel()
        nativeEntry.getAll().each { SparqlNativePersistentEntityProperty property ->
            println "add to Model $property.predicate, $property.value"
            model.add(iri, property.predicate, property.value)
        }
        datastore.repository.connection.add(model)

        return storeId;
    }

    /**
     * Updates an existing entry to the actual datastore
     *
     * @param persistentEntity The PersistentEntity
     * @param entityAccess The EntityAccess
     * @param key The key of the object to update
     * @param entry The entry
     */
    @Override
    protected void updateEntry(PersistentEntity persistentEntity, EntityAccess entityAccess, Serializable storeId, SparqlNativePersistentEntity nativeEntry) {
        println " >> updateEntry";
        IRI iri = getIRIFromIdentifier(storeId)
        datastore.repository.connection.remove(iri, (IRI) null, null)
        storeEntry(persistentEntity, entityAccess, storeId, nativeEntry);
    }

    /**
     * Deletes one or many entries for the given list of Keys
     *
     * @param family The family
     * @param keys The keys
     */
    @Override
    protected void deleteEntries(String family, List<Serializable> keys) {
        println " >> deleteEntries";
        keys.collect { Serializable storeId ->
            getIRIFromIdentifier(storeId)
        }.each { IRI storeId ->
            datastore.repository.connection.remove(model)

            /**
             * TODO: Hier müssen außerdem die eingehenden Referenzen zu dieser Entität gelöscht werden
             * Weiterhin muss der Chache entsprechend Invalidiert werden.
             * Problem: Welche Items müssen aus dem Cache gelöscht werden?
             * Hier kommt es auch auf den Typ der entsprchenden Entitäten an.
             *
             */
        }

    }

    @Override
    protected void refreshObjectStateFromNativeEntry(PersistentEntity persistentEntity, Object obj,
                                                     Serializable nativeKey, SparqlNativePersistentEntity nativeEntry, boolean isEmbedded = false) {
        println " >> refreshObjectStateFromNativeEntry"

        EntityAccess ea = createEntityAccess(persistentEntity, obj, nativeEntry)
        ea.setProperty(ea.getIdentifierName(), nativeKey);

        IRI iri = getIRIFromIdentifier(nativeKey);
        (ea.entity as SparqlEntity).setIRI(iri);

        final List<PersistentProperty> props = persistentEntity.getPersistentProperties();
        for (final PersistentProperty prop : props) {
            String propKey = prop.getName()

            if (prop instanceof Simple) {
                // this magically converts most types to the correct property type, using bean converters.
                ea.setProperty(prop.getName(), getEntryValue(nativeEntry, propKey));
            } else if (prop instanceof Basic) { // Basic Collection Type
                Object entryValue = getEntryValue(nativeEntry, propKey);
                entryValue = convertBasicEntryValue(persistentEntity, prop as Basic, entryValue);
                ea.setProperty(prop.getName(), entryValue);
            } else if (prop instanceof Custom) {
                // handle Custom
            } else if (prop instanceof ToOne) {
                ToOne association = (ToOne) prop;
                PersistentEntity associatedEntity = association.getAssociatedEntity();
                final Serializable associationId = getEntryValue(nativeEntry, propKey);

                PropertyMapping<Property> associationPropertyMapping = prop.getMapping();
                boolean isLazy = isLazyAssociation(associationPropertyMapping);

                Object value = isLazy ?
                        session.proxy(prop.getType(), associationId) :
                        session.retrieve(prop.getType(), associationId);
                ea.setProperty(prop.getName(), value);
            } else if (prop instanceof ToMany) {
                println "ToMany"

                Association association = (Association) prop;
                PropertyMapping<Property> associationPropertyMapping = association.getMapping();
                def keys = getEntryValue(nativeEntry, propKey);

                boolean isLazy = isLazyAssociation(associationPropertyMapping);
                if (isLazy) {

                    if (List.class.isAssignableFrom(association.getType())) {
                        PersistentList list = new PersistentList(keys, association.getAssociatedEntity().getJavaClass(), session);
                        ea.setPropertyNoConversion(association.getName(), list)
                    }
                    else if (SortedSet.class.isAssignableFrom(association.getType())) {
                        SortedSet sortedSet = new PersistentSortedSet(keys, association.getAssociatedEntity().getJavaClass(), session);
                        ea.setPropertyNoConversion(association.getName(), sortedSet);
                    }
                    else if (Set.class.isAssignableFrom(association.getType())) {
                        Set set = new PersistentSet(keys, association.getAssociatedEntity().getJavaClass(), session)
                        ea.setPropertyNoConversion(association.getName(), set);
                    }
                } else {

                    ea.setProperty(association.getName(), session.retrieveAll(association.getAssociatedEntity().getJavaClass(), keys));
                }
            } else {
                throw new RuntimeException("the property type $prop is not supported");
            }
        }

    }

    private boolean isLazyAssociation(PropertyMapping<Property> associationPropertyMapping) {
        if (associationPropertyMapping == null) {
            return true;
        }

        Property kv = associationPropertyMapping.getMappedForm();
        return kv.getFetchStrategy() == FetchType.LAZY;
    }

    /**
     * Creates a query for the entity
     *
     * @return The Query object
     */
    @Override
    Query createQuery() {
        println " >> createQuery";
        return new SparqlQuery(session, getPersistentEntity(), this);
    }
}
