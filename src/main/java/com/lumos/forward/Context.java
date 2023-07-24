package com.lumos.forward;

import java.util.ArrayList;
import java.util.List;

import com.lumos.analysis.MethodInfo;

import polyglot.ast.Call;
import soot.SootMethod;
import soot.jimple.Stmt;

public class Context {

    public List<CallSite> ctrace;

    @Override
    public String toString() {
        return "Context [ctrace=" + ctrace + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ctrace == null) ? 0 : ctrace.hashCode());
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
        Context other = (Context) obj;
        if (ctrace == null) {
            if (other.ctrace != null)
                return false;
        } else if (!ctrace.equals(other.ctrace))
            return false;
        return true;
    }

    public List<CallSite> getCtrace() {
        return ctrace;
    }

    public void setCtrace(List<CallSite> ctrace) {
        this.ctrace = ctrace;
    }

    public Context(List<CallSite> ctrace) {
        this.ctrace = ctrace;
    }

    public Context deepcopy() {
        List<CallSite> nctx = new ArrayList<>();
        for (CallSite cs : this.ctrace) {
            nctx.add(cs);
        }
        return new Context(nctx);
    }

    public void append(CallSite cs) {
        this.ctrace.add(cs);
    }

    public void append(Stmt callingStmt, MethodInfo minfo) {
        this.append(new CallSite(callingStmt, minfo));
    }

    public MethodInfo getStackLast() {
        if (ctrace.size() == 0) {
            return null;
        }
        CallSite cs = ctrace.get(ctrace.size() - 1);
        return cs.minfo;
    }
}
