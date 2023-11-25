package com.lumos.forward.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.glassfish.jaxb.core.v2.model.core.Ref;

import com.lumos.App;
import com.lumos.forward.Context;
import com.lumos.forward.ContextSensitiveValue;
import com.lumos.forward.Definition;
import com.lumos.forward.memory.AbstractAddress;
import com.lumos.forward.memory.Memory;
import com.lumos.forward.memory.RefBasedAddress;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Constant;
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
    // public CallSite lastCall;
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
        // List<CallSite> ctrace = context.getCtrace();
        // lastCall = context.getLastCallSite();
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
    public void flow(Memory out) {
        // ExitNode enode = (ExitNode) node;
        ContextSensitiveValue cvcaller = getRet();

        if (isRemote) {
            for (AbstractAddress addr : out.getCurrMapping().keySet()) {
                if (addr instanceof RefBasedAddress) {
                    RefBasedAddress un = (RefBasedAddress) addr;
                    Context original = un.getBase().getContext();
                    // App.p(original + ", " + un);
                    boolean modified = false;
                    // if (!this.context.parentOf(original)) {
                    if (!this.context.strictParentOf(original)) {
                        for (Definition def : out.getCurrMapping().get(un)) {
                            if (def.getDefinedLocation() != null) {
                                Context remoteContext = this.getReturnStmtNodes().get(0).getContext();
                                // if (this.context.strictParentOf(def.getDefinedLocation().getContext())) {
                                if (remoteContext.parentOf(def.getDefinedLocation().getContext())) {
                                    modified = true;
                                    break;
                                }
                            }
                        }
                        if (modified) {
                            App.p("Warning: overwritting!!!!");
                            App.p("Curr value: " + un + ", " + out.getCurrMapping().get(un));
                            for (Definition def : out.getCurrMapping().get(un)) {
                                App.p("Def of curr: " + def.getDefinedLocation() + ", " + def.getDefinedValue());
                                if (def.getDefinedLocation() != null) {
                                    App.p(def.getDefinedLocation().getDescription());
                                }
                            }
                            if (un.getBase().getValue() instanceof StaticFieldRef) {
                                continue;
                            }

                            App.p("original base context: " + original);
                            App.p("This node is: " + this);
                            App.panicni();
                        }
                    }
                } else {
                    App.p("CollectionContentAddress not implemented!");
                    App.panicni();
                }
            }
        }

        if (cvcaller != null) {
            for (IPNode retNode : getReturnStmtNodes()) {
                Stmt stmt = retNode.getStmt();
                if (stmt instanceof JReturnStmt) {
                    ContextSensitiveValue cvcallee = ContextSensitiveValue.getCValue(retNode.getContext(),
                            ((JReturnStmt) stmt).getOp());
                    Set<Definition> defs = out.getDefinitionsByCV(cvcallee);
                    Set<Definition> retdefs = new HashSet<>();

                    // if (cvcallee.getValue() instanceof NullConstant) {
                    if (cvcallee.getValue() instanceof Constant) {
                        out.putDefinition(cvcallee,
                                Definition.getDefinition(RefBasedAddress.getRefBasedAddress(cvcallee), retNode));
                    }
                    for (Definition def : defs) {
                        retdefs.add(Definition.getDefinition(def.definedValue, retNode));
                    }
                    out.putDefinition(cvcaller, retdefs);
                }
            }
        }
        gc(out);

    }

    public void gc(Memory out) {
        Set<AbstractAddress> toEvict = new HashSet<>();
        for (AbstractAddress addr : out.getCurrMapping().keySet()) {
            if (addr instanceof RefBasedAddress) {
                RefBasedAddress raddr = (RefBasedAddress) addr;
                if (raddr.getSuffix().size() == 0) {
                    ContextSensitiveValue cv = raddr.getBase();

                }
            } else {
                App.panicni();
            }
        }
    }

    @Override
    public boolean isSingleIdAssign() {
        return false;
    }

}
