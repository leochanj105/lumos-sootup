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
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JReturnStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.interaction.FlowInfo;

public class ForwardIPAnalysis {
    public final Map<IPNode, IPFlowInfo> liveIn = new HashMap<>();
    public final Map<IPNode, IPFlowInfo> liveOut = new HashMap<>();

    public ForwardIPAnalysis(InterProcedureGraph igraph) {
        List<IPNode> startingNodes = new ArrayList<>();
        for (IPNode node : igraph.nodes) {
            // Unit unit = it.next();
            liveIn.put(node, new IPFlowInfo());
            liveOut.put(node, new IPFlowInfo());
            // if (node.getPredecesors() == null) {
            // App.p(((WrapperNode) node).getEnter());
            // }
            if (node.getPredecesors().isEmpty()) {
                startingNodes.add(node);
            }
        }
        int round = 0;
        boolean fixed = false;
        while (!fixed) {
            round += 1;
            App.p("Round " + round);
            fixed = true;
            Deque<IPNode> queue = new ArrayDeque<>(startingNodes);
            HashSet<IPNode> visitedNodes = new HashSet<>();
            while (!queue.isEmpty()) {
                IPNode node = queue.removeFirst();
                visitedNodes.add(node);

                IPFlowInfo in = copy(liveIn.get(node));
                for (IPNode pred : node.getPredecesors()) {
                    in = merge(in, liveOut.get(pred));
                }
                boolean currChanged = false;
                if (isNotEqual(in, liveIn.get(node))) {
                    currChanged = true;
                    fixed = false;
                    liveIn.put(node, in);
                }

                IPFlowInfo out = copy(in);
                if (node instanceof EnterNode) {
                    EnterNode enode = (EnterNode) node;
                    // out.aliases.addAll(enode.getAliasPairs());
                    // App.p(enode.getSm());
                    for (Set<Value> aliasp : enode.getAliasPairs()) {
                        // App.p("!!! " + aliasp);
                        out.addAlias(aliasp);
                    }
                } else if (node instanceof ExitNode) {
                    ExitNode enode = (ExitNode) node;
                    Value ret = enode.getRet();
                    if (ret != null) {
                        for (Stmt stmt : enode.getReturnStmts()) {
                            Value vv = ((JReturnStmt) stmt).getOp();
                            if (!vv.toString().contains("null")) {
                                out.addAlias(vv, ret);
                            }
                        }
                        out.autoDefs.add(ret);
                    }
                } else if (node instanceof StmtNode) {
                    Stmt stmt = node.getStmt();
                    if (stmt instanceof JIdentityStmt) {
                        // continue;
                        // App.p("Skipping " + node);
                    } else if (stmt instanceof JAssignStmt) {
                        JAssignStmt astmt = (JAssignStmt) stmt;
                        Value lop = astmt.getLeftOp();
                        Value rop = astmt.getRightOp();
                        Set<IPNode> nset = new HashSet<>();
                        nset.add(node);
                        if (lop instanceof Local) {
                            Set<Value> sv = out.aliasMap.remove(lop);
                            if (sv != null) {
                                sv.remove(lop);
                            }
                            if (rop instanceof Local || rop instanceof JInstanceFieldRef) {
                                // App.p("@@ " + stmt);
                                out.addAlias(rop, lop);
                            }

                        } else if (lop instanceof JInstanceFieldRef) {
                            // Set<Value> newer = null;
                            // App.p(stmt);
                            if (!out.aliasMap.containsKey(lop)) {
                                out.addAlias(lop);
                            }
                            // if (out.aliasMap.containsKey(lop)) {
                            Set<Value> original = out.aliasMap.get(lop);
                            Set<Value> detach = new HashSet<>();
                            for (Value val : original) {
                                if (val instanceof JInstanceFieldRef) {
                                    detach.add(val);
                                }
                            }
                            for (Value val : detach) {
                                original.remove(val);
                            }
                            detach.add(rop);
                            Set<Value> merged = out.addAlias(detach);

                            Set<Value> toremove = new HashSet<>();
                            for (Value v : out.defSet.keySet()) {
                                if ((v instanceof JInstanceFieldRef) && merged.contains(v)) {
                                    toremove.add(v);
                                }
                            }
                            for (Value v : toremove) {
                                out.defSet.put(v, nset);
                            }
                        }

                        out.defSet.put(lop, nset);
                    }

                } else if (node instanceof NoopNode) {
                    // NOOP; just flow through
                } else {
                    App.p("WTH??");
                    App.panicni();
                }

                if (isNotEqual(out, liveOut.get(node))) {
                    fixed = false;
                    liveOut.put(node, out);
                }

                for (IPNode succ : node.getSuccessors()) {

                    if (!visitedNodes.contains(succ)) {
                        queue.addLast(succ);
                    }
                }
            }
        }

    }

    public IPFlowInfo copy(IPFlowInfo original) {
        IPFlowInfo newm = new IPFlowInfo();
        for (Value v : original.defSet.keySet()) {
            newm.defSet.put(v, new HashSet<>(original.defSet.get(v)));
        }

        for (Set<Value> s : original.aliases) {
            Set<Value> news = new HashSet<>(s);
            Set<Value> actual = newm.addAlias(news);
            for (Value v : news) {
                newm.put(v, actual);
            }
        }
        for (Value v : original.autoDefs) {
            newm.autoDefs.add(v);
        }

        // for (Value v : original.aliasMap.keySet()) {
        // newm.aliasMap.put(v, original.aliasMap.get(v));
        // }
        return newm;
    }

    private IPFlowInfo merge(IPFlowInfo f1, IPFlowInfo f2) {

        IPFlowInfo f3 = copy(f1);
        for (Value v : f2.defSet.keySet()) {
            for (IPNode node : f2.defSet.get(v)) {
                if (!f3.defSet.containsKey(v)) {
                    f3.defSet.put(v, new HashSet<>());
                }
                f3.defSet.get(v).add(node);
            }
        }

        for (Set<Value> s : f2.aliases) {
            f3.aliases.add(s);
        }
        for (Value v : f2.autoDefs) {
            f3.autoDefs.add(v);
        }
        return f3;

    }

    private boolean isNotEqual(IPFlowInfo f1, IPFlowInfo f2) {
        return !f1.equals(f2);
    }

}
