package com.lumos.wire;

import java.util.ArrayList;
import java.util.List;

public class Banned {

    public static List<List<String>> bannedTypes;
    public static List<List<String>> bannedStmts;

    public static boolean isTypeBanned(String query) {
        if (bannedTypes == null) {
            bannedTypes = new ArrayList<>();
            addBannedType("Repository");
            addBannedType("RestTemplate");
            addBannedType("StringBuilder");
            addBannedType("AsyncTask");
            addBannedType("Service");
            addBannedType("Iterator");
        }
        for (int i = 0; i < bannedTypes.size(); i++) {
            List<String> name = bannedTypes.get(i);
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

    public static boolean isStmtBanned(String query) {
        if (bannedStmts == null) {
            bannedStmts = new ArrayList<>();
            addBannedStmt("changeOrderResult = (cancel.domain.ChangeOrderResult) $stack12",
                    "cancel.queue.MsgReveiceBean");
            addBannedStmt("changeOrderInfo = (other.domain.ChangeOrderInfo) $stack12",
                    "other.queue.MsgReveiceBean");
        }
        for (int i = 0; i < bannedStmts.size(); i++) {
            List<String> name = bannedStmts.get(i);
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

    public static void addBannedStmt(String... strs) {
        List<String> nlist = new ArrayList<>();
        for (String str : strs) {
            nlist.add(str);
        }
        bannedStmts.add(nlist);
    }

    public static void addBannedType(String... strs) {
        List<String> nlist = new ArrayList<>();
        for (String str : strs) {
            nlist.add(str);
        }
        bannedTypes.add(nlist);
    }

}
