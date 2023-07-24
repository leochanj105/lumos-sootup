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

    @Override
    public String toString() {
        return "HTTPReceiveWirePoint [targetMethod=" + targetMethod + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((targetMethod == null) ? 0 : targetMethod.hashCode());
        result = prime * result + ((sendParamIndices == null) ? 0 : sendParamIndices.hashCode());
        result = prime * result + ((recvParamIndices == null) ? 0 : recvParamIndices.hashCode());
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
        HTTPReceiveWirePoint other = (HTTPReceiveWirePoint) obj;
        if (targetMethod == null) {
            if (other.targetMethod != null)
                return false;
        } else if (!targetMethod.equals(other.targetMethod))
            return false;
        if (sendParamIndices == null) {
            if (other.sendParamIndices != null)
                return false;
        } else if (!sendParamIndices.equals(other.sendParamIndices))
            return false;
        if (recvParamIndices == null) {
            if (other.recvParamIndices != null)
                return false;
        } else if (!recvParamIndices.equals(other.recvParamIndices))
            return false;
        return true;
    }

}