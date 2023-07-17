package com.lumos.common;

import java.lang.reflect.Field;
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
        this.fields = new ArrayList<>();
        if (fields != null) {
            for (FieldSignature sig : fields) {
                this.fields.add(sig);
            }
        }

    }

    // public RefSeq(Value value, FieldSignature field) {
    // this.value = value;
    // this.fields = new ArrayList<>();
    // this.fields.add(field);
    // }

    public RefSeq(Value value) {
        this(value, null);
    }

    public void appendTail(FieldSignature sig) {
        this.fields.add(sig);
    }

    public void appendHead(FieldSignature sig) {
        this.fields.add(0, sig);
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

    // public RefSeq shiftLeft(int pos){
    // return new RefSeq(this.value,)
    // }

    @Override
    public String toString() {
        return "(" + value.toString() + "." + fields.toString() + ")";
    }

    public int hashCode() {
        return value.hashCode() + fields.hashCode();
    }
}