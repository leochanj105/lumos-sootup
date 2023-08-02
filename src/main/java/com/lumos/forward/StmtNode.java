package com.lumos.forward;

import java.util.HashSet;
import java.util.Set;

import com.lumos.App;

import soot.Local;
import soot.RefLikeType;
import soot.Value;
import soot.jimple.Constant;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JInstanceFieldRef;

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
            ContextSensitiveValue cvlop = ContextSensitiveValue.getCValue(getContext(), lop);
            ContextSensitiveValue cvrop = ContextSensitiveValue.getCValue(getContext(), rop);

            Set<Definition> defs = new HashSet<>();
            if ((rop instanceof Local) || (rop instanceof JInstanceFieldRef) || (rop instanceof Constant)) {
                defs.addAll(out.getDefinitionsByCV(cvrop));
            } else {
                defs.add(Definition.getDefinition(new UniqueName(cvlop), this));
            }
            // Set<Definition> defs = out.getDefinitionsByCV(cvrop);
            if (lop instanceof Local) {
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
                    out.putDefinition(cvlop, newdefs);
                }
                // if(cvlop)
            } else if (lop instanceof JInstanceFieldRef) {
                Set<UniqueName> unames = out.getUniqueNamesForRef(cvlop);
                Set<Definition> possibleDefinitions = out.getDefinitionsByCV(cvrop);
                if (unames == null || unames.isEmpty()) {
                    App.p("This can't be unresolved");
                }

                Set<Definition> currDefs = new HashSet<>();

                for (Definition def : possibleDefinitions) {
                    currDefs.add(Definition.getDefinition(def.definedValue, this));
                }

                for (UniqueName uname : unames) {
                    if (unames.size() == 1) {
                        out.putDefinition(uname, currDefs);
                    } else {
                        out.getCurrMapping().get(uname).addAll(currDefs);
                    }
                }
            }

            else {
                App.panicni();
            }
        }
    }
}
