package com.lumos;

import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.model.Body.BodyBuilder;
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
        ClassType classType = project.getIdentifierFactory().getClassType("Test");
        // SootClass<JavaSootClassSource> sootClass = (SootClass<JavaSootClassSource>)
        // view.getClass(classType).get();
        SootClass sootClass = view.getClass(classType).get();
        p(sootClass.getClassSource().getClass());
        for (JavaSootMethod sm : ((JavaSootClass) sootClass).getMethods()) {
            WalaSootMethod wsm = (WalaSootMethod) sm;
            if (wsm.toString().contains("some")) {
                Map<TracePoint, List<TracePoint>> depGMap = analyzeDef(wsm);
                for (TracePoint tp : depGMap.keySet()) {
                    if (tp.name == null)
                        continue;
                    p(tp + "  ?  " + tp.stmt + " ====> ");
                    for (TracePoint tp2 : depGMap.get(tp)) {
                        p(" " + tp2 + "  ?  " + tp2.stmt);
                    }
                }
                p("-----");
                p(parameters(wsm));
                p(getReturns(wsm));
            }

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

    public static List<TracePoint> getReturns(WalaSootMethod wsm) {
        List<TracePoint> tps = new ArrayList<>();
        for (Stmt stmt : wsm.getBody().getStmtGraph()) {
            if (stmt instanceof JReturnStmt) {
                tps.add(new TracePoint(stmt, ((JReturnStmt) stmt).getUses().get(0)));
            }
        }
        return tps;
    }

    public static Map<TracePoint, List<TracePoint>> analyzeDef(WalaSootMethod wsm) {
        // if (wsm.toString().contains("some")) {
        String[][] names = wsm.getDebugInfo().getSourceNamesForValues();
        Map<Integer, Local> lmap = wsm.localMap;
        Map<Value, String> nmap = new HashMap<>();
        for (Integer i : lmap.keySet()) {
            if (names[i].length > 0) {
                p(i + " : " + lmap.get(i) + " -- " + names[i][0]);
                nmap.put(lmap.get(i), names[i][0]);
            } else {
                p(i + " : " + lmap.get(i));
            }
        }
        Body body = wsm.getBody();

        BodyBuilder builder = Body.builder(body, wsm.getModifiers());

        // builder.insertBefore(null, null)

        StmtGraph<?> cfg = wsm.getBody().getStmtGraph();
        // p(cfg instanceof MutableBlockStmtGraph);
        Set<Value> sv = new HashSet<>();
        for (Iterator<Stmt> unitIt = cfg.iterator(); unitIt.hasNext();) {
            Stmt currStmt = unitIt.next();

        }

        RWAnalysis analysis = new RWAnalysis(cfg);

        Map<TracePoint, List<TracePoint>> depGraph = new HashMap<>();
        p(nmap + "\n -----");
        for (Stmt stmt : cfg.getStmts()) {

            Map<Value, Set<Dependency>> dmap = analysis.getBeforeStmt(stmt);
            for (Value v : stmt.getUsesAndDefs()) {
                String name = null;
                name = nmap.get(v);
                if (v instanceof JInstanceFieldRef) {
                    JInstanceFieldRef refv = (JInstanceFieldRef) v;
                    String basename = nmap.get(refv.getBase());
                    if (basename == null)
                        basename = refv.getBase().getName();
                    String refname = refv.getFieldSignature().getName();
                    name = basename + "." + refname;
                }

                TracePoint tmp = new TracePoint(stmt, v, name);
                if (!depGraph.containsKey(tmp)) {
                    depGraph.put(tmp, new ArrayList<>());
                }
            }
            for (Value v : stmt.getDefs()) {
                TracePoint tmp = new TracePoint(stmt, v);
                for (Value v2 : stmt.getUses()) {
                    TracePoint tmp2 = new TracePoint(stmt, v2);
                    depGraph.get(tmp).add(tmp2);
                }
            }
            for (Value v : stmt.getUses()) {
                TracePoint tmp = new TracePoint(stmt, v);
                if (dmap.containsKey(v)) {
                    // p(v + " :: " + dmap.get(v));
                    for (Dependency dep : dmap.get(v)) {
                        for (Value v2 : dep.stmt.getUsesAndDefs()) {
                            TracePoint tmp2 = new TracePoint(dep.stmt, v2);
                            depGraph.get(tmp).add(tmp2);
                        }
                    }
                }
            }
        }

        return depGraph;
        // }

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