package com.lumos.common;

import soot.Unit;

public class Query {
    public RefSeq refSeq;
    public Unit unit;

    public Query(RefSeq refSeq, Unit unit) {
        this.refSeq = refSeq;
        this.unit = unit;
    }

    public String toString() {
        return "[" + this.refSeq.toString() + " at " + unit + "]";
    }

    public boolean equals(Object o) {
        if (!(o instanceof Query) || o == null)
            return false;
        Query other = (Query) o;
        return other.refSeq.equals(this.refSeq) && other.unit.equals(this.unit);
    }

    public int hashCode() {
        return refSeq.hashCode() + unit.hashCode();
    }
}
