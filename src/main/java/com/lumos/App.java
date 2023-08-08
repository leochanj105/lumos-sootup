package com.lumos;

import java.nio.file.Paths;
import java.sql.Ref;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

import javax.swing.text.AbstractDocument.Content;

import org.glassfish.jaxb.runtime.v2.runtime.reflect.Lister.Pack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;

// import com.google.protobuf.Option;

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
// import com.lumos.common.TracePoint;
import com.lumos.common.Dependency.DepType;
import com.lumos.compile.CompileUtils;
import com.lumos.forward.CallSite;
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
import com.lumos.forward.TracePoint;
import com.lumos.forward.UniqueName;
import com.lumos.wire.Banned;
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
import soot.IntType;
import soot.Local;
import soot.PackManager;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.JastAddJ.Signatures.FieldSignature;
import soot.JastAddJ.Signatures.MethodSignature;
import soot.jbco.util.BodyBuilder;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NullConstant;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.internal.AbstractInstanceInvokeExpr;
import soot.jimple.internal.AbstractInvokeExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.options.Options;
import soot.util.Cons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
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
    public static String outputDirectory = "WFF";
    public static String analyzeResultDirectory;
    public static boolean showMethod = true;
    public static boolean showUnit = false;
    public static String outputFormat = "class";

    public static final String LOG_PREFIX = "LUMOS-LOG";

    public static boolean compileJimple = false;
    public static boolean compileClass = false;

    public static boolean showRound = false;
    public static boolean showLineNum = true;

    public static Map<String, MethodInfo> methodMap;

    public static Map<String, SootClass> classMap = new HashMap<>();

    public static Map<String, String> serviceMap = new HashMap<>();

    public static String exclude = "goto [?= $stack66 = $stack62 & $stack68]";

    public static void main(String[] args) {
        // readParams();
        String[] services = new String[] {
                "ts-launcher",
                "ts-inside-payment-service",
                "ts-order-other-service",
                "ts-order-service",
                "ts-payment-service",
                "ts-cancel-service",
                "ts-sso-service"
        };
        methodMap = new HashMap<>();
        // analyzePath("C:\\Users\\jchen\\Desktop\\Academic\\sootup\\lumos-sootup\\src\\code");
        String base = "C:\\Users\\jchen\\Desktop\\Academic\\lumos\\lumos-experiment\\";
        String suffix = "\\target\\classes";

        for (String str : services) {
            String complete = base + str + suffix;
            analyzePath(complete, str);
        }

        if (compileJimple)
            return;

        // List<TracePoint> readtps = (List<TracePoint>) readObj("TP", "tps");
        // for (TracePoint tp : readtps) {
        // // for (TracePoint tp : tps) {
        // System.err.println(tp.sm + ": " + tp.s + ", " + tp.v + "." + tp.suffix);
        // // }
        // }
        readTPs("TP", "tps");
        if (true)
            return;

        InterProcedureGraph igraph = new InterProcedureGraph(methodMap);
        // // igraph.build(services);
        // MethodInfo minfo = searchMethod("sendInsidePayment");
        // ContextSensitiveInfo cinfo = igraph.build("sendInsidePayment");
        ContextSensitiveInfo cinfo = igraph.build("doErrorQueue(");

        long start = System.currentTimeMillis();
        IPNode firstNode = igraph.searchNode("$stack16 = new launcher.domain.RegisterInfo");
        ForwardIPAnalysis fia = new ForwardIPAnalysis(igraph, firstNode);
        // App.p(cinfo.getFirstNode().context.getStackLast().sm.);
        long analysisDuration = System.currentTimeMillis() - start;
        // p(igraph.getLastNode());
        // IPNode ipnode = igraph
        // .searchNode("$stack29 = virtualinvoke $stack28.<java.lang.Boolean:
        // booleanbooleanValue()>()", "noop");

        IPNode ipnode = igraph.searchNode(
                "$stack66 = $stack62 & $stack68",
                "stmt");
        p(ipnode.stmt.toString());
        IPFlowInfo cmap = fia.getAfter(ipnode);
        ContextSensitiveValue cvalue = ContextSensitiveValue.getCValue(ipnode.getContext(),
                ((JAssignStmt) ipnode.getStmt()).getLeftOp());

        Set<IPNode> unresolvedNodes = new HashSet<>();
        Set<ContextSensitiveValue> visitedCVs = new HashSet<>();
        Set<IPNode> visitedNodes = new HashSet<>();

        // Set<Definition> satisfiedDefs = new HashSet<>();
        for (Definition def : cmap.getDefinitionsByCV(cvalue)) {
            App.p(def.d());
            Stmt defstmt = def.getDefinedLocation().getStmt();
            // if ((defstmt.toString().contains("return 1")) ||
            // (defstmt.toString().contains("return 0") &&
            // defstmt.getJavaSourceStartLineNumber() != 59)) {
            // continue;
            // }
            unresolvedNodes.add(def.getDefinedLocation());
        }

        // unresolvedCVs.add(cvalue);

        // int provCount = 0;
        Set<TracePoint> tps = new HashSet<>();
        start = System.currentTimeMillis();
        while (!unresolvedNodes.isEmpty()) {
            IPNode node = unresolvedNodes.iterator().next();

            unresolvedNodes.remove(node);
            if (visitedNodes.contains(node)) {
                continue;
            }
            visitedNodes.add(node);
            Stmt stmt = node.getStmt();
            App.p("\nBacktracking at " + node);

            MethodInfo minfo = null;
            // if (node instanceof ExitNode) {
            // List<CallSite> cs = node.getContext().getCtrace();
            // minfo = cs.get(cs.size() - 2).getMinfo();
            // } else {
            minfo = node.getContext().getStackLast();
            // }
            // p(node.getContext() + ", " + stmt);
            // minfo.cfg.getBody().
            for (Stmt cfstmt : minfo.cfDependency.get(stmt)) {
                // Context context = null;
                // if()
                IPNode cfnode = igraph.getIPNode(node.getContext(), cfstmt);
                unresolvedNodes.add(cfnode);
            }
            CallSite csite = node.getContext().getLastCallSite();
            // App.p("???? " + csite.getCallingStmt());
            // App.p("???? " + csite.minfo.sm);
            if (csite.getCallingStmt() != null) {
                for (Stmt cfstmt : node.getContext().getStackSecondToLast().cfDependency
                        .get(csite.getCallingStmt())) {
                    IPNode cfnode = igraph.getIPNode(node.getContext().popLast(), cfstmt);
                    // App.p("!!!! " + cfnode + ", " + node.getContext().popLast() + ", " +
                    // csite.getCallingStmt());
                    unresolvedNodes.add(cfnode);
                }
            }

            for (ContextSensitiveValue cv : node.getUsed()) {
                Set<Definition> satisfiedDefs = fia.getBefore(node).getDefinitionsByCV(cv);
                if (Banned.isTypeBanned(cv.getValue().getType().toString())) {
                    p("Not tracking banned type: " + cv.getValue().getType());
                    continue;
                }
                if (!cv.getValue().toString().equals("null")) {
                    App.p("Potential Provenance value: " + cv);
                    // ContextSensitiveValue trasnformedCV = ne
                    if (cv.getValue() instanceof JInstanceFieldRef) {
                        JInstanceFieldRef cvref = ((JInstanceFieldRef) cv.getValue());
                        Value vbase = cvref.getBase();
                        List<SootFieldRef> suf = Collections.singletonList(cvref.getFieldRef());
                        tps.add(new TracePoint(node.getStmt(), vbase, node.getMethodInfo().sm, suf));
                    } else {
                        tps.add(new TracePoint(node.getStmt(), cv.getValue(), node.getMethodInfo().sm));
                    }
                }
                // App.p(cv.getValue().getClass());

                boolean unresolved = true;
                List<Value> constants = new ArrayList<>();

                Set<Definition> unresolves = new HashSet<>();
                for (Definition satdef : satisfiedDefs) {
                    if (satdef.getDefinedLocation() != null) {
                        // unresolved = false;
                        unresolvedNodes.add(satdef.getDefinedLocation());
                        if (satdef.d().toString().contains("login")) {
                            App.p("Solved def for " + cv + " :  " + satdef.d());
                        }
                    } else {
                        if (satdef.d().toString().contains("login")) {
                            App.p("UnSolved def for " + cv + " :  " + satdef.d());
                        }
                        unresolves.add(satdef);
                        // Value defval = satdef.getDefinedValue().getBase().getValue();
                        // if ((defval instanceof Constant) && (defval instanceof NullConstant)) {
                        // constants.add(defval);
                        // }
                    }
                }

                if (!unresolves.isEmpty()) {

                    // App.p(">>>>>\nUnresolved " + cv + " with:");

                    for (Definition satdef : unresolves) {
                        App.p(">>>>>Unresolved " + cv + " with " + satdef);
                        Value defVal = satdef.getDefinedValue().getBase().getValue();
                        if ((defVal instanceof Constant) && !(defVal instanceof NullConstant)) {
                            p("Not tracking constant: " + cv + " with constant value " + defVal);
                            continue;
                        }
                        App.p(satdef.getDefinedValue());
                        UniqueName cvun = satdef.getDefinedValue();
                        // if (!cvun.getBase().toString().contains("null")) {
                        App.p("Try resolving base:");
                        ContextSensitiveValue cvbase = cvun.getBase();
                        Set<Definition> resbasedefs = fia.getBefore(node).getDefinitionsByCV(cvbase);
                        App.p(resbasedefs);

                        boolean alternative = false;
                        if (!resbasedefs.isEmpty()) {
                            for (Definition bdef : resbasedefs) {
                                App.p(bdef.getDefinedLocation());
                                if (bdef.getDefinedLocation() != null) {

                                    alternative = true;
                                    unresolvedNodes.add(bdef.getDefinedLocation());
                                    if (!bdef.getDefinedValue().getBase().getValue().toString().equals("null")) {
                                        App.p("New Provenance due to field: " + cvun + " at "
                                                + bdef.getDefinedLocation() + " with base " + bdef.getDefinedValue());
                                        tps.add(new TracePoint(bdef.getDefinedLocation().getStmt(),
                                                bdef.getDefinedValue().getBase().getValue(),
                                                bdef.getDefinedLocation().getMethodInfo().sm, cvun.getSuffix()));
                                    }
                                }
                            }
                        }
                        if (!alternative) {
                            App.p("Can't resolve " + cvun + " at all");
                        }
                        // }
                    }
                    // App.p("<<<<<\n");

                }

            }

        }

        long backtrackDuration = System.currentTimeMillis()
                - start;
        p2("Analysis time: " + analysisDuration + ",  backtrack time: " + backtrackDuration);
        p2("#TPs: " + tps.size());
        // for (TracePoint tp : tps) {
        // System.err.println(tp.sm + ": " + tp.s + ", " + tp.v + "." + tp.suffix);
        // }

        // saveObj(tps, "TP", "tps");
        writeTPs(tps, "TP", "tps");
    }

    public static void readTPs(String path, String name) {
        File file = new File(path + File.separator + name);
        try {
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                // System.out.println(data);

                String[] fields = data.split(",{2}");
                String serviceName = fields[0];
                String methodName = fields[1];
                SootMethod sm = methodMap.get(methodName).sm;
                int lineNum = Integer.parseInt(fields[2]);
                String stmtStr = fields[3];
                Stmt stmt = CompileUtils.searchStmt(sm.getActiveBody(), stmtStr, lineNum);
                String baseStr = fields[4];
                Value base = CompileUtils.findLocal(stmt, baseStr);
                if (base.getType().toString().contains("List")) {
                    continue;
                }
                String suffix = fields[5];
                suffix = suffix.substring(1, suffix.length() - 1);
                // App.p(suffix.length());
                String[] refs = suffix.split(",");

                // p(sm);
                // String stmt =
                // "".concat(baseStr)
                Value curr = base;
                // for (String field : refs) {
                // field = field.strip();
                // }

                for (String field : refs) {
                    p2(curr);
                    if (field.isEmpty())
                        continue;
                    p2(curr.getType());
                    String actual = field.strip();
                    p2(actual);

                    // if(curr.getType() != )
                    SootClass sc = classMap.get(curr.getType().toString());
                    // Scene.v().getSootClass();
                    App.p(sc);
                    // App.p(sc.getFieldByName(field));
                    SootField sf = null;
                    for (SootField f : sc.getFields()) {
                        // App.p(f);
                        if (f.getName().contains(actual)) {
                            sf = f;
                            break;
                        }
                        // App.p(f);
                    }
                    // sf.getSub
                    App.p(sf.getDeclaringClass());
                    // boolean
                    SootMethod getter = null;
                    if (sf.isPrivate()) {

                        for (SootMethod method : sc.getMethods()) {
                            String fname = sf.getName();
                            String prefix = sf.getType().toString().equals("boolean") ? "is" : "get";
                            String cand = prefix + fname.substring(0, 1).toUpperCase() + fname.substring(1);
                            if (method.getName().equals(cand)) {
                                getter = method;
                                break;

                            }
                        }
                        App.p(getter);
                    }
                    Local actualBase = null;
                    if (!(curr instanceof Local)) {
                        Local tmp = Jimple.v().newLocal("tp" + field, RefType.v(curr.getType().toString()));
                        Stmt st = Jimple.v().newAssignStmt(tmp, curr);
                        App.p(st);
                        actualBase = tmp;
                    } else {
                        actualBase = (Local) curr;
                    }
                    if (sf.isPrivate()) {
                        Local tmp = Jimple.v().newLocal("tpget" + field, RefType.v(sf.getType().toString()));
                        Stmt st = Jimple.v().newAssignStmt(tmp,
                                Jimple.v().newVirtualInvokeExpr(actualBase, getter.makeRef()));
                        App.p(st);
                        curr = tmp;
                    } else {
                        curr = Jimple.v().newInstanceFieldRef(actualBase, sf.makeRef());
                    }
                }
                p2("---");
            }
            myReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeTPs(Set<TracePoint> tps, String path, String name) {
        File outputDir = new File(path);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        File file = new File(outputDir + File.separator + name);
        try {
            // FileOutputStream fileOut = new FileOutputStream(file);
            FileWriter fileWriter = new FileWriter(file);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            for (TracePoint tp : tps) {
                printWriter.println(tp.d(",,"));
            }
            // ObjectOutputStream out = new ObjectOutputStream(fileOut);
            // out.writeObject(obj);
            // out.close();
            printWriter.close();
            // fileOut.close();
            System.out.printf("TP data is saved in " + file.getAbsolutePath());
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public static Object readObj(String path, String name) {
        File file = new File(path + File.separator + name);
        try {
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            Object o = in.readObject();
            in.close();
            fileIn.close();
            return o;
        } catch (IOException i) {
            i.printStackTrace();
            // return null;
        } catch (ClassNotFoundException c) {
            System.out.println("Employee class not found");
            c.printStackTrace();
            // return null;
        }
        return null;
    }

    public static void saveObj(Object obj, String path, String name) {
        File outputDir = new File(path);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        File file = new File(outputDir + File.separator + name);
        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(obj);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in " + file.getAbsolutePath());
        } catch (IOException i) {
            i.printStackTrace();
        }
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
        Options.v().set_java_version(8);
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
        Options.v().setPhaseOption("jb", "preserve-source-annotations:true");
        // Options.v().setPhaseOption("jb.ls", "enabled:false");
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

    public static void analyzePath(String path, String serviceName) {
        // Options.v().set
        p("Analyzing " + path);
        setupSoot(path);
        for (SootClass cls : Scene.v().getApplicationClasses()) {
            // Scene.v().forceResolve(cls.getName(), SootClass.BODIES);
            if (cls.toString().contains("conf.HttpAspect")) {
                continue;
            }

            for (SootMethod sm : cls.getMethods()) {
                if (sm.isAbstract()) {
                    continue;
                }

                sm.retrieveActiveBody();

                MethodInfo minfo = new MethodInfo(sm);
                // Map<TracePoint, List<TracePoint>> depGMap = minfo.analyzeDef();
                minfo.buildPostDominanceFrontier();
                methodMap.put(sm.getSignature(), minfo);
                CompileUtils.bodyMap.put(sm.getSignature(), ((Body) sm.getActiveBody().clone()));
                serviceMap.put(sm.toString(), serviceName);
                // if (sm.toString().contains("cancelOrder")) {
                // App.p("!!! " + sm);
                // }
            }
            classMap.put(cls.toString(), cls);
            if (compileJimple) {
                CompileUtils.outputJimple(cls, "joutput");
            }
            // PackManager.v().
            // PackManager.v().runPacks();

            // CompileUtils.outputJimple(cls, "WFH");
            // App.p("!!!!!!!!!!!!");
            // if (searchMethod("sendInsidePayment") != null) {

            //
            // CompileUtils.outputJimple(searchMethod("sendInsidePayment").sm.getDeclaringClass(),
            // path);
            // }
        }

        // CompileUtils.outputJimple(searchMethod("sendInsidePayment").sm.getDeclaringClass(),
        // path);
        // PackManager.v().runPacks();
        // PackManager.v().writeOutput();
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

    public static void p2(Object s) {
        System.out.println(s);
    }

}
