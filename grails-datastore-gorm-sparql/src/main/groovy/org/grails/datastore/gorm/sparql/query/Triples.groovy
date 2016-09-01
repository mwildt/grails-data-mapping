package org.grails.datastore.gorm.sparql.query

import org.openrdf.model.IRI

/**
 * Created by mwildt on 04.07.16.
 */
class Triples {

    static String printValue(value){
        if(value == null){
            return value
        }
        if(IRI.isAssignableFrom(value.class)){
            return "<$value>"
        } else {
            return value.toString()
        }
    }

    static class Triple {

        public Triple(){}

        public Triple(s, p, o){
            this.withSubject(s).withPredicate(p).withObject(o)
        }

        def triples = []
        def SparqlFilterExpression filter = null;

        def boolean recursive = false;


        def subject = "?s"
        def predicate = "?p"
        def object = "?o"

        def Triple with(Triple triple){
            triples << triple
            this
        }

        def Triple withRecursive(boolean recursive){
            this.recursive = recursive
            this
        }

        def Triple with(SparqlFilterExpression filter){
            this.filter = filter
            this
        }

        def Triple withSubject(value){
            this.subject = value
            return this;
        }

        def Triple withPredicate(value){
            this.predicate = value
            return this;
        }

        def Triple withObject(value){
            this.object = value
            return this;
        }

        String toString(){
            String s = subject instanceof IRI ? "<$subject>" : subject;
            String p = predicate instanceof IRI ? "<$predicate>" : predicate;
            String o = object instanceof IRI ? "<$object>" : object;
            return "$s $p ${recursive? '*' : ''} $o ."
        }

        List<Triple> flatten(){
            [this] + this.triples
        }

        String getFilters(){
            filter ? filter.toString() : null;
        }
    }

    static interface SparqlFilterExpression {}

    static class SparqlSimpleFilter implements SparqlFilterExpression {
        def expression

        public SparqlSimpleFilter(String expression){
            this.expression = expression
        }

        def String toString(){
            expression
        }
    }



    static class SparqlFilterAND implements SparqlFilterExpression{

        def List<SparqlFilterExpression> filter = [];

        public add(SparqlFilterExpression filter){
            if(filter){
                this.filter << filter
            }
        }

        def String toString(){
            this.filter.collect{
                it.toString()
            }.join(" && ")
        }
    }

    static class SparqlFilterOR implements SparqlFilterExpression{

        def List<SparqlFilterExpression> filter = [];

        public add(SparqlFilterExpression filter){
            if(filter){
                this.filter << filter
            }
        }

        def String toString(){
            this.filter.collect{
                it.toString()
            }.join(" || ")
        }
    }

    static class SparqlFilterNOT implements SparqlFilterExpression{

        def filter = null;

        public set(SparqlFilterExpression filter){
            this.filter = filter
        }

        def String toString(){
            return "NOT ( ${filter.toString()} )"
        }
    }

}
