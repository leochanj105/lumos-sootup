package com.lumos.analysis;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

// import com.lumos.common.Dependency.DepType;
import com.lumos.common.Dependency;

import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.toolkits.graph.DirectedGraph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

// imp

public class CFAnalysis {
    public final Map<Unit, Set<Dependency>> liveIn = new HashMap<>();
    public final Map<Unit, Set<Dependency>> liveOut = new HashMap<>();
    public ReachingDefAnalysis rwa;

    public CFAnalysis(DirectedGraph<Unit> graph, ReachingDefAnalysis rwa) {
        this.rwa = rwa;
        // List<Unit> startingUnits = new ArrayList<>();
        Set<Unit> workList = new HashSet<>();
        for (Iterator<Unit> it = graph.iterator(); it.hasNext();) {
            Unit unit = it.next();
            liveIn.put(unit, Collections.emptySet());
            liveOut.put(unit, Collections.emptySet());
            workList.add(unit);
            // if (graph.getPredsOf(unit).isEmpty()) {
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

            Set<Dependency> in = new HashSet<>();
            for (Unit pred : graph.getPredsOf(unit)) {
                in = merge(in, liveOut.get(pred));
            }

            if (isNotEqual(in, liveIn.get(unit))) {
                // fixed = false;
                liveIn.put(unit, new HashSet<>(in));
            }

            Set<Dependency> out = copy(in);

            // Exclude any cf dependency when all values current used
            // was defined before a CF block
            List<Dependency> deptoremove = new ArrayList<>();
            List<Value> valtoremove = new ArrayList<>();
            for (Dependency dep : out) {
                boolean outofscope = true;
                for (ValueBox vmpbox : unit.getUseBoxes()) {
                    Value vmp = vmpbox.getValue();
                    Set<Dependency> rwadeps = rwa.getBeforeUnit(unit).get(vmp);
                    if (rwadeps != null) {
                        for (Dependency dpmp : rwa.getBeforeUnit(unit).get(vmp)) {
                            int rwpos = dpmp.unit.getJavaSourceStartLineNumber();
                            int cfpos = dep.unit.getJavaSourceStartLineNumber();
                            if (rwpos >= cfpos) {
                                outofscope = false;
                                break;
                            }
                        }
                        if (!outofscope)
                            break;
                    }
                }
                if (outofscope) {
                    // valtoremove.add(v);
                    deptoremove.add(dep);
                }
            }

            // for (int i = 0; i < valtoremove.size(); i++) {
            // Value v = valtoremove.get(i);
            // Dependency dep = deptoremove.get(i);
            for (Dependency dep : deptoremove) {
                // out.remove(dep);
            }
            // }

            if (graph.getSuccsOf(unit).size() > 1) {
                for (ValueBox vbox : unit.getUseBoxes()) {
                    Value v = vbox.getValue();
                    out.add(new Dependency(unit, Dependency.DepType.CF));
                }
            }

            if (isNotEqual(out, liveOut.get(unit))) {
                // fixed = false;
                for (Unit succ : graph.getSuccsOf(unit)) {
                    workList.add(succ);
                }
                liveOut.put(unit, out);
            }

            // }
        }

    }

    public Set<Dependency> copy(Set<Dependency> original) {
        Set<Dependency> news = new HashSet<>();
        for (Dependency v : original) {
            news.add(v);
        }
        return news;
    }

    public Set<Dependency> getBeforeUnit(@Nonnull Unit unit) {
        if (!liveIn.containsKey(unit)) {
            throw new RuntimeException("Unit: " + unit + " is not in UnitGraph!");
        }
        return liveIn.get(unit);
    }

    /** Get all live locals after the given unit. */

    public Set<Dependency> getAfterUnit(@Nonnull Unit unit) {
        if (!liveOut.containsKey(unit)) {
            throw new RuntimeException("Unit: " + unit + " is not in UnitGraph!");
        }
        return liveOut.get(unit);
    }

    private Set<Dependency> merge(@Nonnull Set<Dependency> set1,
            @Nonnull Set<Dependency> set2) {
        if (set1.isEmpty()) {
            return set2;
        } else {
            Set<Dependency> set3 = copy(set1);

            for (Dependency dep : set2) {
                set3.add(dep);
            }
            // set1.addAll(set2);
            return set3;
        }
    }

    private boolean isNotEqual(@Nonnull Set<Dependency> set1, @Nonnull Set<Dependency> set2) {
        return !(set1.equals(set2));
    }

}
