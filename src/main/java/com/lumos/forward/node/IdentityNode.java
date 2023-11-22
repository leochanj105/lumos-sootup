package com.lumos.forward.node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lumos.App;
import com.lumos.forward.AbstractAddress;
import com.lumos.forward.Context;
import com.lumos.forward.ContextSensitiveValue;
import com.lumos.forward.Definition;
import com.lumos.forward.IPFlowInfo;
import com.lumos.forward.RefBasedAddress;
import com.lumos.utils.Utils;
import com.lumos.wire.IdentityWire;

import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInstanceFieldRef;

public class IdentityNode extends IPNode {

    // boolean visible;
    Set<ContextSensitiveValue> cvdefs, cvuses;

    // RADICAL means that we assume a function call do not have more than one defs
    // CONSERVATIVE means that everything is a def
    public static String idMode = "RADICAL";
    public static boolean warnForUnsafe = true;
    // public static String idMode = "CONSERVATIVE";

    public IdentityNode(Context context, Stmt stmt) {
        // super(context, stmt);
        // super();
        this.context = context;
        this.stmt = stmt;

        if (!(stmt.containsInvokeExpr())) {
            App.p("useless...");
            App.panicni();
        }

        InvokeExpr iexpr = stmt.getInvokeExpr();

        cvdefs = new HashSet<>();
        cvuses = new HashSet<>();

        if (idMode.equals("RADICAL")) {
            if (!App.safeList.contains(iexpr.getMethod().getSignature())) {
                if (warnForUnsafe) {
                    App.p("===[WARN]===\nUnsafe method: " + iexpr.getMethod().getSignature());
                }
            }
            if (iexpr.getArgs().size() > 0) {
                for (Value arg : iexpr.getArgs()) {
                    cvuses.add(ContextSensitiveValue.getCValue(context, arg));
                }
                // if ((stmt instanceof JAssignStmt) && (iexpr instanceof InstanceInvokeExpr)) {
                if (!isSingleIdAssign() && iexpr instanceof InstanceInvokeExpr) {
                    cvuses.add(ContextSensitiveValue.getCValue(context, ((InstanceInvokeExpr) iexpr).getBase()));
                }
            } else {
                if (iexpr instanceof StaticInvokeExpr) {
                    // cvuses.add(ContextSensitiveValue.getCValue(context, iexpr));
                } else {
                    cvuses.add(ContextSensitiveValue.getCValue(context, ((InstanceInvokeExpr) iexpr).getBase()));
                }
            }

            if (!(stmt instanceof JAssignStmt)) {
                // App.p(stmt);
                if (iexpr instanceof StaticInvokeExpr) {
                    if (iexpr.getArgs().size() > 0) {
                        cvdefs.add(ContextSensitiveValue.getCValue(context, iexpr.getArg(0)));
                    }
                } else {
                    cvdefs.add(ContextSensitiveValue.getCValue(context, ((InstanceInvokeExpr) iexpr).getBase()));
                }
            } else {
                cvdefs.add(ContextSensitiveValue.getCValue(context, ((JAssignStmt) stmt).getLeftOp()));
            }
        } else if (idMode.equals("CONSERVATIVE")) {
            for (Value arg : iexpr.getArgs()) {
                cvuses.add(ContextSensitiveValue.getCValue(context, arg));
                cvdefs.add(ContextSensitiveValue.getCValue(context, arg));
            }
            if (iexpr instanceof InstanceInvokeExpr) {
                cvuses.add(ContextSensitiveValue.getCValue(context, ((InstanceInvokeExpr) iexpr).getBase()));
                cvdefs.add(ContextSensitiveValue.getCValue(context, ((InstanceInvokeExpr) iexpr).getBase()));
            }
            if (stmt instanceof JAssignStmt) {
                cvdefs.add(ContextSensitiveValue.getCValue(context, ((JAssignStmt) stmt).getLeftOp()));
            }
        } else {
            App.panicni();
        }

        if (App.showInitOnly) {
            if (isSingleIdAssign()) {
                if (iexpr.getMethod().getSignature().contains("<init>") && !isEmptyInit()) {
                    App.initList.add(iexpr.getMethod().getSignature());
                }
            }
        }

        this.type = "identity";
    }

    public boolean handleComposite(IPFlowInfo out) {
        // Rules for composite data structures
        InvokeExpr iexpr = stmt.getInvokeExpr();
        if (stmt instanceof InvokeStmt) {
            if (iexpr instanceof InstanceInvokeExpr) {
                InstanceInvokeExpr inexpr = (InstanceInvokeExpr) iexpr;
                String tstr = inexpr.getBase().getType().toString();
                String mstr = inexpr.getMethod().getName();
                ContextSensitiveValue cvlop, cvrop;
                if (mstr.equals("add")
                        && (tstr.contains("List") || tstr.contains("Set"))) {
                    cvlop = ContextSensitiveValue.getCValue(context, inexpr.getBase());
                    cvrop = ContextSensitiveValue.getCValue(context, inexpr.getArg(0));
                    Set<Definition> defs = out.getDefinitionsByCV(cvrop);
                    out.putDefinition(cvlop, defs);
                    return true;
                } else if (mstr.equals("put")
                        && (tstr.contains("Map"))) {
                    cvlop = ContextSensitiveValue.getCValue(context, inexpr.getBase());
                    cvrop = ContextSensitiveValue.getCValue(context, inexpr.getArg(1));
                    Set<Definition> defs = out.getDefinitionsByCV(cvrop);
                    out.putDefinition(cvlop, defs);
                    return true;
                }
            }
        }
        if (stmt instanceof JAssignStmt) {
            ContextSensitiveValue cvlop, cvrop;
            if (iexpr instanceof InstanceInvokeExpr) {
                InstanceInvokeExpr inexpr = (InstanceInvokeExpr) iexpr;
                if ((inexpr.getMethod().getName().equals("iterator")
                        && inexpr.getBase().getType().toString().contains("List"))
                        // || (inexpr.getMethod().getName().equals("next")
                        // && inexpr.getBase().getType().toString().contains("Iterator"))
                        || (inexpr.getMethod().getName().equals("get")
                                && inexpr.getBase().getType().toString().contains("List"))) {
                    if (inexpr.getMethod().getName().equals("next")
                            && inexpr.getBase().getType().toString().contains("Iterator")) {
                        // App.p("xxxxxxx " + this);
                    }

                    cvlop = ContextSensitiveValue.getCValue(context,
                            ((JAssignStmt) stmt).getLeftOp());
                    out.clearDefinition(cvlop);
                    cvrop = ContextSensitiveValue.getCValue(context, inexpr.getBase());
                    Set<Definition> defs = out.getDefinitionsByCV(cvrop);
                    out.putDefinition(cvlop, defs);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void flow(IPFlowInfo out) {
        if (isEmptyInit()) {
            return;
        }

        if (App.showIDNodesOnly) {
            App.idnodes.add(this);
        }

        if (handleComposite(out)) {
            return;
        }

        for (ContextSensitiveValue cvlop : cvdefs) {

            if (isSafeOverwrite()) {
                out.clearDefinition(cvlop);
            }
            // App.p("xxxxxx " + this.stmt);
            if (idMode.equals("CONSERVATIVE") || ((cvuses.size() > 1 || cvuses.size() == 0)
                    || !isSingleIdAssign())) {
                RefBasedAddress un = new RefBasedAddress(cvlop);
                out.putDefinition(cvlop, Definition.getDefinition(un, this));
            } else {
                for (ContextSensitiveValue cvrop : cvuses) {
                    Set<Definition> defs = new HashSet<>();
                    for (AbstractAddress ra : out.getCurrMapping().keySet()) {
                        if (ra.getBase().equals(cvrop)) {
                            defs.addAll(out.getCurrMapping().get(ra));
                        }
                    }
                    // Set<Definition> defs = out.getDefinitionsByCV(cvrop);
                    Set<Definition> newdefs = new HashSet<>();
                    for (Definition def : defs) {
                        newdefs.add(Definition.getDefinition(def.getDefinedValue(), this));
                    }
                    out.putDefinition(cvlop, newdefs);
                }

            }
        }

    }

    public boolean isSafeOverwrite() {
        return isSingleIdAssign() || (stmt instanceof JAssignStmt);
    }

    @Override
    public boolean isSingleIdAssign() {
        return isEmptyInit() || (cvuses.size() == 1
                && IdentityWire.findWire(this.stmt.getInvokeExpr().getMethod().getSignature().toString()));
    }

    public boolean isEmptyInit() {
        SootMethod sm = stmt.getInvokeExpr().getMethod();
        return sm.getName().contains("<init>") && sm.getParameterCount() == 0;
    }

    @Override
    public Set<ContextSensitiveValue> getUsed() {
        return getUsed(null);
    }

    @Override
    public Set<ContextSensitiveValue> getUsed(Set<ContextSensitiveValue> implicits) {
        Set<ContextSensitiveValue> results = new HashSet<>();
        for (ContextSensitiveValue cv : cvuses) {
            Value v = cv.getValue();
            String tstr = v.getType().toString();
            if (v instanceof Local && Utils.isCompositeType(tstr)) {
                if (implicits != null) {
                    implicits.add(cv);
                }
            } else {
                results.add(cv);
            }
        }
        return results;
    }

    @Override
    public String toString() {
        return "IdNode [" + stmt + "]";
    }

}
