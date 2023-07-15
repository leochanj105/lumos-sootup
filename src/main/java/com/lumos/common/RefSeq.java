package com.lumos.common;

import java.util.List;

import sootup.core.signatures.FieldSignature;

public class RefSeq {
    // public RefSeq base;
    public List<FieldSignature> fields;

    public RefSeq(List<FieldSignature> fields) {
        // this.base = base;
        this.fields = fields;
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
}