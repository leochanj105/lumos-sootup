package com.lumos;

import sootup.core.model.SootMethod;

public class ReturnWirePoint {
    public SootMethod sm;

    public ReturnWirePoint(SootMethod sm) {
        this.sm = sm;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ReturnWirePoint)) {
            return false;
        }
        ReturnWirePoint wp = (ReturnWirePoint) obj;
        return this.sm.getSignature().equals(wp.sm.getSignature());
    }

    @Override
    public String toString() {
        return "(" + sm + ")";
    }

    @Override
    public int hashCode() {
        return this.sm.getSignature().hashCode();
    }
}