package com.lumos.forward.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.lumos.App;
import com.lumos.analysis.MethodInfo;
import com.lumos.forward.Context;
import com.lumos.forward.ContextSensitiveValue;
import com.lumos.forward.IPFlowInfo;
import com.lumos.forward.InterProcedureGraph;

// import fj.data.Set;
import soot.jimple.Stmt;

public abstract class IPNode {
    public List<IPNode> predecesors;
    public List<IPNode> successors;

    public Stmt stmt;
    public Context context;
    public String type;

    public List<IPNode> cfDepNodes;

    // public InterProcedureGraph igraph;

    public InterProcedureGraph getIgraph() {
        return context == null ? null : context.getIgraph();
    }

    // public void setIgraph(InterProcedureGraph igraph) {
    // this.igraph = igraph;
    // }

    public abstract boolean isSingleIdAssign();

    public MethodInfo getMethodInfo() {
        return context.getStackLast();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Stmt getStmt() {
        return stmt;
    }

    public void setStmt(Stmt stmt) {
        this.stmt = stmt;
    }

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

    public String getDescription() {
        return this.context.toString() + ", " + this.stmt.toString() + ", " + this.type + ", "
                + this.stmt.getJavaSourceStartLineNumber();
    }

    public List<IPNode> getCfDepNodes() {
        if (this.cfDepNodes != null) {
            return this.cfDepNodes;
        }
        cfDepNodes = new ArrayList<>();
        MethodInfo minfo = getContext().getStackLast();
        for (Stmt cfstmt : minfo.cfDependency.get(stmt)) {
            cfDepNodes.add(getIgraph().getIPNode(context, cfstmt));
        }
        // Stmt callingStmt = getContext().getCallingStmt();

        if (getContext() != null) {
            for (IPNode cfnode : getContext().getCfDepNodesRecursive()) {
                cfDepNodes.add(cfnode);
            }
        }
        return cfDepNodes;
    }

    public abstract void flow(IPFlowInfo out);

    public abstract Set<ContextSensitiveValue> getUsed();

    public Set<ContextSensitiveValue> getUsed(Set<ContextSensitiveValue> implicits) {
        return getUsed();
    };
    // public abstract Set<ContextSensitiveValue> getUsed(Set<ContextSensitiveValue>
    // implicits)();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((stmt == null) ? 0 : stmt.hashCode());
        result = prime * result + ((context == null) ? 0 : context.hashCode());
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
        IPNode other = (IPNode) obj;
        if (stmt == null) {
            if (other.stmt != null)
                return false;
        } else if (!stmt.equals(other.stmt))
            return false;
        if (context == null) {
            if (other.context != null)
                return false;
        } else if (!context.equals(other.context))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

}
