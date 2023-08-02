package com.lumos.forward;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
    public List<IPNode> returnStmtNodes;
    public ContextSensitiveValue ret;

    public List<IPNode> getReturnStmtNodes() {
        return returnStmtNodes;
    }

    public void setReturnStmtNodes(List<IPNode> returnStmtNodes) {
        this.returnStmtNodes = returnStmtNodes;
    }

    public SootMethod getSm() {
        return sm;
    }

    public void setSm(SootMethod sm) {
        this.sm = sm;
    }

    public ContextSensitiveValue getRet() {
        return ret;
    }

    public void setRet(ContextSensitiveValue ret) {
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

    @Override
    public String toString() {
        return "ExitNode [" + sm.getName() + ", " + lastCall + "]";
    }

    @Override
    public void flow(IPFlowInfo out) {
        // ExitNode enode = (ExitNode) node;
        ContextSensitiveValue cvcaller = getRet();
        if (cvcaller != null) {
            for (IPNode retNode : getReturnStmtNodes()) {
                Stmt stmt = retNode.getStmt();
                if (stmt instanceof JReturnStmt) {
                    // Value vv = ((JReturnStmt) stmt).getOp();
                    ContextSensitiveValue cvcallee = ContextSensitiveValue.getCValue(getContext(),
                            ((JReturnStmt) stmt).getOp());
                    if (!cvcallee.getValue().toString().contains("null")) {
                        // for (UniqueName un : out.getUniqueNames().get(cvcallee)) {
                        // out.putUname(cvcaller, un);
                        // }
                        Set<Definition> defs = out.getDefinitionsByCV(cvcallee);
                        Set<Definition> retdefs = new HashSet<>();
                        for (Definition def : defs) {
                            retdefs.add(Definition.getDefinition(def.definedValue, retNode));
                        }
                        out.putDefinition(cvcaller, retdefs);
                        // App.p("!!! " + cvcaller + ", " + cvcallee);
                    }
                }
            }
        }
    }
}
