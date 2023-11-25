package com.lumos.forward.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lumos.forward.ContextSensitiveValue;

import soot.SootFieldRef;
import soot.Value;

public class RefBasedAddress implements AbstractAddress {

    public ContextSensitiveValue base;
    public List<SootFieldRef> suffix;

    // public String tag;

    public static Map<ContextSensitiveValue, Map<List<SootFieldRef>, RefBasedAddress>> cache = new HashMap<>();

    public static RefBasedAddress getRefBasedAddress(RefBasedAddress un, SootFieldRef ref) {
        // this(un.getBase(), un.getSuffix());
        // this.append(ref);
        ContextSensitiveValue rbase = un.getBase();
        List<SootFieldRef> sfs = un.getSuffix();
        List<SootFieldRef> newsfs = new ArrayList<>();
        if (sfs != null) {
            newsfs.addAll(sfs);
        }
        newsfs.add(ref);
        return getRefBasedAddress(rbase, newsfs);
    }

    public static RefBasedAddress getRefBasedAddress(ContextSensitiveValue cv) {
        return getRefBasedAddress(cv, null);
    }

    public static RefBasedAddress getRefBasedAddress(ContextSensitiveValue base, List<SootFieldRef> suffix) {
        // App.p(context);
        Map<List<SootFieldRef>, RefBasedAddress> map1 = cache.get(base);
        if (map1 == null) {
            map1 = new HashMap<>();
            cache.put(base, map1);
        }
        if (!map1.containsKey(suffix)) {
            // ContextSensitiveValue cvalue = new ContextSensitiveValue(v, value);
            RefBasedAddress ra = new RefBasedAddress(base, suffix);
            map1.put(suffix, ra);
        }
        return cache.get(base).get(suffix);
    }

    public List<SootFieldRef> getSuffix() {
        return suffix;
    }

    public void setSuffix(List<SootFieldRef> suffix) {
        this.suffix = suffix;
    }

    @Override
    public ContextSensitiveValue getBase() {
        return base;
    }

    @Override
    public void setBase(ContextSensitiveValue base) {
        this.base = base;
    }

    private RefBasedAddress(ContextSensitiveValue base, List<SootFieldRef> suffix) {
        this.base = base;
        this.suffix = suffix;
        // this.suffix = new ArrayList<>();
        // if (suffix != null) {
        // this.suffix.addAll(suffix);
        // }
        // this.tag = tag;
    }

    // public RefBasedAddress(RefBasedAddress un, SootFieldRef ref) {
    // this(un.getBase(), un.getSuffix());
    // this.append(ref);
    // }

    // public RefBasedAddress(ContextSensitiveValue cv) {
    // this(cv, null);
    // }

    public void append(SootFieldRef fref) {
        suffix.add(fref);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((base == null) ? 0 : base.hashCode());
        List<String> names = null;
        if (suffix != null) {
            names = new ArrayList<>();
            for (SootFieldRef ref : suffix) {
                names.add(ref.name());
            }
        }
        result = prime * result + ((names == null) ? 0 : names.hashCode());
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
        RefBasedAddress other = (RefBasedAddress) obj;
        if (base == null) {
            if (other.base != null)
                return false;
        } else if (!base.equals(other.base))
            return false;
        if (suffix == null) {
            if (other.suffix != null)
                return false;
        } else {
            if (other.suffix == null)
                return false;
            for (int i = 0; i < suffix.size(); i++) {
                if (!suffix.get(i).name().equals(other.suffix.get(i).name()))
                    return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String result = "";
        result += base;
        if (suffix != null) {
            result += "[";
            for (SootFieldRef sref : suffix) {
                result += "." + sref.name();
            }
            result += "]";
        }
        return result;
    }

}