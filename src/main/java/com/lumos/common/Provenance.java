package com.lumos.common;

import java.lang.reflect.Method;

import com.lumos.analysis.MethodInfo;

public class Provenance {
    public Dependency dep;
    public MethodInfo minfo;
    public RefSeq refSeq;
    public int prefix;

    public Provenance(Dependency dep, MethodInfo minfo, RefSeq refSeq, int prefix) {
        this.dep = dep;
        this.minfo = minfo;
        this.refSeq = refSeq;
        this.prefix = prefix;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof Provenance)) {
            return false;
        }
        Provenance other = (Provenance) o;
        return other.dep.equals(this.dep);
    }

    public boolean isBefore(Object o) {
        if (o == null || !(o instanceof Provenance)) {
            return false;
        }
        Provenance other = (Provenance) o;
        if (other.equals(this)) {
            return false;
        }
        return this.minfo.wsm.getBody().getStmtGraph().hasEdgeConnecting(this.dep.stmt, other.dep.stmt);
    }
}
