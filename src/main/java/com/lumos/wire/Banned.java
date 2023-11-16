package com.lumos.wire;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.lumos.App;
import com.lumos.utils.Utils;

public class Banned {

    public static List<List<String>> bannedTypes;
    public static List<List<String>> bannedStmts;

    public static boolean isTypeBanned(String query) {
        if (bannedTypes == null) {
            bannedTypes = new ArrayList<>();
            List<String> strs = Utils.readFrom(App.caseStudyPath + "bannedTypes");
            for (String s : strs) {
                addBannedType(s);
            }
            // addBannedType("Repository");
            // addBannedType("RestTemplate");
            // addBannedType("StringBuilder");
            // addBannedType("AsyncTask");
            // addBannedType("Service");
            // addBannedType("Iterator");
            // addBannedType("DecimalFormat");
        }
        for (int i = 0; i < bannedTypes.size(); i++) {
            List<String> name = bannedTypes.get(i);
            boolean matched = true;
            for (String n : name) {
                if (!query.contains(n)) {
                    matched = false;
                    break;
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

            List<String> strs = Utils.readFrom(App.caseStudyPath + "bannedStmts");
            for (String s : strs) {
                String[] substrs = s.split(",");
                // App.p("!!! " + Arrays.asList(substrs));
                addBannedStmt(substrs);
            }
            // addBannedStmt("changeOrderResult = (cancel.domain.ChangeOrderResult)
            // $stack12",
            // "cancel.queue.MsgReveiceBean");
            // addBannedStmt("changeOrderInfo = (other.domain.ChangeOrderInfo) $stack12",
            // "other.queue.MsgReveiceBean");
            // App.p(bannedStmts);
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
            nlist.add(str.trim());
        }
        bannedStmts.add(nlist);
    }

    public static void addBannedType(String... strs) {
        List<String> nlist = new ArrayList<>();
        for (String str : strs) {
            nlist.add(str.trim());
        }
        bannedTypes.add(nlist);
    }

}