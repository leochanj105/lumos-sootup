package com.lumos.forward;

import java.util.Collections;
import java.util.List;

import soot.SootField;
import soot.SootFieldRef;
import soot.Value;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;

public class TracePoint {
    public Stmt s;
    public Value v;
    public List<SootFieldRef> suffix;

    public TracePoint(Stmt s, Value v, List<SootFieldRef> suffix) {
        this.s = s;
        this.v = v;
        this.suffix = suffix;
    }

    public TracePoint(Stmt s, Value v) {
        this.s = s;
        this.v = v;
        this.suffix = Collections.emptyList();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((s == null) ? 0 : s.hashCode());
        result = prime * result + ((v == null) ? 0 : v.hashCode());
        result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TracePoint other = (TracePoint) obj;
        if (s == null) {
            if (other.s != null)
                return false;
        } else if (!s.equals(other.s))
            return false;
        if (v == null) {
            if (other.v != null)
                return false;
        } else if (!v.equals(other.v))
            return false;
        if (suffix == null) {
            if (other.suffix != null)
                return false;
        } else if (!suffix.equals(other.suffix))
            return false;
        return true;
    }

}
