package com.lumos.forward.memory;

import com.lumos.forward.ContextSensitiveValue;

public interface AbstractAddress {
    public AbstractAllocation getBase();

    public void setBase(AbstractAllocation cv);
}
