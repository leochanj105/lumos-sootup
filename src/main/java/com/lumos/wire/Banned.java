package com.lumos.wire;

import java.util.ArrayList;
import java.util.List;

public class Banned {

    public static List<List<String>> banned;

    public static boolean isBanned(String query) {
        if (banned == null) {
            banned = new ArrayList<>();
            addBanned("Repository");
            addBanned("RestTemplate");
            addBanned("StringBuilder");
        }
        for (int i = 0; i < banned.size(); i++) {
            List<String> name = banned.get(i);
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

    public static void addBanned(String... strs) {
        List<String> nlist = new ArrayList<>();
        for (String str : strs) {
            nlist.add(str);
        }
        banned.add(nlist);
    }

}
