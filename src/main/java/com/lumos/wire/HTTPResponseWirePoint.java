package com.lumos.wire;

public class HTTPResponseWirePoint {
    public String reqType;
    public String reqURL;

    public HTTPResponseWirePoint(String reqType, String reqURL) {
        this.reqType = reqType;
        this.reqURL = reqURL;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HTTPResponseWirePoint)) {
            return false;
        }
        HTTPResponseWirePoint wp = (HTTPResponseWirePoint) obj;
        return (this.reqType.equals(wp.reqType) &&
                this.reqURL.equals(wp.reqURL));
    }

    @Override
    public String toString() {
        return "(" + reqType + ", " + reqURL + ")";
    }

    @Override
    public int hashCode() {
        return this.reqType.hashCode() + reqURL.hashCode();
    }
}