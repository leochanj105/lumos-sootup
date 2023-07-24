package com.lumos.forward;

import java.util.List;

import polyglot.ast.Call;
import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;

public class EnterNode extends IPNode {
    public SootMethod sm;

    public Context context;

    public List<Value> arguments;
    public List<Local> parameters;
    public CallSite lastCall;

    public EnterNode(Context context, InvokeExpr iexpr) {
        this.sm = iexpr.getMethod();
        this.context = context;
        List<CallSite> ctrace = context.getCtrace();
        lastCall = ctrace.get(ctrace.size() - 1);
        this.arguments = lastCall.getCallingStmt().getInvokeExpr().getArgs();
        this.parameters = this.sm.getActiveBody().getParameterLocals();
    }

}
