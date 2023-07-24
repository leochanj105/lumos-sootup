package com.lumos.forward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.MemoryHandler;

import com.lumos.App;
import com.lumos.analysis.MethodInfo;
import com.lumos.wire.HTTPReceiveWirePoint;
import com.lumos.wire.WireHTTP;

import polyglot.ast.Call;
import soot.SootMethod;
import soot.Unit;
import soot.JastAddJ.ClassAccess;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.toolkits.graph.BriefUnitGraph;

public class InterProcedureGraph {

    public static Map<String, MethodInfo> methodMap;
    public static Map<Context, Map<Unit, StmtNode>> stmtMap;

    public InterProcedureGraph(Map<String, MethodInfo> methodMap) {
        this.methodMap = methodMap;
        this.stmtMap = new HashMap<>();

    }

    public static MethodInfo searchMethod(String... str) {
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

    public static MethodInfo resolveMethod(Context context, Stmt stmt) {
        SootMethod sm = stmt.getInvokeExpr().getMethod();
        MethodInfo minfo = null;
        minfo = methodMap.get(sm.getSignature());
        if (minfo == null) {
            HTTPReceiveWirePoint hwire = WireHTTP.get(context.getStackLast().sm.getSignature(),
                    stmt.getJavaSourceStartLineNumber());
            // if (stmt.toString().contains("exchange")) {
            // App.p(context.getStackLast().sm.getSignature() + ", " +
            // stmt.getJavaSourceStartLineNumber());
            // }
            if (hwire != null) {
                // App.p("!!! " + hwire);
                String target = hwire.targetMethod;
                minfo = searchMethod(target);
                if (minfo != null) {
                    App.p("wired " + target);
                }
            }
        }
        return minfo;
    }

    public void build(Context context) {
        List<CallSite> ctrace = context.getCtrace();
        CallSite lastSite = ctrace.get(ctrace.size() - 1);
        MethodInfo minfo = lastSite.getMinfo();
        BriefUnitGraph cfg = minfo.cfg;
        for (Unit unit : cfg) {
            if (!(unit instanceof Stmt)) {
                App.p("Not stmt");
                App.panicni();
            }

            Stmt stmt = (Stmt) unit;

            if (stmt.containsInvokeExpr()) {
                InvokeExpr iexpr = stmt.getInvokeExpr();
                SootMethod sm = iexpr.getMethod();
                Context nctx = context.deepcopy();

                MethodInfo cminfo = resolveMethod(context, stmt);
                if (cminfo == null) {
                    // App.p(sm + " not found!");
                } else {
                    nctx.append(stmt, cminfo);
                    // App.p(nctx);
                    App.p(stmt);
                    build(nctx);
                }
            }

            StmtNode snode = getStmtNode(stmt);
            List<IPNode> preNodes = new ArrayList<>();
            List<IPNode> succNodes = new ArrayList<>();
            for (Unit u : cfg.getPredsOf(snode.getStmt())) {
                Stmt pre = (Stmt) u;
                StmtNode prenode = getStmtNode(pre);
                preNodes.add(prenode);
            }
            snode.setPredecesors(preNodes);

            for (Unit u : cfg.getSuccsOf(snode.getStmt())) {
                Stmt succ = (Stmt) u;
                StmtNode succnode = getStmtNode(succ);
                succNodes.add(succnode);
            }
            snode.setSuccessors(succNodes);

        }
    }

    public void build(String... method) {
        MethodInfo minfo = App.searchMethod(method);
        if (minfo == null) {
            App.p(method + " Not found");
            return;
        }

        build(topContext(minfo));
    }

    public static Context topContext(MethodInfo minfo) {
        CallSite top = new CallSite(null, minfo);
        return new Context(Collections.singletonList(top));
    }

    public static Context emptyContext() {
        return new Context(new ArrayList<>());
    }

    public StmtNode getStmtNode(Stmt stmt) {
        return getStmtNode(emptyContext(), stmt);
    }

    public StmtNode getStmtNode(Context context, Stmt stmt) {
        Map<Unit, StmtNode> umap;
        if (!stmtMap.containsKey(context)) {
            // snode = new StmtNode(stmt);
            stmtMap.put(context, new HashMap<>());
        }
        umap = stmtMap.get(context);

        StmtNode snode = null;
        if (!umap.containsKey(stmt)) {
            snode = new StmtNode(context, stmt);
            umap.put(stmt, snode);
        } else {
            snode = umap.get(stmt);
        }
        return snode;
    }

}
