package com.lumos.wire;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IdentityWire {
    public static List<List<String>> wires;
    public static List<Boolean> visibles;
    public static List<List<String>> specials;

    public static int findWire(String query) {
        if (wires == null || visibles == null) {
            wires = new ArrayList<>();
            visibles = new ArrayList<>();
            add(false, "org.springframework.http.ResponseEntity", "getBody");
            add(false, "java.lang.Boolean", "booleanValue");
            add(false, "java.util.concurrent.Future", "get");
            add(false, "org.springframework.scheduling.'annotation'.AsyncResult", "init");
            add(false, "java.lang.Boolean", "valueOf(boolean)");
            add(true, "java.math.BigDecimal", "int compareTo");
            add(true, "java.math.BigDecimal", "java.math.BigDecimal add");
            add(true, "java.math.BigDecimal", "void <init>");
            add(false, "java.util.Iterator", "java.lang.Object next");
            add(false, "java.util.List", "java.util.Iterator iterator");
            add(true, "Repository", "findByUserId");
            add(true, "Repository", "findById");
            add(true, "javax.servlet.http.Cookie", "java.lang.String getValue");

        }
        for (int i = 0; i < wires.size(); i++) {
            List<String> name = wires.get(i);
            boolean matched = true;
            for (String n : name) {
                if (!query.contains(n)) {
                    matched = false;
                }
            }
            if (matched) {
                return visibles.get(i).booleanValue() ? 1 : 0;
            }
        }
        return -1;
    }

    public static boolean isSpecial(String query) {
        if (specials == null) {
            specials = new ArrayList<>();
            addSpecial("java.util.Iterator", "boolean hasNext");
            addSpecial("Object", "void <init>");
            // addSpecial("com.google.gson.Gson", "void <init>");
        }
        for (int i = 0; i < specials.size(); i++) {
            List<String> name = specials.get(i);
            boolean matched = true;
            for (String n : name) {
                if (!query.contains(n)) {
                    matched = false;
                }
            }
            if (matched) {
                return true;
            }

        }
        return false;

    }

    public static void addSpecial(String... strs) {
        List<String> nlist = new ArrayList<>();
        for (String str : strs) {
            nlist.add(str);
        }
        specials.add(nlist);
    }

    public static void add(boolean vis, String... strs) {
        List<String> nlist = new ArrayList<>();
        for (String str : strs) {
            nlist.add(str);
        }
        wires.add(nlist);
        visibles.add(vis);
    }
}
