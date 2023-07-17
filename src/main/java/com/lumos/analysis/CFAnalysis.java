package com.lumos.analysis;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.SerializationUtils;

// import com.lumos.common.Dependency.DepType;
import com.lumos.common.Dependency;

import java.util.HashSet;
import java.util.List;

import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Position;

// imp

public class CFAnalysis {
    public final Map<Stmt, Map<Value, Set<Dependency>>> liveIn = new HashMap<>();
    public final Map<Stmt, Map<Value, Set<Dependency>>> liveOut = new HashMap<>();
    public ReachingDefAnalysis rwa;

    public CFAnalysis(StmtGraph<?> graph, ReachingDefAnalysis rwa) {
        this.rwa = rwa;
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

                // Exclude any cf dependency when all values current used
                // was defined before a CF block
                List<Dependency> deptoremove = new ArrayList<>();
                List<Value> valtoremove = new ArrayList<>();
                for (Value v : out.keySet()) {
                    for (Dependency dep : out.get(v)) {
                        boolean outofscope = true;
                        for (Value vmp : stmt.getUses()) {
                            Set<Dependency> rwadeps = rwa.getBeforeStmt(stmt).get(vmp);
                            if (rwadeps != null) {
                                for (Dependency dpmp : rwa.getBeforeStmt(stmt).get(vmp)) {
                                    Position rwpos = dpmp.stmt.getPositionInfo().getStmtPosition();
                                    Position cfpos = dep.stmt.getPositionInfo().getStmtPosition();
                                    if (rwpos.compareTo(cfpos) >= 0) {
                                        outofscope = false;
                                        break;
                                    }
                                }
                                if (!outofscope)
                                    break;
                            }
                        }
                        if (outofscope) {
                            valtoremove.add(v);
                            deptoremove.add(dep);
                        }
                    }
                }

                for (int i = 0; i < valtoremove.size(); i++) {
                    Value v = valtoremove.get(i);
                    Dependency dep = deptoremove.get(i);
                    out.get(v).remove(dep);
                }

                if (graph.getAllSuccessors(stmt).size() > 1) {
                    for (Value v : stmt.getUses()) {
                        out = generate(out, v, new Dependency(stmt, Dependency.DepType.CF));
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
