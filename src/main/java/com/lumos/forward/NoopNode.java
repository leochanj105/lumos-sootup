package com.lumos.forward;

import soot.jimple.Stmt;

public class NoopNode extends IPNode {
    public NoopNode(Context context, Stmt stmt) {
        this.context = context;
        this.stmt = stmt;
        this.type = "noop";
    }
}