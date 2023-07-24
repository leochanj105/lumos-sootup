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

public class ExitNode extends IPNode {
    public SootMethod sm;

    // public Context context;

    // public List<Value> arguments;
    // public List<Local> parameters;
    public CallSite lastCall;
    public List<Stmt> returnStmts;
    public Value ret;

    public SootMethod getSm() {
        return sm;
    }

    public void setSm(SootMethod sm) {
        this.sm = sm;
    }

    public Value getRet() {
        return ret;
    }

    public void setRet(Value ret) {
        this.ret = ret;
    }

    public ExitNode(Context context, Stmt stmt) {
        this.sm = stmt.getInvokeExpr().getMethod();
        this.context = context;
        this.stmt = stmt;
        List<CallSite> ctrace = context.getCtrace();
        lastCall = ctrace.get(ctrace.size() - 1);
        this.type = "exit";
        // List<Stmt> rets = new ArrayList<>();
        // this.ret = ret;
    }
}
