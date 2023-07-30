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
            App.p("Round " + round);
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
            if (node instanceof EnterNode) {
                EnterNode enode = (EnterNode) node;

                for (List<ContextSensitiveValue> aliasp : enode.getAliasPairs()) {
                    ContextSensitiveValue cv1 = aliasp.get(0);
                    ContextSensitiveValue cv2 = aliasp.get(1);
                    if (!enode.isRemote()) {
                        Set<Definition> defs = out.getDefinitionsByCV(cv1);
                        // for (UniqueName un : unames) {
                        // out.putUname(cv2, un);
                        // }
                        out.putDefinition(cv2, defs);
                    } else {
                        out.putDefinition(cv2);
                    }
                }

            } else if (node instanceof ExitNode) {
                ExitNode enode = (ExitNode) node;
                ContextSensitiveValue cvcaller = enode.getRet();
                if (cvcaller != null) {
                    for (Stmt stmt : enode.getReturnStmts()) {
                        if (stmt instanceof JReturnStmt) {
                            // Value vv = ((JReturnStmt) stmt).getOp();
                            ContextSensitiveValue cvcallee = ContextSensitiveValue.getCValue(enode.getContext(),
                                    ((JReturnStmt) stmt).getOp());
                            if (!cvcallee.getValue().toString().contains("null")) {
                                // for (UniqueName un : out.getUniqueNames().get(cvcallee)) {
                                // out.putUname(cvcaller, un);
                                // }
                                out.putDefinition(cvcaller, out.getDefinitionsByCV(cvcallee));
                                // App.p("!!! " + cvcaller + ", " + cvcallee);
                            }
                        }
                    }
                    // out.autoDefs.add(ret);
                    // App.p("!!!!!!!!!!!!!!! \n" + enode.getContext() + "\n" + out);
                }
            } else if (node instanceof StmtNode) {
                Stmt stmt = node.getStmt();
                if (stmt instanceof JIdentityStmt) {
                    // continue;
                } else if (stmt instanceof JAssignStmt) {
                    JAssignStmt astmt = (JAssignStmt) stmt;
                    Value lop = astmt.getLeftOp();
                    Value rop = astmt.getRightOp();
                    if (rop instanceof JCastExpr) {
                        rop = ((JCastExpr) rop).getOp();
                    }
                    ContextSensitiveValue cvlop = ContextSensitiveValue.getCValue(node.getContext(), lop);
                    ContextSensitiveValue cvrop = ContextSensitiveValue.getCValue(node.getContext(), rop);
                    if ((rop instanceof Local) || (rop instanceof JInstanceFieldRef) || (rop instanceof Constant)) {
                        Set<Definition> defs = out.getDefinitionsByCV(cvrop);
                        if (lop instanceof Local) {
                            // Set<Definition> defs = out.getDefinitionsByCV(cvrop);
                            if (defs == null) {
                                if (lop.getType() instanceof RefLikeType) {
                                    App.p("This is not possible...");
                                    App.panicni();
                                    // out.putUname(cvlop);
                                }
                            } else {
                                Set<Definition> newdefs = new HashSet<>();
                                for (Definition def : defs) {
                                    UniqueName value = def.getDefinedValue();
                                    newdefs.add(Definition.getDefinition(value, node));
                                }
                                out.putDefinition(cvrop, newdefs);
                            }
                        } else if (lop instanceof JInstanceFieldRef) {
                            Set<UniqueName> unames = out.getUniqueNamesForRef(cvlop);
                            Set<Definition> possibleDefinitions = out.getDefinitionsByCV(cvrop);
                            if (unames == null || unames.isEmpty()) {
                                // App.p(stmt);
                                // App.p(cvlop);
                                App.p("This can't be unresolved");
                            }

                            Set<Definition> currDefs = new HashSet<>();

                            for (Definition def : possibleDefinitions) {
                                currDefs.add(Definition.getDefinition(def.definedValue, node));
                            }

                            for (UniqueName uname : unames) {
                                if (unames.size() == 1) {
                                    out.putDefinition(uname, currDefs);
                                } else {
                                    out.getCurrMapping().get(uname).addAll(currDefs);
                                }
                            }
                            // Set<UniqueName> runames = out.getUnamesByCV(cvrop);

                            // if (runames == null) {
                            // App.panicni();
                            // }
                            // Set<UniqueName> lunames = out.getUnamesByCV(cvlop);

                            // out.currMapping.put()
                        } else {
                            App.panicni();
                        }
                    }
                }
                // if (stmt.toString()
                // .contains("return $stack1")) {
                // for (IPNode pred : node.getPredecesors()) {
                // App.p(pred + " =====" + liveOut.get(pred));
                // }
                // App.p(in);
                // }
            } else if (node instanceof NoopNode) {
                // NOOP; just flow through
            } else {
                App.p("WTH??");
                App.panicni();
            }

            // App.p(node.getStmt());
            // App.p(out);
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
