package com.lumos.forward;

import soot.jimple.Stmt;

public class WrapperNode extends IPNode {
    public EnterNode enter;
    public ExitNode exit;

    public WrapperNode(Context context, EnterNode enter, ExitNode exit, Stmt stmt) {
        // super(stmt);
        this.stmt = stmt;
        this.enter = enter;
        this.exit = exit;
        this.type = "wrapper";
        this.context = context;
    }

    public EnterNode getEnter() {
        return enter;
    }

    public void setEnter(EnterNode enter) {
        this.enter = enter;
    }

    public ExitNode getExit() {
        return exit;
    }

    public void setExit(ExitNode exit) {
        this.exit = exit;
    }

}
