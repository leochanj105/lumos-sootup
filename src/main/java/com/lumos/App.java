package com.lumos;

import java.nio.file.Paths;
import java.sql.Ref;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;

import org.checkerframework.checker.units.qual.min;
import org.eclipse.jdt.core.dom.CastExpression;
import org.objectweb.asm.commons.JSRInlinerAdapter;

import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl;
import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl.ConcreteJavaMethod;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.loader.AstMethod.DebuggingInformation;
import com.ibm.wala.cfg.AbstractCFG;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.lumos.analysis.MethodInfo;
import com.lumos.analysis.ReachingDefAnalysis;
import com.lumos.common.Dependency;
import com.lumos.common.Provenance;
import com.lumos.common.Query;
import com.lumos.common.RefSeq;
import com.lumos.common.TracePoint;

import fj.Unit;
import fj.test.reflect.Check;
// import soot.toolkits.scalar.ForwardFlowAnalysis;
import sootup.core.Project;
import sootup.core.frontend.OverridingClassSource;
import sootup.core.frontend.SootClassSource;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.graph.StmtGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.expr.JInterfaceInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.model.Body.BodyBuilder;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.OverridingJavaClassSource;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.frontend.WalaJavaClassProvider;
import sootup.java.sourcecode.frontend.WalaSootMethod;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;

/**
 * Hello world!
 *
 */
public class App {
    public static List<String> processList;

    public static String sourceDirectory;
    public static String outputDirectory;
    public static String analyzeResultDirectory;
    public static boolean showMethod = true;
    public static boolean showUnit = false;
    public static String outputFormat = "jimple";
    public static final String LOG_PREFIX = "LUMOS-LOG";

    public static Map<MethodSignature, MethodInfo> methodMap;

    public static void main(String[] args) {
        readParams();
        analyzePath("src/code");
    }

    public static void analyzePath(String path) {
        AnalysisInputLocation<JavaSootClass> inputLocation = new JavaSourcePathAnalysisInputLocation(
                path.toString());

        JavaLanguage language = new JavaLanguage(8);
        // Project<JavaSootClass, JavaView> project =
        // JavaProject.builder(language).addInputLocation(inputLocation)
        // .build();
        JavaProject project = JavaProject.builder(language).addInputLocation(inputLocation).build();
        JavaView view = project.createView();

        String[] classes = new String[] { "Test", "Test$T", "Test$Order", "Test$Result" };
        methodMap = new HashMap<>();
        MethodSignature startMethod = null;
        for (String clstr : classes) {
            ClassType classType = project.getIdentifierFactory().getClassType(clstr);
            SootClass sootClass = view.getClass(classType).get();

            for (JavaSootMethod sm : ((JavaSootClass) sootClass).getMethods()) {
                WalaSootMethod wsm = (WalaSootMethod) sm;
                // if (wsm.toString().contains("some")) {
                MethodInfo minfo = new MethodInfo(wsm);
                Map<TracePoint, List<TracePoint>> depGMap = minfo.analyzeDef();
                if (wsm.toString().contains("some")) {
                    startMethod = wsm.getSignature();
                }
                methodMap.put(wsm.getSignature(), minfo);
            }
        }

        p("----------");

        MethodInfo minfo = searchMethod("some");
        TracePoint target = minfo.getReturnTps().get(0);
        backtrack(new Query(new RefSeq(target.value, null), target.stmt), minfo);
        // p(methodMap);
        // for (TracePoint tp : minfo.getPrev(target)) {
        // for (TracePoint tp2 : minfo.getPrev(tp)) {

        // if (tp2.toString().contains("<$r2, 55")) {
        // p(tp2);
        // Stmt curr = minfo.getPrev(tp2).get(0).stmt;
        // Value rop = ((JAssignStmt) curr).getRightOp();
        // MethodSignature sig = ((JVirtualInvokeExpr) rop).getMethodSignature();
        // p(rop);
        // RefSeq solvedSeq = walkMethod(new RefSeq(rop, null), methodMap.get(sig),
        // getParameters((JVirtualInvokeExpr) rop));

        // p(solvedSeq);
        // }
        // }
        // // p(tp.value.getClass());
        // }

    }

    public static MethodInfo searchMethod(String... str) {
        for (MethodSignature sig : methodMap.keySet()) {
            boolean match = true;
            for (String s : str) {
                if (!sig.toString().contains(s)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return methodMap.get(sig);
            }
        }
        return null;
    }

    public static void backtrack(Query query, MethodInfo minfo) {

        Deque<Query> currQueries = new ArrayDeque<>();
        currQueries.add(query);

        while (!currQueries.isEmpty()) {
            Query currQuery = currQueries.pop();

            Set<Provenance> pureDependencies = new HashSet<>();
            if (currQuery.refSeq.fields.size() > 0) {
                Value refHead = new JInstanceFieldRef((Local) currQuery.refSeq.value, currQuery.refSeq.fields.get(0));
                // Should check all aliases of prefixes of reference sequence
                for (Dependency dep : minfo.getPrev(currQuery.stmt, refHead)) {
                    if (!(dep.dtype == Dependency.DepType.CF)) {
                        pureDependencies.add(new Provenance(dep, minfo, currQuery.refSeq, 1));
                    }
                }
            }

            for (Dependency dep : minfo.getPrev(currQuery.stmt, currQuery.refSeq.value)) {
                if (!(dep.dtype == Dependency.DepType.CF)) {
                    pureDependencies.add(new Provenance(dep, minfo, currQuery.refSeq, 0));
                }
            }
            // Find closet dependencies

            List<Provenance> toRemove = new ArrayList<>();
            for (Provenance prov : pureDependencies) {
                for (Provenance prov2 : pureDependencies) {
                    if (prov.isBefore(prov2)) {
                        toRemove.add(prov);
                        break;
                    }
                }
            }

            for (Provenance prov : toRemove) {
                pureDependencies.remove(prov);
            }

            for (Provenance prov : pureDependencies) {
                Stmt currStmt = prov.dep.stmt;

                p(prov.dep + ", " + currQuery);

                if (currStmt instanceof JAssignStmt) {
                    Value rop = ((JAssignStmt) currStmt).getRightOp();
                    // for (Value use : rop.getUses()) {
                    // p(use.getClass());
                    // }
                    // p(rop.getClass());
                    if (rop instanceof AbstractInvokeExpr) {
                        AbstractInvokeExpr iexpr = (AbstractInvokeExpr) rop;
                        List<Value> parameters = new ArrayList<>();
                        // for()
                        walkMethod(currQuery.refSeq, searchMethod(iexpr.getMethodSignature().toString()),
                                getParameters(iexpr), -1);
                    } else {
                        for (Value use : rop.getUses()) {
                            if (use instanceof JCastExpr) {
                                continue;
                            } else if (use instanceof Local) {
                                currQueries.add(new Query(new RefSeq(use, null), currStmt));
                            } else if (use instanceof JInstanceFieldRef) {
                                JInstanceFieldRef tmpRef = (JInstanceFieldRef) use;
                                RefSeq refSeq = new RefSeq(tmpRef.getBase());

                                refSeq.appendRef(tmpRef.getFieldSignature());
                                currQueries.add(new Query(refSeq, currStmt));
                            } else {
                                p(use.getClass());
                                panicni();
                            }
                        }
                    }
                } else {
                    p(currStmt.getClass());
                    panicni();
                }
            }
        }

    }

    public static List<Value> getParameters(AbstractInvokeExpr iexpr) {
        List<Value> params = new ArrayList<>();
        if (iexpr instanceof AbstractInstanceInvokeExpr) {
            AbstractInstanceInvokeExpr aiexpr = (AbstractInstanceInvokeExpr) iexpr;
            params.add(aiexpr.getBase());
        }

        for (Value v : iexpr.getArgs()) {
            params.add(v);
        }
        return params;
    }

    public static void panicni() {
        throw new RuntimeException("Not implemented");
    }

    public static void walkMethod(RefSeq seq, MethodInfo minfo, List<Value> parameters, int baseIndex) {

        for (TracePoint retPoint : minfo.getReturnTps()) {
            Value baseVal = null;
            if (baseIndex == -1) {
                baseVal = retPoint.value;
            } else {
                baseVal = minfo.getParamValues().get(baseIndex);
            }
            RefSeq actualSeq = new RefSeq(baseVal, seq.fields);
            Query actualQuery = new Query(actualSeq, retPoint.stmt);
            backtrack(actualQuery, minfo);
        }
        // if (seq.fields == null) {

        // }
        // RefSeq currSeq = seq;
        // Value base = seq.value;

        // Value ref = null;
        // if (seq.fields.size() > 0)
        // ref = new JInstanceFieldRef((Local) base, seq.fields.get(0));

        // List<Value> paramValues = minfo.getParamValues();
        // TracePoint baseRet = minfo.getReturnTps().get(0);
        // minfo.reachingAnalysis.getBeforeStmt(baseRet.stmt);
        // // p(curr.value);

        // TracePoint baseProv = null;
        // List<TracePoint> provenance = minfo.getPrev(baseRet);

        // // for(TracePoint tp : provenance){
        // // if()
        // // }
        // if (ref != null) {
        // for (Dependency dp : minfo.getPrev(baseRet.stmt, ref)) {
        // Stmt provStmt = dp.stmt;
        // if (provStmt instanceof JAssignStmt) {
        // Value lop = ((JAssignStmt) provStmt).getLeftOp();
        // Value rop = ((JAssignStmt) provStmt).getRightOp();

        // if (rop instanceof AbstractInvokeExpr) {
        // RefSeq newSeq = walkMethod(new RefSeq(lop, null), minfo, parameters);
        // } else {
        // currSeq = new RefSeq(rop, currSeq.fields.subList(1, currSeq.fields.size()));
        // }
        // } else if (provStmt.containsInvokeExpr()) {
        // AbstractInvokeExpr iexpr = provStmt.getInvokeExpr();

        // }
        // }
        // }

        // p(tp1);
        // if (tp1.stmt instanceof JAssignStmt) {
        // Value rop = ((JAssignStmt) tp1.stmt).getRightOp();
        // TracePoint tp2 = minfo.tpMap.get(tp1.stmt).get(rop);
        // p(tp2);
        // List<TracePoint> ltps = minfo.getPrev(tp2);
        // if (ltps.size() == 0) {
        // Value toresolve = tp2.value;
        // JInstanceFieldRef rexp = (JInstanceFieldRef) toresolve;
        // Value rbase = rexp.getBase();

        // Value resolved = null;
        // int index = paramValues.indexOf(rbase);
        // if (index >= 0) {
        // p(paramValues.get(index));
        // resolved = parameters.get(index);
        // p(resolved);

        // }
        // // p(toresolve);
        // JInstanceFieldRef solvedExpr = new JInstanceFieldRef((Local) resolved,
        // rexp.getFieldSignature());
        // p(solvedExpr);
        // RefSeq newSeq = new RefSeq(solvedExpr.getBase(), seq.fields);
        // newSeq.fields.add(0, solvedExpr.getFieldSignature());
        // return newSeq;
        // // p(minfo.getPrev(tp2.stmt, rbase));
        // // p();
        // }
        // // p(tp3);
        // }

        // return seq;

    }

    public static void analyzeExchange(WalaSootMethod wsm) {
        for (Stmt stmt : wsm.getBody().getStmts()) {
            if (!stmt.containsInvokeExpr())
                continue;
            MethodSignature ms = stmt.getInvokeExpr().getMethodSignature();
            if (!ms.toString().contains("fff(")) // This should be exchange, but I'm testing for now
                continue;

            // p(ms);
            ReachingDefAnalysis rda = new ReachingDefAnalysis(wsm.getBody().getStmtGraph());
            Map<Value, Set<Dependency>> mdep = rda.getBeforeStmt(stmt);
            Value argURL = stmt.getInvokeExpr().getArg(0);
            p(mdep.get(argURL));
        }
    }

    public static List<Value> parameters(WalaSootMethod wsm) {
        // int hasThis = wsm.isStatic() ? 0 : 1;
        // int methodParamCount = wsm.getParameterCount() + hasThis;
        List<Value> methodParams = new ArrayList<>();
        // int count = 0;
        for (Value v : wsm.getBody().getParameterLocals()) {
            methodParams.add(v);
        }
        if (!wsm.isStatic()) {
            methodParams.add(0, wsm.getBody().getThisLocal());
        }
        return methodParams;

    }

    public static Body insertBefore(WalaSootMethod wsm, Stmt stmt, Stmt toinsert) {
        // MutableStmtGraph graph = new MutableBlockStmtGraph(body.getStmtGraph());
        BodyBuilder builder = Body.builder(wsm.getBody(), wsm.getModifiers());
        builder.insertBefore(stmt, toinsert);
        return builder.build();
    }

    public static void readParams() {
        String oformat = System.getProperty("outputFormat");
        if (oformat == null) {
            outputFormat = "none";
        } else {
            outputFormat = oformat;
        }
        System.out.println("Output format: " + outputFormat);

        String sdir = System.getProperty("sourceDir");
        if (sdir == null) {
            sdir = "";
        }
        sourceDirectory = System.getProperty("user.dir") + "/" + sdir;
        System.out.println("Source directory: " + sourceDirectory);

        String odir = System.getProperty("outputDir");
        if (odir == null) {
            odir = "sootOutput";
        }
        outputDirectory = System.getProperty("user.dir") + "/" + odir;
        System.out.println("Output directory: " + outputDirectory);

        String adir = System.getProperty("analyzeDir");
        if (adir == null) {
            analyzeResultDirectory = "";
        } else {
            analyzeResultDirectory = System.getProperty("user.dir") + "/" + adir;
            System.out.println("Analyze Result Directory: " + analyzeResultDirectory);
        }

        String showM = System.getProperty("showMethod");
        if (showM == null || showM.equals("true")) {
            showMethod = true;
        } else {
            showMethod = false;
        }

        String showU = System.getProperty("showUnit");
        if (showU == null || showU.equals("false")) {
            showUnit = false;
        } else {
            showUnit = true;
        }
    }

    public static void p(Object s) {
        System.out.println(s);
    }

}
