package com.lumos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.java.sourcecode.frontend.WalaSootMethod;

public class MethodInfo {
    WalaSootMethod wsm;
    Map<TracePoint, List<TracePoint>> depGraph;
    Map<Value, String> nameMap;

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
    }

    public Map<TracePoint, List<TracePoint>> analyzeDef() {

        StmtGraph<?> cfg = wsm.getBody().getStmtGraph();

        ReachingDefAnalysis analysis = new ReachingDefAnalysis(cfg);

        depGraph = new HashMap<>();
        // p(nmap + "\n -----");
        for (Stmt stmt : cfg.getStmts()) {

            Map<Value, Set<Dependency>> dmap = analysis.getBeforeStmt(stmt);

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
                if (!depGraph.containsKey(tmp)) {
                    depGraph.put(tmp, new ArrayList<>());
                }
            }

            // Defs depend on Uses in one line
            for (Value v : stmt.getDefs()) {
                TracePoint tmp = new TracePoint(stmt, v);
                for (Value v2 : stmt.getUses()) {
                    TracePoint tmp2 = new TracePoint(stmt, v2);
                    depGraph.get(tmp).add(tmp2);
                }
            }

            // Uses depend on previous reaching defs
            for (Value v : stmt.getUses()) {
                TracePoint tmp = new TracePoint(stmt, v);
                if (dmap.containsKey(v)) {
                    // p(v + " :: " + dmap.get(v));
                    for (Dependency dep : dmap.get(v)) {
                        for (Value v2 : dep.stmt.getUsesAndDefs()) {
                            TracePoint tmp2 = new TracePoint(dep.stmt, v2);
                            depGraph.get(tmp).add(tmp2);
                        }
                    }
                }
            }
        }

        return depGraph;
    }
    // public void construct

}