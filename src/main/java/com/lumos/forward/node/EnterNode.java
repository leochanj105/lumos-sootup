package com.lumos.forward.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lumos.App;
import com.lumos.forward.CallSite;
import com.lumos.forward.Context;
import com.lumos.forward.ContextSensitiveValue;
import com.lumos.forward.Definition;
import com.lumos.forward.memory.Memory;

// import fj.data.HashSet;
// import polyglot.ast.Call;
import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;

public class EnterNode extends IPNode {
    public SootMethod sm;

    // public Context context;
    public ExitNode exitNode;

    public List<Value> arguments;
    public List<Local> parameters;
    public List<List<ContextSensitiveValue>> aliasPairs;
    public CallSite lastCall;

    boolean isRemote;

    public ExitNode getExitNode() {
        return exitNode;
    }

    public void setExitNode(ExitNode exitNode) {
        this.exitNode = exitNode;
    }

    public boolean isRemote() {
        return isRemote;
    }

    public void setRemote(boolean isRemote) {
        this.isRemote = isRemote;
    }

    public ContextSensitiveValue getAlias(ContextSensitiveValue toresolve) {
        for (List<ContextSensitiveValue> pair : aliasPairs) {
            if (pair.get(1).equals(toresolve)) {
                return pair.get(0);
            }
        }
        return null;
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
        // List<CallSite> ctrace = context.getCtrace();
        // lastCall = ctrace.get(ctrace.size() - 1);
        // this.arguments = lastCall.getCallingStmt().getInvokeExpr().getArgs();
        // this.arguments = stmt.getInvokeExpr().getArgs();
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

    @Override
    public boolean isSingleIdAssign() {
        return false;
    }

    public void addAlising(ContextSensitiveValue v1, ContextSensitiveValue v2) {
        this.aliasPairs.add(Arrays.asList(new ContextSensitiveValue[] { v1, v2 }));
    }

    @Override
    public void flow(Memory out) {
        for (List<ContextSensitiveValue> aliasp : getAliasPairs()) {
            ContextSensitiveValue cv1 = aliasp.get(0);
            ContextSensitiveValue cv2 = aliasp.get(1);

            Set<Definition> defs = out.getDefinitionsByCV(cv1);
            out.clearDefinition(cv2);
            out.putDefinition(cv2, defs);

            if (cv2.toString().contains("login")) {
                for (Definition def : defs) {
                    // App.p("Enter: " + cv1 + ", " + cv2 + ", " + def.d());
                }
            }
        }

    }

    @Override
    public Set<ContextSensitiveValue> getUsed() {
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return "EnterNode [" + sm.getName() + ", " + context + "]";
    }
}
