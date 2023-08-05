package com.lumos.forward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.lumos.App;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.NullConstant;
import soot.jimple.StaticFieldRef;
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
    boolean isRemote;

    EnterNode enterNode;

    public boolean isRemote() {
        return isRemote;
    }

    public void setRemote(boolean isRemote) {
        this.isRemote = isRemote;
    }

    public EnterNode getEnterNode() {
        return enterNode;
    }

    public void setEnterNode(EnterNode enterNode) {
        this.enterNode = enterNode;
    }

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
        isRemote = false;
        // List<Stmt> rets = new ArrayList<>();
        // this.ret = ret;
    }

    @Override
    public String toString() {
        return "ExitNode [" + sm.getName() + ", " + context + ": " + stmt + "]";
    }

    @Override
    public Set<ContextSensitiveValue> getUsed() {
        return Collections.emptySet();
    }

    @Override
    public void flow(IPFlowInfo out) {
        // ExitNode enode = (ExitNode) node;
        ContextSensitiveValue cvcaller = getRet();

        if (isRemote) {
            for (UniqueName un : out.getCurrMapping().keySet()) {
                Context original = un.getBase().getContext();
                boolean modified = false;

                if (!this.context.parentOf(original)) {
                    for (Definition def : out.getCurrMapping().get(un)) {
                        if (def.getDefinedLocation() != null) {
                            if (this.context.strictParentOf(def.getDefinedLocation().getContext())) {
                                modified = true;
                                break;
                            }
                        }
                    }
                    if (modified) {
                        App.p("Warning: overwritting!!!!");
                        App.p(un + ", " + out.getCurrMapping().get(un));
                        if (un.getBase().getValue() instanceof StaticFieldRef) {
                            continue;
                        }

                        App.p(this.context + ", " + original);
                        App.panicni();
                    }
                }
            }
        }

        if (cvcaller != null) {
            for (IPNode retNode : getReturnStmtNodes()) {
                Stmt stmt = retNode.getStmt();
                if (stmt instanceof JReturnStmt) {
                    // Value vv = ((JReturnStmt) stmt).getOp();
                    ContextSensitiveValue cvcallee = ContextSensitiveValue.getCValue(retNode.getContext(),
                            ((JReturnStmt) stmt).getOp());
                    // if (!cvcallee.getValue().toString().contains("null")) {
                    // for (UniqueName un : out.getUniqueNames().get(cvcallee)) {
                    // out.putUname(cvcaller, un);
                    // }
                    Set<Definition> defs = out.getDefinitionsByCV(cvcallee);
                    Set<Definition> retdefs = new HashSet<>();
                    // if (cvcallee.getValue().toString().contains("$r1")) {

                    if (cvcallee.getValue() instanceof NullConstant) {
                        out.putDefinition(cvcallee, Definition.getDefinition(new UniqueName(cvcallee), retNode));
                        // App.p(cvcallee + ", " + retNode);
                        // App.panicni();
                        // }
                        // App.p("!!!!!!!! " + defs);
                    }
                    for (Definition def : defs) {
                        retdefs.add(Definition.getDefinition(def.definedValue, retNode));
                        // retdefs.add(Definition.getDefinition(def.definedValue, this));
                    }
                    out.putDefinition(cvcaller, retdefs);
                    // out.putDefinition(cvcaller, retdefs);
                    // App.p("!!! " + cvcaller + ", " + cvcallee);
                    // }
                }
            }
        }

    }
}
