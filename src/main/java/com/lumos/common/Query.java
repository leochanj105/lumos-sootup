package com.lumos.common;

import sootup.core.jimple.common.stmt.Stmt;

public class Query {
    public RefSeq refSeq;
    public Stmt stmt;

    public Query(RefSeq refSeq, Stmt stmt) {
        this.refSeq = refSeq;
        this.stmt = stmt;
    }

    public String toString() {
        return "[" + this.refSeq.toString() + " ?? " + stmt + "]";
    }

    public boolean equals(Object o) {
        if (!(o instanceof Query) || o == null)
            return false;
        Query other = (Query) o;
        return other.refSeq.equals(this.refSeq) && other.stmt.equals(this.stmt);
    }
}
