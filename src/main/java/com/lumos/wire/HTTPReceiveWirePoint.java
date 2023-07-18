package com.lumos.wire;

import java.util.List;

import soot.jimple.InvokeExpr;

public class HTTPReceiveWirePoint {
    public String targetMethod;

    public List<Integer> sendParamIndices;
    public List<Integer> recvParamIndices;

    public HTTPReceiveWirePoint(String targetMethod, List<Integer> sendParamIndices,
            List<Integer> recvParamIndices) {
        this.targetMethod = targetMethod;
        this.sendParamIndices = sendParamIndices;
        this.recvParamIndices = recvParamIndices;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof HTTPReceiveWirePoint)) {
            return false;
        }
        HTTPReceiveWirePoint other = (HTTPReceiveWirePoint) o;
        return this.targetMethod.equals(other.targetMethod) && other.sendParamIndices.equals(this.sendParamIndices)
                && other.recvParamIndices.equals(this.recvParamIndices);
    }

    @Override
    public int hashCode() {
        return this.targetMethod.hashCode() + this.sendParamIndices.hashCode() + this.recvParamIndices.hashCode();
    }

}