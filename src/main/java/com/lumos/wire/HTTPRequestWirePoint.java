package com.lumos.wire;

public class HTTPRequestWirePoint {
    public String reqType;
    public int index;
    public String reqURL;

    public HTTPRequestWirePoint(String reqType, int index, String reqURL) {
        this.reqType = reqType;
        this.index = index;
        this.reqURL = reqURL;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HTTPRequestWirePoint)) {
            return false;
        }
        HTTPRequestWirePoint wp = (HTTPRequestWirePoint) obj;
        return (this.reqType.equals(wp.reqType) &&
                this.index == wp.index &&
                this.reqURL.equals(wp.reqURL));
    }

    @Override
    public String toString() {
        return "(" + reqType + ", " + index + ", " + reqURL + ")";
    }

    @Override
    public int hashCode() {
        return this.reqType.hashCode() + index + reqURL.hashCode();
    }
}