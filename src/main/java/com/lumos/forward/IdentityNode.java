package com.lumos.forward;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lumos.App;
import com.lumos.wire.IdentityWire;

import fj.P;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
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
                if ((stmt instanceof JAssignStmt) && (iexpr instanceof InstanceInvokeExpr)) {
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

        this.type = "identity";
        // this.visible = cvuses.size() > 1 || IdentityWire.isSpecial(iexpr.toString());
    }

    @Override
    public void flow(IPFlowInfo out) {
        // EnterNode enode = (EnterNode) node;
        String stmtStr = this.stmt.getInvokeExpr().toString();
        if (stmtStr.contains("Object: void <init>")) {
            return;
        }

        // if (stmtStr.contains("javax.servlet.http.Cookie: java.lang.String
        // getValue()>")) {
        // return;
        // }

        if (App.showIPNodesOnly)

        {
            // if (cvuses.size() == 1) {
            App.idnodes.add(this);
            // }
        }

        // if ((!visible) && cvuses.size() > 1) {
        // App.panicni();
        // }

        for (ContextSensitiveValue cvlop : cvdefs) {
            UniqueName un = new UniqueName(cvlop);
            out.clearDefinition(cvlop);
            if (idMode.equals("CONSERVATIVE") || ((cvuses.size() > 1 || cvuses.size() == 0)
                    || !isSingleAssign())) {
                out.putDefinition(cvlop, Definition.getDefinition(un, this));
            } else {
                for (ContextSensitiveValue cvrop : cvuses) {
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

    @Override
    public boolean isSingleAssign() {
        return IdentityWire.findWire(this.stmt.getInvokeExpr().toString());
    }

    @Override
    public Set<ContextSensitiveValue> getUsed() {
        return cvuses;
    }

    @Override
    public String toString() {
        return "IdNode [" + stmt + "]";
    }

}
