package com.lumos.forward;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lumos.App;

// import fj.data.HashSet;
import polyglot.ast.Call;
import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;

public class EnterNode extends IPNode {
    public SootMethod sm;

    // public Context context;

    public List<Value> arguments;
    public List<Local> parameters;
    public List<List<ContextSensitiveValue>> aliasPairs;
    public CallSite lastCall;

    boolean isRemote;

    public boolean isRemote() {
        return isRemote;
    }

    public void setRemote(boolean isRemote) {
        this.isRemote = isRemote;
    }

    public List<List<ContextSensitiveValue>> getAliasPairs() {
        return aliasPairs;
    }

    public SootMethod getSm() {
        return sm;
    }

    public void setSm(SootMethod sm) {
        this.sm = sm;
    }

    public List<Local> getParameters() {
        return parameters;
    }

    public void setParameters(List<Local> parameters) {
        this.parameters = parameters;
    }

    public EnterNode(Context context, Stmt stmt) {
        this.stmt = stmt;
        this.sm = stmt.getInvokeExpr().getMethod();
        this.context = context;
        List<CallSite> ctrace = context.getCtrace();
        lastCall = ctrace.get(ctrace.size() - 1);
        this.arguments = lastCall.getCallingStmt().getInvokeExpr().getArgs();
        this.aliasPairs = new ArrayList<>();
        this.type = "enter";
        this.isRemote = false;
        // this.parameters = this.sm.getActiveBody().getParameterLocals();
    }

    public void addAlising(List<ContextSensitiveValue> pair) {
        if (pair.size() != 2) {
            App.p("This should be a pair!");
            App.panicni();
        }
        this.aliasPairs.add(pair);
    }

    public void addAlising(ContextSensitiveValue v1, ContextSensitiveValue v2) {
        this.aliasPairs.add(Arrays.asList(new ContextSensitiveValue[] { v1, v2 }));
    }

    @Override
    public String toString() {
        return "EnterNode [sm=" + sm.getName() + ", lastCall=" + lastCall + "]";
    }
}
