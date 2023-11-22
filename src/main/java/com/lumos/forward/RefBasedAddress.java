package com.lumos.forward;

import java.util.ArrayList;
import java.util.List;

import soot.SootFieldRef;

public class RefBasedAddress implements AbstractAddress {

    public ContextSensitiveValue base;
    public List<SootFieldRef> suffix;

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

    public RefBasedAddress(ContextSensitiveValue base, List<SootFieldRef> suffix) {
        this.base = base;
        this.suffix = new ArrayList<>();
        if (suffix != null) {
            this.suffix.addAll(suffix);
        }
    }

    public RefBasedAddress(RefBasedAddress un, SootFieldRef ref) {
        this(un.getBase(), un.getSuffix());
        this.append(ref);
    }

    public RefBasedAddress(ContextSensitiveValue cv) {
        this(cv, null);
    }

    public void append(SootFieldRef fref) {
        suffix.add(fref);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((base == null) ? 0 : base.hashCode());
        List<String> names = new ArrayList<>();
        for (SootFieldRef ref : suffix) {
            names.add(ref.name());
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
        for (SootFieldRef sref : suffix) {
            result += "." + sref.name();
        }
        return result;
    }

}