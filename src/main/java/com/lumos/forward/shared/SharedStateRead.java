package com.lumos.forward.shared;

import java.util.ArrayList;
import java.util.List;

import com.lumos.App;
import com.lumos.forward.ContextSensitiveValue;
import com.lumos.forward.IPNode;

import soot.SootFieldRef;
import soot.SootMethod;
import soot.Value;
import soot.jimple.Stmt;

public class SharedStateRead {
    public String type;
    public IPNode rnode;
    public ContextSensitiveValue cvalue;
    // public List<SootFieldRef> refs;
    public List<String> refs;

    public String d() {
        return d(",");
    }

    public String lessSuffix() {
        return refs.toString();
    }

    public String d(String separator) {
        SootMethod sm = cvalue.getContext().getStackLast().sm;
        Stmt s = rnode.stmt;
        Value v = cvalue.getValue();
        return type + separator + App.serviceMap.get(sm.toString()) + separator + sm + separator
                + s.getJavaSourceStartLineNumber()
                + separator + s + separator + v + separator
                + lessSuffix();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((rnode == null) ? 0 : rnode.getStmt().hashCode());
        result = prime * result + ((cvalue == null) ? 0 : cvalue.getValue().hashCode());
        // result = prime * result + ((rnode == null) ? 0 : rnode.hashCode());
        // result = prime * result + ((cvalue == null) ? 0 : cvalue.hashCode());
        result = prime * result + ((refs == null) ? 0 : refs.hashCode());
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
        SharedStateRead other = (SharedStateRead) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (rnode == null) {
            if (other.rnode != null)
                return false;
            // } else if (!rnode.equals(other.rnode))
        } else if (!rnode.getStmt().equals(other.rnode.getStmt()))
            return false;
        if (cvalue == null) {
            if (other.cvalue != null)
                return false;
            // } else if (!cvalue.equals(other.cvalue))
        } else if (!cvalue.getValue().equals(other.cvalue.getValue()))
            return false;
        if (refs == null) {
            if (other.refs != null)
                return false;
        } else if (!refs.equals(other.refs))
            return false;
        return true;
    }

    public SharedStateRead(String type, IPNode rnode, ContextSensitiveValue cvalue, List<SootFieldRef> refs) {
        this.type = type;
        this.rnode = rnode;
        this.cvalue = cvalue;
        this.refs = App.refToString(refs);
    }

    @Override
    public String toString() {
        return "SharedStateRead [ type=" + type + "\n rnode=" + rnode + "\n cvalue=" + cvalue + "\n refs=" + refs
                + "]";
    }

    public String shortd(String separator) {
        SootMethod sm = cvalue.getContext().getStackLast().sm;
        Stmt s = rnode.stmt;
        Value v = cvalue.getValue();
        return type + separator + App.serviceMap.get(sm.toString()) + separator + sm + separator
                + s.getJavaSourceStartLineNumber()
                + separator + s + separator + v;
    }

}
