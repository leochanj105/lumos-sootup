package com.lumos.forward;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JReturnVoidStmt;

public class ExitNode {
    public SootMethod sm;

    public Context context;

    // public List<Value> arguments;
    // public List<Local> parameters;
    public CallSite lastCall;
    public List<Stmt> returnStmts;
    public Value ret;

    public ExitNode(Context context, InvokeExpr iexpr, Value ret) {
        this.sm = iexpr.getMethod();
        this.context = context;
        List<CallSite> ctrace = context.getCtrace();
        lastCall = ctrace.get(ctrace.size() - 1);
        // List<Stmt> rets = new ArrayList<>();
        this.ret = ret;
    }
}
