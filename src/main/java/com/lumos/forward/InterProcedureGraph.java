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
import com.lumos.utils.Utils;
import com.lumos.wire.Banned;
import com.lumos.wire.HTTPReceiveWirePoint;
import com.lumos.wire.IdentityWire;
import com.lumos.wire.WireHTTP;
import com.lumos.wire.WireInterface;

import heros.flowfunc.Identity;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.AbstractInstanceInvokeExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JGotoStmt;
import soot.toolkits.graph.BriefUnitGraph;

public class InterProcedureGraph {

    public Map<String, MethodInfo> methodMap;
    public Map<Context, Map<Unit, IPNode>> stmtMap;

    public Set<IPNode> nodes = new HashSet<>();
    // public Set<IPNode> TrapNodes = new HashSet<>();

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
                // '!' at the beginning means don't match
                if (s.charAt(0) == '!') {
                    if (node.getDescription().contains(s.substring(1, s.length()))) {
                        match = false;
                        break;
                    }
                } else {
                    if (!node.getDescription().contains(s)) {
                        match = false;
                        break;
                    }
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
        String signature = sm.getSignature();
        if (signature.contains("Service:")) {
            String translated = WireInterface.translateServiceInterface(signature);
            // App.p("==== " + translated);
            if (translated.contains("ServiceImpl:")) {
                App.p("wired interface " + translated);
                signature = translated;
            }
        }
        minfo = methodMap.get(signature);
        String mname = sm.getSignature().toString();
        String lastMethod = context.getStackLast().sm.getSignature();
        if (minfo == null && (Utils.isCrossContext(mname))) {
            HTTPReceiveWirePoint hwire = WireHTTP.templateWire(stmt, context.getStackLast());
            if (hwire == null) {
                // App.p(stmt + ", " + stmt.getJavaSourceStartLineNumber());
                hwire = WireHTTP.get(lastMethod,
                        stmt.getJavaSourceStartLineNumber());
            }
            if (hwire != null) {
                String target = hwire.targetMethod;

                minfo = searchMethod(target);
                if (minfo != null) {
                    App.p("wired remote " + target);
                }
                result.setHwire(hwire);
            } else {
                App.p("===[SEVERE]===\nHTTP wiring failed for " + stmt);
            }

        }

        if (minfo != null) {
            result.setMinfo(minfo);
        }
        return result;
    }

    public ContextSensitiveInfo build(Context context) {

        // List<CallSite> ctrace = context.getCtrace();
        // CallSite lastSite = ctrace.get(ctrace.size() - 1);
        MethodInfo minfo = context.getLastCallSite().getMInfo();

        BriefUnitGraph cfg = minfo.cfg;

        ContextSensitiveInfo cinfo = new ContextSensitiveInfo();

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
            // minfo.sm.getActiveBody().
            for (Unit u : cfg.getSuccsOf(snode.getStmt())) {
                Stmt succ = (Stmt) u;
                IPNode succNode = getIPNode(context, succ);
                if (succNode instanceof WrapperNode) {
                    WrapperNode wn = (WrapperNode) succNode;
                    succNodes.add(wn.getEnter());
                } else {
                    succNodes.add(succNode);
                }
            }

            if (cfg.getPredsOf(stmt).isEmpty() && !(stmt.toString().contains("caughtexception"))) {
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
        return build(topContext(minfo, this));
    }

    public static Context topContext(MethodInfo minfo, InterProcedureGraph igraph) {
        return new Context(emptyContext(), new CallSite(null, minfo.sm), igraph);
    }

    public static Context emptyContext() {
        return null;
    }

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

                // Context nctx = context.deepcopy();
                ResolveResult result = resolveMethod(context, stmt);
                MethodInfo callerinfo = context.getStackLast();
                MethodInfo calleeinfo = result.getMinfo();

                if (calleeinfo == null) {
                    snode = new IdentityNode(context, stmt);
                    // if (((IdentityNode) snode).cvuses.size() == 1) {
                    // App.idnodes.add(snode);
                    // }
                } else {
                    Context nctx = context.append(stmt, calleeinfo);
                    // App.p(nctx);
                    // App.p(stmt);
                    ContextSensitiveInfo newcinfo = build(nctx);
                    EnterNode enter = new EnterNode(context, stmt);

                    Value ret = null;
                    if (stmt instanceof JAssignStmt) {
                        ret = ((JAssignStmt) stmt).getLeftOp();
                    }
                    ExitNode exit = new ExitNode(context, stmt);

                    enter.setExitNode(exit);
                    exit.setEnterNode(enter);

                    enter.setSuccessors(Collections.singletonList(newcinfo.getFirstNode()));
                    newcinfo.getFirstNode().setPredecesors(Collections.singletonList(enter));

                    List<IPNode> retStmtNodes = new ArrayList<>();
                    for (Stmt retStmt : calleeinfo.getReturnStmts()) {
                        retStmtNodes.add(getIPNode(nctx, retStmt));
                    }
                    exit.setReturnStmtNodes(retStmtNodes);
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

                            if (v1 == null || v2 == null) {
                                // App.p(v1 + ", " + v2 + ", " + s1 + ", " + s2);
                                App.p(calleeinfo.localMap);
                                App.p(calleeinfo.sm.getSignature());
                                App.p("Wiring null values!");
                                App.panicni();
                            }
                            ContextSensitiveValue cv1 = ContextSensitiveValue.getCValue(context, v1);
                            ContextSensitiveValue cv2 = ContextSensitiveValue.getCValue(nctx, v2);
                            enter.addAlising(cv1, cv2);
                            enter.setRemote(true);
                            exit.setRemote(true);
                        }
                    } else {
                        // If normal method, just connect params with args
                        SootMethod calleesm = calleeinfo.sm;
                        if (!calleesm.isStatic()) {
                            Value base = ((AbstractInstanceInvokeExpr) iexpr).getBase();
                            Value calleethis = calleesm.getActiveBody().getThisLocal();
                            ContextSensitiveValue cvbase = ContextSensitiveValue.getCValue(context, base);
                            ContextSensitiveValue cvcallee = ContextSensitiveValue.getCValue(nctx, calleethis);
                            // if(calleesm.toString().contains("sen"))

                            enter.addAlising(cvbase, cvcallee);
                        }
                        for (int i = 0; i < calleesm.getParameterCount(); i++) {
                            ContextSensitiveValue cvcaller = ContextSensitiveValue.getCValue(context, iexpr.getArg(i));
                            ContextSensitiveValue cvcallee = ContextSensitiveValue.getCValue(nctx,
                                    calleesm.getActiveBody().getParameterLocal(i));
                            enter.addAlising(cvcaller, cvcallee);
                        }
                    }
                    ContextSensitiveValue cvret = ContextSensitiveValue.getCValue(context, ret);
                    exit.setRet(cvret);
                    snode = new WrapperNode(context, enter, exit, stmt);
                }
            } else {
                String stmtname = context.getStackLast().sm.toString() + " " + stmt.toString();
                if (Banned.isStmtBanned(stmtname)) {
                    App.p("Banned stmt: " + stmtname);
                    snode = new NoopNode(context, stmt);
                } else {
                    snode = new StmtNode(context, stmt);
                }
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
