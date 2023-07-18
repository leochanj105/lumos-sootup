package com.lumos.compile;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import sootup.core.model.SootClass;
import sootup.core.util.printer.JimplePrinter;
import sootup.java.core.JavaSootClass;

public class CompileUtils {

    public static void outputJimple(SootClass cl) {
        // File outputDir = new File("jimpleOutput");
        // if (!outputDir.exists()) {
        // outputDir.mkdir();
        // }
        // File file = new File(outputDir + File.separator + cl.getName() + ".jimple");
        PrintWriter writer;
        // try {
        // writer = new PrintWriter(file);
        writer = new PrintWriter(System.out, true);
        JimplePrinter printer = new JimplePrinter();
        printer.printTo(cl, writer);
        // printer.printTo(cl, writer);
        // writer.flush();
        // writer.close();
        // } catch (FileNotFoundException e) {
        // dont throw again - as this is for debug purposes only
        // e.printStackTrace();
        // }

    }

}
