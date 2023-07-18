package com.lumos.common;

import soot.Unit;

public class Dependency {
    public enum DepType {
        RW, CF, CALL
    };

    public Unit unit;
    // public Value v;
    public DepType dtype;

    public Dependency(Unit unit, DepType dtype) {
        this.unit = unit;
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
        return "[" + this.typeString() + "] " + this.unit;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof Dependency)) {
            return false;
        }
        Dependency other = (Dependency) o;
        return this.unit.equals(other.unit) && this.dtype == other.dtype;
    }

    public int hashCode() {
        int hashValue = unit == null ? 0 : unit.hashCode();
        return hashValue + this.dtype.hashCode();
    }
}