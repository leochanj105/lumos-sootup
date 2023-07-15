package com.lumos.common;

import sootup.core.signatures.FieldSignature;

public class RefSeq {
    public RefSeq base;
    public FieldSignature field;

    public RefSeq(RefSeq base, FieldSignature field) {
        this.base = base;
        this.field = field;
    }

    public RefSeq(RefSeq base) {
        this(null, null);
    }

    // @Override
    // public boolean equals(Object o) {
    // if (o == null || !(o instanceof RefSeq))
    // return false;
    // RefSeq other = (RefSeq) o;

    // // if(other.field == null && this.field ==null && )
    // return other.field.equals(this.field);
    // }
}