package com.lumos.wire;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lumos.App;

public class WireInterface {
    private static Map<RequestWirePoint, String> wps = null;

    public static String translateServiceInterface(String sig) {
        int index = sig.indexOf("Service:");
        String first = sig.substring(0, index);
        String second = sig.substring(index + "Service:".length(), sig.length());
        return first + "ServiceImpl:" + second;
    }

    public static String get(String reqMethod, int lineNum) {
        if (wps == null) {
            wps = new HashMap<>();
        }
        for (RequestWirePoint wp : wps.keySet()) {
            if (reqMethod.contains(wp.reqMethod) && lineNum == wp.lineNum) {
                return wps.get(wp);
            }
        }
        return null;
    }

    public static void add(String reqMethod, int lineNum, String recvMethod) {
        RequestWirePoint reqp = new RequestWirePoint(reqMethod, lineNum);
        wps.put(reqp, recvMethod);
    }
}
