package com.lumos.forward.node;

import java.util.HashSet;
import java.util.Set;

import com.lumos.App;
import com.lumos.forward.Context;
import com.lumos.forward.ContextSensitiveValue;
import com.lumos.forward.Definition;
import com.lumos.forward.memory.Memory;
import com.lumos.forward.memory.RefBasedAddress;
import com.lumos.utils.Utils;

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
import soot.jimple.internal.JIfStmt;
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
    public void flow(Memory out) {
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
            // Make sure a field variable is "static"
            if (lop instanceof StaticFieldRef) {
                cvlop = ContextSensitiveValue.getCValue(Context.emptyContext(), lop);
            } else {
                cvlop = ContextSensitiveValue.getCValue(getContext(), lop);
            }

            if (rop instanceof StaticFieldRef) {

                cvrop = ContextSensitiveValue.getCValue(Context.emptyContext(), rop);
            } else {
                cvrop = ContextSensitiveValue.getCValue(getContext(), rop);
            }

            Set<Definition> defs = new HashSet<>();
            if ((rop instanceof Local) || (rop instanceof JInstanceFieldRef) || (rop instanceof Constant)
                    || (rop instanceof StaticFieldRef)) {
                defs.addAll(out.getDefinitionsByCV(cvrop));
            } else {
                // App.p("xxxx " + this.stmt);
                defs.add(Definition.getDefinition(new RefBasedAddress(cvlop), this));
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
                        RefBasedAddress value = def.getDefinedValue();
                        newdefs.add(Definition.getDefinition(value, this));
                    }
                    out.clearDefinition(cvlop);
                    out.putDefinition(cvlop, newdefs);
                }

                // if (cvrop.toString().contains("this.<inside_payment.domain.AddMoney")) {

                // App.p(defs.size());
                // }
            } else if (lop instanceof JInstanceFieldRef) {
                Set<RefBasedAddress> unames = out.getUniqueNamesForRef(cvlop);

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
                for (RefBasedAddress uname : unames) {
                    if (unames.size() == 1) {
                        out.clearDefinition(uname);
                        out.putDefinition(uname, currDefs);
                    } else {
                        if (!out.getCurrMapping().containsKey(uname)) {
                            App.p2(uname);
                            App.p(this.getContext());
                            App.p2(stmt.getJavaSourceStartLineNumber() + ": " + this.stmt);

                        }
                        out.putDefinition(uname, currDefs);
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
        return getUsed(null);
    }

    @Override
    public Set<ContextSensitiveValue> getUsed(Set<ContextSensitiveValue> implicits) {
        Set<ContextSensitiveValue> cvused = new HashSet<>();
        if (stmt instanceof JReturnStmt) {
            Value ret = ((JReturnStmt) stmt).getOp();
            cvused.add(ContextSensitiveValue.getCValue(getContext(), ret));
        } else {

            // This "banned" is to ensure that if a reference is used, its
            // base is not added
            Set<Value> banned = new HashSet<>();
            App.p("-------");
            for (ValueBox vbox : stmt.getUseBoxes()) {

                Value use = vbox.getValue();
                // App.p(use + ", ___" + use.getUseBoxes().size() + ", ___" + use.getClass());

                if (banned.contains(use)) {
                    continue;
                }
                // Left Op is ref
                if ((stmt instanceof JAssignStmt)
                        && ((JAssignStmt) stmt).getLeftOp().getUseBoxes().contains(vbox)) {
                    if (implicits != null) {
                        implicits.add(ContextSensitiveValue.getCValue(getContext(), vbox.getValue()));
                        // App.p("Implicit: " + vbox.getValue());
                    }
                    continue;
                }

                if ((use instanceof Local) || (use instanceof JInstanceFieldRef) || (use instanceof StaticFieldRef)
                        || (use instanceof Constant)) {
                    if (use instanceof JInstanceFieldRef) {
                        banned.add(((JInstanceFieldRef) use).getBase());
                        if (implicits != null) {
                            implicits
                                    .add(ContextSensitiveValue.getCValue(context, ((JInstanceFieldRef) use).getBase()));
                            App.p("Implicit: " + ((JInstanceFieldRef) use).getBase());
                        }
                    }
                    String tstr = use.getType().toString();
                    if ((use instanceof Local) && (stmt instanceof JAssignStmt) && Utils.isCompositeType(tstr)) {
                        if (implicits != null) {
                            // App.p(implicits);
                            implicits.add(ContextSensitiveValue.getCValue(context, use));
                            App.p("Implicit: " + use + ", " + tstr);
                        }
                    } else {
                        ContextSensitiveValue cvuse = ContextSensitiveValue.getCValue(getContext(), use);
                        cvused.add(cvuse);
                        App.p("Use: " + use + ", " + use.getClass() + ", " + use.getType());
                    }

                } else {
                    App.p(use + ", ___" + use.getUseBoxes().size() + ", ___" + use.getClass());
                }
            }

        }
        return cvused;
    }

    @Override
    public boolean isSingleIdAssign() {
        // return !(this.stmt instanceof JIfStmt) && getUsed().size() <= 1;

        if (stmt instanceof JReturnStmt) {
            return true;
        }
        if (!(stmt instanceof JAssignStmt)) {
            return false;
        }
        Value rightop = ((JAssignStmt) stmt).getRightOp();
        // int leftFieldRef = (((JAssignStmt) stmt).getLeftOp() instanceof
        // JInstanceFieldRef) ? 1 : 0;
        // Set<ContextSensitiveValue> implicits = new HashSet<>();
        // Set<ContextSensitiveValue> explicits = getUsed(implicits);
        // if (implicits.size() + explicits.size() - leftFieldRef <= 1) {
        // return true;
        // }
        return (rightop instanceof Local) || (rightop instanceof JInstanceFieldRef) ||
                (rightop instanceof StaticFieldRef) || (rightop instanceof Constant) ||
                (rightop instanceof JCastExpr);
    }

}
