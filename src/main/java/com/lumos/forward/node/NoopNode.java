package com.lumos.forward.node;

import java.util.Collections;
import java.util.Set;

import com.lumos.App;
import com.lumos.forward.Context;
import com.lumos.forward.ContextSensitiveValue;
import com.lumos.forward.memory.Memory;

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
    public void flow(Memory out) {

    }

    @Override
    public boolean isSingleIdAssign() {
        return false;
    }

}
