package com.lumos.forward;

import java.util.Collections;
import java.util.Set;

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

    @Override
    public Set<ContextSensitiveValue> getUsed() {
        return Collections.emptySet();
    }

    @Override
    public void flow(IPFlowInfo out) {

    }

    @Override
    public boolean isSingleIdAssign() {
        return false;
    }

}
