package com.lumos.forward;

import java.util.HashMap;
import java.util.Map;

// import soot.Value;

public class Definition {
    public UniqueName definedValue;
    public IPNode definedLocation;

    public static Map<UniqueName, Map<IPNode, Definition>> cache = new HashMap<>();

    public Definition(UniqueName definedValue, IPNode definedLocation) {
        this.definedValue = definedValue;
        this.definedLocation = definedLocation;
    }

    @Override
    public String toString() {
        return definedValue + "";
    }

    public String d() {
        String result = "";
        result += this.definedLocation + ", ";
        result += this.definedValue;
        return result;
    }

    public static Definition getDefinition(UniqueName un, IPNode node) {
        Map<IPNode, Definition> map1 = cache.get(un);
        if (map1 == null) {
            map1 = new HashMap<>();
            cache.put(un, map1);
        }
        if (node == null || !map1.containsKey(node)) {
            Definition def = new Definition(un, node);
            map1.put(node, def);
        }
        return cache.get(un).get(node);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((definedValue == null) ? 0 : definedValue.hashCode());
        result = prime * result + ((definedLocation == null) ? 0 : definedLocation.hashCode());
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
        Definition other = (Definition) obj;
        if (definedValue == null) {
            if (other.definedValue != null)
                return false;
        } else if (!definedValue.equals(other.definedValue))
            return false;
        if (definedLocation == null) {
            if (other.definedLocation != null)
                return false;
        } else if (!definedLocation.equals(other.definedLocation))
            return false;
        return true;
    }

    public UniqueName getDefinedValue() {
        return definedValue;
    }

    public void setDefinedValue(UniqueName definedValue) {
        this.definedValue = definedValue;
    }

    public IPNode getDefinedLocation() {
        return definedLocation;
    }

    public void setDefinedLocation(IPNode definedLocation) {
        this.definedLocation = definedLocation;
    }

}
