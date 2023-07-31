package com.lumos.analysis;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

// import com.lumos.common.DepType;
import com.lumos.common.Dependency;

import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.baf.Inst;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.AbstractInstanceInvokeExpr;
import soot.jimple.internal.AbstractInvokeExpr;
import soot.jimple.internal.JReturnVoidStmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

// imp

public class ReachingDefAnalysis {
    public final Map<Unit, Map<Value, Set<Dependency>>> liveIn = new HashMap<>();
    public final Map<Unit, Map<Value, Set<Dependency>>> liveOut = new HashMap<>();

    public ReachingDefAnalysis(DirectedGraph<Unit> cfg) {
        // List<Unit> startingUnits = new ArrayList<>();
        Set<Unit> workList = new HashSet<>();
        for (Iterator<Unit> it = cfg.iterator(); it.hasNext();) {
            Unit unit = it.next();
            liveIn.put(unit, Collections.emptyMap());
            liveOut.put(unit, Collections.emptyMap());
            workList.add(unit);
            // if (cfg.getPredsOf(unit).isEmpty()) {
            // startingUnits.add(unit);
            // }
        }

        // boolean fixed = false;
        while (!workList.isEmpty()) {
            // fixed = true;
            // Deque<Unit> queue = new ArrayDeque<>(startingUnits);
            // HashSet<Unit> visitedUnits = new HashSet<>();
            // while (!queue.isEmpty()) {
            Unit unit = workList.iterator().next();
            workList.remove(unit);
            // visitedUnits.add(unit);
            // System.out.println(unit);
            // if (!(unit instanceof Unit)) {
            // continue;
            // }

            Map<Value, Set<Dependency>> in = new HashMap<>();
            for (Unit pred : cfg.getPredsOf(unit)) {
                in = merge(in, liveOut.get(pred));
            }
            // for (Unit epred : graph.exceptionalPredecessors(unit)) {
            // in = merge(in, liveOut.get(epred));
            // // System.out.println(unit + " ---> " + epred);
            // }

            if (isNotEqual(in, liveIn.get(unit))) {
                // fixed = false;
                liveIn.put(unit, new HashMap<>(in));
            }

            // if (unit instanceof JReturnVoidStmt) {
            // System.out.println("--- " + liveIn.get(unit));
            // }

            Map<Value, Set<Dependency>> out = copy(in);
            for (ValueBox vbox : unit.getDefBoxes()) {
                Value v = vbox.getValue();
                out = kill(out, v);
                out = generate(out, v, new Dependency(unit, Dependency.DepType.RW));
            }

            // This conservatively assume a function call changes all parameters
            // This is the "safe" data-flow function approach
            if (unit instanceof Stmt) {
                Stmt stmt = (Stmt) unit;

                if (stmt.containsInvokeExpr()) {
                    // System.out.println(unit + " === " + in);
                    InvokeExpr iexpr = stmt.getInvokeExpr();
                    if (iexpr instanceof AbstractInstanceInvokeExpr) {
                        Value caller = ((AbstractInstanceInvokeExpr) iexpr).getBase();
                        out = generate(out, caller, new Dependency(unit, Dependency.DepType.CALL));
                    }
                    for (int i = 0; i < iexpr.getArgCount(); i++) {
                        Value arg = iexpr.getArg(i);
                        out = generate(out, arg, new Dependency(unit, Dependency.DepType.CALL));
                    }
                }
            }

            if (isNotEqual(out, liveOut.get(unit))) {
                // fixed = false;
                for (Unit succ : cfg.getSuccsOf(unit)) {
                    workList.add(succ);
                }
                liveOut.put(unit, out);
            }

        }

    }

    public Map<Value, Set<Dependency>> copy(Map<Value, Set<Dependency>> original) {
        Map<Value, Set<Dependency>> newm = new HashMap<>();
        for (Value v : original.keySet()) {
            newm.put(v, new HashSet<>(original.get(v)));
        }
        return newm;
    }

    public Map<Value, Set<Dependency>> getBeforeUnit(@Nonnull Unit unit) {
        if (!liveIn.containsKey(unit)) {
            throw new RuntimeException("Unit: " + unit + " is not in UnitGraph!");
        }
        return liveIn.get(unit);
    }

    public Map<Value, Set<Dependency>> getAfterUnit(@Nonnull Unit unit) {
        if (!liveOut.containsKey(unit)) {
            throw new RuntimeException("Unit: " + unit + " is not in UnitGraph!");
        }
        return liveOut.get(unit);
    }

    private Map<Value, Set<Dependency>> merge(@Nonnull Map<Value, Set<Dependency>> set1,
            @Nonnull Map<Value, Set<Dependency>> set2) {
        if (set1.isEmpty()) {
            return set2;
        } else {
            Map<Value, Set<Dependency>> set3 = copy(set1);
            for (Value v : set3.keySet()) {
                if (set2.containsKey(v)) {
                    Set<Dependency> loc3 = set3.get(v);
                    for (Dependency dep : set2.get(v)) {
                        loc3.add(dep);
                    }
                }
            }
            // set1.addAll(set2);
            return set3;
        }
    }

    private Map<Value, Set<Dependency>> kill(@Nonnull Map<Value, Set<Dependency>> set1, Value v) {
        if (!set1.containsKey(v))
            return set1;
        Set<Dependency> dset = set1.get(v);
        dset.removeIf(d -> d.dtype != Dependency.DepType.CF);
        return set1;
    }

    private Map<Value, Set<Dependency>> generate(@Nonnull Map<Value, Set<Dependency>> set1, Value v,
            Dependency dep) {
        if (!set1.containsKey(v)) {
            Set<Dependency> sdep = new HashSet<>();
            sdep.add(dep);
            set1.put(v, sdep);
        } else {
            set1.get(v).add(dep);
        }
        return set1;
    }

    private boolean isNotEqual(@Nonnull Map<Value, Set<Dependency>> set1, @Nonnull Map<Value, Set<Dependency>> set2) {
        if (!set1.keySet().equals(set2.keySet())) {
            return true;
        } else {
            for (Value v : set1.keySet()) {
                if (!set1.get(v).equals(set2.get(v))) {
                    return true;
                }
            }
        }
        return false;
    }

}
