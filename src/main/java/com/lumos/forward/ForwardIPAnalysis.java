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
import soot.jimple.InstanceInvokeExpr;
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

    public ForwardIPAnalysis(InterProcedureGraph igraph, IPNode firstNode) {
        // App.p("!!! " + firstNode.getSuccessors());
        Set<IPNode> workList = new HashSet<>();
        for (IPNode node : igraph.nodes) {
            // Unit unit = it.next();
            liveIn.put(node, new IPFlowInfo());
            liveOut.put(node, new IPFlowInfo());
            if (node.equals(firstNode)) {
                workList.add(node);
                // App.p("!!! " + node);
            }
        }

        Set<IPNode> unreacheableNodes = new HashSet<>();

        for (IPNode node : igraph.nodes) {
            for (IPNode pred : node.getPredecesors()) {
                if (!canReach(firstNode, pred)) {
                    unreacheableNodes.add(node);
                }
            }
        }

        int round = 0;

        // boolean fixed = false;

        Set<IPNode> visited = new HashSet<>();
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
            // App.p("!!! " + node);
            workList.remove(node);
            if (!node.equals(firstNode)) {
                boolean isReady = true;
                for (IPNode pred : node.getPredecesors()) {
                    if (!visited.contains(pred) && !(canReach(node, pred))
                            && !unreacheableNodes.contains(pred)) {
                        if (!(pred.stmt.toString().contains("if") || pred.stmt.toString().contains("goto")
                                || pred.stmt.toString().contains("return"))) {
                            // App.p("!!!!!!!!! " + node + ", " + pred);
                        }
                        isReady = false;
                        break;
                    }
                }
                if (!isReady) {
                    continue;
                }
            }
            visited.add(node);

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

            // if (node.getStmt().toString().contains(
            // "interfaceinvoke $stack37.<java.util.Iterator: java.lang.Object next()>()"))
            // {
            // App.p("xxxxxxxxxxx " + node);
            // in.getDefinitionsByCV(ContextSensitiveValue.getCValue(node.getContext(),
            // ((InstanceInvokeExpr) node.getStmt().getInvokeExpr()).getBase())).forEach(d
            // -> {
            // App.p(d.d());
            // });
            // App.p("-----------");
            // out.getDefinitionsByCV(ContextSensitiveValue.getCValue(node.getContext(),
            // ((InstanceInvokeExpr) node.getStmt().getInvokeExpr()).getBase())).forEach(d
            // -> {
            // App.p(d.d());
            // });
            // }

            if (isNotEqual(out, liveOut.get(node)) || node.equals(firstNode)) {
                for (IPNode succ : node.getSuccessors()) {
                    // if (!workList.contains(succ)) {
                    workList.add(succ);
                    // App.p("!!! " + succ);
                    // }
                }
                liveOut.put(node, out);
            }

        }

    }

    public boolean canReach(IPNode node1, IPNode node2) {
        Set<IPNode> visited = new HashSet<>();
        Deque<IPNode> queue = new ArrayDeque<>();
        queue.add(node1);

        while (!queue.isEmpty()) {
            IPNode node = queue.pop();
            if (visited.contains(node)) {
                continue;
            }
            visited.add(node);
            if (node.equals(node2)) {
                return true;
            }

            for (IPNode succ : node.getSuccessors()) {
                queue.add(succ);
            }
        }

        return false;
    }

    public IPFlowInfo copy(IPFlowInfo original) {
        IPFlowInfo newm = new IPFlowInfo(original);
        return newm;
    }

    private IPFlowInfo merge(IPFlowInfo f1, IPFlowInfo f2) {
        IPFlowInfo f3 = copy(f1);

        for (RefBasedAddress un : f2.getCurrMapping().keySet()) {
            Map<RefBasedAddress, Set<Definition>> mapping = f3.getCurrMapping();
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
