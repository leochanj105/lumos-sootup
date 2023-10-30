package com.lumos.wire;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lumos.App;
import com.lumos.analysis.MethodInfo;
import com.lumos.utils.Utils;

import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;

public class WireHTTP {
    private static Map<RequestWirePoint, HTTPReceiveWirePoint> wps = null;

    public static HTTPReceiveWirePoint templateWire(Stmt currStmt, MethodInfo minfo) {
        InvokeExpr expr = currStmt.getInvokeExpr();
        SootMethod invokedMethod = expr.getMethod();
        if (Utils.isCrossContext(invokedMethod.toString())) {
            String srcLine = Utils.getSourceLine(minfo.sm.getDeclaringClass().getName(),
                    +currStmt.getJavaSourceStartLineNumber());

            for (String address : App.remoteMap.keySet()) {
                // App.p(address + ": " + remoteMap.get(address));
                if (srcLine.contains(address)) {
                    App.p(srcLine);
                    SootMethod wiredMethod = App.remoteMap.get(address);
                    // p("=========");
                    // p(currStmt);
                    // p("Matched " + wiredMethod);
                    HTTPReceiveWirePoint hwire = new HTTPReceiveWirePoint(
                            wiredMethod.getSubSignature(), new ArrayList<>(),
                            new ArrayList<>());
                    if (wiredMethod.getParameterCount() == 1) {
                        for (Local plocal : wiredMethod.getActiveBody().getParameterLocals()) {
                            for (Value arg : expr.getArgs()) {
                                // p(arg.getType().toString());
                                // p("++ " + plocal.getType().);
                                // plocal.getType().
                                String callerName = Utils.getDirectName(
                                        arg.getType().toString());
                                String calleeName = Utils.getDirectName(
                                        plocal.getType().toString());

                                // if (callerName.contains("Payment")) {
                                // p("++ " + callerName + ", " + calleeName);
                                // }
                                if (Utils.typeMatch(callerName, calleeName)) {
                                    // p("----- " + arg + " matched for remote "
                                    // + plocal);
                                    hwire.getSendParams().add(arg.toString());
                                    hwire.getRecvParams().add(plocal.getName());
                                    if (currStmt instanceof AssignStmt) {
                                        String retName = ((AssignStmt) currStmt)
                                                .getLeftOp()
                                                .toString();
                                        App.p("!!!! " + retName + ", " + arg
                                                + ", " + plocal);
                                        // hwire.setRetWireName(retName);
                                        return hwire;
                                    }
                                }
                            }
                        }
                    }
                    // App.p(expr.getArgs());
                }
            }
        }
        return null;
    }

    public static HTTPReceiveWirePoint get(String reqMethod, int lineNum) {

        if (wps == null) {
            wps = new HashMap<>();

            // bannedStmts = new ArrayList<>();

            List<String> strs = Utils.readFrom(App.caseStudyPath + "httpWires");
            for (int i = 0; i < strs.size() / 5; i++) {
                // App.p("!!! " + strs.get(i + 1));
                int start = i * 5;
                add(strs.get(start), Integer.valueOf(strs.get(start + 1)),
                        strs.get(start + 2),
                        strs.get(start + 3).split(","),
                        strs.get(start + 4).split(","));
            }
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
            String[] recvParams) {
        RequestWirePoint reqp = new RequestWirePoint(reqMethod, lineNum);
        List<String> reqarr = Arrays.asList(reqParams);

        List<String> recvarr = Arrays.asList(recvParams);
        HTTPReceiveWirePoint recvp = new HTTPReceiveWirePoint(recvMethod, reqarr, recvarr);
        // App.p("AAAA " + reqp + ", " + recvp);
        wps.put(reqp, recvp);
    }
}
