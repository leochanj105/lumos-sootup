package com.lumos.forward;

import com.lumos.App;

import soot.jimple.Stmt;

public class NoopNode extends IPNode {
    public NoopNode(Context context, Stmt stmt) {
        this.context = context;
        this.stmt = stmt;
        this.type = "noop";
    }

    @Override
    public String toString() {
        return "NoopNode [" + this.stmt + (App.showLineNum ? ", " + stmt.getJavaSourceStartLineNumber() : "") + "]";
    }

}
