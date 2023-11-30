package com.lumos.forward.memory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.jaxb.core.v2.model.core.Ref;

import com.lumos.App;
import com.lumos.forward.Context;
import com.lumos.forward.ContextSensitiveValue;
import com.lumos.forward.Definition;

import soot.Local;
import soot.RefLikeType;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.StaticFieldRef;
import soot.jimple.internal.JInstanceFieldRef;

public class Memory {
    Map<AbstractAddress, Set<Definition>> currMapping;

    public Map<AbstractAddress, Set<Definition>> getCurrMapping() {
        return currMapping;
    }

    public void destroy() {
        for (AbstractAddress a : currMapping.keySet()) {
            currMapping.get(a).clear();
        }
        currMapping.clear();
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
        Memory other = (Memory) obj;
        if (currMapping == null) {
            if (other.currMapping != null)
                return false;
        } else if (!currMapping.equals(other.currMapping))
            return false;
        return true;
    }

    public void setCurrMapping(Map<AbstractAddress, Set<Definition>> currMapping) {
        this.currMapping = currMapping;
    }

    public Memory() {
        this.currMapping = new HashMap<>();
    }

    public Memory(Memory other) {
        this();

        Map<AbstractAddress, Set<Definition>> mappings = other.getCurrMapping();

        for (AbstractAddress un : mappings.keySet()) {
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

    public Set<RefBasedAddress> getUniqueNamesForCollection(ContextSensitiveValue cv) {
        // JInstanceFieldRef ref = (JInstanceFieldRef) cv.getValue();
        Set<Definition> baseDefs = getDefinitionsByCV(cv);

        Set<RefBasedAddress> unames = new HashSet<>();
        for (Definition def : baseDefs) {
            RefBasedAddress unref = def.getDefinedValue();
            assert (unref.getSuffix() != null);
            unames.add(unref);
        }
        return unames;
    }

    public Set<RefBasedAddress> getRefBasedAaddressesForRef(ContextSensitiveValue cv) {
        JInstanceFieldRef ref = (JInstanceFieldRef) cv.getValue();
        Set<Definition> baseDefs = getDefinitionsByCV(cv.getContext(), ref.getBase());

        Set<RefBasedAddress> unames = new HashSet<>();

        for (Definition def : baseDefs) {
            RefBasedAddress unref = RefBasedAddress.getRefBasedAddress(def.getDefinedValue(), ref.getFieldRef());
            unames.add(unref);
        }
        return unames;
    }

    public Set<Definition> getDefinitionsByCV(ContextSensitiveValue cv) {
        Set<Definition> resultDefs = new HashSet<>();

        if (cv.getValue() instanceof JInstanceFieldRef) {
            for (RefBasedAddress unref : getRefBasedAaddressesForRef(cv)) {
                assert (unref.getSuffix() != null);
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
            return resultDefs;
        } else {
            // String tstr = cv.getValue().getType().toString();
            // if (tstr.contains("List")
            // || tstr.contains("Set")
            // || tstr.contains("Iterator")
            // || tstr.contains("Map")
            // || tstr.contains("[]")) {
            // for (RefBasedAddress unref : getUniqueNamesForCollection(cv)) {
            // Set<Definition> definitions = new HashSet<>();
            // Set<Definition> heapDefs = currMapping.get(unref);
            // if (heapDefs != null) {
            // definitions.addAll(heapDefs);
            // } else {
            // definitions.add(Definition.getDefinition(unref, null));
            // this.currMapping.put(unref, definitions);
            // }
            // resultDefs.addAll(definitions);
            // }
            // return resultDefs;
            // }
        }
        RefBasedAddress un = RefBasedAddress.getRefBasedAddress(cv);

        if (cv.getValue() instanceof StaticFieldRef) {
            un = RefBasedAddress
                    .getRefBasedAddress(ContextSensitiveValue.getCValue(Context.emptyContext(), cv.getValue()));
        }

        Set<Definition> defs = currMapping.get(un);
        if (defs == null || defs.isEmpty()) {
            currMapping.put(un, new HashSet<>());
            resultDefs.add(Definition
                    .getDefinition(RefBasedAddress.getRefBasedAddress(un.getBase(), Collections.emptyList()), null));
        } else {
            resultDefs.addAll(defs);
        }

        return resultDefs;

    }

    public void clearDefinition(RefBasedAddress un) {
        if (!currMapping.containsKey(un)) {
            currMapping.put(un, new HashSet<>());
        } else {
            currMapping.get(un).clear();
        }
    }

    public void clearDefinition(ContextSensitiveValue cv) {
        clearDefinition(RefBasedAddress.getRefBasedAddress(cv));
    }

    public void putDefinition(RefBasedAddress un, Definition def) {
        if (!currMapping.containsKey(un)) {
            currMapping.put(un, new HashSet<>());
        }
        currMapping.get(un).add(def);
    }

    public void putDefinition(RefBasedAddress un, Set<Definition> defs) {
        if (!currMapping.containsKey(un)) {
            currMapping.put(un, new HashSet<>());
        }
        currMapping.get(un).addAll(defs);
    }

    public void putDefinition(ContextSensitiveValue cv, Definition def) {
        RefBasedAddress un = RefBasedAddress.getRefBasedAddress(cv);
        putDefinition(un, def);
    }

    public void putDefinition(Context c, Value v, Definition def) {
        ContextSensitiveValue cv = ContextSensitiveValue.getCValue(c, v);
        putDefinition(cv, def);
    }

    public void putDefinition(Context c, Value v, RefBasedAddress uname) {
        ContextSensitiveValue cv = ContextSensitiveValue.getCValue(c, v);
        putDefinition(cv, Definition.getDefinition(uname, null));
    }

    public void putDefinition(ContextSensitiveValue cv, RefBasedAddress uname) {
        putDefinition(cv, Definition.getDefinition(uname, null));
    }

    public void putDefinition(ContextSensitiveValue cv, Set<Definition> defs) {
        RefBasedAddress un = RefBasedAddress.getRefBasedAddress(cv);
        putDefinition(un, defs);
    }

    public void putDefinition(Context c, Value v, Set<Definition> defs) {
        ContextSensitiveValue cv = ContextSensitiveValue.getCValue(c, v);
        putDefinition(cv, defs);
    }

    // public void putDefinition(Context c, Value v) {
    // ContextSensitiveValue cv = ContextSensitiveValue.getCValue(c, v);
    // putDefinition(cv, RefBasedAddress.getRefBasedAddress(cv, null));
    // }

    // public void putDefinition(ContextSensitiveValue cv) {
    // putDefinition(cv, RefBasedAddress.getRefBasedAddress(cv, null));
    // }

    @Override
    public String toString() {
        String result = "";
        result += "Memory: \n";
        // result += "UniqueNames:\n";
        // for (ContextSensitiveValue cv : uniqueNames.keySet()) {
        // result += cv + ": " + uniqueNames.get(cv);
        // result += "\n";
        // }
        result += "\nMapping:\n";
        for (AbstractAddress un : currMapping.keySet()) {
            result += un + ": " + currMapping.get(un);
            result += "\n";
        }
        // [uniqueNames=" + uniqueNames + ", currMapping=" + currMapping + "]";
        return result;
    }

    // @Override
    public String ssimple(String target) {
        String result = "";
        result += "Memory: \n";
        // result += "UniqueNames:\n";
        // for (ContextSensitiveValue cv : uniqueNames.keySet()) {
        // result += cv + ": " + uniqueNames.get(cv);
        // result += "\n";
        // }
        result += "\nMapping:\n";
        for (AbstractAddress un : currMapping.keySet()) {
            if (un.toString().contains(target)) {
                result += un + ": " + currMapping.get(un);
                result += "\n";
            }
        }
        // [uniqueNames=" + uniqueNames + ", currMapping=" + currMapping + "]";
        return result;
    }

}
