package com.lumos.forward;

import java.util.HashSet;
import java.util.Set;

import com.lumos.App;

import soot.Local;
import soot.RefLikeType;
import soot.Value;
import soot.ValueBox;
import soot.jimple.Constant;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JStaticInvokeExpr;

public class StmtNode extends IPNode {
    // Context context;
    // Stmt stmt;

    public Context getContext() {
        return this.context;
    }

    public StmtNode(Context context, Stmt stmt) {
        this.context = context;
        this.stmt = stmt;
        this.type = "stmt";
    }

    // public void setContext(Context context) {
    // this.context = context;
    // }

    public Stmt getStmt() {
        return this.stmt;
    }

    public void setStmt(Stmt stmt) {
        this.stmt = stmt;
    }

    public StmtNode(Stmt stmt) {
        super();
        this.stmt = stmt;
    }

    @Override
    public String toString() {
        return "StmtNode [stmt=" + stmt + (App.showLineNum ? ", " + stmt.getJavaSourceStartLineNumber() : "") + "]";
    }

    @Override
    public void flow(IPFlowInfo out) {
        Stmt stmt = this.getStmt();
        if (stmt instanceof JIdentityStmt) {
            return;
        } else if (stmt instanceof JAssignStmt) {
            JAssignStmt astmt = (JAssignStmt) stmt;
            Value lop = astmt.getLeftOp();
            Value rop = astmt.getRightOp();
            if (rop instanceof JCastExpr) {
                rop = ((JCastExpr) rop).getOp();
            }
            ContextSensitiveValue cvlop = null;
            ContextSensitiveValue cvrop = null;
            if (lop instanceof StaticFieldRef) {
                cvlop = ContextSensitiveValue.getCValue(Context.emptyContext(), lop);
                // Value xx = ((StaticFieldRef) lop).getField();
            } else {
                cvlop = ContextSensitiveValue.getCValue(getContext(), lop);
            }

            if (rop instanceof StaticFieldRef) {
                cvrop = ContextSensitiveValue.getCValue(Context.emptyContext(), rop);
            } else {
                cvrop = ContextSensitiveValue.getCValue(getContext(), rop);
            }
            // ContextSensitiveValue cvrop = ContextSensitiveValue.getCValue(getContext(),
            // rop);

            Set<Definition> defs = new HashSet<>();
            if ((rop instanceof Local) || (rop instanceof JInstanceFieldRef) || (rop instanceof Constant)
                    || (rop instanceof StaticFieldRef)) {
                defs.addAll(out.getDefinitionsByCV(cvrop));
            } else {
                defs.add(Definition.getDefinition(new UniqueName(cvrop), this));
            }
            // Set<Definition> defs = out.getDefinitionsByCV(cvrop);
            if ((lop instanceof Local) || (lop instanceof StaticFieldRef)) {
                // Set<Definition> defs = out.getDefinitionsByCV(cvrop);

                if (defs.isEmpty()) {
                    if (lop.getType() instanceof RefLikeType) {
                        App.p("This is not possible...");
                        App.panicni();
                        // out.putUname(cvlop);
                    } else {
                        App.p("oh no........");
                        App.p(this);
                        App.panicni();
                    }
                } else {
                    Set<Definition> newdefs = new HashSet<>();
                    for (Definition def : defs) {
                        UniqueName value = def.getDefinedValue();
                        newdefs.add(Definition.getDefinition(value, this));
                    }
                    out.clearDefinition(cvlop);
                    out.putDefinition(cvlop, newdefs);
                }

                // if (cvrop.toString().contains("this.<inside_payment.domain.AddMoney")) {

                // App.p(defs.size());
                // }
            } else if (lop instanceof JInstanceFieldRef) {
                Set<UniqueName> unames = out.getUniqueNamesForRef(cvlop);

                Set<Definition> possibleDefinitions = out.getDefinitionsByCV(cvrop);
                // if (context.toString().contains("doErrorQueue,sendOrderCancel,<init>")) {
                // App.p("!!! " + cvlop + ", " + unames);
                // }
                if (unames == null || unames.isEmpty()) {
                    App.p("This can't be unresolved");
                }

                Set<Definition> currDefs = new HashSet<>();

                for (Definition def : possibleDefinitions) {
                    currDefs.add(Definition.getDefinition(def.definedValue, this));
                }

                for (UniqueName uname : unames) {
                    if (unames.size() == 1) {
                        out.clearDefinition(uname);
                        out.putDefinition(uname, currDefs);
                    } else {
                        out.getCurrMapping().get(uname).addAll(currDefs);
                    }
                }
            } else {
                App.p(stmt);
                App.panicni();
            }
        }
    }

    @Override
    public Set<ContextSensitiveValue> getUsed() {
        Set<ContextSensitiveValue> cvused = new HashSet<>();
        if (stmt instanceof JReturnStmt) {
            Value ret = ((JReturnStmt) stmt).getOp();
            cvused.add(ContextSensitiveValue.getCValue(getContext(), ret));
        } else {
            // JAssignStmt astmt = (JAssignStmt) stmt;
            Set<Value> banned = new HashSet<>();
            for (ValueBox vbox : stmt.getUseBoxes()) {
                Value use = vbox.getValue();

                if (banned.contains(use)) {
                    continue;
                }
                if ((stmt instanceof JAssignStmt)
                        && ((JAssignStmt) stmt).getLeftOp().getUseBoxes().contains(vbox)) {
                    continue;
                }

                if ((use instanceof Local) || (use instanceof JInstanceFieldRef)) {
                    ContextSensitiveValue cvuse = ContextSensitiveValue.getCValue(getContext(), use);
                    cvused.add(cvuse);
                    if (use instanceof JInstanceFieldRef) {
                        banned.add(((JInstanceFieldRef) use).getBase());
                    }
                }
            }

        }
        return cvused;
    }
}
