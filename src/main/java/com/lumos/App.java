package com.lumos;

import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.checkerframework.checker.units.qual.min;
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
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
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

        Map<SootMethod, MethodInfo> methodMap = new HashMap<>();
        WalaSootMethod startMethod = null;
        for (JavaSootMethod sm : ((JavaSootClass) sootClass).getMethods()) {
            WalaSootMethod wsm = (WalaSootMethod) sm;
            // if (wsm.toString().contains("some")) {
            MethodInfo minfo = new MethodInfo(wsm);
            Map<TracePoint, List<TracePoint>> depGMap = minfo.analyzeDef();
            for (TracePoint tp : depGMap.keySet()) {
                if (tp.name == null)
                    continue;
            }
            if (wsm.toString().contains("some")) {
                startMethod = wsm;
            }
            methodMap.put(wsm, minfo);
        }

        p("----------");

        MethodInfo minfo = methodMap.get(startMethod);
        TracePoint target = minfo.getReturnTps().get(0);
        p(target);
        for (TracePoint tp : minfo.getPrev(target)) {
            for (TracePoint tp2 : minfo.getPrev(tp)) {

                if (tp2.toString().contains("<$r2, 55")) {
                    p(tp2);
                    // p(tp2.stmt);
                    // p(minfo.reachingAnalysis.getBeforeStmt(tp2.stmt).get(tp2));
                    p(minfo.getPrev(tp2));
                }
            }
            // p(tp.value.getClass());
        }

    }

    public static void walkMethod(Value val, MethodInfo minfo) {

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
