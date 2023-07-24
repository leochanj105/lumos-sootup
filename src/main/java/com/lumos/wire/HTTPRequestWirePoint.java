package com.lumos.wire;

import java.util.List;

import soot.jimple.InvokeExpr;

public class HTTPRequestWirePoint {
    // public InvokeExpr iexpr;
    public String reqMethod;
    public int lineNum;
    // public String targetMethod;

    // public List<Integer> sendParamIndices;
    // public List<Integer> recvParamIndices;

    public HTTPRequestWirePoint(String reqMethod, int lineNum) {
        this.reqMethod = reqMethod;
        this.lineNum = lineNum;
        // this.targetMethod = targetMethod;
        // this.sendParamIndices = sendParamIndices;
        // this.recvParamIndices = recvParamIndices;
    }

    @Override
    public String toString() {
        return "HTTPRequestWirePoint [reqMethod=" + reqMethod + ", lineNum=" + lineNum + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((reqMethod == null) ? 0 : reqMethod.hashCode());
        result = prime * result + lineNum;
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
        HTTPRequestWirePoint other = (HTTPRequestWirePoint) obj;
        if (reqMethod == null) {
            if (other.reqMethod != null)
                return false;
        } else if (!reqMethod.equals(other.reqMethod))
            return false;
        if (lineNum != other.lineNum)
            return false;
        return true;
    }

}