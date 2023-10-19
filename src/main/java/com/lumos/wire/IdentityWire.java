package com.lumos.wire;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.lumos.App;
import com.lumos.utils.Utils;

public class IdentityWire {
    public static List<List<String>> wires;
    public static List<Boolean> visibles;
    public static List<List<String>> specials;

    public static boolean findWire(String query) {
        if (wires == null || visibles == null) {
            wires = new ArrayList<>();
            visibles = new ArrayList<>();
            List<String> strs = Utils.readFrom(App.caseStudyPath + "identityWires");
            for (String s : strs) {
                add(true, s.split(","));
            }
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
                return true;
            }
        }
        return false;
    }

    public static void add(String... strs) {
        add(true, strs);
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
