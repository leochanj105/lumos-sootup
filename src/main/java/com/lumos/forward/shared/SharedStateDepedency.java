package com.lumos.forward.shared;

import java.util.List;

import soot.SootFieldRef;

public class SharedStateDepedency {
    public String storeName;
    public List<String> refs;

    public SharedStateDepedency(String storeName, List<String> refs) {
        this.storeName = storeName;
        this.refs = refs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((storeName == null) ? 0 : storeName.hashCode());
        result = prime * result + ((refs == null) ? 0 : refs.hashCode());
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
        SharedStateDepedency other = (SharedStateDepedency) obj;
        if (storeName == null) {
            if (other.storeName != null)
                return false;
        } else if (!storeName.equals(other.storeName))
            return false;
        if (refs == null) {
            if (other.refs != null)
                return false;
        } else if (!refs.equals(other.refs))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SharedStateDepedency [storeName=" + storeName + ", refs=" + refs + "]";
    }

}
