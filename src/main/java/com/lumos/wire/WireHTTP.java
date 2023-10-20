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
        private static Map<RequestWirePoint, HTTPReceiveWirePoint> wps = null;

        public static HTTPReceiveWirePoint get(String reqMethod, int lineNum) {
                if (wps == null) {
                        wps = new HashMap<>();
                        add("java.util.concurrent.Future sendInsidePayment", 99,
                                        "inside_payment.service.InsidePaymentServiceImpl: boolean pay(inside_payment.domain.PaymentInfo,javax.servlet.http.HttpServletRequest)",
                                        new String[] { "$u0", "loginId" }, new String[] { "info", "$stack16" },
                                        "$stack29");
                        add("boolean pay", 50,
                                        "other.controller.OrderOtherController: other.domain.GetOrderResult getOrderById",
                                        new String[] { "$stack17" }, new String[] { "info" }, "$stack84");
                        add("boolean pay", 46,
                                        "order.controller.OrderController: order.domain.GetOrderResult getOrderById",
                                        new String[] { "$stack17" }, new String[] { "info" }, "$stack23");
                        add("inside_payment.service.InsidePaymentServiceImpl: boolean pay(inside_payment.domain.PaymentInfo,javax.servlet.http.HttpServletRequest)",
                                        95,
                                        "com.trainticket.controller.PaymentController: boolean pay(com.trainticket.domain.PaymentInfo)",
                                        new String[] { "$stack54" }, new String[] { "info" }, "$stack60");
                        add("java.util.concurrent.Future sendOrderCancel",
                                        115,
                                        "cancel.controller.CancelController: cancel.domain.CancelOrderResult cancelTicket(cancel.domain.CancelOrderInfo,java.lang.String,java.lang.String)",
                                        new String[] { "$u0", "loginId", "loginToken" },
                                        new String[] { "info", "loginId", "loginToken" },
                                        "$stack25");

                        add("cancel.service.CancelServiceImpl: cancel.domain.GetOrderResult getOrderByIdFromOrderOther",
                                        340,
                                        "other.controller.OrderOtherController: other.domain.GetOrderResult getOrderById(other.domain.GetOrderByIdInfo)",
                                        new String[] { "info" },
                                        new String[] { "info" },
                                        "$stack6");

                        add("cancel.service.CancelServiceImpl: cancel.domain.GetOrderResult getOrderByIdFromOrder", 332,
                                        "order.controller.OrderController: order.domain.GetOrderResult getOrderById(order.domain.GetOrderByIdInfo)",
                                        new String[] { "info" },
                                        new String[] { "info" },
                                        "$stack6");
                        add("cancel.service.CancelServiceImpl: cancel.domain.ChangeOrderResult cancelFromOrder", 282,
                                        "order.controller.OrderController: order.domain.ChangeOrderResult saveOrderInfo(order.domain.ChangeOrderInfo)",
                                        new String[] { "info" },
                                        new String[] { "orderInfo" },
                                        "$stack6");

                        add("cancel.service.CancelServiceImpl: boolean drawbackMoney(java.lang.String,java.lang.String)",
                                        312,
                                        "inside_payment.controller.InsidePaymentController: boolean drawBack(inside_payment.domain.DrawBackInfo)",
                                        new String[] { "$stack6" },
                                        new String[] { "info" },
                                        "$stack9");

                        add("cancel.service.CancelServiceImpl: cancel.domain.GetAccountByIdResult getAccount(cancel.domain.GetAccountByIdInfo)",
                                        322,
                                        "sso.controller.AccountSsoController: sso.domain.GetAccountByIdResult getAccountById(sso.domain.GetAccountByIdInfo)",
                                        new String[] { "info" },
                                        new String[] { "info" },
                                        "$stack6");
                        add("cancel.controller.CancelController: cancel.domain.VerifyResult verifySsoLogin(java.lang.String)",
                                        64,
                                        "sso.controller.AccountSsoController: sso.domain.VerifyResult verifyLoginToken(java.lang.String)",
                                        new String[] { "loginToken" },
                                        new String[] { "token" },
                                        "$stack9");
                        add("cancel.queue.MsgSendingBean: void sendCancelInfoToOrderOther(cancel.domain.ChangeOrderInfo)",
                                        27, "other.queue.MsgReveiceBean: void receiveQueueInfo(java.lang.Object)",
                                        new String[] { "changeOrderInfo" },
                                        new String[] { "changeOrderInfo" }, "");
                        add("other.queue.MsgSendingBean: void sendLoginInfoToSso(other.domain.ChangeOrderResult)",
                                        27, "cancel.queue.MsgReveiceBean: void receiveQueueInfo(java.lang.Object)",
                                        new String[] { "changeOrderResult" },
                                        new String[] { "changeOrderResult" }, "");
                        add("order.controller.OrderController: order.domain.VerifyResult verifySsoLogin(java.lang.String)",
                                        155,
                                        "sso.controller.AccountSsoController: sso.domain.VerifyResult verifyLoginToken(java.lang.String)",
                                        new String[] { "loginToken" },
                                        new String[] { "token" },
                                        "$stack9");

                }
                for (RequestWirePoint wp : wps.keySet()) {

                        if (reqMethod.contains(wp.reqMethod) && lineNum == wp.lineNum) {
                                // App.p("!!! " + reqMethod + ", " + wps.get(wp));
                                return wps.get(wp);
                        }
                }
                // return wps.get(new HTTPRequestWirePoint(reqMethod, lineNum));
                return null;
        }

        public static void add(String reqMethod, int lineNum, String recvMethod, String[] reqParams,
                        String[] recvParams,
                        String retWireName) {
                RequestWirePoint reqp = new RequestWirePoint(reqMethod, lineNum);
                List<String> reqarr = Arrays.asList(reqParams);

                List<String> recvarr = Arrays.asList(recvParams);
                HTTPReceiveWirePoint recvp = new HTTPReceiveWirePoint(recvMethod, reqarr, recvarr, retWireName);
                wps.put(reqp, recvp);
        }
}
