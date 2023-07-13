package com.lumos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.cast.ir.ssa.AssignInstruction;

import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ReferenceType;
import sootup.core.types.Type;

public class LocalAliasAnalysis {
    MethodInfo minfo;

    StmtGraph<?> graph;

    Map<Value, Set<Value>> aliasSets = new HashMap<>();

    public LocalAliasAnalysis(MethodInfo minfo) {
        this.minfo = minfo;
        this.graph = minfo.wsm.getBody().getStmtGraph();
        System.out.println(minfo.nameMap);
        doAnalysis();
    }

    public void doAnalysis() {
        for (Stmt stmt : this.minfo.wsm.getBody().getStmts()) {

            if (stmt instanceof JAssignStmt) {
                JAssignStmt astmt = (JAssignStmt) stmt;
                // astmt.get
                Value rop = astmt.getRightOp();
                // Value lop = astmt.getLeftOp();
                // if (rop.getType() instanceof ReferenceType) {
                System.out.println(stmt);
                System.out.println(rop.getClass());
                // }
            }
        }
    }
}