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
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.java.core.JavaSootMethod;
import sootup.java.sourcecode.frontend.WalaSootMethod;

public class MethodInfo {
    public JavaSootMethod sm;
    public Map<TracePoint, List<TracePoint>> depGraph;
    // public Map<Value, String> nameMap;

    public Map<Stmt, Map<Value, TracePoint>> tpMap = new HashMap<>();
    public Map<Integer, List<Stmt>> stmtMap = new HashMap<>();

    public ReachingDefAnalysis reachingAnalysis;
    public CFAnalysis cfAnalysis;

    public MethodInfo(JavaSootMethod sm) {
        this.sm = sm;
        // buildNameMap();
    }

    // public void buildNameMap() {
    // String[][] names = this.wsm.getDebugInfo().getSourceNamesForValues();

    // Map<Integer, Local> lmap = wsm.localMap;
    // Map<Value, String> nmap = new HashMap<>();
    // for (Integer i : lmap.keySet()) {
    // if (names[i].length > 0) {
    // nmap.put(lmap.get(i), names[i][0]);
    // } else {
    // }
    // }
    // this.nameMap = nmap;

    // // this.nameMap = new HashMap<>();
    // }

    public Map<TracePoint, List<TracePoint>> analyzeDef() {
        // System.out.println(this);
        StmtGraph<?> cfg = sm.getBody().getStmtGraph();

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
            // if (lineNum == 126) {
            // System.out.println(lineNum + ", " + stmt + ", " + stmtMap.get(lineNum) + ", "
            // + this + ", " + this.sm);
            // }
            Map<Value, Set<Dependency>> dmap = reachingAnalysis.getBeforeStmt(stmt);

            Map<Value, TracePoint> currStmtMap = tpMap.get(stmt);

            String refName = null;
            // Create Tracepoints and add description texts from frontend
            for (Value v : stmt.getUsesAndDefs()) {

                // String name = null;
                // name = nameMap.get(v);
                // if (v instanceof JInstanceFieldRef) {
                // JInstanceFieldRef refv = (JInstanceFieldRef) v;
                // String basename = nameMap.get(refv.getBase());
                // String refname = refv.getFieldSignature().getSubSignature().getName();
                // if (basename == null) {
                // basename = refv.getBase().getName();
                // }

                // name = basename + "." + refname;
                // refName = name;
                // // nameMap.put(v, name);

                // }

                // TracePoint tmp = new TracePoint(stmt, v, name);
                TracePoint tmp = new TracePoint(stmt, v, "");
                currStmtMap.put(v, tmp);
                if (!depGraph.containsKey(tmp)) {
                    depGraph.put(tmp, new ArrayList<>());
                }
            }

            // Propagate name of references
            // if (stmt instanceof JAssignStmt) {
            // JAssignStmt astmt = (JAssignStmt) stmt;
            // Value rop = astmt.getRightOp();

            // if (rop instanceof JInstanceFieldRef) {
            // Value lop = astmt.getLeftOp();
            // System.out.println(lop + ", " + refName);
            // nameMap.put(lop, refName);
            // }
            // }

            // Defs depend on Uses in one line
            // for (Value v : stmt.getDefs()) {
            // TracePoint tmp = currStmtMap.get(v);
            // for (Value v2 : stmt.getUses()) {
            // TracePoint tmp2 = currStmtMap.get(v2);
            // depGraph.get(tmp).add(tmp2);

            // // tpMap.get(stmt).put(tmp2);
            // }
            // }

            // Uses depend on previous reaching defs
            // for (Value v : stmt.getUses()) {
            // TracePoint tmp = currStmtMap.get(v);
            // if (dmap.containsKey(v)) {
            // // p(v + " :: " + dmap.get(v));
            // for (Dependency dep : dmap.get(v)) {
            // for (Value v2 : dep.stmt.getDefs()) {
            // if (tpMap.get(dep.stmt) == null) {
            // continue;
            // // System.out.println(dep.stmt + ", " + v2);
            // }
            // TracePoint tmp2 = tpMap.get(dep.stmt).get(v2);
            // depGraph.get(tmp).add(tmp2);
            // // if (tmp.toString().contains("<$r2, 55>"))
            // // System.out.println(tmp + ", " + tmp2);
            // }
            // }
            // }
            // }
        }

        return depGraph;
    }

    public void printLine(int line) {
        // System.out.println(line + ".. " + stmtMap.get(line) + ", " + this.sm);
        List<Stmt> sl = stmtMap.get(line);
        for (int i = 0; i < sl.size(); i++) {
            System.out.println(line + ": " + i + ", " + sl.get(i));
        }
    }

    public Stmt getStmt(int line, int pos) {
        return stmtMap.get(line).get(pos);
    }

    public Value getValue(Stmt stmt, int pos) {
        List<Value> tpl = new ArrayList<>(tpMap.get(stmt).keySet());
        return tpl.get(pos);
    }

    public void printValue(Stmt stmt) {
        System.out.println(stmt + " at line " +
                stmt.getPositionInfo().getStmtPosition().getFirstLine() + " : ******");
        List<Value> tpl = new ArrayList<>(tpMap.get(stmt).keySet());
        for (int i = 0; i < tpl.size(); i++) {
            System.out.println(i + ", " + tpl.get(i));
        }
    }

    public void analyzeCF() {
        if (this.reachingAnalysis == null) {
            System.out.println("RA analysis not done before CF analysis!!");
            return;
        }
        StmtGraph<?> cfg = sm.getBody().getStmtGraph();
        this.cfAnalysis = new CFAnalysis(cfg, reachingAnalysis);
    }

    public List<TracePoint> getPrev(TracePoint tp) {
        return depGraph.get(tp);
    }

    public Set<Dependency> getPrev(Stmt stmt, Value value) {
        // return getPrev(this.tpMap.get(stmt).get(value));
        return this.reachingAnalysis.getBeforeStmt(stmt).get(value);
    }

    public Set<Dependency> getCF(Stmt stmt) {
        return this.cfAnalysis.getBeforeStmt(stmt);
    }

    public void printToJimple() {

    }

    public List<TracePoint> getReturnTps() {
        List<TracePoint> tps = new ArrayList<>();

        for (Stmt stmt : sm.getBody().getStmtGraph()) {
            if (stmt instanceof JReturnStmt) {
                tps.add(tpMap.get(stmt).get(((JReturnStmt) stmt).getUses().get(0)));
            }
        }
        return tps;
    }

    public List<Stmt> getReturnStmts() {
        List<Stmt> rets = new ArrayList<>();

        for (Stmt stmt : sm.getBody().getStmtGraph()) {
            if ((stmt instanceof JReturnStmt) || (stmt instanceof JReturnVoidStmt)) {
                rets.add(stmt);
            }
        }
        return rets;
    }

    public List<Value> getParamValues() {
        List<Value> params = new ArrayList<>();
        if (!this.sm.isStatic()) {
            params.add(this.sm.getBody().getThisLocal());
        }
        for (Value v : this.sm.getBody().getParameterLocals()) {
            params.add(v);
        }
        return params;
    }

}