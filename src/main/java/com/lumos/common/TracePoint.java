package com.lumos.common;

import soot.Unit;
import soot.Value;

public class TracePoint {
    public Unit unit;
    public Value value;
    public int lineNumber;
    public String name;

    public boolean isTraceable = false;

    public TracePoint(Unit unit, Value value) {
        this.unit = unit;
        this.value = value;
        this.lineNumber = this.unit.getJavaSourceStartLineNumber();
    }

    public TracePoint(Unit unit, Value value, String name) {
        this(unit, value);
        this.name = name;
    }

    public String toString() {
        return "<" + (name != null ? name : value.toString()) + ", " + this.lineNumber + ", " + unit + ">";
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TracePoint))
            return false;
        TracePoint other = (TracePoint) obj;
        return this.unit.equals(other.unit) && this.value.equals(other.value);
    }

    public int hashCode() {
        int hashStmt = unit == null ? 0 : unit.hashCode();
        int hashValue = value == null ? 0 : value.hashCode();
        return hashValue + hashStmt;
    }
}