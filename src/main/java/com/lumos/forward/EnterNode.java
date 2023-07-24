package com.lumos.forward;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.lumos.App;

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
    public List<List<Value>> aliasPairs;

    public CallSite lastCall;

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
        // this.parameters = this.sm.getActiveBody().getParameterLocals();
    }

    public void addAlising(List<Value> pair) {
        if (pair.size() != 2) {
            App.p("This should be a pair!");
            App.panicni();
        }
        this.aliasPairs.add(pair);
    }

    public void addAlising(Value v1, Value v2) {
        this.aliasPairs.add(Arrays.asList(new Value[] { v1, v2 }));
    }

    @Override
    public String toString() {
        return "EnterNode [sm=" + sm.getName() + ", lastCall=" + lastCall + "]";
    }
}
