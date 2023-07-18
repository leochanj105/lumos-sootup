package com.lumos.common;

// import org.checkerframework.checker.units.qual.min;

import com.lumos.analysis.MethodInfo;

import soot.Unit;
import soot.Value;
import soot.jimple.internal.AbstractInvokeExpr;

public class InstrumentPoint {
    public Unit unit;
    public Value value;
    public String msig;

    public String name;
    public int lineNum = 0;
    public boolean isBefore;

    public InstrumentPoint(Unit unit, Value value, String msig, boolean isBefore) {
        // AbstractInvokeExpr iexpr;
        // iexpr.getMethod().get
        this.unit = unit;
        this.value = value;
        this.msig = msig;
        this.isBefore = isBefore;
        if (this.value == null) {
            this.name = "L" + unit.getJavaSourceStartLineNumber();
        } else {
            this.name = this.value.toString();
        }
    }

    public InstrumentPoint(Unit unit, Value value, String msig) {
        this(unit, value, msig, false);
    }

    public InstrumentPoint(TracePoint tp, String msig) {
        this(tp.unit, tp.value, msig);
        if (tp.name == null) {
            this.name = tp.value.toString();
        } else {
            this.name = tp.name;
        }
    }

    public InstrumentPoint(Unit unit, Value value, MethodInfo minfo, boolean isBefore) {
        this(unit, value, minfo.sm.getSignature(), isBefore);
    }

    public InstrumentPoint(Unit unit, Value value, MethodInfo minfo) {
        this(unit, value, minfo.sm.getSignature());
    }

    public InstrumentPoint(TracePoint tp, MethodInfo minfo) {
        this(tp.unit, tp.value, minfo);
        if (tp.name == null) {
            this.name = tp.value.toString();
        } else {
            this.name = tp.name;
        }
    }

    public String toString() {
        return "[" + name + " " + (this.isBefore ? "before"
                : "after") + " " + unit + "  line " + unit.getJavaSourceStartLineNumber()
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
        return other.unit.equals(this.unit)
                && (other.value == null ? this.value == null : other.value.equals(this.value))
                && other.msig.equals(this.msig)
                && other.isBefore == this.isBefore;
    }

    @Override
    public int hashCode() {
        return this.unit.hashCode() + (this.value == null ? 0 : this.value.hashCode()) + this.msig.hashCode()
                + (this.isBefore ? 1 : 0);
    }
}
