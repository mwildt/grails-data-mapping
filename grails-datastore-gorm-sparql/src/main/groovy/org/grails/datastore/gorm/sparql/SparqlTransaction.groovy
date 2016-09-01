package org.grails.datastore.gorm.sparql;

import org.grails.datastore.mapping.transactions.Transaction
import org.openrdf.model.Resource
import org.openrdf.query.Query;
import org.openrdf.repository.RepositoryConnection

import java.beans.Statement;

/**
 * Created by didier on 03.05.16.
 */
public class SparqlTransaction implements Transaction<RepositoryConnection> {
    private RepositoryConnection connection;

    SparqlTransaction(RepositoryConnection connection) {
        this.connection = connection;
    }
    /**
     * Commit the transaction.
     */
    @Override
    public void commit() {
        connection.commit();
    }

    /**
     * Rollback the transaction.
     */
    @Override
    public void rollback() {
        connection.rollback();
    }

    /**
     * @return the native transaction object.
     */
    @Override
    public RepositoryConnection getNativeTransaction() {
        return connection;
    }

    /**
     * Whether the transaction is active
     *
     * @return True if it is
     */
    @Override
    public boolean isActive() {
        connection.isActive();
    }

    /**
     * Sets the transaction timeout period
     *
     * @param timeout The timeout
     */
    @Override
    public void setTimeout(int timeout) {

    }

    public Query prepareQuery(String query) {
        connection.prepareQuery(query)
    }

    public void add(Iterable<Statement> statements, Resource... context) {
        connection.add(statements, context)
    }
}
