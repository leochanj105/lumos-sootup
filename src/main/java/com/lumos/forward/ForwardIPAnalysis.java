package com.lumos.forward;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.lumos.App;
import com.lumos.common.Dependency;

import fj.P;
import java_cup.terminal;
import soot.Local;
import soot.RefLikeType;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.Constant;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JReturnStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.interaction.FlowInfo;

public class ForwardIPAnalysis {
    public final Map<IPNode, IPFlowInfo> liveIn = new HashMap<>();
    public final Map<IPNode, IPFlowInfo> liveOut = new HashMap<>();

    public ForwardIPAnalysis(InterProcedureGraph igraph) {
        Set<IPNode> workList = new HashSet<>();
        for (IPNode node : igraph.nodes) {
            // Unit unit = it.next();
            liveIn.put(node, new IPFlowInfo());
            liveOut.put(node, new IPFlowInfo());
            // if (node.getPredecesors() == null) {
            // App.p(((WrapperNode) node).getEnter());
            // }
            // if (node.getPredecesors().isEmpty()) {
            workList.add(node);
            // App.p(node);
            // }
        }
        int round = 0;

        // boolean fixed = false;
        while (!workList.isEmpty()) {
            // fixed = true;
            round += 1;
            if (App.showRound) {
                App.p("Round " + round);
            }
            // fixed = true;
            // Deque<IPNode> queue = new ArrayDeque<>(startingNodes);
            // HashSet<IPNode> visitedNodes = new HashSet<>();
            // while (!queue.isEmpty()) {
            IPNode node = workList.iterator().next();

            workList.remove(node);
            // if (workList.size() < 50) {
            // for (IPNode nd : workList) {
            // // App.p(nd + ", " + nd.getContext());
            // // if (!liveIn.get(nd).getCurrMapping().isEmpty())
            // // App.p(liveOut.get(nd));
            // }
            // // App.p("\n\n\n\n");
            // // App.p(liveOut);
            // if (round > 4010) {
            // break;
            // }
            // }

            // visitedNodes.add(node);

            // IPFlowInfo in = copy(liveIn.get(node));
            IPFlowInfo in = new IPFlowInfo();
            for (IPNode pred : node.getPredecesors()) {
                in = merge(in, liveOut.get(pred));
            }

            if (isNotEqual(in, liveIn.get(node))) {
                // fixed = false;
                liveIn.put(node, in);
            }

            IPFlowInfo out = copy(in);
            node.flow(out);

            if (isNotEqual(out, liveOut.get(node))) {
                // fixed = false;
                // if (round > 4000) {
                // App.p("*************** " + node + ", " + node.getContext());

                // App.p(out);
                // App.p("---------");
                // App.p(liveOut.get(node));
                // }
                for (IPNode succ : node.getSuccessors()) {
                    // if (!workList.contains(succ)) {
                    workList.add(succ);
                    // }
                }
                liveOut.put(node, out);
            }

            // }
        }

    }

    public IPFlowInfo copy(IPFlowInfo original) {
        IPFlowInfo newm = new IPFlowInfo(original);

        // for (Value v : original.aliasMap.keySet()) {
        // newm.aliasMap.put(v, original.aliasMap.get(v));
        // }
        return newm;
    }

    private IPFlowInfo merge(IPFlowInfo f1, IPFlowInfo f2) {
        IPFlowInfo f3 = copy(f1);
        // for (ContextSensitiveValue cv : f2.getUniqueNames().keySet()) {
        // Map<ContextSensitiveValue, Set<UniqueName>> fun = f3.getUniqueNames();
        // if (!fun.containsKey(cv)) {
        // fun.put(cv, new HashSet<>());
        // }
        // for (UniqueName un : f2.getUniqueNames().get(cv)) {
        // fun.get(cv).add(un);
        // }
        // }

        for (UniqueName un : f2.getCurrMapping().keySet()) {
            Map<UniqueName, Set<Definition>> mapping = f3.getCurrMapping();
            if (!mapping.containsKey(un)) {
                mapping.put(un, new HashSet<>());
            }
            for (Definition def : f2.getCurrMapping().get(un)) {
                mapping.get(un).add(def);
            }
        }

        return f3;

    }

    private boolean isNotEqual(IPFlowInfo f1, IPFlowInfo f2) {
        return !f1.equals(f2);
    }

    public IPFlowInfo getBefore(IPNode node) {
        return this.liveIn.get(node);
    }

    public IPFlowInfo getAfter(IPNode node) {
        return this.liveOut.get(node);
    }
}
