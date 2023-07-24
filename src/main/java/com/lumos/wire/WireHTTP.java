package com.lumos.wire;

import java.util.ArrayList;
import java.util.Arrays;
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
                    new String[] { "$u0" }, new String[] { "info" }, "$stack29");
            add("boolean pay", 46, "order.service.OrderServiceImpl: order.domain.GetOrderResult getOrderById",
                    new String[] { "$stack17" }, new String[] { "info" }, "result");
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

    public static void add(String reqMethod, int lineNum, String recvMethod, String[] reqParams, String[] recvParams,
            String retWireName) {
        HTTPRequestWirePoint reqp = new HTTPRequestWirePoint(reqMethod, lineNum);
        List<String> reqarr = Arrays.asList(reqParams);

        List<String> recvarr = Arrays.asList(recvParams);
        HTTPReceiveWirePoint recvp = new HTTPReceiveWirePoint(recvMethod, reqarr, recvarr, retWireName);
        wps.put(reqp, recvp);
    }
}
