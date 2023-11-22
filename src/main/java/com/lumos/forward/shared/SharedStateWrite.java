package com.lumos.forward.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.lumos.App;
import com.lumos.forward.ContextSensitiveValue;
import com.lumos.forward.node.IPNode;

import soot.SootFieldRef;
import soot.SootMethod;
import soot.Value;
import soot.jimple.Stmt;

public class SharedStateWrite {
    public String type;
    public IPNode wnode;
    public Set<List<String>> fields;

    public String d() {
        return d(",");
    }

    public String lessSuffix() {
        List<String> names = new ArrayList<>();
        for (List<String> fs : fields) {
            String name = "";
            for (int i = 0; i < fs.size(); i++) {
                name += fs.get(i);
                if (i != fs.size() - 1) {
                    name += ".";
                }
            }
            names.add(name);
        }
        return names.toString();
    }

    public String d(String separator) {
        SootMethod sm = wnode.getContext().getStackLast().sm;
        Stmt s = wnode.stmt;
        Value v = s.getInvokeExpr().getArg(0);
        return type + separator + App.serviceMap.get(sm.toString()) + separator + sm + separator
                + s.getJavaSourceStartLineNumber()
                + separator + s + separator + v + separator
                + lessSuffix();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        // result = prime * result + ((wnode == null) ? 0 : wnode.hashCode());
        result = prime * result + ((wnode == null) ? 0 : wnode.getStmt().hashCode());
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
            // } else if (!wnode.equals(other.wnode))
        } else if (!wnode.getStmt().equals(other.wnode.getStmt()))
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

    public SharedStateWrite(String type, IPNode wnode, Set<List<String>> fields) {
        this.type = type;
        this.wnode = wnode;
        this.fields = fields;
    }

}
