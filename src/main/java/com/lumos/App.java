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
import com.lumos.forward.ContextSensitiveInfo;
import com.lumos.forward.ContextSensitiveValue;
import com.lumos.forward.Definition;
import com.lumos.forward.EnterNode;
import com.lumos.forward.ExitNode;
import com.lumos.forward.ForwardIPAnalysis;
import com.lumos.forward.IPFlowInfo;
import com.lumos.forward.IPNode;
import com.lumos.forward.InterProcedureGraph;
import com.lumos.forward.StmtNode;
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

    public static boolean showRound = false;
    public static boolean showLineNum = true;

    public static Map<String, MethodInfo> methodMap;

    public static void main(String[] args) {
        readParams();
        String[] services = new String[] {
                "ts-launcher",
                "ts-inside-payment-service",
                "ts-order-service"
        };
        methodMap = new HashMap<>();
        // analyzePath("C:\\Users\\jchen\\Desktop\\Academic\\sootup\\lumos-sootup\\src\\code");
        String base = "C:\\Users\\jchen\\Desktop\\Academic\\lumos\\lumos-experiment\\";
        String suffix = "\\target\\classes";

        for (String str : services) {
            String complete = base + str + suffix;
            analyzePath(complete);
        }

        InterProcedureGraph igraph = new InterProcedureGraph(methodMap);
        // // igraph.build(services);
        // MethodInfo minfo = searchMethod("sendInsidePayment");
        ContextSensitiveInfo cinfo = igraph.build("sendInsidePayment");
        // ContextSensitiveInfo cinfo = igraph.build("InsidePaymentServiceImpl",
        // "pay(");
        // p(cinfo.getFirstNode());

        // long start = System.currentTimeMillis();
        ForwardIPAnalysis fia = new ForwardIPAnalysis(igraph);
        // IPNode ipnode = igraph.getLastNode();
        // p(((StmtNode) ipnode).getContext().getStackLast().sm + ", " +
        // ipnode.getDescription());
        IPNode ipnode = igraph.searchNode(
                "$stack29 = virtualinvoke $stack28.<java.lang.Boolean: boolean booleanValue()>()",
                "noop");
        // p(ipnode);
        // p(ipnode.getContext().getStackLast().cfDependency.get(ipnode.getStmt()));
        IPFlowInfo cmap = fia.getBefore(ipnode);
        ContextSensitiveValue cvalue = ContextSensitiveValue.getCValue(ipnode.getContext(),
                ((JAssignStmt) ipnode.getStmt()).getLeftOp());

        Set<IPNode> unresolvedNodes = new HashSet<>();
        Set<ContextSensitiveValue> visitedCVs = new HashSet<>();
        Set<IPNode> visitedNodes = new HashSet<>();

        // Set<Definition> satisfiedDefs = new HashSet<>();
        for (Definition def : cmap.getDefinitionsByCV(cvalue)) {
            App.p(def.d());
            Stmt defstmt = def.getDefinedLocation().getStmt();
            if ((defstmt.toString().contains("return 1")) ||
                    (defstmt.toString().contains("return 0") && defstmt.getJavaSourceStartLineNumber() != 59)) {
                continue;
            }
            unresolvedNodes.add(def.getDefinedLocation());
        }

        // unresolvedCVs.add(cvalue);

        while (!unresolvedNodes.isEmpty()) {
            IPNode node = unresolvedNodes.iterator().next();

            unresolvedNodes.remove(node);
            if (visitedNodes.contains(node)) {
                continue;
            }
            visitedNodes.add(node);
            Stmt stmt = node.getStmt();
            App.p(node);
            MethodInfo minfo = node.getContext().getStackLast();
            for (Stmt cfstmt : minfo.cfDependency.get(stmt)) {
                IPNode cfnode = igraph.getIPNode(node.getContext(), cfstmt);
                unresolvedNodes.add(cfnode);
            }

            // Definition def = satisfiedDefs.iterator().next();
            // satisfiedDefs.remove(def);
            // if (stmt.toString().contains("return $stack1")) {
            // App.p(fia.getBefore(node));
            // }

            Set<ContextSensitiveValue> cvused = new HashSet<>();
            if (stmt instanceof JReturnStmt) {
                Value ret = ((JReturnStmt) stmt).getOp();
                cvused.add(ContextSensitiveValue.getCValue(node.getContext(), ret));
            } else {
                // JAssignStmt astmt = (JAssignStmt) stmt;
                Set<Value> banned = new HashSet<>();
                for (ValueBox vbox : stmt.getUseBoxes()) {
                    Value use = vbox.getValue();

                    if (banned.contains(use)) {
                        continue;
                    }
                    if ((stmt instanceof JAssignStmt)
                            && ((JAssignStmt) stmt).getLeftOp().getUseBoxes().contains(vbox)) {
                        continue;
                    }

                    if ((use instanceof Local) || (use instanceof JInstanceFieldRef)) {
                        ContextSensitiveValue cvuse = ContextSensitiveValue.getCValue(node.getContext(), use);
                        cvused.add(cvuse);
                        if (use instanceof JInstanceFieldRef) {
                            banned.add(((JInstanceFieldRef) use).getBase());
                        }
                    }
                }

            }
            for (ContextSensitiveValue cv : cvused) {

                Set<Definition> satisfiedDefs = fia.getBefore(node).getDefinitionsByCV(cv);

                // if (cv.toString().contains("sendInsidePayment,pay,getStatus ==> $stack1")) {
                // App.p("------------------- \n" + node);
                // App.p(fia.getBefore(node));
                // IPNode nodex = node.getPredecesors().get(0);
                // App.p("------------------- \n" + nodex);
                // App.p(fia.getAfter(nodex));
                // }

                App.p(cv);

                if (cv.toString().contains("this.<inside_payment.domain.Order: int status>")) {
                    // App.p("!!!!! " + cv);
                    // for (Definition satdef : satisfiedDefs) {
                    // App.p(satdef.getDefinedLocation() + ":: " + satdef.getDefinedValue());
                    // }
                }
                boolean unresolved = true;
                for (Definition satdef : satisfiedDefs) {

                    if (satdef.getDefinedLocation() != null) {
                        unresolved = false;
                        unresolvedNodes.add(satdef.getDefinedLocation());
                    }
                }
                if (unresolved) {
                    App.p(">>>>>\nUnresolved " + cv + " with:");
                    for (Definition satdef : satisfiedDefs) {
                        App.p(satdef.getDefinedValue());
                    }
                    App.p("<<<<<\n");
                }

            }

        }

        // p(fia.getAfter(ipnode));
        // p("!!! " + ipnode.getSuccessors());

        // ipnode = igraph.searchNode("getOrderById", "getOrderId",
        // "return $stack1",
        // "stmt");
        // p(fia.getBefore(ipnode));
        // p(fia.getBefore(ipnode).getCurrMapping());
        // long stop = System.currentTimeMillis();
        // App.p((stop - start) / 1000.0);
        // play();
    }

    // public static void backtrack()

    public static void play() {
        MethodInfo minfo = searchMethod("doErrorQueue", "LauncherServiceImpl");

        p(minfo);
        // minfo.printLine(126);
        Unit start = minfo.getStmt(126, 11);
        p(start);
        // minfo.printValue(start);
        Value startVal = minfo.getValue(start, 1);
        p(startVal);

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
        // String cname = "Test";

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

                sm.retrieveActiveBody();

                MethodInfo minfo = new MethodInfo(sm);
                Map<TracePoint, List<TracePoint>> depGMap = minfo.analyzeDef();
                // minfo.analyzeCF();
                minfo.buildPostDominanceFrontier();

                // p(sm.getActiveBody());
                // if (sm.toString().contains("Test$T: void <init>(Test,boolean)")) {
                // p("--- " +
                // minfo.reachingAnalysis.getBeforeUnit(minfo.getReturnStmts().get(0)));
                // }
                // if (sm.getSignature().contains("getOrderById")) {
                // p("!! " + sm.getSignature());
                // }
                methodMap.put(sm.getSignature(), minfo);
            }
            // CompileUtils.outputJimple(cls, path);
        }

        p("----------");
        // panicni();

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
