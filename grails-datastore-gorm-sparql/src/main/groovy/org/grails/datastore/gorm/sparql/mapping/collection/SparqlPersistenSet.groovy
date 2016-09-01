package org.grails.datastore.gorm.sparql.mapping.collection

import org.grails.datastore.gorm.sparql.engine.SparqlEntityPersister
import org.grails.datastore.mapping.core.Session
import org.grails.datastore.mapping.model.PersistentEntity

/**
 * Created by mwildt on 25.08.16.
 */
public class SparqlPersistenSet implements Set {

    def inizializing = null;
    def initialized = false;

    SparqlEntityPersister persister
    Session session
    Collection keys
    Collection data

    protected SparqlPersistenSet(Session session, SparqlEntityPersister persister, Collection keys) {
        this.persister = persister;
        this.session = session;
        this.keys = keys;
    }

    private initialize(){
        if(null != inizializing || initialized){
            return;
        }
        inizializing = true;
        try{
            persister.session.retrieveAll()
            data = persister.retrieveAll(keys)
        } finally {
            inizializing = false;
            initialized = true;
        }
    }

    @Override
    int size() {
        this.initialize()
        return data.size()
    }

    @Override
    boolean isEmpty() {
        this.initialize()
        return data.isEmpty()
    }

    @Override
    boolean contains(Object o) {
        this.initialize()
        return data.contains(o)
    }

    @Override
    Iterator iterator() {
        this.initialize()
        return data.iterator()
    }

    @Override
    Object[] toArray() {
        this.initialize()
        return data.toArray()
    }

    @Override
    boolean add(Object o) {
        this.initialize()
        return data.add(o)
    }

    @Override
    boolean remove(Object o) {
        this.initialize()
        return data.remove(o)
    }

    @Override
    boolean addAll(Collection c) {
        this.initialize()
        return data.addAll(c)
    }

    @Override
    void clear() {
        this.initialize()
        data.clear()
    }

    @Override
    boolean removeAll(Collection c) {
        this.initialize()
        return data.removeAll(c)
    }

    @Override
    boolean retainAll(Collection c) {
        this.initialize()
        return data.retainAll(c)
    }

    @Override
    boolean containsAll(Collection c) {
        this.initialize()
        return data.containsAll(c)
    }

    @Override
    Object[] toArray(Object[] a) {
        this.initialize()
        return data.toArray(a)
    }
}
