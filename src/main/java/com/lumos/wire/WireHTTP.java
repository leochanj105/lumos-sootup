package com.lumos.wire;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lumos.App;

public class WireHTTP {
    private static Map<HTTPRequestWirePoint, HTTPReceiveWirePoint> wps = null;

    public static HTTPReceiveWirePoint get(String reqMethod, int lineNum) {
        if (wps == null) {
            wps = new HashMap<>();
            add("java.util.concurrent.Future sendInsidePayment", 99,
                    "inside_payment.service.InsidePaymentServiceImpl: boolean pay(inside_payment.domain.PaymentInfo,javax.servlet.http.HttpServletRequest)",
                    new int[] { 2 }, new int[] { 1 });
        }
        // System.out.println(reqMethod + ", " + lineNum);
        for (HTTPRequestWirePoint wp : wps.keySet()) {

            if (reqMethod.contains(wp.reqMethod) && lineNum == wp.lineNum) {
                // App.p("!!!" + wp + ", " + reqMethod + ", " + wps.get(wp));
                return wps.get(wp);
            }
        }
        // return wps.get(new HTTPRequestWirePoint(reqMethod, lineNum));
        return null;
    }

    public static void add(String reqMethod, int lineNum, String recvMethod, int[] reqParams, int[] recvParams) {
        HTTPRequestWirePoint reqp = new HTTPRequestWirePoint(reqMethod, lineNum);
        List<Integer> reqarr = new ArrayList<Integer>();
        for (int i : reqParams) {
            reqarr.add(i);
        }

        List<Integer> recvarr = new ArrayList<Integer>();
        for (int i : recvParams) {
            recvarr.add(i);
        }
        HTTPReceiveWirePoint recvp = new HTTPReceiveWirePoint(recvMethod, reqarr, recvarr);
        wps.put(reqp, recvp);
    }
}
