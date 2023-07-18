package com.lumos.common;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import com.lumos.analysis.MethodInfo;

import soot.Unit;

public class Provenance {
    public Dependency dep;
    public MethodInfo minfo;
    public RefSeq refSeq;
    public int prefix;

    public Provenance(Dependency dep, MethodInfo minfo, RefSeq refSeq, int prefix) {
        this.dep = dep;
        this.minfo = minfo;
        this.refSeq = refSeq;
        this.prefix = prefix;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof Provenance)) {
            return false;
        }
        Provenance other = (Provenance) o;
        return other.dep.equals(this.dep);
    }

    public boolean isBefore(Object o) {
        if (o == null || !(o instanceof Provenance)) {
            return false;
        }
        Provenance other = (Provenance) o;
        if (other.equals(this)) {
            return false;
        }
        // if(this.query)
        boolean found = false;
        Deque<Unit> q = new ArrayDeque<>();
        Set<Unit> visited = new HashSet<>();
        q.add(this.dep.unit);
        while (!found && !q.isEmpty()) {
            Unit currUnit = q.pop();
            if (visited.contains(currUnit)) {
                continue;
            } else {
                visited.add(currUnit);
            }
            for (Unit succ : this.minfo.cfg.getSuccsOf(currUnit)) {
                if (!visited.contains(succ)) {
                    if (succ.equals(other.dep.unit)) {
                        found = true;
                        break;
                    }
                    q.add(succ);
                }
            }
        }
        return found;
    }

}
