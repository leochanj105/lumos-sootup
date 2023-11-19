package com.lumos.forward.shared;

import java.util.List;
import java.util.Set;

import com.lumos.forward.IPNode;

public class SharedStateWrite {
    public IPNode wnode;
    public Set<String> fields;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((wnode == null) ? 0 : wnode.hashCode());
        result = prime * result + ((fields == null) ? 0 : fields.hashCode());
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
        SharedStateWrite other = (SharedStateWrite) obj;
        if (wnode == null) {
            if (other.wnode != null)
                return false;
        } else if (!wnode.equals(other.wnode))
            return false;
        if (fields == null) {
            if (other.fields != null)
                return false;
        } else if (!fields.equals(other.fields))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SharedStateWrite [wnode=" + wnode + ", fields=" + fields + "]";
    }

    public SharedStateWrite(IPNode wnode, Set<String> fields) {
        this.wnode = wnode;
        this.fields = fields;
    }

}
