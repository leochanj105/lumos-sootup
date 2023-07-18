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
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;

// import org.checkerframework.checker.units.qual.min;
// import org.eclipse.jdt.core.dom.CastExpression;
// import org.jf.dexlib2.analysis.ClassProvider;
// import org.objectweb.asm.commons.JSRInlinerAdapter;

import com.lumos.analysis.MethodInfo;
import com.lumos.analysis.ReachingDefAnalysis;
import com.lumos.common.BacktrackInfo;
import com.lumos.common.Dependency;
import com.lumos.common.InstrumentPoint;
import com.lumos.common.Provenance;
import com.lumos.common.Query;
import com.lumos.common.RefSeq;
import com.lumos.common.TracePoint;
import com.lumos.common.Dependency.DepType;
import com.lumos.compile.CompileUtils;
import com.lumos.wire.HTTPReceiveWirePoint;
import com.lumos.wire.WireForAllParams;
import com.lumos.wire.WireHTTP;

import soot.Body;
// import soot.toolkits.scalar.ForwardFlowAnalysis;
// import sootup.java.sourcecode.frontend.WalaIRToJimpleConverter;
// import sootup.java.sourcecode.frontend.WalaJavaClassProvider;
// import sootup.java.sourcecode.frontend.WalaSootMethod;
// import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;
import soot.G;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.JastAddJ.Signatures.FieldSignature;
import soot.JastAddJ.Signatures.MethodSignature;
import soot.jbco.util.BodyBuilder;
import soot.jimple.Constant;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.AbstractInstanceInvokeExpr;
import soot.jimple.internal.AbstractInvokeExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JReturnStmt;
import soot.options.Options;

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

    public static Map<String, MethodInfo> methodMap;

    public static void main(String[] args) {
        readParams();
        String[] services = new String[] { "ts-launcher", "ts-inside-payment-service" };
        // analyzePath("src/code");
        String base = "C:\\Users\\jchen\\Desktop\\Academic\\lumos\\lumos-experiment\\";
        String suffix = "\\target\\classes";
        methodMap = new HashMap<>();

        for (String str : services) {
            String complete = base + str + suffix;
            analyzePath(complete);
        }
        // MethodInfo minfo = searchMethod("some", "Test");
        // Unit start = minfo.getStmt(68, 0);
        // // p(start);
        // // minfo.printValue(start);
        // Value startVal = minfo.getValue(start, 0);
        // p(startVal);

        // BacktrackInfo binfo = backtrack(new Query(new RefSeq(startVal, null),
        // start), minfo);
        // for (InstrumentPoint ipoint : binfo.insPoints) {
        // p(ipoint);
        // }
        play();
    }

    public static void play() {
        MethodInfo minfo = searchMethod("doErrorQueue", "LauncherServiceImpl");

        p(minfo);
        // minfo.printLine(126);
        Unit start = minfo.getStmt(126, 11);
        p(start);
        // minfo.printValue(start);
        Value startVal = minfo.getValue(start, 1);
        p(startVal);

        BacktrackInfo binfo = backtrack(new Query(new RefSeq(startVal, null),
                start), minfo);
        for (InstrumentPoint ipoint : binfo.insPoints) {
            p(ipoint);
        }
    }

    public static void setupSoot(String path) {
        G.reset();

        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_keep_line_number(true);
        Options.v().set_whole_program(true);
        Options.v().set_validate(true);

        Options.v().set_allow_phantom_elms(true);

        // This is needed to prevent compile error for unimplemented
        // methods in interfaces !!
        Options.v().set_ignore_resolution_errors(true);

        // Need this to makesure paramter names are kept!!
        // Spring annotations rely on this!!
        Options.v().set_write_local_annotations(true);

        Options.v().set_soot_classpath(path);
        // Options.v().set_process_dir(Collections.singletonList(sourceDirectory));
        processList = new ArrayList<String>();
        // try {
        // File myObj = new File("listWire");
        // Scanner myReader = new Scanner(myObj);
        // while (myReader.hasNextLine()) {
        // String data = myReader.nextLine();
        // processList.add(sourceDirectory + "/" + data + "/original/BOOT-INF/classes");
        // // System.out.println(data);
        // }
        // myReader.close();
        // } catch (Exception e) {
        // System.out.println("An error occurred.");
        // e.printStackTrace();
        // }
        // Options.v().set_process_dir(processList);

        // String arr[] = { "../cancel/BOOT-INF/classes" };
        String arr[] = { path };
        Options.v().set_process_dir(Arrays.asList(arr));

        Options.v().set_output_dir(outputDirectory);
        if (outputFormat.equals("jimple")) {
            Options.v().set_output_format(Options.output_format_J);
        } else if (outputFormat.equals("class")) {
            Options.v().set_output_format(Options.output_format_class);
        } else if (outputFormat.equals("none")) {
            Options.v().set_output_format(Options.output_format_none);
        }
        // Turning off jimple body generation for dependency classes
        // This should be turned off for more sound analysis
        // String[] exClasses = {"price.repository.*"};
        // List<String> excludePackagesList = Arrays.asList(exClasses);
        // Options.v().set_exclude(excludePackagesList);

        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_print_tags_in_output(true);

        // Use original names
        Options.v().setPhaseOption("jb", "optimize:false");
        Options.v().setPhaseOption("jb", "use-original-names:true");
        // Options.v().setPhaseOption("jj", "use-original-names:true");
        Options.v().setPhaseOption("jb", "preserve-source-annotations:true");

        // Need this to avoid the need to provide an entry point
        Options.v().setPhaseOption("cg", "all-reachable:true");

        // Need this to include all subtypes
        // Options.v().setPhaseOption("cg", "library:any-subtype");

        Scene.v().loadNecessaryClasses();
        Scene.v().addBasicClass("java.io.PrintStream", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.String", SootClass.SIGNATURES);
        // Scene.v().loadClassAndSupport();

    }

    public static void analyzePath(String path) {
        // path =
        // "C:\\Users\\jchen\\Desktop\\Academic\\sootup\\lumos-sootup\\src\\code\\";
        p("Analyzing " + path);
        setupSoot(path);
        // String cname = "launcher.service.LauncherServiceImpl";
        String cname = "Test";

        // SootMethod sm;
        for (SootClass cls : Scene.v().getApplicationClasses()) {
            Scene.v().forceResolve(cls.getName(), SootClass.BODIES);
            if (cls.toString().contains("conf.HttpAspect")) {
                continue;
            }

            for (SootMethod sm : cls.getMethods()) {
                if (sm.isAbstract()) {
                    continue;
                }
                // p(sm.getSignature());
                sm.retrieveActiveBody();
                // p(sm.getActiveBody());

                // if (!sm.toString().contains("Test$T: void <init>(Test,boolean)")) {
                // continue;
                // minfo.reachingAnalysis.getBeforeUnit(minfo.getReturnStmts().get(0));
                // }
                // // WalaSootMethod wsm = (WalaSootMethod) sm;
                // // if (wsm.toString().contains("some")) {
                MethodInfo minfo = new MethodInfo(sm);
                Map<TracePoint, List<TracePoint>> depGMap = minfo.analyzeDef();
                minfo.analyzeCF();
                // p(sm.getActiveBody());
                if (sm.toString().contains("Test$T: void <init>(Test,boolean)")) {
                    p("--- " +
                            minfo.reachingAnalysis.getBeforeUnit(minfo.getReturnStmts().get(0)));
                }
                methodMap.put(sm.getSignature(), minfo);
            }
            // CompileUtils.outputJimple(cls, path);
        }

        p("----------");

    }

    public static MethodInfo searchMethod(String... str) {
        for (String sig : methodMap.keySet()) {
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
        p("BackTracking " + query + " in " + minfo.sm.getName() + ", " + minfo.getParamValues());

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
            // p("!!! " + currQuery);
            // p(currQuery.refSeq);

            visitedQueries.add(currQuery);
            Set<Provenance> pureDependencies = new HashSet<>();
            // If current refSeq == null, then only backtrack CF dependencies
            if (currQuery.refSeq != null) {

                if (currQuery.refSeq.fields.size() > 0) {
                    // new JInstanceFieldRef();
                    Value refHead = new JInstanceFieldRef(currQuery.refSeq.value, currQuery.refSeq.fields.get(0));

                    Set<Dependency> deps = minfo.getPrev(currQuery.unit, refHead);

                    // Urrr... JInstanceFieldRef does not have hashcode() implementation
                    // We might have to retrive equal idential ref values manually
                    if (deps == null) {
                        Map<Value, Set<Dependency>> smap = minfo.reachingAnalysis.getBeforeUnit(currQuery.unit);
                        for (Value v : smap.keySet()) {
                            if (v instanceof JInstanceFieldRef) {
                                JInstanceFieldRef iref = (JInstanceFieldRef) v;
                                JInstanceFieldRef href = (JInstanceFieldRef) refHead;
                                if (iref.getBase().equals(href.getBase())
                                        && iref.getFieldRef().equals(href.getFieldRef())) {
                                    deps = smap.get(v);
                                }
                            }
                        }
                    }

                    // Should check all aliases of prefixes of reference sequence
                    if (deps != null) {

                        for (Dependency dep : deps) {
                            // if (!(dep.dtype == Dependency.DepType.CF)) {
                            // if (dep.dtype == Dependency.DepType.CF) {
                            // p(dep);
                            // }
                            pureDependencies.add(new Provenance(dep, minfo, currQuery.refSeq, 1));
                            // }
                        }
                    }
                }
                Set<Dependency> deps = minfo.getPrev(currQuery.unit, currQuery.refSeq.value);

                if (deps != null) {

                    for (Dependency dep : deps) {

                        pureDependencies.add(new Provenance(dep, minfo, currQuery.refSeq, 0));

                    }
                }
            }

            // if (currQuery.refSeq != null &&
            // currQuery.refSeq.toString().contains("stack48")) {
            // for (Provenance prov : pureDependencies) {
            // p("--- " + prov.dep);
            // }
            // }
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

            // Should add CF deps after removing redundant RW deps...
            Set<Dependency> cfdeps = minfo.getCF(currQuery.unit);

            // RefSeq == null represents control-flow dependency
            for (Dependency dep : cfdeps) {
                pureDependencies.add(new Provenance(dep, minfo, null, -2));
            }
            boolean allId = true;
            for (Provenance prov : pureDependencies) {
                // if (currQuery.toString().contains("l0.[<Test$T: java.lang.Boolean body>")) {
                // p(prov.dep);
                // }
                if (!(prov.dep.unit instanceof JIdentityStmt)) {
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
                    Stmt currStmt = (Stmt) prov.dep.unit;

                    for (ValueBox usebox : currStmt.getUseBoxes()) {
                        Value use = usebox.getValue();
                        p("[CF] " + use + " at line " + currStmt.getJavaSourceStartLineNumber());
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

                Stmt currStmt = (Stmt) prov.dep.unit;

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
                        BacktrackInfo binfo = walkMethod(currQuery.refSeq, prov, -1);
                        Set<Query> resolvedQueries = binfo.unresolvedRequeries;
                        iPoints.addAll(binfo.insPoints);

                        for (Query resolvedQuery : resolvedQueries) {
                            currQueries.add(resolvedQuery);
                        }
                    } else {
                        List<SootFieldRef> currSuffix = currQuery.refSeq.fields;
                        List<SootFieldRef> newSuffix = currSuffix.subList(prov.prefix, currSuffix.size());
                        // p("!!!!!! " + rop + ", " + rop.getClass() + " .... " + rop.getUses());
                        if (rop instanceof JInstanceFieldRef) {
                            JInstanceFieldRef tmpRef = (JInstanceFieldRef) rop;
                            RefSeq refSeq = new RefSeq(tmpRef.getBase(), newSuffix);
                            refSeq.appendHead(tmpRef.getFieldRef());
                            currQueries.add(new Query(refSeq, currStmt));
                        } else if (rop instanceof JCastExpr) {
                            JCastExpr cexpr = (JCastExpr) rop;
                            RefSeq refSeq = new RefSeq(cexpr.getOp(), newSuffix);
                            currQueries.add(new Query(refSeq, currStmt));
                        } else if (rop instanceof Local) {
                            currQueries.add(new Query(new RefSeq(rop, newSuffix), currStmt));
                        } else if (rop instanceof Constant) {
                            currQueries.add(new Query(null, currStmt));
                        } else {

                            for (ValueBox usebox : rop.getUseBoxes()) {
                                Value use = usebox.getValue();
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
                    InvokeExpr iexpr = ((JInvokeStmt) currStmt).getInvokeExpr();
                    List<Value> params = getParameters(iexpr);
                    int indexOfBase = params.indexOf(currQuery.refSeq.value);
                    p("Index: " + indexOfBase);
                    if (indexOfBase < 0) {
                        p("Base not found");
                        panicni();
                    }
                    BacktrackInfo binfo = walkMethod(currQuery.refSeq, prov, indexOfBase);
                    Set<Query> resolvedQueries = binfo.unresolvedRequeries;
                    iPoints.addAll(binfo.insPoints);
                    // Set<Query> resolvedQueries =

                    // If the this method did not
                    // if (resolvedQueries.isEmpty()) {
                    // currQueries.add(new Query(currQuery.refSeq, currStmt));
                    // } else {
                    for (Query resolvedQuery : resolvedQueries) {
                        currQueries.add(resolvedQuery);
                    }
                    // }
                } else {
                    p(currStmt + ", " + currStmt.getClass());
                    panicni();
                }
            }
        }
        BacktrackInfo currInfo = new BacktrackInfo(unresolvedQueries, iPoints);
        return currInfo;
    }

    public static List<Value> getParameters(InvokeExpr iexpr) {
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

    public static String wireExchange(InvokeExpr Expr, String reqMethod, int lineNum) {
        return "";
    }

    public static Set<Query> resolveMethod(InvokeExpr iexpr, Stmt stmt) {
        String methodName = iexpr.getMethod().getSignature();
        Set<Query> resolvedQueries = new HashSet<>();

        if (wireByNameForParams(methodName)) {
            p("Manually wired " + methodName + " at " + stmt);
            for (Value v : getParameters(iexpr)) {
                // p("!!! " + v);
                resolvedQueries.add(new Query(new RefSeq(v), stmt));
            }
        } else {
            p("Method " + iexpr.getMethod().getSignature() + " not found.");
        }
        return resolvedQueries;
    }

    public static boolean wireByNameForParams(String name) {
        boolean found = false;
        for (String str : WireForAllParams.getNames()) {
            if (name.contains(str)) {
                found = true;
                break;
            }
        }

        return found;
    }

    public static BacktrackInfo walkMethod(RefSeq seq, Provenance prov, int baseIndex) {
        Stmt stmt = (Stmt) prov.dep.unit;
        InvokeExpr iexpr = stmt.getInvokeExpr();
        Set<Query> resolvedQueries = new HashSet<>();
        Set<InstrumentPoint> resolvedInsts = new HashSet<>();
        MethodInfo minfo = searchMethod(iexpr.getMethod().getSignature().toString());
        HTTPReceiveWirePoint remotep = null;
        if (minfo == null) {
            Set<Query> wireSolved = resolveMethod(iexpr, stmt);
            if (!wireSolved.isEmpty()) {
                resolvedQueries.addAll(wireSolved);
                return new BacktrackInfo(resolvedQueries, resolvedInsts);
            } else {
                if (iexpr.getMethod().getSignature().contains("exchange")) {
                    p("======");
                }
                remotep = WireHTTP.get(prov.minfo.sm.getSignature(),
                        stmt.getJavaSourceStartLineNumber());
                if (remotep != null) {
                    minfo = searchMethod(remotep.targetMethod);
                    p("!!!!! " + minfo);
                } else {
                    // If current depedency is a MAY-CALL, and no dependency is resolved,
                    // we keep searching backwards
                    if (resolvedQueries.isEmpty()) {
                        if (prov.dep.dtype == DepType.CALL) {
                            resolvedQueries.add(new Query(seq, stmt));
                        }
                    }
                    return new BacktrackInfo(resolvedQueries, resolvedInsts);
                }
            }

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
                        if (remotep != null) {
                            int idx = remotep.recvParamIndices.indexOf(i);
                            if (idx >= 0) {
                                int oridx = remotep.sendParamIndices.get(idx);
                                Query resolvedQuery = new Query(new RefSeq(parameters.get(oridx), query.refSeq.fields),
                                        stmt);
                                resolvedQueries.add(resolvedQuery);
                                p("Resovled: " + resolvedQuery);
                            }
                        } else {
                            Query resolvedQuery = new Query(new RefSeq(parameters.get(i), query.refSeq.fields), stmt);
                            resolvedQueries.add(resolvedQuery);
                            p("Resovled: " + resolvedQuery);
                        }
                    }
                }
            }
        }
        p("Finished WALK in " + minfo.sm.getName() + ": " + seq + ", " + stmt);
        return new BacktrackInfo(resolvedQueries, resolvedInsts);
    }

    // public static void analyzeExchange(SootMethod wsm) {
    // for (Stmt stmt : wsm.getBody().getStmts()) {
    // if (!stmt.containsInvokeExpr())
    // continue;
    // MethodSignature ms = stmt.getInvokeExpr().getMethodSignature();
    // if (!ms.toString().contains("fff(")) // This should be exchange, but I'm
    // testing for now
    // continue;

    // // p(ms);
    // ReachingDefAnalysis rda = new
    // ReachingDefAnalysis(wsm.getBody().getStmtGraph());
    // Map<Value, Set<Dependency>> mdep = rda.getBeforeStmt(stmt);
    // Value argURL = stmt.getInvokeExpr().getArg(0);
    // p(mdep.get(argURL));
    // }
    // }

    public static List<Value> parameters(SootMethod wsm) {
        // int hasThis = wsm.isStatic() ? 0 : 1;
        // int methodParamCount = wsm.getParameterCount() + hasThis;
        List<Value> methodParams = new ArrayList<>();
        // int count = 0;
        for (Value v : wsm.getActiveBody().getParameterLocals()) {
            methodParams.add(v);
        }
        if (!wsm.isStatic()) {
            methodParams.add(0, wsm.getActiveBody().getThisLocal());
        }
        return methodParams;

    }

    public static Body insertBefore(SootMethod wsm, Stmt stmt, Stmt toinsert) {
        // // MutableStmtGraph graph = new MutableBlockStmtGraph(body.getStmtGraph());
        // BodyBuilder builder = Body.builder(wsm.getBody(), wsm.getModifiers());
        // builder.insertBefore(stmt, toinsert);
        return null;
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
