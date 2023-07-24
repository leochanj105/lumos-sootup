package com.lumos.forward;

import java.util.List;

public abstract class IPNode {
    public List<IPNode> predecesors;
    public List<IPNode> successors;

    public List<IPNode> getPredecesors() {
        return this.predecesors;
    }

    public void setPredecesors(List<IPNode> predecesors) {
        this.predecesors = predecesors;
    }

    public List<IPNode> getSuccessors() {
        return this.successors;
    }

    public void setSuccessors(List<IPNode> successors) {
        this.successors = successors;
    }

}
