package com.lumos.forward.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.glassfish.jaxb.runtime.v2.schemagen.Util;

import com.lumos.App;
import com.lumos.forward.Context;
import com.lumos.forward.ContextSensitiveValue;
import com.lumos.forward.Definition;
import com.lumos.forward.memory.AbstractAddress;
import com.lumos.forward.memory.AbstractAllocation;
import com.lumos.forward.memory.Memory;
import com.lumos.forward.memory.RefBasedAddress;
import com.lumos.utils.Utils;
import com.lumos.wire.IdentityWire;

import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
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
                if (!isSingleIdAssign() && iexpr instanceof InstanceInvokeExpr
                        && !(iexpr.getMethod().toString().contains("<init>"))) {
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
        // boolean toShow = false;
        // Set<ContextSensitiveValue> cvs = new HashSet<>(cvdefs);
        if (!stmt.toString().contains("<java.lang.Object: void <init>") && !stmt.toString().contains("findBy")
                && !stmt.toString().contains("save")) {
            for (ContextSensitiveValue cv : cvdefs) {
                String cname = cv.getValue().getType().toString();
                SootClass sc = App.searchClass(cname);
                if (sc != null || cname.contains("Object")) {
                    // toShow = true;
                    // break;
                    App.p("ooo " + stmt + "," + stmt.getJavaSourceStartLineNumber() + ", " + sc + ", " + cv);
                }
            }
        }
        this.type = "identity";
    }

    public boolean handleCollection(Memory out) {
        // Rules for composite data structures

        boolean handled = false;
        InvokeExpr iexpr = stmt.getInvokeExpr();
        ContextSensitiveValue cvlop = null, cvrop = null;
        Set<Definition> defs = new HashSet<>();
        if (stmt instanceof InvokeStmt) {
            if (iexpr instanceof InstanceInvokeExpr) {
                InstanceInvokeExpr inexpr = (InstanceInvokeExpr) iexpr;
                String tstr = inexpr.getBase().getType().toString();
                String mstr = inexpr.getMethod().getName();

                if (mstr.equals("add")
                        && (tstr.contains("List") || tstr.contains("Set"))) {
                    cvlop = ContextSensitiveValue.getCValue(context, inexpr.getBase());
                    cvrop = ContextSensitiveValue.getCValue(context, inexpr.getArg(0));

                    handled = true;
                } else if (mstr.equals("put")
                        && (tstr.contains("Map"))) {

                    cvlop = ContextSensitiveValue.getCValue(context, inexpr.getBase());
                    cvrop = ContextSensitiveValue.getCValue(context, inexpr.getArg(1));
                    handled = true;
                }
            }
            if (handled) {
                defs.addAll(out.getDefinitionsByCV(cvrop));
                Set<RefBasedAddress> unames = out.getUniqueNamesForCollection(cvlop);
                if (unames == null || unames.isEmpty()) {
                    App.p("This can't be unresolved");
                    App.panicni();
                }
                Set<Definition> currDefs = new HashSet<>();
                for (Definition def : defs) {
                    currDefs.add(Definition.getDefinition(def.definedValue, this));
                }
                // App.p("!!! " + this.stmt);
                // App.p(currDefs + "");
                // for (Definition def : out.getDefinitionsByCV(cvlop)) {
                // App.p(def.getDefinedLocation() + ", " + def.getDefinedValue());
                // }

                for (RefBasedAddress uname : unames) {
                    out.putDefinition(uname, currDefs);
                    // App.p(out.getCurrMapping().get(uname));
                }
                // App.p(unames);
                // App.p(unames + "");

            }
        }
        if (stmt instanceof JAssignStmt) {
            if (iexpr instanceof InstanceInvokeExpr) {
                InstanceInvokeExpr inexpr = (InstanceInvokeExpr) iexpr;
                String tstr = inexpr.getBase().getType().toString();
                String mstr = inexpr.getMethod().getName();
                // if (mstr.equals("iterator") && (tstr.contains("List") ||
                // tstr.contains("Set"))) {
                // cvlop = ContextSensitiveValue.getCValue(context, ((JAssignStmt)
                // stmt).getLeftOp());
                // cvrop = ContextSensitiveValue.getCValue(context, inexpr.getBase());
                // defs.addAll(out.getDefinitionsByCV(cvrop));
                // Set<Definition> currDefs = new HashSet<>();
                // for (Definition def : defs) {
                // currDefs.add(Definition.getDefinition(def.definedValue, this));
                // }
                // out.clearDefinition(cvlop);
                // out.putDefinition(cvlop, currDefs);
                // return true;
                // } else
                if (((mstr.equals("next") && tstr.contains("Iterator"))
                        || (mstr.equals("get") && tstr.contains("List"))
                        || (mstr.equals("get") && tstr.contains("Map")))) {
                    cvlop = ContextSensitiveValue.getCValue(context, ((JAssignStmt) stmt).getLeftOp());
                    cvrop = ContextSensitiveValue.getCValue(context, inexpr.getBase());
                    handled = true;
                }
            }
            if (handled) {
                // defs.addAll(out.getDefinitionsByCV(cvrop));
                Set<RefBasedAddress> unames = out.getUniqueNamesForCollection(cvrop);
                for (RefBasedAddress uname : unames) {
                    Set<Definition> collectionDefs = out.getCurrMapping().get(uname);

                    if (collectionDefs != null) {
                        defs.addAll(collectionDefs);
                    } else {
                        defs.add(Definition.getDefinition(
                                RefBasedAddress.getRefBasedAddress(new AbstractAllocation(cvrop, this),
                                        Collections.emptyList()),
                                this));
                    }
                }
                Set<Definition> currDefs = new HashSet<>();
                for (Definition def : defs) {
                    currDefs.add(Definition.getDefinition(def.definedValue, this));
                }
                out.clearDefinition(cvlop);
                out.putDefinition(cvlop, currDefs);
            }
        }
        return handled;
    }

    @Override
    public void flow(Memory out) {
        if (isEmptyInit()) {
            return;
        }

        if (App.showIDNodesOnly) {
            App.idnodes.add(this);
        }

        if (handleCollection(out)) {
            return;
        }
        // App.p("??? " + this.stmt);
        for (ContextSensitiveValue cvlop : cvdefs) {
            // if (isSafeOverwrite()) {
            out.clearDefinition(cvlop);
            // }
            if (idMode.equals("CONSERVATIVE") || ((cvuses.size() > 1 || cvuses.size() == 0)
                    || !isSingleIdAssign())) {
                RefBasedAddress un = RefBasedAddress.getRefBasedAddress(new AbstractAllocation(cvlop, this),
                        Collections.emptyList());
                out.putDefinition(cvlop, Definition.getDefinition(un, this));
                // if (Utils.isCompositeType(cvlop.getValue())) {
                // out.putDefinition(cvlop, Definition
                // .getDefinition(RefBasedAddress.getRefBasedAddress(cvlop,
                // Collections.emptyList()), this));
                // }
            } else {
                // Single Identity assignment
                for (ContextSensitiveValue cvrop : cvuses) {
                    // Set<Definition> defs = new HashSet<>();
                    // Set<RefBasedAddress> unames = out.getUniqueNamesForRef(cvlop);
                    // for (AbstractAddress ra : out.getCurrMapping().keySet()) {
                    // if (ra.getBase().equals(cvrop)) {
                    // defs.addAll(out.getCurrMapping().get(ra));
                    // }
                    // }
                    Set<Definition> defs = out.getDefinitionsByCV(cvrop);
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
