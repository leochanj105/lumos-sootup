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
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;

// import org.checkerframework.checker.units.qual.min;
// import org.eclipse.jdt.core.dom.CastExpression;
// import org.jf.dexlib2.analysis.ClassProvider;
// import org.objectweb.asm.commons.JSRInlinerAdapter;

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
import com.lumos.common.BacktrackInfo;
import com.lumos.common.Dependency;
import com.lumos.common.InstrumentPoint;
import com.lumos.common.Provenance;
import com.lumos.common.Query;
import com.lumos.common.RefSeq;
import com.lumos.common.TracePoint;
import com.lumos.compile.CompileUtils;

import fj.Unit;
import fj.test.reflect.Check;
// import soot.toolkits.scalar.ForwardFlowAnalysis;
import sootup.core.Project;
// import sootup.core.frontend.ClassProvider;
// import sootup.core.frontend.OverridingClassSource;
// import sootup.core.frontend.SootClassSource;
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
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.model.Body.BodyBuilder;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
// import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.OverridingJavaClassSource;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;
// import sootup.java.sourcecode.frontend.WalaIRToJimpleConverter;
// import sootup.java.sourcecode.frontend.WalaJavaClassProvider;
// import sootup.java.sourcecode.frontend.WalaSootMethod;
// import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Files;
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
        String[] services = new String[] { "ts-launcher" };
        // analyzePath(be
        // "C:\\Users\\jchen\\Desktop\\Academic\\lumos\\lumos-experiment\\ts-launcher\\target\\classes\\launcher\\service\\");
        // analyzePath(
        // "C:\\Users\\jchen\\Desktop\\Academic\\lumos\\lumos-experiment\\ts-launcher\\target\\classes");
        String base = "C:\\Users\\jchen\\Desktop\\Academic\\lumos\\lumos-experiment\\";
        String suffix = "\\target\\classes";

        for (String str : services) {
            String complete = base + str + suffix;
            analyzePath(complete);
        }

        // WalaIRToJimpleConverter walaToSoot = new WalaIRToJimpleConverter();
        // JavaSourceLoaderImpl.JavaClass walaClass = loadWalaClass(signature,
        // walaToSoot);
        // return Optional.ofNullable(walaClass).map(walaToSoot::convertClass);
    }

    public static void analyzePath(String path) {
        // path =
        // "C:\\Users\\jchen\\Desktop\\Academic\\sootup\\lumos-sootup\\src\\code\\";
        p("Analyzing " + path);
        // AnalysisInputLocation<JavaSootClass> inputLocation = new
        // JavaSourcePathAnalysisInputLocation(
        // SourceType.Application, Paths.get(path).toString());

        // JavaLanguage language = new JavaLanguage(8);
        // JavaProject project =
        // JavaProject.builder(language).addInputLocation(inputLocation).build();
        // JavaView view = project.createView();
        Path pa = Paths.get(path);
        p(pa.toAbsolutePath());
        AnalysisInputLocation<JavaSootClass> inputLocation = new PathBasedAnalysisInputLocation(
                pa, SourceType.Application);
        JavaLanguage language = new JavaLanguage(8);

        JavaProject project = JavaProject.builder(language).addInputLocation(inputLocation).build();
        JavaView view = project.createView();
        // p(inputLocation.getSourceType());
        // p(view.getScope());
        String cname = "launcher.service.LauncherServiceImpl";
        // String cname = "Test";
        ClassType classType = project.getIdentifierFactory().getClassType(cname);
        SootClass<JavaSootClassSource> sootClass = (SootClass<JavaSootClassSource>) view.getClass(classType).get();
        // p(view.getClasses());
        // if (true)
        // return;
        // String[] classes = new String[] { "Test", "Test$T", "Test$Order",
        // "Test$Result" };
        methodMap = new HashMap<>();
        // MethodSignature startMethod = null;
        for (JavaSootClass cls : view.getClasses()) {
            // ClassType classType = project.getIdentifierFactory().getClassType(clstr);
            // SootClass sootClass = view.getClass(classType).get();
            CompileUtils.outputJimple(cls);
            if (true)
                return;
            for (JavaSootMethod sm : cls.getMethods()) {
                if (sm.isAbstract()) {
                    // p(sm);
                    continue;
                }
                // WalaSootMethod wsm = (WalaSootMethod) sm;
                // if (wsm.toString().contains("some")) {
                MethodInfo minfo = new MethodInfo(sm);
                Map<TracePoint, List<TracePoint>> depGMap = minfo.analyzeDef();
                minfo.analyzeCF();
                // if (wsm.toString().contains("some")) {
                // startMethod = wsm.getSignature();
                // }
                // p();
                methodMap.put(sm.getSignature(), minfo);
            }
        }

        p("----------");

        MethodInfo minfo = searchMethod("doErrorQueue", "LauncherServiceImpl");
        for (Stmt stmt : minfo.sm.getBody().getStmts()) {
            // p(stmt.getPositionInfo().getStmtPosition().getFirstLine() + ", " + stmt);
            if (stmt.containsInvokeExpr()) {
                p(stmt.getPositionInfo().getStmtPosition().getFirstLine() + ", " + stmt);
                AbstractInvokeExpr aiexpr = stmt.getInvokeExpr();
                for (Value v : aiexpr.getArgs()) {
                    p(v + ", " + v.getClass());
                }
            }
        }
        p(minfo);
        // minfo.printLine(125);
        minfo.printLine(81);
        // Stmt start = minfo.getStmt(127, 0);
        // minfo.printValue(start);
        // Value startVal = minfo.getValue(start, 0);
        // p(startVal);

        // BacktrackInfo binfo = backtrack(new Query(new RefSeq(startVal, null),
        // start), minfo);
        // for (InstrumentPoint ipoint : binfo.insPoints) {
        // p(ipoint);
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

    public static BacktrackInfo backtrack(Query query, MethodInfo minfo) {
        p("BackTracking " + query + " in " + minfo.sm.getName());
        Deque<Query> currQueries = new ArrayDeque<>();
        Set<Query> visitedQueries = new HashSet<>();
        currQueries.add(query);
        Set<Query> unresolvedQueries = new HashSet<>();
        Set<InstrumentPoint> iPoints = new HashSet<>();
        while (!currQueries.isEmpty()) {
            Query currQuery = currQueries.pop();
            if (visitedQueries.contains(currQuery)) {
                continue;
            }

            visitedQueries.add(currQuery);
            Set<Provenance> pureDependencies = new HashSet<>();
            if (currQuery.refSeq.fields.size() > 0) {
                Value refHead = new JInstanceFieldRef((Local) currQuery.refSeq.value, currQuery.refSeq.fields.get(0));
                Set<Dependency> deps = minfo.getPrev(currQuery.stmt, refHead);

                // Urrr... JInstanceFieldRef does not have hashcode() implementation
                // We might have to retrive equal idential ref values manually
                if (deps == null) {
                    Map<Value, Set<Dependency>> smap = minfo.reachingAnalysis.getBeforeStmt(currQuery.stmt);
                    for (Value v : smap.keySet()) {
                        if (v instanceof JInstanceFieldRef) {
                            JInstanceFieldRef iref = (JInstanceFieldRef) v;
                            JInstanceFieldRef href = (JInstanceFieldRef) refHead;
                            if (iref.getBase().equals(href.getBase())
                                    && href.getFieldSignature().equals(href.getFieldSignature())) {
                                deps = smap.get(v);
                            }
                        }
                    }
                }

                // Should check all aliases of prefixes of reference sequence
                if (deps != null) {
                    for (Dependency dep : deps) {
                        // if (!(dep.dtype == Dependency.DepType.CF)) {
                        if (dep.dtype == Dependency.DepType.CF) {
                            p(dep);
                        }
                        pureDependencies.add(new Provenance(dep, minfo, currQuery.refSeq, 1));
                        // }
                    }
                }
            }
            Set<Dependency> deps = minfo.getPrev(currQuery.stmt, currQuery.refSeq.value);
            if (deps != null) {
                for (Dependency dep : deps) {
                    // if (!(dep.dtype == Dependency.DepType.CF)) {
                    pureDependencies.add(new Provenance(dep, minfo, currQuery.refSeq, 0));
                    // }
                }
            }
            Set<Dependency> cfdeps = minfo.getCF(currQuery.stmt);

            // RefSeq == null represents control-flow dependency
            for (Dependency dep : cfdeps) {
                pureDependencies.add(new Provenance(dep, minfo, null, -2));
            }
            // Find closest dependencies

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
            boolean allId = true;
            for (Provenance prov : pureDependencies) {
                if (!(prov.dep.stmt instanceof JIdentityStmt)) {
                    allId = false;
                    break;
                }
            }
            if (pureDependencies.size() == 0 || allId) {
                p("Unresolved: " + currQuery);

                RefSeq unresolvedSeq = currQuery.refSeq;
                if (unresolvedSeq.value instanceof Local) {
                    unresolvedQueries.add(currQuery);
                }
            }

            for (Provenance prov : pureDependencies) {
                if (prov.refSeq == null) {
                    Stmt currStmt = prov.dep.stmt;

                    for (Value use : currStmt.getUses()) {
                        p("[CF] " + use);
                        if (use instanceof Local) {
                            // CF usage tp put before the branching statement
                            iPoints.add(new InstrumentPoint(currStmt, use, minfo, true));
                            currQueries.add(new Query(new RefSeq(use), currStmt));

                        } else if (use instanceof Constant) {
                            // Do nothing for constants
                        } else {
                            // p(use.getClass());
                            // panicni();
                        }
                    }
                    continue;
                }

                Stmt currStmt = prov.dep.stmt;

                p(prov.dep + " answering " + currQuery);

                if ((currStmt instanceof JAssignStmt)) {
                    Value rop = null;
                    // if (currStmt instanceof JAssignStmt) {
                    rop = ((JAssignStmt) currStmt).getRightOp();
                    // } else {
                    // rop = ((JIdentityStmt) currStmt).getRightOp();
                    // }
                    Value lop = ((JAssignStmt) currStmt).getLeftOp();
                    if (rop instanceof AbstractInvokeExpr) {
                        AbstractInvokeExpr iexpr = (AbstractInvokeExpr) rop;
                        List<Value> parameters = new ArrayList<>();
                        BacktrackInfo binfo = walkMethod(currQuery.refSeq, currStmt, -1);
                        Set<Query> resolvedQueries = binfo.unresolvedRequeries;
                        iPoints.addAll(binfo.insPoints);

                        for (Query resolvedQuery : resolvedQueries) {
                            currQueries.add(resolvedQuery);
                        }
                    } else {
                        List<FieldSignature> currSuffix = currQuery.refSeq.fields;
                        List<FieldSignature> newSuffix = currSuffix.subList(prov.prefix, currSuffix.size());
                        // p("!!!!!! " + rop + ", " + rop.getClass() + " .... " + rop.getUses());
                        if (rop instanceof JInstanceFieldRef) {
                            JInstanceFieldRef tmpRef = (JInstanceFieldRef) rop;
                            RefSeq refSeq = new RefSeq(tmpRef.getBase(), newSuffix);
                            refSeq.appendHead(tmpRef.getFieldSignature());
                            currQueries.add(new Query(refSeq, currStmt));
                        } else if (rop instanceof JCastExpr) {
                            JCastExpr cexpr = (JCastExpr) rop;
                            RefSeq refSeq = new RefSeq(cexpr.getOp(), newSuffix);
                            currQueries.add(new Query(refSeq, currStmt));
                        } else if (rop instanceof Local) {
                            currQueries.add(new Query(new RefSeq(rop, newSuffix), currStmt));
                        } else {
                            for (Value use : rop.getUses()) {
                                // p(use + " ========= " + use.getClass());
                                if (use instanceof Local) {
                                    // p("***\n" + use + ": ");
                                    currQueries.add(new Query(new RefSeq(use, newSuffix), currStmt));
                                } else {
                                    p(use.getClass());
                                    panicni();
                                }
                            }
                        }

                    }
                    if (currQuery.refSeq.fields.size() == 0 && !(lop instanceof JInstanceFieldRef)) {
                        iPoints.add(new InstrumentPoint(minfo.tpMap.get(currStmt).get(lop), minfo));
                    }
                } else if (currStmt instanceof JIdentityStmt) {
                    // Do nothing; local identities already resolved
                } else if (currStmt instanceof JInvokeStmt) {
                    AbstractInvokeExpr iexpr = ((JInvokeStmt) currStmt).getInvokeExpr();
                    List<Value> params = getParameters(iexpr);
                    int indexOfBase = params.indexOf(currQuery.refSeq.value);
                    p("Index: " + indexOfBase);
                    if (indexOfBase < 0) {
                        p("Base not found");
                        panicni();
                    }
                    BacktrackInfo binfo = walkMethod(currQuery.refSeq, currStmt, indexOfBase);
                    Set<Query> resolvedQueries = binfo.unresolvedRequeries;
                    iPoints.addAll(binfo.insPoints);
                    // Set<Query> resolvedQueries =
                    for (Query resolvedQuery : resolvedQueries) {
                        currQueries.add(resolvedQuery);
                    }
                } else {
                    p(currStmt + ", " + currStmt.getClass());
                    panicni();
                }
            }
        }
        BacktrackInfo currInfo = new BacktrackInfo(unresolvedQueries, iPoints);
        return currInfo;
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

    public static Set<Query> resolveMethod(AbstractInvokeExpr iexpr, Stmt stmt) {
        String methodName = iexpr.getMethodSignature().toString();
        Set<Query> resolvedQueries = new HashSet<>();
        if (methodName.contains("java.lang.String: boolean equals(java.lang.Object)")) {
            p("Manually wired " + methodName);
            for (Value v : getParameters(iexpr)) {
                resolvedQueries.add(new Query(new RefSeq(v), stmt));
            }
        } else {
            p("Method " + iexpr.getMethodSignature() + " not found.");
        }
        return resolvedQueries;
    }

    public static BacktrackInfo walkMethod(RefSeq seq, Stmt stmt, int baseIndex) {
        AbstractInvokeExpr iexpr = stmt.getInvokeExpr();
        Set<Query> resolvedQueries = new HashSet<>();
        Set<InstrumentPoint> resolvedInsts = new HashSet<>();
        MethodInfo minfo = searchMethod(iexpr.getMethodSignature().toString());
        if (minfo == null) {
            resolvedQueries.addAll(resolveMethod(iexpr, stmt));
            return new BacktrackInfo(resolvedQueries, resolvedInsts);
        }
        List<Value> parameters = getParameters(iexpr);
        List<Value> actualParams = minfo.getParamValues();

        p("WALK in " + minfo.sm.getName() + ": " + seq + ", " + stmt);
        for (Stmt retPoint : minfo.getReturnStmts()) {
            Value baseVal = null;
            if (baseIndex == -1) {
                JReturnStmt rstmt = (JReturnStmt) retPoint;
                baseVal = rstmt.getOp();
            } else {
                baseVal = minfo.getParamValues().get(baseIndex);
            }
            RefSeq actualSeq = new RefSeq(baseVal, seq.fields);
            Query actualQuery = new Query(actualSeq, retPoint);
            BacktrackInfo currInfo = backtrack(actualQuery, minfo);
            Set<Query> unresolved = currInfo.unresolvedRequeries;
            resolvedInsts.addAll(currInfo.insPoints);

            for (Query query : unresolved) {
                Value base = query.refSeq.value;
                for (int i = 0; i < actualParams.size(); i++) {
                    Value v = actualParams.get(i);
                    if (base.equals(v)) {
                        Query resolvedQuery = new Query(new RefSeq(parameters.get(i), query.refSeq.fields), stmt);
                        resolvedQueries.add(resolvedQuery);
                        p("Resovled: " + resolvedQuery);
                    }
                }
            }
        }
        p("Finished WALK in " + minfo.sm.getName() + ": " + seq + ", " + stmt);
        return new BacktrackInfo(resolvedQueries, resolvedInsts);
    }

    public static void analyzeExchange(SootMethod wsm) {
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

    public static List<Value> parameters(SootMethod wsm) {
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

    public static Body insertBefore(SootMethod wsm, Stmt stmt, Stmt toinsert) {
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
