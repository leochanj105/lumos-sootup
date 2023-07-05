package com.lumos;

import sootup.core.jimple.basic.Trap;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;

public class TracePoint {
    public Stmt stmt;
    public Value value;
    public int lineNumber;
    public String name;

    public TracePoint(Stmt stmt, Value value) {
        this.stmt = stmt;
        this.value = value;
        this.lineNumber = this.stmt.getPositionInfo().getStmtPosition().getFirstLine();
    }

    public TracePoint(Stmt stmt, Value value, String name) {
        this(stmt, value);
        this.name = name;
    }

    public String toString() {
        return "[" + (name != null ? name : value.toString()) + ", " + this.lineNumber + "]";
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TracePoint))
            return false;
        TracePoint other = (TracePoint) obj;
        return this.stmt.equals(other.stmt) && this.value.equals(other.value);
    }

    public int hashCode() {
        int hashStmt = stmt == null ? 0 : stmt.hashCode();
        int hashValue = value == null ? 0 : value.hashCode();
        return hashValue + hashStmt;
    }
}