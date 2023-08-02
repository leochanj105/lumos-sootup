package com.lumos.compile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lumos.App;
import com.lumos.forward.ContextSensitiveValue;
import com.lumos.forward.UniqueName;

import jas.StringCP;
import soot.Body;
import soot.G;
import soot.IntType;
import soot.Local;
import soot.PackManager;
import soot.PatchingChain;
import soot.Printer;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.baf.BafASMBackend;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.internal.JAssignStmt;
import soot.options.Options;

public class CompileUtils {

    public static Map<String, Body> bodyMap = new HashMap<>();

    public static int id = 0;

    public static List<Stmt> play(Body body, Value v) {
        Local tpLocal = Jimple.v().newLocal("tpLocal", RefType.v("java.io.PrintStream"));
        if (CompileUtils.getLocal(body, tpLocal) == null) {
            body.getLocals().add(tpLocal);

            PatchingChain<Unit> units = body.getUnits();
            units.insertBefore(Jimple.v().newAssignStmt(
                    tpLocal, Jimple.v().newStaticFieldRef(
                            Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())),
                    ((JimpleBody) body).getFirstNonIdentityStmt());
            // body.validate();
            // java.lang.System.
        }

        Local intLocal = Jimple.v().newLocal("intLocal", IntType.v());
        body.getLocals().add(intLocal);

        Local tmpString1 = Jimple.v().newLocal("tmpString1", RefType.v("java.lang.String"));
        if (CompileUtils.getLocal(body, tmpString1) == null) {
            body.getLocals().add(tmpString1);
            // body.validate();
        }
        Local tmpString2 = Jimple.v().newLocal("tmpString2", RefType.v("java.lang.String"));
        if (CompileUtils.getLocal(body, tmpString2) == null) {
            body.getLocals().add(tmpString2);
            // body.validate();
        }
        List<Stmt> stlist = new ArrayList<>();
        // App.p("1!!!! " + Scene.v().getSootClass("other.domain.Order").getFields());
        Local val = CompileUtils.getLocal(body, v);
        AssignStmt stmtx = Jimple.v().newAssignStmt(intLocal,
                Jimple.v().newVirtualInvokeExpr(val,
                        Scene.v().getSootClass("other.domain.Order").getMethod("int getStatus()").makeRef()));
        stlist.add(stmtx);

        AssignStmt stmt = Jimple.v().newAssignStmt(tmpString1, StringConstant.v("$stack7.status="));
        stlist.add(stmt);
        // Value actualVal = null;
        // if(cv.g)
        // cv.getValue();

        SootMethod toStringMethod = CompileUtils.getValueOfMethod(intLocal);
        stmt = Jimple.v().newAssignStmt(tmpString2, Jimple.v().newStaticInvokeExpr(toStringMethod.makeRef(), intLocal));
        stlist.add(stmt);
        SootMethod concatMethod = Scene.v().getSootClass("java.lang.String")
                .getMethod("java.lang.String concat(java.lang.String)");
        stmt = Jimple.v().newAssignStmt(tmpString1, Jimple.v().newVirtualInvokeExpr(tmpString1,
                concatMethod.makeRef(), tmpString2));
        stlist.add(stmt);
        SootMethod toCall = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");
        InvokeStmt printStmt = Jimple.v()
                .newInvokeStmt(Jimple.v().newVirtualInvokeExpr(tpLocal, toCall.makeRef(), tmpString1));
        stlist.add(printStmt);
        return stlist;
    }

    public static void insertAt(Body body, Stmt target, Stmt toinsert, boolean before) {
        PatchingChain<Unit> units = body.getUnits();
        if (before) {
            units.insertBefore(toinsert, target);
        } else {
            units.insertAfter(toinsert, target);
        }
        // body.validate();
    }

    public static void insertAt(Body body, Stmt target, List<Stmt> toinsert, boolean before) {
        PatchingChain<Unit> units = body.getUnits();
        if (before) {
            units.insertBefore(toinsert, target);
        } else {
            units.insertAfter(toinsert, target);
        }
        // body.validate();
    }

    public static List<Stmt> generateTPStmts(Body body, UniqueName un) {
        Local tpLocal = Jimple.v().newLocal("tpLocal", RefType.v("java.io.PrintStream"));
        if (getLocal(body, tpLocal) == null) {
            body.getLocals().add(tpLocal);

            PatchingChain<Unit> units = body.getUnits();
            units.insertBefore(Jimple.v().newAssignStmt(
                    tpLocal, Jimple.v().newStaticFieldRef(
                            Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())),
                    ((JimpleBody) body).getFirstNonIdentityStmt());
            // body.validate();
            // java.lang.System.
        }

        Local tmpString1 = Jimple.v().newLocal("tmpString1", RefType.v("java.lang.String"));
        if (getLocal(body, tmpString1) == null) {
            body.getLocals().add(tmpString1);
            // body.validate();
        }
        Local tmpString2 = Jimple.v().newLocal("tmpString2", RefType.v("java.lang.String"));
        if (getLocal(body, tmpString2) == null) {
            body.getLocals().add(tmpString2);
            // body.validate();
        }
        List<Stmt> stlist = new ArrayList<>();

        Value val = null;
        Value baseval = un.getBase().getValue();
        if (un.getSuffix().isEmpty()) {
            // App.p(baseval.getClass());
            if (baseval instanceof Constant) {
                val = baseval;
            } else {
                // App.p(baseval instanceof Constant);
                val = getLocal(body, baseval);
            }
        } else {
            Value curr = getLocal(body, baseval);
            List<Local> locallist = new ArrayList<>();
            for (SootFieldRef ref : un.getSuffix()) {
                Local tmp = Jimple.v().newLocal("tmpLocal" + (id++), ref.declaringClass().getType());
                body.getLocals().add(tmp);
                body.validate();
                locallist.add(tmp);

                AssignStmt stmt = Jimple.v().newAssignStmt(tmp, Jimple.v().newInstanceFieldRef(curr, ref));
                stlist.add(stmt);
                curr = tmp;
            }
            val = curr;
            // for(int i = 0; i < un.getSuffix())

        }

        AssignStmt stmt = Jimple.v().newAssignStmt(tmpString1, StringConstant.v(val.toString() + "="));
        stlist.add(stmt);
        // Value actualVal = null;
        // if(cv.g)
        // cv.getValue();

        SootMethod toStringMethod = getValueOfMethod(val);
        stmt = Jimple.v().newAssignStmt(tmpString2, Jimple.v().newStaticInvokeExpr(toStringMethod.makeRef(), val));
        stlist.add(stmt);
        SootMethod concatMethod = Scene.v().getSootClass("java.lang.String")
                .getMethod("java.lang.String concat(java.lang.String)");
        stmt = Jimple.v().newAssignStmt(tmpString1, Jimple.v().newVirtualInvokeExpr(tmpString1,
                concatMethod.makeRef(), tmpString2));
        stlist.add(stmt);
        SootMethod toCall = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");
        InvokeStmt printStmt = Jimple.v()
                .newInvokeStmt(Jimple.v().newVirtualInvokeExpr(tpLocal, toCall.makeRef(), tmpString1));
        stlist.add(printStmt);
        return stlist;
    }

    public static Local getLocal(Body b, Value v) {
        for (Local l : b.getLocals()) {
            if (l.getName().equals(((Local) v).getName())) {
                return l;
            }
        }
        return null;
    }

    public static SootMethod getValueOfMethod(Value value) {
        SootMethod toCall;
        if (value.getType().toString().equals("int")) {
            toCall = Scene.v().getSootClass("java.lang.String").getMethod("java.lang.String valueOf(int)");
        } else if (value.getType().toString().equals("byte")) {
            toCall = Scene.v().getSootClass("java.lang.String").getMethod("java.lang.String valueOf(int)");
        } else if (value.getType().toString().equals("float")) {
            toCall = Scene.v().getSootClass("java.lang.String").getMethod("java.lang.String valueOf(float)");
        } else if (value.getType().toString().equals("double")) {
            toCall = Scene.v().getSootClass("java.lang.String").getMethod("java.lang.String valueOf(double)");
        } else if (value.getType().toString().equals("boolean")) {
            toCall = Scene.v().getSootClass("java.lang.String").getMethod("java.lang.String valueOf(boolean)");
        } else if (value.getType().toString().equals("char")) {
            toCall = Scene.v().getSootClass("java.lang.String").getMethod("java.lang.String valueOf(char)");
        } else if (value.getType().toString().equals("char[]")) {
            toCall = Scene.v().getSootClass("java.lang.String").getMethod("java.lang.String valueOf(char[])");
        } else if (value.getType().toString().equals("long")) {
            toCall = Scene.v().getSootClass("java.lang.String").getMethod("java.lang.String valueOf(long)");
        } else {
            toCall = Scene.v().getSootClass("java.lang.String").getMethod("java.lang.String valueOf(java.lang.Object)");
        }
        return toCall;
    }

    public static void outputJimple(SootClass cl, String analysisPath) {

        System.out.println("compiling " + cl);

        File outputDir = new File(analysisPath);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        File file = new File(outputDir + File.separator + cl.getName() + ".jimple");
        PrintWriter writer, writerfile;
        try {
            writerfile = new PrintWriter(file);
            ByteArrayOutputStream bstream = new ByteArrayOutputStream(8192);
            writer = new PrintWriter(bstream, true);

            // JimplePrinter printer = new JimplePrinter();
            Printer.v().printTo(cl, writerfile);
            // printer.printTo(cl, writer);

            // printer.printTo(cl, writerfile);
            writerfile.close();

            // System.out.println(bstream.toString());

            // ByteArrayInputStream binput = new
            // ByteArrayInputStream(bstream.toByteArray());
            // FileInputStream finput = new FileInputStream(file);

            BafASMBackend backend = new BafASMBackend(cl, 52);
            // PackManager.v().createASMBackend(cl);
            File file2 = new File(outputDir + File.separator + cl.getName() + ".class");
            // writerfile = new PrintWriter(file2);
            FileOutputStream classout = new FileOutputStream(file2);
            backend.generateClassFile(classout);
            classout.close();
            // System.out.println(sclass.getMethods());
            // finput.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
