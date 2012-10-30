package org.opentripplanner.analyst.batch;

import org.opentripplanner.analyst.core.Sample;

/**
 * A SampleList is the part of a Population that store a series of relationships between data 
 * points and graph vertices. SampleLists may use packing and compression techniques, so samples
 * may be generated by iterators from a different underlying representation. 
 */
public interface SampleList extends Iterable<Sample> {

    public abstract int getSize();

}
