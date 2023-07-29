package com.lumos.forward;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lumos.App;
import com.lumos.analysis.MethodInfo;
import com.lumos.wire.HTTPReceiveWirePoint;
import com.lumos.wire.WireHTTP;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.AbstractInstanceInvokeExpr;
import soot.jimple.internal.JAssignStmt;
import soot.toolkits.graph.BriefUnitGraph;

public class InterProcedureGraph {

    public Map<String, MethodInfo> methodMap;
    public Map<Context, Map<Unit, IPNode>> stmtMap;

    public Set<IPNode> nodes = new HashSet<>();

    public IPNode initialNode;

    public InterProcedureGraph(Map<String, MethodInfo> methodMap) {
        this.methodMap = methodMap;
        this.stmtMap = new HashMap<>();

    }

    public IPNode getLastNode() {
        for (IPNode node : nodes) {
            if (node.getSuccessors().isEmpty()) {
                return node;
            }
        }
        return null;
    }

    public IPNode searchNode(String... str) {
        for (IPNode node : nodes) {
            // if (!node.getType().equals("stmt")) {
            // continue;
            // }
            boolean match = true;
            for (String s : str) {
                if (!node.getDescription().contains(s)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return node;
            }
        }
        return null;
    }

    public MethodInfo searchMethod(String... str) {
        for (String sig : methodMap.keySet()) {
            boolean match = true;
            for (String s : str) {
                if (!sig.toString().contains(s)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return methodMap.get(sig);
            }
        }
        return null;
    }

    public ResolveResult resolveMethod(Context context, Stmt stmt) {
        SootMethod sm = stmt.getInvokeExpr().getMethod();
        MethodInfo minfo = null;
        // App.p(context + ", " + stmt);
        ResolveResult result = new ResolveResult();
        minfo = methodMap.get(sm.getSignature());
        if (minfo == null) {
            String lastMethod = context.getStackLast().sm.getSignature();
            HTTPReceiveWirePoint hwire = WireHTTP.get(lastMethod,
                    stmt.getJavaSourceStartLineNumber());
            // if (stmt.toString().contains("exchange")) {
            // App.p(context.getStackLast().sm.getSignature() + ", " +
            // stmt.getJavaSourceStartLineNumber());
            // }
            if (hwire != null) {
                String target = hwire.targetMethod;
                minfo = searchMethod(target);
                if (minfo != null) {
                    App.p("wired " + target);
                }
                result.setHwire(hwire);
            } else {

                // App.p(stmt + " at " + stmt.getJavaSourceStartLineNumber() + " not found");
            }
        }

        if (minfo != null) {
            result.setMinfo(minfo);
        }
        return result;
    }

    // public IPNode resolveStmt(Context context, Stmt stmt) {

    // } else {
    // return getIPNode(context, stmt);
    // }
    // }

    public ContextSensitiveInfo build(Context context) {

        List<CallSite> ctrace = context.getCtrace();
        CallSite lastSite = ctrace.get(ctrace.size() - 1);
        MethodInfo minfo = lastSite.getMinfo();
        BriefUnitGraph cfg = minfo.cfg;

        ContextSensitiveInfo cinfo = new ContextSensitiveInfo();
        // if (minfo.sm.getName().contains("pay")) {
        // Value v = minfo.localMap.get("info");
        // App.p("!!!!! " + v + ", " + v.getType());
        // }
        // if (minfo.sm.getName().contains("sendInsidePayment")) {
        // Value v = minfo.localMap.get("$u0");
        // App.p("!!!!! " + v + ", " + v.getType());
        // }

        for (Unit unit : cfg) {
            if (!(unit instanceof Stmt)) {
                App.p("Not stmt");
                App.panicni();
            }

            Stmt stmt = (Stmt) unit;
            List<IPNode> preNodes = new ArrayList<>();
            List<IPNode> succNodes = new ArrayList<>();
            IPNode snode = getIPNode(context, stmt);
            for (Unit u : cfg.getPredsOf(snode.getStmt())) {
                Stmt pre = (Stmt) u;
                IPNode prenode = getIPNode(context, pre);
                if (prenode instanceof WrapperNode) {
                    WrapperNode wn = (WrapperNode) prenode;
                    preNodes.add(wn.getExit());
                } else {
                    preNodes.add(prenode);
                }

            }

            for (Unit u : cfg.getSuccsOf(snode.getStmt())) {
                Stmt succ = (Stmt) u;
                IPNode succNode = getIPNode(context, succ);
                // if (stmt.toString().contains("specialinvoke this.<java.lang.Object: void
                // <init>()>()") &&
                // context.ctrace.size() < 3) {
                // App.p("()()" + succ + ", " + succNode);
                // }
                if (succNode instanceof WrapperNode) {
                    WrapperNode wn = (WrapperNode) succNode;
                    succNodes.add(wn.getEnter());
                } else {
                    succNodes.add(succNode);
                }
            }
            // if (stmt.toString().contains("specialinvoke this.<java.lang.Object: void
            // <init>()>()") &&
            // context.ctrace.size() < 3) {
            // App.p("----!!! " + context);
            // App.p(cfg.getSuccsOf(stmt));
            // App.p(succNodes);
            // }

            if (cfg.getPredsOf(stmt).isEmpty()) {
                cinfo.setFirstNode(snode);
            }

            if (snode instanceof WrapperNode) {
                WrapperNode wn = (WrapperNode) snode;
                EnterNode enter = wn.getEnter();
                ExitNode exit = wn.getExit();
                enter.setPredecesors(preNodes);
                // enter.setSuccessors(Collections.singletonList(newcinfo.getFirstNode()));

                // exit.setPredecesors(newcinfo.getRetNodes());
                exit.setSuccessors(succNodes);

            } else {
                snode.setPredecesors(preNodes);
                snode.setSuccessors(succNodes);

            }
            if ((stmt instanceof ReturnStmt) || (stmt instanceof ReturnVoidStmt)) {
                cinfo.append(snode);
            }

        }
        return cinfo;

    }

    public ContextSensitiveInfo build(String... method) {
        MethodInfo minfo = App.searchMethod(method);
        if (minfo == null) {
            App.p(method + " Not found");
            return null;
        }
        App.p("buiding " + minfo.sm);
        return build(topContext(minfo));
    }

    public static Context topContext(MethodInfo minfo) {
        CallSite top = new CallSite(null, minfo);
        return new Context(Collections.singletonList(top));
    }

    public static Context emptyContext() {
        return new Context(new ArrayList<>());
    }

    // public IPNode getIPNode(Stmt stmt) {
    // return getIPNode(emptyContext(), stmt);
    // }

    public IPNode getIPNode(Context context, Stmt stmt) {
        Map<Unit, IPNode> umap;
        if (!stmtMap.containsKey(context)) {
            // snode = new StmtNode(stmt);
            stmtMap.put(context, new HashMap<>());
        }
        umap = stmtMap.get(context);

        IPNode snode = null;
        if (!umap.containsKey(stmt)) {
            if (stmt.containsInvokeExpr()) {
                InvokeExpr iexpr = stmt.getInvokeExpr();

                Context nctx = context.deepcopy();
                ResolveResult result = resolveMethod(context, stmt);
                MethodInfo callerinfo = context.getStackLast();
                MethodInfo calleeinfo = result.getMinfo();

                if (calleeinfo == null) {
                    // App.p(sm + " not found!");
                    snode = new NoopNode(context, stmt);
                } else {
                    // App.p("Enter " + calleeinfo.sm.getName() + " from " +
                    // callerinfo.sm.getName());
                    nctx.append(stmt, calleeinfo);
                    // App.p(nctx);
                    // App.p(stmt);
                    ContextSensitiveInfo newcinfo = build(nctx);
                    EnterNode enter = new EnterNode(nctx, stmt);

                    Value ret = null;
                    if (stmt instanceof JAssignStmt) {
                        ret = ((JAssignStmt) stmt).getLeftOp();
                    }
                    ExitNode exit = new ExitNode(nctx, stmt);

                    enter.setSuccessors(Collections.singletonList(newcinfo.getFirstNode()));
                    newcinfo.getFirstNode().setPredecesors(Collections.singletonList(enter));
                    exit.setReturnStmts(calleeinfo.getReturnStmts());
                    exit.setPredecesors(newcinfo.getRetNodes());
                    for (IPNode n : newcinfo.getRetNodes()) {
                        n.setSuccessors(Collections.singletonList(exit));
                    }

                    HTTPReceiveWirePoint hwire = result.getHwire();
                    if (hwire != null) {
                        // If this is wired method, then connect aliases
                        // by the specification
                        List<String> sendParams = hwire.getSendParams();
                        List<String> recvParams = hwire.getRecvParams();
                        for (int i = 0; i < sendParams.size(); i++) {
                            String s1 = sendParams.get(i);
                            String s2 = recvParams.get(i);
                            Value v1 = callerinfo.getLocal(s1);
                            Value v2 = calleeinfo.getLocal(s2);
                            // App.p("Added aliasing pairs: " + s1 + " in " + callerinfo.sm + " and " + s2 +
                            // " in "
                            // + calleeinfo.sm);
                            if (v1 == null || v2 == null) {
                                App.p("Wiring null values!");
                                App.panicni();
                            }
                            ContextSensitiveValue cv1 = new ContextSensitiveValue(context, v2);
                            ContextSensitiveValue cv2 = new ContextSensitiveValue(nctx, v2);
                            enter.addAlising(cv1, cv2);
                            enter.setRemote(true);
                            // App.p("Added aliasing pairs: " + v1 + " in " + callerinfo.sm + " and " + v2 +
                            // " in "
                            // + calleeinfo.sm);
                            ret = callerinfo.getLocal(hwire.getRetWireName());
                            if (ret == null) {
                                App.p("Wiring null return value!");
                                App.panicni();
                            }
                        }
                    } else {
                        // If normal method, just connect params with args
                        SootMethod calleesm = calleeinfo.sm;
                        if (!calleesm.isStatic()) {
                            Value base = ((AbstractInstanceInvokeExpr) iexpr).getBase();
                            Value calleethis = calleesm.getActiveBody().getThisLocal();
                            ContextSensitiveValue cvbase = new ContextSensitiveValue(context, base);
                            ContextSensitiveValue cvcallee = new ContextSensitiveValue(nctx, calleethis);
                            enter.addAlising(cvbase, cvcallee);
                        }
                        for (int i = 0; i < calleesm.getParameterCount(); i++) {
                            ContextSensitiveValue cvcaller = new ContextSensitiveValue(context, iexpr.getArg(i));
                            ContextSensitiveValue cvcallee = new ContextSensitiveValue(nctx,
                                    calleesm.getActiveBody().getParameterLocal(i));
                            enter.addAlising(cvcaller, cvcallee);
                        }
                    }
                    ContextSensitiveValue cvret = new ContextSensitiveValue(context, ret);
                    exit.setRet(cvret);
                    snode = new WrapperNode(context, enter, exit, stmt);
                }
            } else {
                snode = new StmtNode(context, stmt);
            }
            umap.put(stmt, snode);
            if (snode instanceof WrapperNode) {
                WrapperNode wnode = (WrapperNode) snode;
                nodes.add(wnode.getEnter());
                nodes.add(wnode.getExit());
            } else {
                nodes.add(snode);
            }
        } else

        {
            snode = umap.get(stmt);
        }
        return snode;
    }

}
