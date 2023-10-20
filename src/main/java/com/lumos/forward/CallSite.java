package com.lumos.forward;

import java.util.List;

import com.lumos.App;
import com.lumos.analysis.MethodInfo;

import soot.SootMethod;
import soot.jimple.Stmt;

public class CallSite {

    public Stmt callingStmt;
    public SootMethod sm;

    public Stmt getCallingStmt() {
        return this.callingStmt;
    }

    public void setCallingStmt(Stmt callingStmt) {
        this.callingStmt = callingStmt;
    }

    public CallSite(Stmt callingStmt, SootMethod sm) {
        this.callingStmt = callingStmt;
        this.sm = sm;
    }

    public MethodInfo getMInfo() {
        return App.methodMap.get(sm.getSignature());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((callingStmt == null) ? 0 : callingStmt.hashCode());
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
        CallSite other = (CallSite) obj;
        if (callingStmt == null) {
            if (other.callingStmt != null)
                return false;
        } else if (!callingStmt.equals(other.callingStmt))
            return false;
        if (sm == null) {
            if (other.sm != null)
                return false;
        } else if (!sm.equals(other.sm))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return (callingStmt == null ? "None" : callingStmt.toString());
    }

    public SootMethod getSm() {
        return sm;
    }

    public void setSm(SootMethod sm) {
        this.sm = sm;
    }

}
