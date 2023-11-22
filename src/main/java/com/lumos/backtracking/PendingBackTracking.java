package com.lumos.backtracking;

import java.util.ArrayList;
import java.util.List;

import com.lumos.forward.node.IPNode;

import soot.SootFieldRef;

public class PendingBackTracking {
    public IPNode node;
    public String mode;
    public List<SootFieldRef> refs;

    public List<SootFieldRef> getRefs() {
        return refs;
    }

    public void setRefs(List<SootFieldRef> refs) {
        this.refs = refs;
    }

    public PendingBackTracking(IPNode node, String mode) {
        this.node = node;
        this.mode = mode;
        this.refs = new ArrayList<>();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((node == null) ? 0 : node.hashCode());
        result = prime * result + ((mode == null) ? 0 : mode.hashCode());
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
        PendingBackTracking other = (PendingBackTracking) obj;
        if (node == null) {
            if (other.node != null)
                return false;
        } else if (!node.equals(other.node))
            return false;
        if (mode == null) {
            if (other.mode != null)
                return false;
        } else if (!mode.equals(other.mode))
            return false;
        return true;
    }

    public IPNode getNode() {
        return node;
    }

    public void setNode(IPNode node) {
        this.node = node;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

}
