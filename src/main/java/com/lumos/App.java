package com.lumos;

import java.nio.file.Paths;
import java.sql.Ref;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.swing.text.AbstractDocument.Content;

import org.glassfish.jaxb.runtime.v2.runtime.reflect.Lister.Pack;
import org.glassfish.jaxb.runtime.v2.schemagen.Util;

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
import com.lumos.forward.IdentityNode;
import com.lumos.forward.InterProcedureGraph;
import com.lumos.forward.StmtNode;
import com.lumos.forward.TracePoint;
import com.lumos.forward.UniqueName;
import com.lumos.utils.Utils;
import com.lumos.wire.Banned;
import com.lumos.wire.HTTPReceiveWirePoint;
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
import soot.jimple.InstanceInvokeExpr;
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
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JReturnVoidStmt;
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

    public static boolean compileJimpleOnly = false;
    public static boolean compileClass = false;

    public static boolean showRound = false;
    public static boolean showLineNum = true;
    public static boolean showIDNodesOnly = false;
    public static boolean showInitOnly = false;

    public static Map<String, MethodInfo> methodMap;

    public static Map<String, SootClass> classMap = new HashMap<>();

    public static Map<String, String> serviceMap = new HashMap<>();
    public static Map<String, String> pathMap = new HashMap<>();
    public static String fileSeparator = ":";
    // public static String exclude = "goto [?= $stack66 = $stack62 & $stack68]";

    public static Set<IPNode> idnodes = new HashSet<>();

    public static String caseStudyPath = "cases/f13/";
    public static String safeListPath = "safelist";
    public static Set<String> safeList = new HashSet<>();

    public static Set<String> initList = new HashSet<>();

    public static String outputTPDir = "TP";
    public static String outputTPFileName = "tps1";
    public static boolean outputTP = false;

    public static void main(String[] args) {
        String[] services = new String[] {
                "ts-launcher",
                "ts-inside-payment-service",
                "ts-order-other-service",
                "ts-order-service",
                "ts-payment-service",
                "ts-cancel-service",
                "ts-sso-service",
                // "ts-admin-basic-info-service",
                // "ts-admin-order-service",
                // "ts-admin-route-service",
                // "ts-admin-travel-service",
                // "ts-admin-user-service",
                // "ts-assurance-service",
                // "ts-basic-service",
                // "ts-config-service",
                // "ts-consign-price-service",
                // "ts-consign-service",
                // "ts-contacts-service",
                // "ts-execute-service",
                // "ts-food-map-service",
                // "ts-food-service",
                // "ts-login-service",
                // "ts-notification-service",
                // "ts-preserve-other-service",
                // "ts-preserve-service",
                // "ts-price-service",
                // "ts-rebook-service",
                // "ts-register-service",
                // "ts-route-plan-service",
                // "ts-route-service",
                // "ts-seat-service",
                // "ts-security-service",
                // "ts-station-service",
                // "ts-ticketinfo-service",
                // "ts-train-service",
                // "ts-travel-plan-service",
                // "ts-travel-service",
                // "ts-travel2-service",
                // "ts-verification-code-service"
        };
        methodMap = new HashMap<>();

        String base = "C:\\Users\\jchen\\Desktop\\Academic\\lumos\\lumos-experiment\\";
        String suffix = "\\target\\classes";
        List<String> paths = new ArrayList<>();
        for (String str : services) {
            String complete = base + str + suffix;
            // String complete = "xxxx";
            pathMap.put(str, complete);
            paths.add(complete);
        }
        analyzePath(pathMap);
        // analyzePath("xxxx\\classes");

        if (compileJimpleOnly)
            return;

        // readTPs("TP", "tps");

        Utils.readFrom(safeListPath).forEach(s -> {
            safeList.add(s);
            // App.p("!!! " + s);
        });

        InterProcedureGraph igraph = new InterProcedureGraph(methodMap);
        // getDB("save", "OrderRepository");
        // App.p(".................................");
        // getDB("save", "OrderOtherRepository");
        // App.p(".................................");
        // getDB("save", "AddMoneyRepository");
        // App.p(".................................");
        // getDB("save", "PaymentRepository");
        // App.p(".................................");
        // getDB("save", "LoginUserListRepository");

        // if (true) {
        // return;
        // }

        List<String> graphConfig = Utils.readFrom(caseStudyPath + "graph");
        String entryMethod = graphConfig.get(0).trim();
        String firstStmt = graphConfig.get(1);
        String symptomStmt = graphConfig.get(2);

        ContextSensitiveInfo cinfo = igraph.build(entryMethod);

        long start = System.currentTimeMillis();
        IPNode firstNode = igraph.searchNode(firstStmt.split(","));
        ForwardIPAnalysis fia = new ForwardIPAnalysis(igraph, firstNode);
        // App.p(cinfo.getFirstNode().context.getStackLast().sm.);
        long analysisDuration = System.currentTimeMillis() - start;

        if (showInitOnly) {
            p("+++++++++++++++++++++");
            initList.forEach(m -> {
                p(m);
            });
            return;
        }

        if (showIDNodesOnly) {
            p("+++++++++++++++++++++");
            Set<String> safeMethods = new HashSet<>();
            for (IPNode node : idnodes) {
                // p(node.getStmt().getInvokeExpr().getMethod());
                safeMethods.add(node.getStmt().getInvokeExpr().getMethod().getSignature());
            }
            safeMethods.forEach(m -> {
                p(m);
            });
            return;
        }

        IPNode ipnode = igraph.searchNode(symptomStmt.split(","));
        p(ipnode.stmt.toString());

        ContextSensitiveValue cvalue = ContextSensitiveValue.getCValue(ipnode.getContext(),
                ((JAssignStmt) ipnode.getStmt()).getLeftOp());

        p2("Analysis time: " + analysisDuration);
        Set<TracePoint> tps = getDependency(igraph, fia, ipnode, cvalue);
        p2("#TPs: " + tps.size());
        if (outputTP) {
            writeTPs(tps, outputTPDir, outputTPFileName);
        }
    }

    public static void getDB(String... strs) {
        // List<Stmt>
        for (String method : methodMap.keySet()) {
            MethodInfo minfo = methodMap.get(method);
            for (Unit unit : minfo.sm.getActiveBody().getUnits()) {
                Stmt stmt = (Stmt) unit;
                if (stmt.containsInvokeExpr()) {
                    InvokeExpr iexpr = stmt.getInvokeExpr();
                    if (iexpr instanceof InstanceInvokeExpr) {
                        InstanceInvokeExpr inexpr = (InstanceInvokeExpr) iexpr;
                        boolean matched = true;
                        for (String s : strs) {
                            if (!inexpr.toString().contains(s)) {
                                matched = false;
                                break;
                            }
                        }
                        if (matched) {
                            p(method + ":  " + stmt);
                        }
                    }
                }

            }
        }
    }

    public static Set<TracePoint> getDependency(InterProcedureGraph igraph, ForwardIPAnalysis fia, IPNode ipnode,
            ContextSensitiveValue cvalue) {

        IPFlowInfo cmap = fia.getAfter(ipnode);

        Set<IPNode> unresolvedNodes = new HashSet<>();
        Set<ContextSensitiveValue> visitedCVs = new HashSet<>();
        Set<IPNode> visitedNodes = new HashSet<>();

        // Set<Definition> satisfiedDefs = new HashSet<>();
        for (Definition def : cmap.getDefinitionsByCV(cvalue)) {
            App.p(def.d());
            // Stmt defstmt = def.getDefinedLocation().getStmt();
            unresolvedNodes.add(def.getDefinedLocation());
        }

        Set<TracePoint> tps = new HashSet<>();
        long start = System.currentTimeMillis();
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
            minfo = node.getContext().getStackLast();
            for (Stmt cfstmt : minfo.cfDependency.get(stmt)) {
                IPNode cfnode = igraph.getIPNode(node.getContext(), cfstmt);
                unresolvedNodes.add(cfnode);
            }
            CallSite csite = node.getContext().getLastCallSite();
            if (csite.getCallingStmt() != null) {
                for (Stmt cfstmt : node.getContext().getStackSecondToLast().cfDependency
                        .get(csite.getCallingStmt())) {
                    IPNode cfnode = igraph.getIPNode(node.getContext().popLast(), cfstmt);
                    App.p("Adding control dep: " + cfnode + " for " + node);
                    unresolvedNodes.add(cfnode);
                }
            }

            for (ContextSensitiveValue cv : node.getUsed()) {
                if (Banned.isTypeBanned(cv.getValue().getType().toString())) {
                    p("Not tracking banned type: " + cv.getValue().getType());
                    continue;
                }

                boolean isConstant = false;
                boolean isOnlyReturn = false;
                Value constval = null;
                if (cv.getValue() instanceof Constant) {
                    // App.p(" !! ! " + cv);
                    isConstant = true;
                    constval = cv.getValue();
                }
                Set<Definition> satisfiedDefs = null;
                if (!isConstant) {
                    satisfiedDefs = fia.getBefore(node).getDefinitionsByCV(cv);
                    if (satisfiedDefs.size() == 1) {
                        Definition onlyDef = satisfiedDefs.iterator().next();
                        if ((onlyDef.getDefinedValue().getBase().getValue()) instanceof Constant) {
                            isConstant = true;
                            constval = onlyDef.getDefinedValue().getBase().getValue();
                        }
                        // if (onlyDef.getDefinedLocation().getStmt() instanceof JReturnStmt) {
                        // isOnlyReturn = true;
                        // }
                    }
                }
                if (isConstant) {
                    p("Not tracking value that is known as a constant! " + cv + " as const " + constval);
                    continue;
                }
                if (!cv.getValue().toString().equals("null")) {
                    if (node.isSingleAssign()) {
                        // if (node instanceof IdentityNode) {
                        // App.idnodes.add(node);
                        // }
                        App.p("Not tracing uses of a identity assignment node: " + node);
                    } else {
                        TracePoint provTP = null;
                        if (cv.getValue() instanceof JInstanceFieldRef) {
                            JInstanceFieldRef cvref = ((JInstanceFieldRef) cv.getValue());
                            Value vbase = cvref.getBase();
                            List<SootFieldRef> suf = Collections.singletonList(cvref.getFieldRef());
                            provTP = new TracePoint(node.getStmt(), vbase, node.getMethodInfo().sm, suf);
                        } else {
                            provTP = new TracePoint(node.getStmt(), cv.getValue(), node.getMethodInfo().sm);
                        }
                        App.p("Potential Provenance value: " + cv);
                        tps.add(provTP);
                    }
                }

                // boolean unresolved = true;
                // List<Value> constants = new ArrayList<>();
                // if (isOnlyReturn) {

                // }
                Set<Definition> unresolves = new HashSet<>();

                for (Definition satdef : satisfiedDefs) {
                    if (satdef.getDefinedLocation() != null) {
                        // unresolved = false;
                        unresolvedNodes.add(satdef.getDefinedLocation());
                        App.p("Bcktrcking " + cv + " at " + satdef.d());
                    } else {
                        unresolves.add(satdef);
                    }
                }

                if (!unresolves.isEmpty()) {
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
                    }

                }

            }

        }

        long backtrackDuration = System.currentTimeMillis()
                - start;
        p2("Backtrack time: " + backtrackDuration);
        return tps;
    }

    public static void readTPs(String path, String name) {
        File file = new File(path + File.separator + name);

        Map<String, Set<String>> smap = new HashMap<>();
        Map<String, List<String>> mmap = new HashMap<>();
        // Map<String, List<String>> mmap = new HashMap<>();
        try {
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                // System.out.println(data);

                String[] fields = data.split(",{2}");
                String serviceName = fields[0];
                String methodName = fields[1];
                if (!smap.containsKey(serviceName)) {
                    smap.put(serviceName, new HashSet<>());
                }
                smap.get(serviceName).add(methodName);
                if (!mmap.containsKey(methodName)) {
                    mmap.put(methodName, new ArrayList<>());
                }
                mmap.get(methodName).add(data);

            }
            myReader.close();

            for (String service : smap.keySet()) {
                // if (!service.equals("ts-launcher"))
                // continue;
                setupSoot(pathMap.values(), Collections.singletonList(pathMap.get(service)));
                setupClass(service);
                // analyzePath(pathMap);
                Set<SootClass> tocompile = new HashSet<>();

                for (String methodName : smap.get(service)) {
                    // String[] fields = data.split(",{2}");
                    // String serviceName = fields[0];
                    // String methodName = fields[1];

                    SootMethod sm = methodMap.get(methodName).sm;
                    Body usedBody = CompileUtils.bodyMap.get(sm.getSignature());
                    PatchingChain<Unit> units = usedBody.getUnits();
                    // App.p(sm.getDeclaringClass());
                    // if (!sm.getDeclaringClass().toString().contains("LoginResult")) {
                    // continue;
                    // }
                    List<Stmt> tostmt = new ArrayList<>();
                    for (String mdata : mmap.get(methodName)) {
                        String[] mfields = mdata.split(",{2}");
                        int lineNum = Integer.parseInt(mfields[2]);
                        String stmtStr = mfields[3];
                        String baseStr = mfields[4];
                        String suffix = mfields[5];
                        Stmt stmt = CompileUtils.searchStmt(usedBody, stmtStr, lineNum);
                        tostmt.add(stmt);
                    }
                    Iterator<Stmt> iter = tostmt.iterator();
                    for (String mdata : mmap.get(methodName)) {
                        // SootMethod sm = Scene.v().getMethod(methodName);
                        String[] mfields = mdata.split(",{2}");
                        int lineNum = Integer.parseInt(mfields[2]);
                        String stmtStr = mfields[3];
                        String baseStr = mfields[4];
                        String suffix = mfields[5];

                        // Stmt stmt = CompileUtils.searchStmt(usedBody, stmtStr, lineNum);

                        Stmt stmt = iter.next();

                        // for (Unit uu : usedBody.getUnits()) {
                        // if (stmt.toString().contains("if $stack72")) {
                        // App.p(stmt);
                        // App.p(((JIfStmt) stmt).getTarget());
                        // // App.panicni();
                        // }
                        // }
                        // App.p(stmt + "\n" + mdata);
                        Value base = CompileUtils.findLocal(stmt, baseStr);
                        if (base.getType().toString().contains("List")) {
                            continue;
                        }
                        // stmt.get

                        suffix = suffix.substring(1, suffix.length() - 1);

                        List<String> refs = Arrays.asList(suffix.split(",")).stream().filter(x -> !x.isEmpty())
                                .collect(Collectors.toList());
                        // App.p(base + ", " + stmt + ", " + methodName);
                        List<Stmt> stmts = CompileUtils.generateTPStmts(usedBody, base, refs, stmt);
                        // for (Stmt stm : stmts) {
                        // App.p(stm);
                        // }

                        boolean isBefore = false;
                        if (stmt instanceof JIfStmt || stmt instanceof JReturnStmt || stmt instanceof JReturnVoidStmt ||
                                stmt instanceof JGotoStmt) {
                            isBefore = true;
                        }

                        // if (service.equals("ts-launcher")) {
                        CompileUtils.insertAt(units, stmt, stmts, isBefore);

                        // }
                        p2("---");
                        // break;
                    }
                    sm.setActiveBody(usedBody);
                    usedBody.validate();
                    tocompile.add(sm.getDeclaringClass());
                }

                for (SootClass scc : tocompile) {
                    // if (scc.toString().contains("CancelOrderInfo")) {
                    SootClass actual = classMap.get(scc.toString());
                    // if (service.toString().contains("ts-launcher"))
                    // if (scc.toString().contains("InsidePaymentServiceImpl"))
                    CompileUtils.outputJimple(actual, "testl");
                    // }
                }
            }

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

    public static void setupSoot(Collection<String> cpath, List<String> pdir) {
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

        String classpath = "";
        // classpath +=
        // "C:\\Users\\jchen\\Desktop\\Academic\\lumos\\lumos-experiment\\ts-launcher\\opentelemetry-javaagent.jar"
        // + File.pathSeparator;
        for (String cp : cpath) {
            classpath += cp + File.pathSeparator;
        }
        if (classpath.charAt(classpath.length() - 1) == File.pathSeparatorChar) {
            classpath = classpath.substring(0, classpath.length() - 1);
        }
        Options.v().set_soot_classpath(classpath);
        Options.v().set_java_version(8);
        processList = new ArrayList<String>();

        Options.v().set_process_dir(pdir);

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
        Options.v().setPhaseOption("jb", "stabilize-local-names:true");
        // Options.v().setPhaseOption("jb.ls", "enabled:false");
        // Need this to avoid the need to provide an entry point
        Options.v().setPhaseOption("cg", "all-reachable:true");

        // Need this to include all subtypes
        // Options.v().setPhaseOption("cg", "library:any-subtype");

        Scene.v().loadNecessaryClasses();
        Scene.v().addBasicClass("java.io.PrintStream", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.String", SootClass.SIGNATURES);
        // Scene.v().addBasicClass("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span",
        // SootClass.SIGNATURES);
        Scene.v().loadNecessaryClasses();
        // Scene.v().loadClassAndSupport();

    }

    public static void analyzePath(Map<String, String> pmap) {
        // Options.v().set

        for (String service : pmap.keySet()) {
            p("Analyzing: " + service);
            // p(service + ": " + pmap.get(service));
            // }
            setupSoot(pmap.values(), Collections.singletonList(pmap.get(service)));
            setupClass(service);
            // PackManager.v().runPacks();
            // PackManager.v().writeOutput();
            p("----------");

        }
    }

    public static void setupClass(String service) {
        SootClass cls = null;
        for (Iterator<SootClass> iter = Scene.v().getApplicationClasses().snapshotIterator(); iter
                .hasNext();) {
            cls = iter.next();
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
                // CompileUtils.bodyMap.put(sm.getSignature(), ((Body)
                // sm.getActiveBody().clone()));
                serviceMap.put(sm.toString(), service);
                // if (sm.toString().contains("cancelOrder")) {
                // App.p("!!! " + sm);
                // }
            }
            classMap.put(cls.toString(), cls);
            if (compileJimpleOnly) {
                CompileUtils.outputJimple(cls, "joutput");
            }

        }

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

    public static List<Value> parameters(SootMethod wsm) {
        List<Value> methodParams = new ArrayList<>();
        for (Value v : wsm.getActiveBody().getParameterLocals()) {
            methodParams.add(v);
        }
        if (!wsm.isStatic()) {
            methodParams.add(0, wsm.getActiveBody().getThisLocal());
        }
        return methodParams;

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
