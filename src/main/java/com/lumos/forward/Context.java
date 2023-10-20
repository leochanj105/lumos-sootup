package com.lumos.forward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.lumos.App;
import com.lumos.analysis.MethodInfo;

import polyglot.ast.Call;
import soot.SootMethod;
import soot.jimple.Stmt;

public class Context {

    // public List<CallSite> ctrace;
    public Context parent;
    public CallSite lastCallSite;

    public static Context emptyContext = null;
    public List<IPNode> cfDepNodes;

    public InterProcedureGraph igraph;

    public InterProcedureGraph getIgraph() {
        return igraph;
    }

    public void setIgraph(InterProcedureGraph igraph) {
        this.igraph = igraph;
    }

    @Override
    public String toString() {
        return (parent == null ? "" : parent.toString()) + "," + lastCallSite.getSm().getName();
    }

    public Context(Context parent, CallSite lastCallSite, InterProcedureGraph igraph) {
        this.parent = parent;
        this.lastCallSite = lastCallSite;
        this.igraph = igraph;
    }

    public Context deepcopy() {
        Context pcopy = parent == null ? null : parent.deepcopy();
        return new Context(pcopy, new CallSite(lastCallSite.getCallingStmt(), lastCallSite.getSm()), igraph);
    }

    public Context append(CallSite cs) {
        return new Context(this, cs, igraph);
    }

    public Context append(Stmt callingStmt, MethodInfo minfo) {
        return append(new CallSite(callingStmt, minfo.sm));
    }

    public MethodInfo getStackLast() {
        return App.methodMap.get(lastCallSite.getSm().getSignature());
    }

    public MethodInfo getStackSecondToLast() {
        if (parent == null) {
            return null;
        }

        return parent.getStackLast();
    }

    public Stmt getCallingStmt() {
        return lastCallSite.getCallingStmt();
    }

    public CallSite getLastCallSite() {
        return lastCallSite;
    }

    // public Context popLast() {
    // List<CallSite> newsite = new ArrayList<>(this.ctrace);
    // newsite.remove(newsite.size() - 1);
    // Context c = new Context(newsite);
    // return c;
    // }

    public List<IPNode> getCfDepNodesRecursive() {
        if (this.cfDepNodes != null) {
            return this.cfDepNodes;
        }
        cfDepNodes = new ArrayList<>();
        MethodInfo minfo = getStackSecondToLast();

        if (minfo == null) {
            return cfDepNodes;
        }

        for (Stmt cfstmt : minfo.cfDependency.get(getCallingStmt())) {
            cfDepNodes.add(igraph.getIPNode(parent, cfstmt));
        }

        if (getParent() != null) {
            for (IPNode cfnode : getParent().getCfDepNodesRecursive()) {
                cfDepNodes.add(cfnode);
            }
        }
        return cfDepNodes;
    }

    public int length() {
        return (parent == null ? 0 : parent.length()) + 1;
    }

    public boolean parentOf(Context other) {
        return other != null && (this.equals(other) || strictParentOf(other));
    }

    public boolean strictParentOf(Context other) {
        if (other == null || other.getParent() == null) {
            return false;
        }
        return parentOf(other.getParent());
    }

    public static Context emptyContext() {
        return emptyContext;
    }

    public Context getParent() {
        return parent;
    }

    public void setParent(Context parent) {
        this.parent = parent;
    }

    public static Context getEmptyContext() {
        return emptyContext;
    }

    public static void setEmptyContext(Context emptyContext) {
        Context.emptyContext = emptyContext;
    }

}
