package com.lumos.forward;

import soot.jimple.Stmt;

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
        return "StmtNode [stmt=" + stmt + "]";
    }

}
