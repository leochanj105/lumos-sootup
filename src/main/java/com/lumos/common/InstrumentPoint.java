package com.lumos.common;

import com.lumos.analysis.MethodInfo;

import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.MethodSignature;

public class InstrumentPoint {
    public Stmt stmt;
    public Value value;
    public MethodSignature msig;

    public InstrumentPoint(Stmt stmt, Value value, MethodSignature msig) {
        this.stmt = stmt;
        this.value = value;
        this.msig = msig;
    }

    public InstrumentPoint(TracePoint tp, MethodSignature msig) {
        this(tp.stmt, tp.value, msig);
    }

    public InstrumentPoint(Stmt stmt, Value value, MethodInfo minfo) {
        this(stmt, value, minfo.wsm.getBody().getMethodSignature());
    }

    public InstrumentPoint(TracePoint tp, MethodInfo minfo) {
        this(tp.stmt, tp.value, minfo);
    }
}
