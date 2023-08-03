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
    // IPNode ipnode;

    // public IPNode getIpnode() {
    // return ipnode;
    // }

    // public void setIpnode(IPNode ipnode) {
    // this.ipnode = ipnode;
    // }

    public Map<UniqueName, Set<Definition>> getCurrMapping() {
        return currMapping;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((currMapping == null) ? 0 : currMapping.hashCode());
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
        IPFlowInfo other = (IPFlowInfo) obj;
        if (currMapping == null) {
            if (other.currMapping != null)
                return false;
        } else if (!currMapping.equals(other.currMapping))
            return false;
        return true;
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

    public Set<Definition> getDefinitionsByCV(Context c, Value v) {
        ContextSensitiveValue cv = ContextSensitiveValue.getCValue(c, v);
        return getDefinitionsByCV(cv);
    }

    public Set<UniqueName> getUniqueNamesForRef(ContextSensitiveValue cv) {
        JInstanceFieldRef ref = (JInstanceFieldRef) cv.getValue();
        Set<Definition> baseDefs = getDefinitionsByCV(cv.getContext(), ref.getBase());
        if (baseDefs == null) {
            App.p(cv);
            App.panicni();
        }

        Set<UniqueName> unames = new HashSet<>();

        for (Definition def : baseDefs) {
            UniqueName unref = null;
            if (def.getDefinedValue().getBase().toString().equals("null")) {
                unref = new UniqueName(def.getDefinedValue(), null);
            } else {
                unref = new UniqueName(def.getDefinedValue(), ref.getFieldRef());
            }
            unames.add(unref);
        }
        return unames;
    }

    public Set<Definition> getDefinitionsByCV(ContextSensitiveValue cv) {
        //
        Set<Definition> resultDefs = new HashSet<>();
        if (cv.getValue() instanceof JInstanceFieldRef) {
            // return currMapping.get(cv);
            for (UniqueName unref : getUniqueNamesForRef(cv)) {
                Set<Definition> definitions = new HashSet<>();
                Set<Definition> heapDefs = currMapping.get(unref);
                if (heapDefs != null) {
                    definitions.addAll(heapDefs);
                } else {
                    definitions.add(Definition.getDefinition(unref, null));
                    this.currMapping.put(unref, definitions);
                }
                // }
                resultDefs.addAll(definitions);
            }
        } else {
            UniqueName un = new UniqueName(cv);
            Set<Definition> defs = currMapping.get(un);
            if (defs == null || defs.isEmpty()) {
                // if (v.getValue().getType() instanceof RefLikeType) {
                // UniqueName u = new UniqueName(cv);
                currMapping.put(un, new HashSet<>());
                resultDefs.add(Definition.getDefinition(un, null));
                // }
            } else {
                resultDefs.addAll(defs);
            }
        }
        // if (resultNames.isEmpty()) {
        // App.p("!!!!!!!! " + cv + ", " + cv.getValue().getClass() + ", "
        // + (cv.getValue().getType() instanceof RefLikeType));
        // }
        return resultDefs;
    }

    public void clearDefinition(UniqueName un) {
        if (!currMapping.containsKey(un)) {
            currMapping.put(un, new HashSet<>());
        } else {
            currMapping.get(un).clear();
        }
    }

    public void clearDefinition(ContextSensitiveValue cv) {
        clearDefinition(new UniqueName(cv));
    }

    public void putDefinition(UniqueName un, Definition def) {
        if (!currMapping.containsKey(un)) {
            currMapping.put(un, new HashSet<>());
        }
        currMapping.get(un).add(def);
    }

    public void putDefinition(UniqueName un, Set<Definition> defs) {
        if (!currMapping.containsKey(un)) {
            currMapping.put(un, new HashSet<>());
        }
        currMapping.get(un).addAll(defs);
    }

    public void putDefinition(ContextSensitiveValue cv, Definition def) {
        UniqueName un = new UniqueName(cv);
        putDefinition(un, def);
    }

    public void putDefinition(Context c, Value v, Definition def) {
        ContextSensitiveValue cv = ContextSensitiveValue.getCValue(c, v);
        putDefinition(cv, def);
    }

    public void putDefinition(Context c, Value v, UniqueName uname) {
        ContextSensitiveValue cv = ContextSensitiveValue.getCValue(c, v);
        putDefinition(cv, Definition.getDefinition(uname, null));
    }

    public void putDefinition(ContextSensitiveValue cv, UniqueName uname) {
        putDefinition(cv, Definition.getDefinition(uname, null));
    }

    public void putDefinition(ContextSensitiveValue cv, Set<Definition> defs) {
        UniqueName un = new UniqueName(cv);
        putDefinition(un, defs);
    }

    public void putDefinition(Context c, Value v, Set<Definition> defs) {
        ContextSensitiveValue cv = ContextSensitiveValue.getCValue(c, v);
        putDefinition(cv, defs);
    }

    public void putDefinition(Context c, Value v) {
        ContextSensitiveValue cv = ContextSensitiveValue.getCValue(c, v);
        putDefinition(cv, new UniqueName(cv, null));
    }

    public void putDefinition(ContextSensitiveValue cv) {
        putDefinition(cv, new UniqueName(cv, null));
    }

    // public Map<ContextSensitiveValue, Set<UniqueName>> getUniqueNames() {
    // return uniqueNames;
    // }

    // public void setUniqueNames(Map<ContextSensitiveValue, Set<UniqueName>>
    // original) {
    // this.uniqueNames = original;
    // }

    @Override
    public String toString() {
        String result = "";
        result += "IPFlowInfo: \n";
        // result += "UniqueNames:\n";
        // for (ContextSensitiveValue cv : uniqueNames.keySet()) {
        // result += cv + ": " + uniqueNames.get(cv);
        // result += "\n";
        // }
        result += "\nMapping:\n";
        for (UniqueName un : currMapping.keySet()) {
            result += un + ": " + currMapping.get(un);
            result += "\n";
        }
        // [uniqueNames=" + uniqueNames + ", currMapping=" + currMapping + "]";
        return result;
    }

}
