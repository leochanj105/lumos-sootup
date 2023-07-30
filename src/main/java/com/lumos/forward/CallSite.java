package com.lumos.forward;

import java.util.List;

import com.lumos.analysis.MethodInfo;

import soot.SootMethod;
import soot.jimple.Stmt;

public class CallSite {

    public Stmt callingStmt;
    public MethodInfo minfo;

    public Stmt getCallingStmt() {
        return this.callingStmt;
    }

    public void setCallingStmt(Stmt callingStmt) {
        this.callingStmt = callingStmt;
    }

    public MethodInfo getMinfo() {
        return this.minfo;
    }

    public void setMinfo(MethodInfo minfo) {
        this.minfo = minfo;
    }

    public CallSite(Stmt callingStmt, MethodInfo minfo) {
        this.callingStmt = callingStmt;
        this.minfo = minfo;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((callingStmt == null) ? 0 : callingStmt.hashCode());
        result = prime * result + ((minfo == null) ? 0 : minfo.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return (callingStmt == null ? "None" : callingStmt.toString());
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
        if (minfo == null) {
            if (other.minfo != null)
                return false;
        } else if (!minfo.equals(other.minfo))
            return false;
        return true;
    }

}
