package com.lumos.forward;

import java.util.HashMap;
import java.util.Map;

import com.lumos.App;

import soot.Value;

public class ContextSensitiveValue {
    public Context context;
    public Value value;

    public static Map<Context, Map<Value, ContextSensitiveValue>> cache = new HashMap<>();
    // public UniqueName uniqueName;

    // public UniqueName getUniqueName() {
    // return uniqueName;
    // }

    // public void setUniqueName(UniqueName uniqueName) {
    // this.uniqueName = uniqueName;
    // }

    public static ContextSensitiveValue getCValue(Context context, Value value) {
        // App.p(context);
        Map<Value, ContextSensitiveValue> map1 = cache.get(context);
        if (map1 == null) {
            map1 = new HashMap<>();
            cache.put(context, map1);
        }
        if (!map1.containsKey(value)) {
            ContextSensitiveValue cvalue = new ContextSensitiveValue(context, value);
            map1.put(value, cvalue);
        }
        return cache.get(context).get(value);
    }

    public ContextSensitiveValue(Context context, Value value) {
        this.context = context;
        this.value = value;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((context == null) ? 0 : context.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        ContextSensitiveValue other = (ContextSensitiveValue) obj;
        if (context == null) {
            if (other.context != null)
                return false;
        } else if (!context.equals(other.context))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "" + context + " ==> " + value + "";
    }

}
