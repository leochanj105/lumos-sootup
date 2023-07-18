package com.lumos.common;

import org.checkerframework.checker.units.qual.min;

import com.lumos.analysis.MethodInfo;

import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.MethodSignature;

public class InstrumentPoint {
    public Stmt stmt;
    public Value value;
    public MethodSignature msig;

    public String name;
    public int lineNum = 0;
    public boolean isBefore;

    public InstrumentPoint(Stmt stmt, Value value, MethodSignature msig, boolean isBefore) {
        this.stmt = stmt;
        this.value = value;
        this.msig = msig;
        this.isBefore = isBefore;
        if (this.value == null) {
            this.name = "L" + stmt.getPositionInfo().getStmtPosition().getFirstLine();
        } else {
            this.name = this.value.toString();
        }
    }

    public InstrumentPoint(Stmt stmt, Value value, MethodSignature msig) {
        this(stmt, value, msig, false);
    }

    public InstrumentPoint(TracePoint tp, MethodSignature msig) {
        this(tp.stmt, tp.value, msig);
        if (tp.name == null) {
            this.name = tp.value.toString();
        } else {
            this.name = tp.name;
        }
    }

    public InstrumentPoint(Stmt stmt, Value value, MethodInfo minfo, boolean isBefore) {
        this(stmt, value, minfo.sm.getBody().getMethodSignature(), isBefore);
    }

    public InstrumentPoint(Stmt stmt, Value value, MethodInfo minfo) {
        this(stmt, value, minfo.sm.getBody().getMethodSignature());
    }

    public InstrumentPoint(TracePoint tp, MethodInfo minfo) {
        this(tp.stmt, tp.value, minfo);
        if (tp.name == null) {
            this.name = tp.value.toString();
        } else {
            this.name = tp.name;
        }
    }

    public String toString() {
        return "[" + name + " " + (this.isBefore ? "before"
                : "after") + " " + stmt + "  line " + stmt.getPositionInfo().getStmtPosition().getFirstLine()
                + " in " + msig + "]";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof InstrumentPoint)) {
            return false;
        }
        InstrumentPoint other = (InstrumentPoint) o;
        return other.stmt.equals(this.stmt)
                && (other.value == null ? this.value == null : other.value.equals(this.value))
                && other.msig.equals(this.msig)
                && other.isBefore == this.isBefore;
    }

    @Override
    public int hashCode() {
        return this.stmt.hashCode() + (this.value == null ? 0 : this.value.hashCode()) + this.msig.hashCode()
                + (this.isBefore ? 1 : 0);
    }
}
