package com.lumos.forward.memory;

import com.lumos.forward.ContextSensitiveValue;

public class CollectionContentAddress implements AbstractAddress {
    public ContextSensitiveValue base;

    public String type;

    @Override
    public String toString() {
        return base + "[" + type + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((base == null) ? 0 : base.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        CollectionContentAddress other = (CollectionContentAddress) obj;
        if (base == null) {
            if (other.base != null)
                return false;
        } else if (!base.equals(other.base))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    public CollectionContentAddress(ContextSensitiveValue base, String type) {
        this.base = base;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public ContextSensitiveValue getBase() {
        return base;
    }

    @Override
    public void setBase(ContextSensitiveValue cv) {
        this.base = cv;
    }

}