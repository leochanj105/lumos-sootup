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

    public boolean equals(Object o) {
        if (o == null || !(o instanceof HTTPRequestWirePoint)) {
            return false;
        }
        HTTPRequestWirePoint other = (HTTPRequestWirePoint) o;
        return this.reqMethod.equals(other.reqMethod) && other.lineNum == this.lineNum;
    }

    @Override
    public int hashCode() {
        return this.reqMethod.hashCode() + this.lineNum;
    }

}