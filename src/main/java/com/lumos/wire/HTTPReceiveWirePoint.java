package com.lumos.wire;

import java.util.List;

import soot.jimple.InvokeExpr;

public class HTTPReceiveWirePoint {
    public String targetMethod;

    public List<String> sendParams;
    public List<String> recvParams;

    // public String retWireName;

    // public String getRetWireName() {
    // return retWireName;
    // }

    // public void setRetWireName(String retWireName) {
    // this.retWireName = retWireName;
    // }

    @Override
    public String toString() {
        return "HTTPReceiveWirePoint [targetMethod=" + targetMethod + "]";
    }

    public HTTPReceiveWirePoint(String targetMethod, List<String> sendParams, List<String> recvParams) {
        this.targetMethod = targetMethod;
        this.sendParams = sendParams;
        this.recvParams = recvParams;
        // this.retWireName = retWireName;
    }

    public String getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public List<String> getSendParams() {
        return sendParams;
    }

    public void setSendParams(List<String> sendParams) {
        this.sendParams = sendParams;
    }

    public List<String> getRecvParams() {
        return recvParams;
    }

    public void setRecvParams(List<String> recvParams) {
        this.recvParams = recvParams;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((targetMethod == null) ? 0 : targetMethod.hashCode());
        result = prime * result + ((sendParams == null) ? 0 : sendParams.hashCode());
        result = prime * result + ((recvParams == null) ? 0 : recvParams.hashCode());
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
        if (sendParams == null) {
            if (other.sendParams != null)
                return false;
        } else if (!sendParams.equals(other.sendParams))
            return false;
        if (recvParams == null) {
            if (other.recvParams != null)
                return false;
        } else if (!recvParams.equals(other.recvParams))
            return false;
        return true;
    }

}