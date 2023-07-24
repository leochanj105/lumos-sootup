package com.lumos.forward;

import java.util.ArrayList;
import java.util.List;

public class ContextSensitiveInfo {
    public IPNode firstNode;
    public List<IPNode> retNodes;

    public IPNode getFirstNode() {
        return firstNode;
    }

    public void setFirstNode(IPNode firstNode) {
        this.firstNode = firstNode;
    }

    public List<IPNode> getRetNodes() {
        return retNodes;
    }

    public void setRetNodes(List<IPNode> retNodes) {
        this.retNodes = retNodes;
    }

    public ContextSensitiveInfo(IPNode firstNode) {
        this.firstNode = firstNode;
        this.retNodes = new ArrayList<>();
    }

    public ContextSensitiveInfo() {
        this.retNodes = new ArrayList<>();
    }

    public void append(IPNode sn) {
        this.retNodes.add(sn);
    }
}
