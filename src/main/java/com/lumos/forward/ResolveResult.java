package com.lumos.forward;

import com.lumos.analysis.MethodInfo;
import com.lumos.wire.HTTPReceiveWirePoint;

public class ResolveResult {
    public MethodInfo minfo;

    public MethodInfo getMinfo() {
        return minfo;
    }

    public void setMinfo(MethodInfo minfo) {
        this.minfo = minfo;
    }

    public HTTPReceiveWirePoint hwire;

    public HTTPReceiveWirePoint getHwire() {
        return hwire;
    }

    public void setHwire(HTTPReceiveWirePoint hwire) {
        this.hwire = hwire;
    }

    public ResolveResult() {
    }

}
