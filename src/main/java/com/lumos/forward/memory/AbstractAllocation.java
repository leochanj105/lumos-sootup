package com.lumos.forward.memory;

import com.lumos.forward.ContextSensitiveValue;
import com.lumos.forward.node.IPNode;

public class AbstractAllocation {
    public ContextSensitiveValue cvalue;
    public IPNode allocationLoc;

    @Override
    public String toString() {
        return "[" + cvalue + ": " + allocationLoc + "]";
    }

    public AbstractAllocation(ContextSensitiveValue cvalue, IPNode allocationLoc) {
        this.cvalue = cvalue;
        this.allocationLoc = allocationLoc;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cvalue == null) ? 0 : cvalue.hashCode());
        result = prime * result + ((allocationLoc == null) ? 0 : allocationLoc.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractAllocation other = (AbstractAllocation) obj;
        if (cvalue == null) {
            if (other.cvalue != null)
                return false;
        } else if (!cvalue.equals(other.cvalue))
            return false;
        if (allocationLoc == null) {
            if (other.allocationLoc != null)
                return false;
        } else if (!allocationLoc.equals(other.allocationLoc))
            return false;
        return true;
    }

    public ContextSensitiveValue getCvalue() {
        return cvalue;
    }

    public void setCvalue(ContextSensitiveValue cvalue) {
        this.cvalue = cvalue;
    }

    public IPNode getAllocationLoc() {
        return allocationLoc;
    }

    public void setAllocationLoc(IPNode allocationLoc) {
        this.allocationLoc = allocationLoc;
    }

}
