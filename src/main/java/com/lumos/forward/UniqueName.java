package com.lumos.forward;

import java.util.ArrayList;
import java.util.List;

import soot.SootFieldRef;

public class UniqueName {

    public ContextSensitiveValue base;
    // public List<SootFieldRef> suffix;
    public List<String> suffix;

    public List<String> getSuffix() {
        return suffix;
    }

    public void setSuffix(List<String> suffix) {
        this.suffix = suffix;
    }

    public ContextSensitiveValue getBase() {
        return base;
    }

    public void setBase(ContextSensitiveValue base) {
        this.base = base;
    }

    public UniqueName(ContextSensitiveValue base, List<String> suffix) {
        this.base = base;
        this.suffix = new ArrayList<>();
        if (suffix != null) {
            this.suffix.addAll(suffix);
        }
    }

    public UniqueName(UniqueName un, String ref) {
        this(un.getBase(), un.getSuffix());
        this.append(ref);
    }

    public UniqueName(ContextSensitiveValue cv) {
        this(cv, null);
    }

    public void append(String fref) {
        suffix.add(fref);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((base == null) ? 0 : base.hashCode());
        result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
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
        UniqueName other = (UniqueName) obj;
        if (base == null) {
            if (other.base != null)
                return false;
        } else if (!base.equals(other.base))
            return false;
        if (suffix == null) {
            if (other.suffix != null)
                return false;
        } else if (!suffix.equals(other.suffix))
            return false;
        return true;
    }

    @Override
    public String toString() {
        String result = "";
        result += base;
        for (String s : suffix) {
            result += "." + s;
        }
        return result;
    }

}