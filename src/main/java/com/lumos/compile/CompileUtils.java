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
import java.util.Arrays;

import soot.G;
import soot.Printer;
import soot.Scene;
import soot.SootClass;
import soot.baf.BafASMBackend;
import soot.jimple.parser.JimpleAST;
import soot.jimple.parser.Parse;
import soot.jimple.parser.lexer.LexerException;
import soot.jimple.parser.parser.ParserException;
import soot.options.Options;

public class CompileUtils {

    public static void outputJimple(SootClass cl, String analysisPath) {
        System.out.println("compiling " + cl);
        File outputDir = new File("jimpleOutput");
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

            BafASMBackend backend = new BafASMBackend(cl, 0);

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
