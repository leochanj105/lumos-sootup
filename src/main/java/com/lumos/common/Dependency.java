package com.lumos.common;

import sootup.core.jimple.common.stmt.Stmt;

public class Dependency {
    public enum DepType {
        RW, CF, CALL
    };

    public Stmt stmt;
    // public Value v;
    public DepType dtype;

    public Dependency(Stmt stmt, DepType dtype) {
        this.stmt = stmt;
        // this.v = v;
        this.dtype = dtype;
    }

    public String typeString() {
        switch (this.dtype) {
            case RW:
                return "RW";
            case CF:
                return "CF";
            case CALL:
                return "CALL";
        }
        return null;
    }

    @Override
    public String toString() {
        return "[" + this.typeString() + "] " + this.stmt;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof Dependency)) {
            return false;
        }
        Dependency other = (Dependency) o;
        return this.stmt.equals(other.stmt) && this.dtype == other.dtype;
    }

    public int hashCode() {
        int hashValue = stmt == null ? 0 : stmt.hashCode();
        return hashValue + this.dtype.hashCode();
    }
}