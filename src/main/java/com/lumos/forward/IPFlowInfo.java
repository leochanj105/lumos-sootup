package com.lumos.forward;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lumos.App;

import soot.Local;
import soot.Value;
import soot.jimple.internal.JInstanceFieldRef;

public class IPFlowInfo {
    public Map<Value, Set<IPNode>> defSet = new HashMap<>();
    public Set<Set<Value>> aliases = new HashSet<>();
    public Set<Value> autoDefs = new HashSet<>();

    public Map<Value, Set<Value>> aliasMap = new HashMap<>();

    public void addAlias(Value v1, Value v2) {
        Set<Value> sv = new HashSet<>(Arrays.asList(new Value[] { v1, v2 }));
        addAlias(sv);
    }

    public void addAlias(Value v) {
        Set<Value> sv = new HashSet<>();
        sv.add(v);
        addAlias(sv);
    }

    public Set<Value> addAlias(Set<Value> sv) {

        aliases.add(sv);
        for (Value v : sv) {
            aliasMap.put(v, sv);
        }
        // App.p("!!! " + aliasMap);
        return merge(sv);
    }

    public void put(Value v, Set<Value> sv) {
        aliasMap.put(v, sv);
    }

    public Set<Value> merge(Set<Value> sv) {
        Set<Value> toMerge = null;
        for (Set<Value> candidate : aliases) {
            if (candidate.equals(sv)) {
                continue;
            }
            // App.p(sv);
            if (canMerge(candidate, sv)) {
                toMerge = candidate;
                break;
            }
        }
        if (toMerge != null) {
            aliases.remove(sv);
            for (Value v : sv) {
                toMerge.add(v);
                aliasMap.put(v, toMerge);
            }
            return merge(toMerge);
        }
        return sv;
    }

    public boolean canAlias(Value v1, Value v2) {

        if (v1.equals(v2)) {
            return true;
        }
        if ((v1 instanceof JInstanceFieldRef) && (v2 instanceof JInstanceFieldRef)) {
            JInstanceFieldRef ref1 = (JInstanceFieldRef) v1;
            JInstanceFieldRef ref2 = (JInstanceFieldRef) v2;
            if (ref1.getField().getName().equals(ref2.getField().getName())) {
                // App.p(ref1 + ", " + ref2);
                return canAlias(ref1.getBase(), ref2.getBase());
            } else {
                return false;
            }
        } else {
            // if (v1.hashCode() == 1946962024) {
            // App.p(aliasMap);
            // App.p(v1.getClass());
            // }
            if (!aliasMap.containsKey(v1)) {
                return false;
            }
            return aliasMap.get(v1).contains(v2);
            // App.p(v1);
        }
    }

    public boolean canMerge(Set<Value> sv1, Set<Value> sv2) {
        if (sv1.equals(sv2)) {
            return false;
        }
        for (Value v1 : sv1) {
            for (Value v2 : sv2) {

                if (canAlias(v1, v2)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((defSet == null) ? 0 : defSet.hashCode());
        result = prime * result + ((aliases == null) ? 0 : aliases.hashCode());
        result = prime * result + ((autoDefs == null) ? 0 : autoDefs.hashCode());
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
        if (defSet == null) {
            if (other.defSet != null)
                return false;
        } else if (!defSet.equals(other.defSet))
            return false;
        if (aliases == null) {
            if (other.aliases != null)
                return false;
        } else if (!aliases.equals(other.aliases))
            return false;
        if (autoDefs == null) {
            if (other.autoDefs != null)
                return false;
        } else if (!autoDefs.equals(other.autoDefs))
            return false;
        return true;
    }

}
