package com.lumos.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lumos.common.Dependency;
import com.lumos.common.TracePoint;

import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.java.sourcecode.frontend.WalaSootMethod;

public class MethodInfo {
    public WalaSootMethod wsm;
    public Map<TracePoint, List<TracePoint>> depGraph;
    public Map<Value, String> nameMap;

    public Map<Stmt, Map<Value, TracePoint>> tpMap = new HashMap<>();
    public Map<Integer, List<Stmt>> stmtMap = new HashMap<>();

    public ReachingDefAnalysis reachingAnalysis;

    public MethodInfo(WalaSootMethod wsm) {
        this.wsm = wsm;

        buildNameMap();
    }

    public void buildNameMap() {
        String[][] names = this.wsm.getDebugInfo().getSourceNamesForValues();

        Map<Integer, Local> lmap = wsm.localMap;
        Map<Value, String> nmap = new HashMap<>();
        for (Integer i : lmap.keySet()) {
            if (names[i].length > 0) {
                nmap.put(lmap.get(i), names[i][0]);
            } else {
            }
        }
        this.nameMap = nmap;

        // this.nameMap = new HashMap<>();
    }

    public Map<TracePoint, List<TracePoint>> analyzeDef() {

        StmtGraph<?> cfg = wsm.getBody().getStmtGraph();

        reachingAnalysis = new ReachingDefAnalysis(cfg);

        depGraph = new HashMap<>();
        // p(nmap + "\n -----");
        for (Stmt stmt : cfg.getStmts()) {
            int lineNum = stmt.getPositionInfo().getStmtPosition().getFirstLine();
            if (!stmtMap.containsKey(lineNum)) {
                stmtMap.put(lineNum, new ArrayList<>());
            }
            stmtMap.get(lineNum).add(stmt);

            if (!tpMap.containsKey(stmt)) {
                tpMap.put(stmt, new HashMap<>());
            }

            Map<Value, Set<Dependency>> dmap = reachingAnalysis.getBeforeStmt(stmt);

            Map<Value, TracePoint> currStmtMap = tpMap.get(stmt);
            // Create Tracepoints and add description texts from frontend
            for (Value v : stmt.getUsesAndDefs()) {

                String name = null;
                name = nameMap.get(v);
                if (v instanceof JInstanceFieldRef) {
                    JInstanceFieldRef refv = (JInstanceFieldRef) v;
                    String basename = nameMap.get(refv.getBase());
                    if (basename == null)
                        basename = refv.getBase().getName();
                    String refname = refv.getFieldSignature().getName();
                    name = basename + "." + refname;
                }

                TracePoint tmp = new TracePoint(stmt, v, name);
                currStmtMap.put(v, tmp);
                if (!depGraph.containsKey(tmp)) {
                    depGraph.put(tmp, new ArrayList<>());
                }
            }

            // Defs depend on Uses in one line
            for (Value v : stmt.getDefs()) {
                TracePoint tmp = currStmtMap.get(v);
                for (Value v2 : stmt.getUses()) {
                    TracePoint tmp2 = currStmtMap.get(v2);
                    depGraph.get(tmp).add(tmp2);

                    // tpMap.get(stmt).put(tmp2);
                }
            }

            // Uses depend on previous reaching defs
            for (Value v : stmt.getUses()) {
                TracePoint tmp = currStmtMap.get(v);
                if (dmap.containsKey(v)) {
                    // p(v + " :: " + dmap.get(v));
                    for (Dependency dep : dmap.get(v)) {
                        for (Value v2 : dep.stmt.getDefs()) {
                            TracePoint tmp2 = tpMap.get(dep.stmt).get(v2);
                            depGraph.get(tmp).add(tmp2);
                            // if (tmp.toString().contains("<$r2, 55>"))
                            // System.out.println(tmp + ", " + tmp2);
                        }
                    }
                }
            }
        }

        return depGraph;
    }

    public List<TracePoint> getPrev(TracePoint tp) {
        return depGraph.get(tp);
    }

    public List<TracePoint> getReturnTps() {
        List<TracePoint> tps = new ArrayList<>();

        for (Stmt stmt : wsm.getBody().getStmtGraph()) {
            if (stmt instanceof JReturnStmt) {
                tps.add(tpMap.get(stmt).get(((JReturnStmt) stmt).getUses().get(0)));
            }
        }
        return tps;
    }

}