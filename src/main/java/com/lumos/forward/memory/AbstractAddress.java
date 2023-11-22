package com.lumos.forward.memory;

import com.lumos.forward.ContextSensitiveValue;

public interface AbstractAddress {
    public ContextSensitiveValue getBase();

    public void setBase(ContextSensitiveValue cv);
}
