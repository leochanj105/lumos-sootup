package com.lumos.forward;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.jaxb.core.v2.model.core.Ref;

import com.lumos.App;

import soot.Local;
import soot.RefLikeType;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.internal.JInstanceFieldRef;

public class IPFlowInfo {
    // Map<ContextSensitiveValue, Set<UniqueName>> uniqueNames;
    Map<UniqueName, Set<Definition>> currMapping;
    IPNode ipnode;

    public IPNode getIpnode() {
        return ipnode;
    }

    public void setIpnode(IPNode ipnode) {
        this.ipnode = ipnode;
    }

    public Map<UniqueName, Set<Definition>> getCurrMapping() {
        return currMapping;
    }

    public void setCurrMapping(Map<UniqueName, Set<Definition>> currMapping) {
        this.currMapping = currMapping;
    }

    public IPFlowInfo() {
        // this.uniqueNames = new HashMap<>();
        this.currMapping = new HashMap<>();
    }

    public IPFlowInfo(IPFlowInfo other) {
        this();
        // Map<ContextSensitiveValue, Set<UniqueName>> original =
        // other.getUniqueNames();

        // for (ContextSensitiveValue cv : original.keySet()) {
        // Set<UniqueName> snames = new HashSet<>();
        // for (UniqueName un : original.get(cv)) {
        // snames.add(un);
        // }
        // this.uniqueNames.put(cv, snames);
        // }

        Map<UniqueName, Set<Definition>> mappings = other.getCurrMapping();

        for (UniqueName un : mappings.keySet()) {
            Set<Definition> unames = new HashSet<>();
            for (Definition un2 : mappings.get(un)) {
                unames.add(un2);
            }
            this.currMapping.put(un, unames);
        }
        assert (this.equals(other));
    }

    public Set<Definition> getHeapDefinitions(Set<UniqueName> unames) {
        Set<Definition> definitions = new HashSet<>();
        for (UniqueName un : unames) {
            Set<Definition> heapNames = currMapping.get(un);
            if (heapNames != null) {
                definitions.addAll(heapNames);
            } else {
                definitions.add(Definition.getDefinition(un, ipnode));
                this.currMapping.put(un, definitions);
            }
        }
        return definitions;
    }
    // public IPFlowInfo(Map<ContextSensitiveValue, Set<UniqueName>> original) {
    // this();
    // for (ContextSensitiveValue cv : original.keySet()) {
    // Set<UniqueName> snames = new HashSet<>();
    // for (UniqueName un : original.get(cv)) {
    // snames.add(un);
    // }
    // this.uniqueNames.put(cv, snames);
    // }
    // }

    public Set<Definition> getDefinitionsByCV(Context c, Value v) {
        ContextSensitiveValue cv = ContextSensitiveValue.getCValue(c, v);
        Set<Definition> resultDefs = new HashSet<>();
        if (cv.getValue() instanceof JInstanceFieldRef) {
            // return currMapping.get(cv);
            JInstanceFieldRef ref = (JInstanceFieldRef) cv.getValue();
            ContextSensitiveValue cvbase = new ContextSensitiveValue(cv.getContext(), ref.getBase());
            // Set<UniqueName> unames = uniqueNames.get(cvbase);
            Set<UniqueName> basenames = getUnamesByCV(cvbase);
            if (basenames == null) {
                App.p(cv);
                App.panicni();
            }

            for (UniqueName un : basenames) {
                UniqueName unref = null;
                if (un.getBase().toString().equals(null)) {
                    unref = new UniqueName(un, null);
                }
                unref = new UniqueName(un, ref.getField().getName());
                resultNames.add(unref);
            }
        } else {
            Set<UniqueName> unames = uniqueNames.get(cv);
            if (unames == null || unames.isEmpty()) {
                if (cv.getValue().getType() instanceof RefLikeType) {
                    UniqueName u = new UniqueName(cv, null);
                    resultNames.add(u);
                }
            } else {
                resultNames.addAll(unames);
            }
        }
        // if (resultNames.isEmpty()) {
        // App.p("!!!!!!!! " + cv + ", " + cv.getValue().getClass() + ", "
        // + (cv.getValue().getType() instanceof RefLikeType));
        // }
        return resultNames;
    }

    public void putUname(ContextSensitiveValue cv, UniqueName un) {
        if (!uniqueNames.containsKey(cv)) {
            uniqueNames.put(cv, new HashSet<>());
        }
        uniqueNames.get(cv).add(un);
    }

    public void putUname(ContextSensitiveValue cv, Set<UniqueName> unames) {
        if (!uniqueNames.containsKey(cv)) {
            uniqueNames.put(cv, new HashSet<>());
        }
        for (UniqueName un : unames) {
            Set<UniqueName> targetSet = uniqueNames.get(cv);
            if (targetSet == null) {
                App.p(cv);
            }
            targetSet.add(un);
        }
    }

    public void putUname(ContextSensitiveValue cv) {
        putUname(cv, new UniqueName(cv, null));
    }

    public Map<ContextSensitiveValue, Set<UniqueName>> getUniqueNames() {
        return uniqueNames;
    }

    public void setUniqueNames(Map<ContextSensitiveValue, Set<UniqueName>> original) {
        this.uniqueNames = original;
    }

    @Override
    public String toString() {
        String result = "";
        result += "IPFlowInfo: \n";
        result += "UniqueNames:\n";
        for (ContextSensitiveValue cv : uniqueNames.keySet()) {
            result += cv + ": " + uniqueNames.get(cv);
            result += "\n";
        }
        result += "\nMapping:\n";
        for (UniqueName un : currMapping.keySet()) {
            result += un + ": " + currMapping.get(un);
            result += "\n";
        }
        // [uniqueNames=" + uniqueNames + ", currMapping=" + currMapping + "]";
        return result;
    }

}
