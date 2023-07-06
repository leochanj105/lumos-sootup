package com.lumos;

import sootup.core.model.SootMethod;

public class InvokeWirePoint {
    public SootMethod sm;
    public int index;

    public InvokeWirePoint(SootMethod sm, int index) {
        this.sm = sm;
        this.index = index;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof InvokeWirePoint)) {
            return false;
        }
        InvokeWirePoint wp = (InvokeWirePoint) obj;
        return (this.sm.getSignature().equals(wp.sm.getSignature()) &&
                this.index == wp.index);
    }

    @Override
    public String toString() {
        return "(" + sm + ", " + index + ")";
    }

    @Override
    public int hashCode() {
        return this.sm.getSignature().hashCode() + this.index;
    }
}