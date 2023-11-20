package com.lumos;

import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

// import com.google.protobuf.Option;

// import org.checkerframework.checker.units.qual.min;
// import org.eclipse.jdt.core.dom.CastExpression;
// import org.jf.dexlib2.analysis.ClassProvider;
// import org.objectweb.asm.commons.JSRInlinerAdapter;

import com.lumos.analysis.MethodInfo;
import com.lumos.analysis.ReachingDefAnalysis;
import com.lumos.backtracking.PendingBackTracking;
import com.lumos.backtracking.TracePoint;
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
import com.lumos.forward.UniqueName;
import com.lumos.forward.shared.SharedStateDepedency;
import com.lumos.forward.shared.SharedStateRead;
import com.lumos.forward.shared.SharedStateWrite;
import com.lumos.utils.Utils;
import com.lumos.wire.Banned;

import fj.P;
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
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
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
import soot.jimple.internal.JInterfaceInvokeExpr;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JReturnVoidStmt;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.options.Options;
import soot.tagkit.AnnotationArrayElem;
import soot.tagkit.AnnotationElem;
import soot.tagkit.AnnotationStringElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.VisibilityAnnotationTag;
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
    public static boolean analyzeControllerOnly = false;
    public static boolean analyzeRepoOnly = false;

    public static Map<String, MethodInfo> methodMap;

    public static Map<String, SootClass> classMap = new HashMap<>();

    public static Map<String, String> serviceMap = new HashMap<>();
    public static Map<String, String> pathMap = new HashMap<>();

    public static Map<String, String> srcMap = new HashMap<>();
    public static String fileSeparator = ":";
    // public static String exclude = "goto [?= $stack66 = $stack62 & $stack68]";

    public static Set<IPNode> idnodes = new HashSet<>();

    public static String caseStudyPath = "cases/f13/";
    public static String safeListPath = "safelist";
    public static Set<String> safeList = new HashSet<>();

    public static Set<String> initList = new HashSet<>();

    public static String outputTPDir = "TPnew";
    public static String outputTPFileName = "tps13";
    public static boolean outputTP = true;

    public static String base = "C:\\Users\\jchen\\Desktop\\Academic\\lumos\\f13\\";
    public static String bcodeSuffix = "\\target\\classes";
    public static String scodeSuffix = "\\src\\main\\java";
    public static Map<String, Path> sourceMap = new HashMap<>();

    public static Map<String, SootMethod> remoteMap = new HashMap<>();

    public static String[] services = new String[] {
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
            "ts-config-service",
            // "ts-consign-price-service",
            // "ts-consign-service",
            // "ts-contacts-service",
            // "ts-execute-service",
            // "ts-food-map-service",
            // "ts-food-service",
            "ts-login-service",
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
            "ts-verification-code-service"
    };

    public static void main(String[] args) {

        methodMap = new HashMap<>();

        List<String> paths = new ArrayList<>();
        for (String str : services) {
            String complete = base + str + bcodeSuffix;
            // String complete = "xxxx";
            pathMap.put(str, complete);
            paths.add(complete);
        }

        analyzePath(pathMap);
        App.p("Class file analysis finished");

        // analyzePath("xxxx\\classes");
        getSourceCodes();
        App.p("Source code analysis finished");
        // if (1 - 2 < 0) {
        // getSourceCodes();
        // for (String s : methodMap.keySet()) {
        // p("! " + s + ", " + methodMap.get(s).sm.getDeclaringClass());
        // }
        // return;
        // }

        if (compileJimpleOnly)
            return;

        if (analyzeRepoOnly) {
            p("Analyzing Repo");
            // analyzeRepo();
            // analyzeDBFlow();
            analyzeRedisFlow();
            return;
        }

        // analyzeSend();
        analyzeController();
        App.p("Analyze controller finished");
        if (analyzeControllerOnly) {
            return;
        }

        // readTPs("TP", "tps");

        // Safelist contains a list of lib methods
        // that don't modify >1 args, and obeying
        // the basic template of R/Ws
        Utils.readFrom(safeListPath).forEach(s -> {
            safeList.add(s);
        });

        InterProcedureGraph igraph = new InterProcedureGraph(methodMap);

        // if (true) {
        // return;
        // }

        List<String> graphConfig = Utils.readFrom(caseStudyPath + "graph");
        String entryMethod = graphConfig.get(0).trim();
        String firstStmt = graphConfig.get(1);
        String symptomStmt = graphConfig.get(2);

        App.p("Starting building graph");
        ContextSensitiveInfo cinfo = igraph.build(entryMethod);
        App.p("Build graph finished");

        long start = System.currentTimeMillis();
        IPNode firstNode = igraph.searchNode(firstStmt.split(","));
        App.p("Starting node is: " + firstNode);
        App.p("Analysis begins");
        ForwardIPAnalysis fia = new ForwardIPAnalysis(igraph, firstNode);
        App.p("Analysis finished");

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
        p("Symptom node is: " + ipnode.stmt.toString());

        List<ContextSensitiveValue> symptomCvalues = new ArrayList<>();
        // f13
        ContextSensitiveValue cvalue = ContextSensitiveValue.getCValue(ipnode.getContext(),
                ((JAssignStmt) ipnode.getStmt()).getLeftOp());

        // f8
        // Value symptom = getFieldRef(((JReturnStmt) ipnode.getStmt()).getOp(),
        // "refund");
        // ContextSensitiveValue cvalue =
        // ContextSensitiveValue.getCValue(ipnode.getContext(), symptom);

        // f8login
        // ContextSensitiveValue cvalue =
        // ContextSensitiveValue.getCValue(ipnode.getContext(),
        // ipnode.getStmt().getInvokeExpr().getArg(0));

        symptomCvalues.add(cvalue);
        p("Symptom values are: " + cvalue);
        p2("Analysis time: " + analysisDuration);

        // if (true)
        // return;
        // List<IPNode> DBreads = new ArrayList<>();
        // List<String> fields = new ArrayList<>();
        Set<SharedStateRead> streads = new HashSet<>();
        Set<TracePoint> tps = getDependency(igraph, fia, Collections.singletonList(ipnode),
                Collections.singletonList(symptomCvalues), streads);

        Set<SharedStateDepedency> stdeps = new HashSet<>();
        streads.forEach(stread -> {
            // App.p(stread);
            // App.p(stread.hashCode());
            // App.p(stread.type.hashCode() + ", " + stread.rnode.hashCode() + ", " +
            // stread.cvalue.hashCode() + ","
            // + stread.refs.hashCode());
            String storeName = "";
            IPNode rnode = stread.rnode;
            InvokeExpr iexpr = rnode.getStmt().getInvokeExpr();
            if (iexpr instanceof InstanceInvokeExpr) {
                storeName = ((InstanceInvokeExpr) iexpr).getBase().getType().toString();
            }
            stdeps.add(new SharedStateDepedency(storeName, stread.refs));
        });

        stdeps.forEach(stdep -> {
            // p(stdep);
        });
        Set<SharedStateWrite> saveNodes = getSaveNodes(igraph, fia, stdeps);
        List<IPNode> swnodes = new ArrayList<>();
        List<List<ContextSensitiveValue>> swcvalues = new ArrayList<>();
        p("Now tracking shared state write dependencies");
        for (SharedStateWrite sw : saveNodes) {
            // App.p(sw.wnode.getStmt().getInvokeExpr().getArgs());

            swnodes.add(sw.wnode);
            List<ContextSensitiveValue> currCvalues = new ArrayList<>();
            // p(getRepoName(sw.wnode));
            if (getBaseType(sw.wnode).contains("Repository")) {
                for (Value arg : sw.wnode.getStmt().getInvokeExpr().getArgs()) {
                    // p(arg);
                    for (List<String> refs : sw.fields) {
                        Value crvalue = getFieldRef(arg, refs);
                        // p(crvalue);
                        currCvalues.add(ContextSensitiveValue.getCValue(sw.wnode.getContext(), crvalue));
                    }
                    p(sw.wnode + ",  " + currCvalues);
                }
            }
            swcvalues.add(currCvalues);
        }
        Set<SharedStateRead> streads2 = new HashSet<>();
        Set<TracePoint> tpsw = getDependency(igraph, fia, swnodes, swcvalues, streads2);

        p2("#TPs: " + tps.size());

        p2("#TPs SW: " + tpsw.size());

        tpsw.removeAll(tps);
        p2("#TPs SW (additional): " + tpsw.size());
        tpsw.forEach(s -> {
            p(s.d());
        });
        tps.addAll(tpsw);
        if (outputTP) {
            writeTPs(tps, outputTPDir, outputTPFileName);
            writeSTReads(streads, outputTPDir, outputTPFileName);
            writeSTWrites(saveNodes, outputTPDir, outputTPFileName);
        }
    }

    public static String getBaseType(IPNode ipnode) {
        if (!ipnode.stmt.containsInvokeExpr()) {
            return null;
        }
        InvokeExpr iexpr = ipnode.stmt.getInvokeExpr();
        if (iexpr instanceof InstanceInvokeExpr) {
            return ((InstanceInvokeExpr) iexpr).getBase().getType().toString();
        }
        return null;
    }

    public static Set<TracePoint> getDependency(InterProcedureGraph igraph, ForwardIPAnalysis fia, List<IPNode> ipnodes,
            List<List<ContextSensitiveValue>> cvalues, Set<SharedStateRead> streads) {
        Set<PendingBackTracking> unresolvedNodes = new HashSet<>();
        Set<ContextSensitiveValue> visitedCVs = new HashSet<>();
        Set<PendingBackTracking> visitedNodes = new HashSet<>();

        for (int i = 0; i < ipnodes.size(); i++) {
            IPNode ipnode = ipnodes.get(i);
            IPFlowInfo cmap = fia.getAfter(ipnode);
            List<ContextSensitiveValue> currValues = cvalues.get(i);
            for (ContextSensitiveValue cvalue : currValues) {
                for (Definition def : cmap.getDefinitionsByCV(cvalue)) {
                    App.p(def.d());
                    // Stmt defstmt = def.getDefinedLocation().getStmt();
                    if (def.getDefinedLocation() != null) {
                        unresolvedNodes.add(new PendingBackTracking(def.getDefinedLocation(), "normal"));
                    } else {
                        handleUnresolvedDeps(def, cvalue, fia, ipnode, unresolvedNodes, streads);
                    }
                }
            }
        }

        Set<TracePoint> tps = new HashSet<>();
        long start = System.currentTimeMillis();
        while (!unresolvedNodes.isEmpty()) {
            PendingBackTracking ptrack = unresolvedNodes.iterator().next();
            IPNode node = ptrack.getNode();
            unresolvedNodes.remove(ptrack);
            if (visitedNodes.contains(ptrack)) {
                continue;
            }
            visitedNodes.add(ptrack);
            // p(node);
            Stmt stmt = node.getStmt();
            App.p("\nBacktracking at " + node);

            for (IPNode cfnode : node.getCfDepNodes()) {
                App.p("Adding control dep: " + cfnode + " for " + node);
                unresolvedNodes.add(new PendingBackTracking(cfnode, "normal"));
            }

            if (ptrack.getMode().equals("cfonly")) {
                p("Not continueing value deps for cfonly query: " + node);
                continue;
            }
            // else if (ptrack.getMode().equals("list")) {
            // p("Handling list definition");
            // if (!(node.getStmt() instanceof JAssignStmt)) {
            // if (node.getStmt().containsInvokeExpr()) {
            // if
            // (node.getStmt().getInvokeExpr().getMethod().getSignature().contains("add")) {
            // Value ele = node.getStmt().getInvokeExpr().getArg(0);

            // }
            // }
            // continue;
            // } else {
            // p("List def is assignment, treating as normal");
            // }
            // }

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
                    }
                }
                if (isConstant) {
                    p("Not tracking value that is known as a constant! " + cv + " as const " + constval);
                    continue;
                }
                if (cv.getValue() instanceof JInstanceFieldRef) {
                    p("Adding cf dep queries for base: " + cv);
                    Set<Definition> baseDefs = fia.getBefore(node).getDefinitionsByCV(getBaseCV(cv));
                    for (Definition def : baseDefs) {
                        if (def.getDefinedLocation() != null) {
                            unresolvedNodes.add(new PendingBackTracking(def.getDefinedLocation(), "cfonly"));
                        }
                    }
                }
                if (!cv.getValue().toString().equals("null")) {
                    if (node.isSingleAssign()) {
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
                Set<Definition> unresolves = new HashSet<>();

                for (Definition satdef : satisfiedDefs) {
                    if (satdef.getDefinedLocation() != null) {
                        // unresolved = false;
                        unresolvedNodes.add(new PendingBackTracking(satdef.getDefinedLocation(), "normal"));
                        checkSTread(satdef, streads, Collections.emptyList());
                        App.p("Added Next Backtracking " + cv + " at " + satdef.d());
                    } else {
                        unresolves.add(satdef);
                    }
                }

                if (!unresolves.isEmpty()) {
                    for (Definition satdef : unresolves) {
                        handleUnresolvedDeps(satdef, cv, fia, node, unresolvedNodes, streads);
                    }
                }

            }

        }

        long backtrackDuration = System.currentTimeMillis()
                - start;
        p2("Backtrack time: " + backtrackDuration);
        return tps;
    }

    public static ContextSensitiveValue getBaseCV(ContextSensitiveValue cv) {
        Value v = cv.getValue();
        if (v instanceof JInstanceFieldRef) {
            return ContextSensitiveValue.getCValue(cv.getContext(), ((JInstanceFieldRef) v).getBase());
        }
        return null;
    }

    public static Value getFieldRef(Value v, String name) {
        SootClass sc = searchClassExact(v.getType().toString());
        SootField sf = null;
        for (SootField f : sc.getFields()) {
            if (f.getName().equals(name)) {
                sf = f;
                break;
            }
        }
        if (sf == null) {
            System.out.println("Field not found!!");
            return null;
        }
        return Jimple.v().newInstanceFieldRef(v, sf.makeRef());
    }

    public static Value getFieldRef(Value v, List<String> names) {

        Value current = v;
        for (String name : names) {
            SootField sf = null;
            SootClass sc = searchClassExact(current.getType().toString());
            for (SootField f : sc.getFields()) {
                if (f.getName().equals(name)) {
                    sf = f;
                    break;
                }
            }
            if (sf == null) {
                System.out.println("Field not found!!");
                return null;
            }
            current = Jimple.v().newInstanceFieldRef(current, sf.makeRef());
        }
        return current;
    }

    public static Set<SharedStateWrite> getSaveNodes(InterProcedureGraph igraph, ForwardIPAnalysis fia,
            Set<SharedStateDepedency> stdeps) {
        Set<SharedStateWrite> result = new HashSet<>();
        for (IPNode ipnode : igraph.nodes) {
            // p(ipnode);
            Stmt stmt = ipnode.getStmt();
            if (stmt.containsInvokeExpr()) {
                InvokeExpr iexpr = stmt.getInvokeExpr();
                if (iexpr instanceof InstanceInvokeExpr) {
                    InstanceInvokeExpr inexpr = (InstanceInvokeExpr) iexpr;
                    SharedStateWrite swrite = new SharedStateWrite("Repository", ipnode, new HashSet<>());
                    for (SharedStateDepedency stdep : stdeps) {
                        String storeName = stdep.storeName;
                        String writeOPName = "";
                        if (storeName.contains("Repository")) {
                            writeOPName = "save";
                        } else if (storeName.contains("ValueOperations")) {
                            writeOPName = "set";
                        }
                        if (inexpr.getMethod().getName().equals(writeOPName)
                                && inexpr.getBase().getType().toString().contains(storeName)) {
                            // for( field: stdep.refs)
                            Value objVal = null;
                            boolean mayNotFromAnotherRepo = false;
                            IPFlowInfo cmap = fia.getAfter(ipnode);
                            for (Definition def : cmap.getDefinitionsByCV(
                                    ContextSensitiveValue.getCValue(ipnode.getContext(), inexpr.getArg(0)))) {
                                if (def.getDefinedLocation() != null) {
                                    if (def.getDefinedLocation().getStmt().containsInvokeExpr()) {
                                        if (!getBaseType(def.getDefinedLocation()).contains("Repository")) {
                                            mayNotFromAnotherRepo = true;
                                            break;
                                        }
                                    }
                                }
                            }

                            List<String> strRefs = stdep.refs;
                            if (strRefs.size() > 0) {
                                objVal = getFieldRef(inexpr.getArg(0), strRefs);
                            } else {
                                objVal = inexpr.getArg(0);
                            }
                            ContextSensitiveValue cvobj = ContextSensitiveValue.getCValue(ipnode.getContext(), objVal);
                            p("-----");
                            // p(method + ":");
                            p(ipnode);

                            boolean resolved = false;
                            for (Definition def : cmap.getDefinitionsByCV(cvobj)) {
                                if (def.getDefinedLocation() != null) {
                                    resolved = true;
                                    break;
                                }
                            }
                            if (mayNotFromAnotherRepo || resolved) {
                                p("Resolved for :" + cvobj);
                                swrite.fields.add(strRefs);
                            }

                            // result.add(ipnode);
                            // p(inexpr.getBase().getType());
                            // hasSave = true;
                        }
                    }
                    if (swrite.fields.size() > 0) {
                        result.add(swrite);
                    }
                }
            }

            // String field = "status";
            // String setterName = "set" + field.substring(0, 1).toUpperCase() +
            // field.substring(1);
            // String oclass = "order.domain.Order";
            // p(setterName);

        }
        return result;

    }

    public static List<String> refToString(List<SootFieldRef> refs) {
        List<String> result = new ArrayList<>();
        for (SootFieldRef ref : refs) {
            result.add(ref.name());
        }
        return result;
    }

    public static void analyzeRedisFlow() {
        String repoName = "orderRepository";
        for (String method : methodMap.keySet()) {
            MethodInfo minfo = methodMap.get(method);
            boolean hasSave = false;
            for (Unit unit : minfo.sm.getActiveBody().getUnits()) {
                Stmt stmt = (Stmt) unit;
                if (stmt.containsInvokeExpr()) {
                    InvokeExpr iexpr = stmt.getInvokeExpr();
                    if (iexpr instanceof InstanceInvokeExpr) {
                        InstanceInvokeExpr inexpr = (InstanceInvokeExpr) iexpr;
                        if (inexpr.getMethod().getName().equals("save")) {
                            // if()
                            p("-----");
                            p(method + ":");
                            p(stmt);
                            p(inexpr.getBase().getType());
                            hasSave = true;
                        }
                    }
                }

            }
        }
    }

    public static void analyzeDBFlow() {
        String repoName = "orderRepository";
        for (String method : methodMap.keySet()) {
            MethodInfo minfo = methodMap.get(method);
            boolean hasSave = false;
            for (Unit unit : minfo.sm.getActiveBody().getUnits()) {
                Stmt stmt = (Stmt) unit;
                if (stmt.containsInvokeExpr()) {
                    InvokeExpr iexpr = stmt.getInvokeExpr();
                    if (iexpr instanceof InstanceInvokeExpr) {
                        InstanceInvokeExpr inexpr = (InstanceInvokeExpr) iexpr;
                        if (inexpr.getMethod().getName().equals("save")) {
                            // if()
                            p("-----");
                            p(method + ":");
                            p(stmt);
                            p(inexpr.getBase().getType());
                            hasSave = true;
                        }
                    }
                }

            }
            String field = "status";
            String setterName = "set" + field.substring(0, 1).toUpperCase() + field.substring(1);
            String oclass = "order.domain.Order";
            // p(setterName);

            if (hasSave) {
                for (Unit unit : minfo.sm.getActiveBody().getUnits()) {
                    Stmt stmt = (Stmt) unit;
                    // p(stmt);
                    if (stmt.containsInvokeExpr()) {
                        InvokeExpr iexpr = stmt.getInvokeExpr();
                        if (iexpr instanceof InstanceInvokeExpr) {
                            InstanceInvokeExpr inexpr = (InstanceInvokeExpr) iexpr;
                            if (inexpr.getMethod().getName().equals(setterName) &&
                                    inexpr.getBase().getType().toString().contains(oclass)) {
                                p("!!!");
                                p(method + ":");
                                p(stmt);
                                p(inexpr.getBase().getType());
                            }
                        }
                    }
                }
            }

        }
    }

    public static String mergeStr(List<String> strs, String separator) {
        String res = "";
        for (int i = 0; i < strs.size(); i++) {
            res += strs.get(i);
            if (i != strs.size() - 1) {
                res += separator;
            }
        }
        return res;
    }

    private static void analyzeRepo() {
        classMap.forEach((name, sclass) -> {
            if (name.contains("Repository")) {
                // p(sclass.SIGNATURES);
                String repoSig = sclass.getTag("SignatureTag").toString();
                repoSig = repoSig.substring(0, repoSig.length() - 1);
                String typeTuple = repoSig.substring(repoSig.indexOf("<"));
                typeTuple = typeTuple.substring(2, typeTuple.length() - 2);
                String type = typeTuple.substring(0, typeTuple.indexOf(";"));
                // p(File.separator);
                // p(File.pathSeparator);
                type = type.replace("/", ".");
                // p(type);
                SootClass sc = searchClassExact(type);
                p(sc);
                p(sc.getShortName());
                classMap.forEach((oname, osclass) -> {
                    if (osclass.getShortName().equals(sc.getShortName())) {
                        p("--" + osclass);
                    }
                });

                p("");
                // .forEach(t -> {
                // p(t.getName());
                // });
            }
        });
        // p("Analyzing Repo saves...");
        // getDB("save", "OrderRepository");
        // App.p(".................................");
        // getDB("save", "OrderOtherRepository");
        // App.p(".................................");
        // getDB("save", "AddMoneyRepository");
        // App.p(".................................");
        // getDB("save", "PaymentRepository");
        // App.p(".................................");
        // getDB("save", "LoginUserListRepository");
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

    public static void analyzeController() {
        methodMap.forEach((s, minfo) -> {
            if (s.contains("Controller")) {
                SootMethod currMethod = minfo.sm;
                SootClass sc = currMethod.getDeclaringClass();
                String serviceURL = "";
                String entryURL = "";
                VisibilityAnnotationTag ctag = (VisibilityAnnotationTag) sc.getTag("VisibilityAnnotationTag");
                if (ctag != null) {
                    for (AnnotationTag annotation : ctag.getAnnotations()) {
                        String aname = annotation.getType();
                        if (aname.contains("RequestMapping")) {
                            AnnotationElem elem = annotation.getElems().iterator().next();
                            if (elem != null && elem instanceof AnnotationArrayElem) {
                                AnnotationArrayElem arrayElem = (AnnotationArrayElem) elem;
                                serviceURL = ((AnnotationStringElem) arrayElem.getValues().get(0)).getValue();
                            }

                        }
                    }
                }

                VisibilityAnnotationTag tag = (VisibilityAnnotationTag) currMethod.getTag("VisibilityAnnotationTag");
                if (tag != null) {
                    for (AnnotationTag annotation : tag.getAnnotations()) {

                        String aname = annotation.getType();
                        if (aname.contains("Mapping")) {
                            AnnotationElem elem = null;
                            Iterator<AnnotationElem> iter = null;
                            if (!annotation.getElems().isEmpty()) {
                                iter = annotation.getElems().iterator();
                                elem = iter.next();
                            }
                            if (elem != null && elem instanceof AnnotationArrayElem) {
                                AnnotationArrayElem arrayElem = (AnnotationArrayElem) elem;
                                entryURL = ((AnnotationStringElem) arrayElem.getValues().get(0)).getValue();
                            }
                            String reqType = "";
                            if (iter != null && iter.hasNext()) {
                                elem = iter.next();
                                if (elem != null && elem instanceof AnnotationArrayElem) {
                                    AnnotationArrayElem arrayElem = (AnnotationArrayElem) elem;
                                    aname = arrayElem.getValues().get(0).toString();
                                    reqType = Utils.getReqTypeString(aname);
                                }

                            }

                            // List<ValueBox> paramBoxes = this.getCurrentParamBoxes();
                            // paramBoxes.remove(0);

                            String entireURL = serviceURL + entryURL;
                            if (entireURL.contains("/{")) {
                                entireURL = entireURL.substring(0, entireURL.indexOf("/{"));
                            }
                            entireURL = Utils.trimSlash(entireURL);
                            // App.p(currMethod.getName() + ": " + entireURL + ", " + reqType);
                            remoteMap.put(entireURL, currMethod);
                            // if (currMethod.getSignature().contains("getDrawbackPercent")) {
                            // App.p(currMethod.getSignature());
                            // }
                        }
                    }
                }

            }
        });
    }

    public static void getSourceCodes() {
        for (String s : services) {
            String spath = base + s + scodeSuffix;
            try (Stream<Path> stream = Files.walk(Paths.get(spath))) {
                stream.filter(Files::isRegularFile)
                        .forEach(file -> {
                            String fstr = file.toString();
                            // p(fstr);
                            // p(spath);
                            String sfilestr = fstr.substring(spath.length() + 1);
                            sfilestr = sfilestr.substring(0, sfilestr.indexOf(".java"));
                            sfilestr = sfilestr.replace("\\", ".");
                            sourceMap.put(sfilestr, file);
                            // if (fstr.contains("OrderOtherServiceImpl")) {
                            // p(sfilestr);

                            // }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void handleUnresolvedDeps(Definition satdef, ContextSensitiveValue cv, ForwardIPAnalysis fia,
            IPNode node, Set<PendingBackTracking> unresolvedNodes, Set<SharedStateRead> streads) {
        App.p(">>>>>Unresolved " + cv + " with " + satdef);
        Value defVal = satdef.getDefinedValue().getBase().getValue();
        if ((defVal instanceof Constant) && !(defVal instanceof NullConstant)) {
            p("Not tracking constant: " + cv + " with constant value " + defVal);
            return;
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
                    unresolvedNodes.add(new PendingBackTracking(bdef.getDefinedLocation(), "normal"));

                    if (!bdef.getDefinedValue().getBase().getValue().toString().equals("null")) {
                        IPNode dloc = bdef.getDefinedLocation();
                        App.p("New Provenance due to field: " + cvun + " at " +
                                dloc + " with base " + bdef.getDefinedValue());

                        if (dloc.getStmt() instanceof JAssignStmt) {
                            JAssignStmt astmt = (JAssignStmt) bdef.getDefinedLocation().getStmt();
                            if (astmt.getRightOp() instanceof InstanceInvokeExpr) {
                                Value basev = ((InstanceInvokeExpr) astmt.getRightOp()).getBase();
                                if (basev.getType().toString()
                                        .contains("Iterator")) {
                                    p("This provenance is due to an iterator; resolve further...");
                                    Set<Definition> iterDefs = fia.getBefore(bdef.getDefinedLocation())
                                            .getDefinitionsByCV(
                                                    ContextSensitiveValue.getCValue(dloc.getContext(), basev));

                                    IPNode iterDefLoc;
                                    for (Definition def : iterDefs) {
                                        // p(def.d());
                                        Stmt defStmt = def.getDefinedLocation().getStmt();
                                        if (defStmt.containsInvokeExpr()
                                                && defStmt.getInvokeExpr().getMethod().getName().equals("iterator")) {
                                            Value src = ((InstanceInvokeExpr) defStmt.getInvokeExpr()).getBase();
                                            p("Found source: " + src);
                                            Set<Definition> collectionDefs = fia.getBefore(bdef.getDefinedLocation())
                                                    .getDefinitionsByCV(
                                                            ContextSensitiveValue.getCValue(dloc.getContext(), src));
                                            for (Definition cdef : collectionDefs) {
                                                // p(cdef.d());
                                                if (cdef.getDefinedLocation() != null) {
                                                    IPNode collectionUseNode = cdef.getDefinedLocation();
                                                    p("Checking source of iterator: " + collectionUseNode);
                                                    // cdef.get
                                                    checkSTread(cdef, streads, cvun.getSuffix());
                                                    // if (src.getType().toString().contains("List")) {
                                                    // PendingBackTracking listRef = new PendingBackTracking(
                                                    // collectionUseNode, "list");
                                                    // listRef.setRefs(cvun.getSuffix());
                                                    // unresolvedNodes.add(listRef);
                                                    // }
                                                }
                                            }
                                            // break;
                                        }
                                    }
                                }
                            }
                        }
                        checkSTread(bdef, streads, cvun.getSuffix());
                    }
                }
            }
        }
        if (!alternative) {
            App.p("Can't resolve " + cvun + " at all");
        }
    }

    public static void checkSTread(Definition satdef, Set<SharedStateRead> streads,
            List<SootFieldRef> refs) {
        Stmt defStmt = satdef.getDefinedLocation().getStmt();
        IPNode defLocation = satdef.getDefinedLocation();
        if (defStmt.containsInvokeExpr()) {
            InvokeExpr iexpr = defStmt.getInvokeExpr();
            SootMethod ism = iexpr.getMethod();

            if (ism.getDeclaringClass().getName().contains("Repository")
                    && ism.getName().contains("findBy")) {
                ContextSensitiveValue cvread = ContextSensitiveValue.getCValue(satdef.getDefinedLocation().getContext(),
                        ((JAssignStmt) defStmt).getLeftOp());
                SharedStateRead stread = new SharedStateRead("Repository", defLocation, cvread, refs);
                streads.add(stread);

            } else if (ism.getDeclaringClass().getName().contains("ValueOperations") &&
                    ism.getName().contains("get")) {
                ContextSensitiveValue cvread = ContextSensitiveValue.getCValue(satdef.getDefinedLocation().getContext(),
                        ((JAssignStmt) defStmt).getLeftOp());
                streads.add(new SharedStateRead("ValueOperations", defLocation, cvread, refs));
            }
        }
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
            FileWriter fileWriter = new FileWriter(file);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println("valueTPs");
            for (TracePoint tp : tps) {
                printWriter.println(tp.d(",,"));
            }
            printWriter.close();
            System.out.printf("TP data is saved in " + file.getAbsolutePath());
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public static void writeSTReads(Set<SharedStateRead> streads, String path, String name) {
        name = name + "_stread";
        File outputDir = new File(path);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        File file = new File(outputDir + File.separator + name);
        try {
            FileWriter fileWriter = new FileWriter(file);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println("STreads");
            Map<String, List<String>> readPts = new HashMap<>();
            for (SharedStateRead stread : streads) {
                String sd = stread.shortd(",,");
                if (!readPts.containsKey(sd)) {
                    readPts.put(sd, new ArrayList<>());
                }
                // p("yyyy " + stread.refs + ", " + mergeStr(stread.refs, ","));
                readPts.get(sd).add(mergeStr(stread.refs, ","));
            }
            for (String rpt : readPts.keySet()) {
                printWriter.println(rpt + ",," + readPts.get(rpt));
            }
            printWriter.close();
            System.out.printf("STread data is saved in " + file.getAbsolutePath());
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public static void writeSTWrites(Set<SharedStateWrite> stwrites, String path, String name) {
        name = name + "_stwrite";
        File outputDir = new File(path);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        File file = new File(outputDir + File.separator + name);
        try {
            FileWriter fileWriter = new FileWriter(file);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println("STwrites");
            for (SharedStateWrite stwrite : stwrites) {
                printWriter.println(stwrite.d(",,"));
            }
            printWriter.close();
            System.out.printf("STwrite data is saved in " + file.getAbsolutePath());
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

    public static SootClass searchClassExact(String... str) {
        for (String sig : classMap.keySet()) {
            boolean match = true;
            for (String s : str) {
                if (!sig.toString().equals(s)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return classMap.get(sig);
            }
        }
        return null;
    }

    public static SootClass searchClass(String... str) {
        for (String sig : classMap.keySet()) {
            boolean match = true;
            for (String s : str) {
                if (!sig.toString().contains(s)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return classMap.get(sig);
            }
        }
        return null;
    }

    public static MethodInfo searchMethod(String... str) {
        // App.p(methodMap.keySet().size());
        for (String sig : methodMap.keySet()) {
            boolean match = true;
            // if (sig.contains("calculate")) {
            // App.p(sig);
            // }
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
