package com.lumos.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lumos.App;
import com.lumos.common.Dependency;
import com.lumos.common.TracePoint;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.Stmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JReturnVoidStmt;
import soot.toolkits.graph.BriefUnitGraph;

public class MethodInfo {
    public SootMethod sm;
    public Map<TracePoint, List<TracePoint>> depGraph;
    // public Map<Value, String> nameMap;

    public Map<Unit, Map<Value, TracePoint>> tpMap = new HashMap<>();
    public Map<Integer, List<Unit>> stmtMap = new HashMap<>();

    public ReachingDefAnalysis reachingAnalysis;

    public BriefUnitGraph cfg;

    public Map<String, Value> localMap;

    public Map<Stmt, Set<Stmt>> cfDependency;

    public MethodInfo(SootMethod sm) {
        this.sm = sm;
        this.cfg = new BriefUnitGraph(sm.getActiveBody());
        buildLocalMap();
        // buildNameMap();
    }

    public void buildLocalMap() {
        localMap = new HashMap<>();
        for (Iterator<Unit> it = cfg.iterator(); it.hasNext();) {
            Unit unit = it.next();
            for (ValueBox vb : unit.getUseAndDefBoxes()) {
                Value v = vb.getValue();
                if (v instanceof Local) {
                    localMap.put(v.toString(), v);
                }
            }
        }
    }

    public Value getLocal(String name) {
        return localMap.get(name);
    }

    public Map<TracePoint, List<TracePoint>> analyzeDef() {
        reachingAnalysis = new ReachingDefAnalysis(this.cfg);

        depGraph = new HashMap<>();
        for (Iterator<Unit> it = cfg.iterator(); it.hasNext();) {
            Unit unit = it.next();

            int lineNum = unit.getJavaSourceStartLineNumber();
            if (!stmtMap.containsKey(lineNum)) {
                stmtMap.put(lineNum, new ArrayList<>());
            }
            stmtMap.get(lineNum).add(unit);

            if (!tpMap.containsKey(unit)) {
                tpMap.put(unit, new HashMap<>());
            }
            Map<Value, Set<Dependency>> dmap = reachingAnalysis.getBeforeUnit(unit);

            Map<Value, TracePoint> currStmtMap = tpMap.get(unit);

            String refName = null;
            // Create Tracepoints and add description texts from frontend
            for (ValueBox vbox : unit.getUseAndDefBoxes()) {
                Value v = vbox.getValue();
                TracePoint tmp = new TracePoint(unit, v, "");
                currStmtMap.put(v, tmp);
                if (!depGraph.containsKey(tmp)) {
                    depGraph.put(tmp, new ArrayList<>());
                }
            }

        }

        return depGraph;
    }

    public void printLine(int line) {
        List<Unit> sl = stmtMap.get(line);
        for (int i = 0; i < sl.size(); i++) {
            System.out.println(line + ": " + i + ", " + sl.get(i));
        }
    }

    public Unit getStmt(int line, int pos) {
        return stmtMap.get(line).get(pos);
    }

    public Value getValue(Unit unit, int pos) {
        List<Value> tpl = new ArrayList<>(tpMap.get(unit).keySet());
        return tpl.get(pos);
    }

    public void printValue(Unit unit) {
        System.out.println(unit + " at line " +
                unit.getJavaSourceStartLineNumber() + " : ******");
        List<Value> tpl = new ArrayList<>(tpMap.get(unit).keySet());
        for (int i = 0; i < tpl.size(); i++) {
            System.out.println(i + ", " + tpl.get(i));
        }
    }

    public List<TracePoint> getPrev(TracePoint tp) {
        return depGraph.get(tp);
    }

    public Set<Dependency> getPrev(Unit unit, Value value) {
        // return getPrev(this.tpMap.get(stmt).get(value));
        return this.reachingAnalysis.getBeforeUnit(unit).get(value);
    }

    public void buildPostDominanceFrontier() {
        Map<Stmt, Set<Stmt>> dominators = new HashMap<>();
        Set<Stmt> worklist = new HashSet<>();
        Map<Stmt, Set<Stmt>> cfdeps = new HashMap<>();
        Set<Stmt> allStmt = new HashSet<>();
        for (Unit u : this.sm.getActiveBody().getUnits()) {
            Stmt stmt = (Stmt) u;
            allStmt.add(stmt);
            worklist.add(stmt);
            cfdeps.put(stmt, new HashSet<>());
        }
        for (Unit u : this.sm.getActiveBody().getUnits()) {
            Stmt stmt = (Stmt) u;
            dominators.put(stmt, new HashSet<>(allStmt));

        }

        while (!worklist.isEmpty()) {
            // changed = false;
            Stmt stmt = worklist.iterator().next();
            worklist.remove(stmt);
            Set<Stmt> pds = new HashSet<>(Collections.singletonList(stmt));
            Set<Stmt> intersection = null;
            for (Unit succ : cfg.getSuccsOf(stmt)) {
                Stmt succStmt = (Stmt) succ;
                Set<Stmt> succpds = dominators.get(succStmt);
                if (intersection == null) {
                    intersection = new HashSet<>(succpds);
                } else {
                    intersection.retainAll(succpds);
                }
            }
            if (!(intersection == null)) {
                pds.addAll(intersection);
            }
            if (!pds.equals(dominators.get(stmt))) {
                for (Unit pred : cfg.getPredsOf(stmt)) {
                    Stmt predStmt = (Stmt) pred;
                    worklist.add(predStmt);
                }
                dominators.put(stmt, pds);
            }
        }

        for (Unit u : this.sm.getActiveBody().getUnits()) {
            Stmt stmt = (Stmt) u;
            Set<Stmt> pdsuccs = new HashSet<>(dominators.get(stmt));
            // boolean existpost = false;
            for (Unit succ : cfg.getSuccsOf(stmt)) {
                Stmt succstmt = (Stmt) succ;
                pdsuccs.addAll(dominators.get(succstmt));
            }
            for (Stmt st : pdsuccs) {
                if (!dominators.get(stmt).contains(st)) {
                    cfdeps.get(st).add(stmt);
                }
            }
        }

        this.cfDependency = cfdeps;
    }

    public List<TracePoint> getReturnTps() {
        List<TracePoint> tps = new ArrayList<>();

        for (Iterator<Unit> it = cfg.iterator(); it.hasNext();) {
            Unit unit = it.next();
            Stmt stmt = (Stmt) unit;
            if (stmt instanceof JReturnStmt) {
                tps.add(tpMap.get(stmt).get(((JReturnStmt) stmt).getUseBoxes().get(0).getValue()));
            }
        }

        return tps;
    }

    public List<Stmt> getReturnStmts() {
        List<Stmt> rets = new ArrayList<>();

        for (Iterator<Unit> it = cfg.iterator(); it.hasNext();) {
            Unit unit = it.next();
            Stmt stmt = (Stmt) unit;
            if ((stmt instanceof JReturnStmt) || (stmt instanceof JReturnVoidStmt)) {
                rets.add(stmt);
            }
        }
        return rets;
    }

    public List<Value> getParamValues() {
        List<Value> params = new ArrayList<>();
        if (!this.sm.isStatic()) {
            params.add(this.sm.getActiveBody().getThisLocal());
        }
        for (Value v : this.sm.getActiveBody().getParameterLocals()) {
            params.add(v);
        }
        return params;
    }

}