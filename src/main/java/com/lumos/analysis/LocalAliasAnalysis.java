package com.lumos.analysis;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

// import javafx.util.Pair;
import com.ibm.wala.cast.ir.ssa.AssignInstruction;

import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ReferenceType;
import sootup.core.types.Type;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.types.JavaClassType;
import sootup.java.sourcecode.frontend.WalaSootMethod;

public class LocalAliasAnalysis {
    MethodInfo minfo;
    JavaSootMethod sm;

    StmtGraph<?> graph;

    // Map<Value, Set<Value>> aliasSets = new HashMap<>();

    // Map<Stmt, Set<Pair<Value, Value>>> aliasPairs;
    public final Map<Stmt, Map<Value, Set<Value>>> liveIn = new HashMap<>();
    public final Map<Stmt, Map<Value, Set<Value>>> liveOut = new HashMap<>();

    public LocalAliasAnalysis(MethodInfo minfo) {
        this.minfo = minfo;

        this.graph = minfo.sm.getBody().getStmtGraph();
        this.sm = minfo.sm;
        // System.out.println(minfo.nameMap);
        doAnalysis();
    }

    public void doAnalysis() {
        System.out.println(this.sm.getBody().getParameterLocals());
        List<Stmt> startingStmts = new ArrayList<>();
        for (Stmt stmt : graph.getNodes()) {
            liveIn.put(stmt, Collections.emptyMap());
            liveOut.put(stmt, Collections.emptyMap());
            if (graph.predecessors(stmt).isEmpty() && graph.exceptionalPredecessors(stmt).isEmpty()) {
                startingStmts.add(stmt);
            }
        }

        boolean fixed = false;
        // while (!fixed) {
        // fixed = true;
        // Deque<Stmt> queue = new ArrayDeque<>(startingStmts);
        // HashSet<Stmt> visitedStmts = new HashSet<>();
        // while (!queue.isEmpty()) {
        // Stmt stmt = queue.removeFirst();
        // visitedStmts.add(stmt);

        // Map<Value, Set<Value>> in = new HashMap<>(liveIn.get(stmt));
        // for (Stmt pred : graph.predecessors(stmt)) {
        // in = merge(in, liveOut.get(pred));
        // }
        // for (Stmt epred : graph.exceptionalPredecessors(stmt)) {
        // in = merge(in, liveOut.get(epred));
        // }

        // if (isNotEqual(in, liveIn.get(stmt))) {
        // fixed = false;
        // liveIn.put(stmt, new HashMap<>(in));
        // }

        // Map<Value, Set<Value>> out = copy(in);
        // if (stmt instanceof JAssignStmt) {
        // JAssignStmt astmt = (JAssignStmt) stmt;
        // Value rop = astmt.getRightOp();
        // Value lop = astmt.getLeftOp();
        // if ((rop instanceof Local || rop instanceof JFieldRef) && lop.getType()
        // instanceof JavaClassType) {
        // List<Pair<Value, Value>> toRemove = new ArrayList<>();
        // for (Pair<Value, Value> pair : out) {
        // if (pair.getKey().equals(lop) || pair.getValue().equals(lop)) {
        // toRemove.add(pair);
        // }
        // }
        // out.add(new Pair<Value, Value>(lop, rop));
        // }

        // }

        // if (isNotEqual(out, liveOut.get(stmt))) {
        // fixed = false;
        // liveOut.put(stmt, out);
        // }

        // for (Stmt succ : graph.successors(stmt)) {
        // if (!visitedStmts.contains(succ)) {
        // queue.addLast(succ);
        // }
        // }
        // for (Stmt esucc : graph.exceptionalSuccessors(stmt).values()) {
        // if (!visitedStmts.contains(esucc)) {
        // queue.addLast(esucc);
        // }
        // }
        // }

        // }

        for (Stmt stmt : this.minfo.sm.getBody().getStmts()) {
            // System.out.println(stmt);
            // if (stmt instanceof JAssignStmt) {
            // JAssignStmt astmt = (JAssignStmt) stmt;
            // // astmt.get
            // Value rop = astmt.getRightOp();
            // System.out.println(stmt + ", " + rop.getClass() + ", " + rop.getType() + ", "
            // + rop.getType().getClass());
            // if ((rop instanceof Local || rop instanceof JFieldRef) && rop.getType()
            // instanceof JavaClassType) {
            // // new JInstanceFieldRef(null, null);
            // // System.out.println(((JInstanceFieldRef) rop).getFieldSignature());
            // }

            // }
        }
    }

    public Map<Value, Set<Value>> copy(Map<Value, Set<Value>> original) {
        Map<Value, Set<Value>> newm = new HashMap<>();
        for (Value val : original.keySet()) {
            newm.put(val, new HashSet<>(original.get(val)));
        }
        return newm;
    }

    private Map<Value, Set<Value>> merge(@Nonnull Map<Value, Set<Value>> set1,
            @Nonnull Map<Value, Set<Value>> set2) {
        if (set1.isEmpty()) {
            return set2;
        } else {
            Map<Value, Set<Value>> set3 = copy(set1);
            for (Value v : set3.keySet()) {
                if (set2.containsKey(v)) {
                    Set<Value> loc3 = set3.get(v);
                    for (Value sv : set2.get(v)) {
                        loc3.add(sv);
                    }
                }
            }
            return set3;
        }
    }

    private boolean isNotEqual(@Nonnull Map<Value, Set<Value>> set1, @Nonnull Map<Value, Set<Value>> set2) {
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