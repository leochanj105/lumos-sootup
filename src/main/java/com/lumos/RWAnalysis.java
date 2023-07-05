package com.lumos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.SerializationUtils;

import java.util.HashSet;
import java.util.List;

import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.Stmt;

// imp

public class RWAnalysis {
    public final Map<Stmt, Map<Value, Set<Dependency>>> liveIn = new HashMap<>();
    public final Map<Stmt, Map<Value, Set<Dependency>>> liveOut = new HashMap<>();

    public RWAnalysis(StmtGraph<?> graph) {
        List<Stmt> startingStmts = new ArrayList<>();
        for (Stmt stmt : graph.getNodes()) {
            liveIn.put(stmt, Collections.emptyMap());
            liveOut.put(stmt, Collections.emptyMap());
            if (graph.predecessors(stmt).isEmpty() && graph.exceptionalPredecessors(stmt).isEmpty()) {
                startingStmts.add(stmt);
            }
        }

        boolean fixed = false;
        while (!fixed) {
            fixed = true;
            Deque<Stmt> queue = new ArrayDeque<>(startingStmts);
            HashSet<Stmt> visitedStmts = new HashSet<>();
            while (!queue.isEmpty()) {
                Stmt stmt = queue.removeFirst();
                visitedStmts.add(stmt);

                Map<Value, Set<Dependency>> in = new HashMap<>(liveIn.get(stmt));
                for (Stmt pred : graph.predecessors(stmt)) {
                    in = merge(in, liveOut.get(pred));
                }
                for (Stmt epred : graph.exceptionalPredecessors(stmt)) {
                    in = merge(in, liveOut.get(epred));
                    // System.out.println(stmt + " ---> " + epred);
                }

                if (isNotEqual(in, liveIn.get(stmt))) {
                    fixed = false;
                    liveIn.put(stmt, new HashMap<>(in));
                }

                Map<Value, Set<Dependency>> out = copy(in);
                for (Value v : stmt.getDefs()) {
                    out = kill(out, v);
                    out = generate(out, v, new Dependency(stmt, DepType.RW));
                }

                // This conservatively assume a function call changes all parameters
                if (stmt.containsInvokeExpr()) {
                    // System.out.println(stmt + " === " + in);
                    AbstractInvokeExpr iexpr = stmt.getInvokeExpr();
                    if (iexpr instanceof AbstractInstanceInvokeExpr) {
                        Value caller = ((AbstractInstanceInvokeExpr) iexpr).getBase();
                        out = generate(out, caller, new Dependency(stmt, DepType.CALL));
                    }
                    for (int i = 0; i < iexpr.getArgCount(); i++) {
                        Value arg = iexpr.getArg(i);
                        out = generate(out, arg, new Dependency(stmt, DepType.CALL));
                    }
                }

                if (isNotEqual(out, liveOut.get(stmt))) {
                    fixed = false;
                    liveOut.put(stmt, out);
                }

                for (Stmt succ : graph.successors(stmt)) {
                    if (!visitedStmts.contains(succ)) {
                        queue.addLast(succ);
                    }
                }
                for (Stmt esucc : graph.exceptionalSuccessors(stmt).values()) {
                    if (!visitedStmts.contains(esucc)) {
                        queue.addLast(esucc);
                    }
                }

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

    public Map<Value, Set<Dependency>> getBeforeStmt(@Nonnull Stmt stmt) {
        if (!liveIn.containsKey(stmt)) {
            throw new RuntimeException("Stmt: " + stmt + " is not in StmtGraph!");
        }
        return liveIn.get(stmt);
    }

    /** Get all live locals after the given stmt. */

    public Map<Value, Set<Dependency>> getAfterStmt(@Nonnull Stmt stmt) {
        if (!liveOut.containsKey(stmt)) {
            throw new RuntimeException("Stmt: " + stmt + " is not in StmtGraph!");
        }
        return liveOut.get(stmt);
    }

    /**
     * Merge two local sets into one set.
     *
     * @return a merged local set
     */

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
        dset.removeIf(d -> d.dtype != DepType.CF);
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

    /**
     * Check whether two sets contains same locals.
     *
     * @return if same return true, else return false;
     */
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
