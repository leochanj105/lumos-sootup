package com.lumos.wire;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lumos.App;

public class WireInterface {
    private static Map<RequestWirePoint, String> wps = null;

    public static String get(String reqMethod, int lineNum) {
        if (wps == null) {
            wps = new HashMap<>();
            add("order.controller.OrderController: order.domain.ChangeOrderResult saveOrderInfo(order.domain.ChangeOrderInfo)",
                    120,
                    "order.service.OrderServiceImpl: order.domain.ChangeOrderResult saveChanges(order.domain.Order)");
            add("cancel.controller.CancelController: cancel.domain.CancelOrderResult cancelTicket(cancel.domain.CancelOrderInfo,java.lang.String,java.lang.String)",
                    53,
                    "cancel.service.CancelServiceImpl: cancel.domain.CancelOrderResult cancelOrder(cancel.domain.CancelOrderInfo,java.lang.String,java.lang.String)");
            // add("other.controller.OrderOtherController: order.domain.ChangeOrderResult
            // saveOrderInfo(order.domain.ChangeOrderInfo)",
            // 120,
            // "order.service.OrderServiceImpl: order.domain.ChangeOrderResult
            // saveChanges(order.domain.Order)");
        }
        // if (reqMethod.contains("saveChanges")) {
        // App.p("!!! " + reqMethod + ", " + lineNum);
        // }
        for (RequestWirePoint wp : wps.keySet()) {
            if (reqMethod.contains(wp.reqMethod) && lineNum == wp.lineNum) {
                return wps.get(wp);
            }
        }
        // return wps.get(new HTTPRequestWirePoint(reqMethod, lineNum));
        return null;
    }

    public static void add(String reqMethod, int lineNum, String recvMethod) {
        RequestWirePoint reqp = new RequestWirePoint(reqMethod, lineNum);
        wps.put(reqp, recvMethod);
    }
}
