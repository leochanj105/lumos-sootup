package com.lumos.common;

import java.util.ArrayList;
import java.util.List;

import sootup.core.jimple.basic.Value;
import sootup.core.signatures.FieldSignature;

public class RefSeq {
    // public RefSeq base;

    public Value value;
    public List<FieldSignature> fields;

    public RefSeq(Value value, List<FieldSignature> fields) {
        // this.base = base;
        this.value = value;
        this.fields = fields;
        if (fields == null) {
            this.fields = new ArrayList<>();
        }
    }

    public RefSeq(Value value) {
        this(value, null);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof RefSeq))
            return false;
        RefSeq other = (RefSeq) o;

        if (other.fields == null) {

            if (this.fields == null) {
                return true;
            } else {
                return false;
            }
        }
        return other.fields.equals(this.fields);
    }

    @Override
    public String toString() {
        return "(" + value.toString() + "." + fields.toString() + ")";
    }
}