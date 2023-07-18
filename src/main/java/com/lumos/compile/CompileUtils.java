package com.lumos.compile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

import soot.G;
import soot.Scene;
import soot.jimple.parser.JimpleAST;
import soot.jimple.parser.Parse;
import soot.jimple.parser.lexer.LexerException;
import soot.jimple.parser.parser.ParserException;
import soot.options.Options;
import sootup.core.model.SootClass;
import sootup.core.util.printer.JimplePrinter;
import sootup.java.core.JavaSootClass;

public class CompileUtils {

    public static void outputJimple(SootClass cl) {
        File outputDir = new File("jimpleOutput");
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        File file = new File(outputDir + File.separator + cl.getName() + ".jimple");
        PrintWriter writer, writerfile;
        try {
            // writerfile = new PrintWriter(file);
            ByteArrayOutputStream bstream = new ByteArrayOutputStream(8192);
            writer = new PrintWriter(bstream, true);

            JimplePrinter printer = new JimplePrinter();
            printer.printTo(cl, writer);

            // printer.printTo(cl, writerfile);
            // writerfile.close();

            System.out.println(bstream.toString());

            ByteArrayInputStream binput = new ByteArrayInputStream(bstream.toByteArray());
            FileInputStream finput = new FileInputStream(file);

            G.reset();

            Options.v().set_allow_phantom_elms(true);
            Options.v().set_write_local_annotations(true);
            Options.v().set_ignore_resolution_errors(true);
            Options.v().set_no_bodies_for_excluded(true);
            Options.v().set_allow_phantom_refs(true);
            Options.v().set_keep_line_number(true);
            Options.v().set_whole_program(true);
            Scene.v().loadNecessaryClasses();
            // Scene.v().addBasicClass(launcher.service.LauncherService, HIERARCHY);
            JimpleAST jast = new JimpleAST(binput);

            soot.SootClass sclass = jast.createSootClass();
            System.out.println(sclass);
            // finput.close();

            // soot.SootClass sclass = sclass = Parse.parse(binput, null);
            // } catch (ParserException | LexerException | IOException e) {
            // // // TODO Auto-generated catch block
            // e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            // Thread.currentThread().getStackTrace();
            // Thread.dumpStack();

            // for (StackTraceElement e2 : e.getStackTrace())
            // System.out.println(e2);
        }

    }

}
