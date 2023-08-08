package com.lumos.forward;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.lumos.App;

import soot.SootField;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.Value;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;

public class TracePoint implements Serializable {
    public Stmt s;
    public Value v;
    public List<SootFieldRef> suffix;
    public SootMethod sm;

    public TracePoint(Stmt s, Value v, SootMethod sm, List<SootFieldRef> suffix) {
        this.s = s;
        this.v = v;
        this.sm = sm;
        this.suffix = suffix;
    }

    public String d() {
        return d(",");
    }

    public String lessSuffix() {
        List<String> names = new ArrayList<>();
        for (SootFieldRef sref : suffix) {
            names.add(sref.name());
        }
        return names.toString();
    }

    public String d(String separator) {
        return App.serviceMap.get(sm.toString()) + separator + sm + separator + s.getJavaSourceStartLineNumber()
                + separator + s + separator + v + separator
                + lessSuffix();
    }

    public TracePoint(Stmt s, Value v, SootMethod sm) {
        this(s, v, sm, Collections.emptyList());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((s == null) ? 0 : s.hashCode());
        result = prime * result + ((v == null) ? 0 : v.hashCode());
        result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
        result = prime * result + ((sm == null) ? 0 : sm.hashCode());
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
        if (sm == null) {
            if (other.sm != null)
                return false;
        } else if (!sm.equals(other.sm))
            return false;
        return true;
    }

}
