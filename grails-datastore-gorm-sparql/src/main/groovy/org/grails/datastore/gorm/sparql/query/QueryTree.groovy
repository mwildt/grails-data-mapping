package org.grails.datastore.gorm.sparql.query

/**
 * Created by mwildt on 04.07.16.
 */
class QueryTree {



    static String getQuery(QueryTreeNode node){
        def q = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
        q += "SELECT ?s ?p ?o WHERE { "
        def triples = node.getTriples()
        triples.each {Triples.Triple triple ->
            q += "${triple.toString()}\n"
        }
        String filter = node.getFilter();
        if(filter){
            q += "FILTER ( $filter )"
        }
        q += " } "
    }

    static interface QueryTreeNode {
        def List<Triples.Triple> getTriples();
        def String getFilter();
    }

    static class TripleOnlyQueryTreeRootNode extends SingleQueryTreeNode {

        def triples = []

        @Override
        List<Triples.Triple> getTriples() {
            def tmp = inner.getTriples()
            def res = [triples] + tmp
            return res;
        }

        @Override
        String getFilter() {
            return inner.getFilter()
        }

    }

    static class QueryTreeRootNode implements QueryTreeNode{
        def List<Triples.Triple> triples = []
        QueryTreeNode inner

        @Override
        List<Triples.Triple> getTriples() {
            def tmp = inner.getTriples()
            def res = triples + tmp
            return res;
        }

        @Override
        String getFilter() {
            return inner.getFilter()
        }

    }

    static class AndQueryTreeNode extends ListQueryTreeNode {

        @Override
        String getFilter() {
            return inner.collect{
                it.getFilter()
            }.join(" && ")
        }

    }

    static class OrQueryTreeNode extends ListQueryTreeNode {

        @Override
        String getFilter() {
            return inner.collect{
                it.getFilter()
            }.join(" || ")
        }

    }

    static abstract class ListQueryTreeNode implements QueryTreeNode {

        List<QueryTreeNode> inner

        @Override
        List<Triples.Triple> getTriples() {
            return inner.collect{it.getTriples()}.flatten()
        }
    }

    static abstract class SingleQueryTreeNode implements QueryTreeNode {
        AndQueryTreeNode inner

        @Override
        List<Triples.Triple> getTriples() {
            return inner.getTriples()
        }

    }

    static class NotQueryTreeNode extends SingleQueryTreeNode{

        @Override
        String getFilter() {
            return " ! ( ${inner.getFilter()} ) "
        }
    }


    static class InQueryTreeNode implements QueryTreeNode {
        Triples.Triple triple
        Triples.SparqlFilterExpression expression = ""

        @Override
        List<Triples.Triple> getTriples() {
            return [triple];
        }

        @Override
        String getFilter() {
            return expression
        }
    }

    static class SimpleExpressionQueryTreeNode implements QueryTreeNode {

        Triples.Triple triple
        Triples.SparqlFilterExpression expression = null

        @Override
        List<Triples.Triple> getTriples() {
            return triple ? [triple] : [];
        }

        @Override
        String getFilter() {
            return expression ? expression.toString() : ""
        }
    }


}
