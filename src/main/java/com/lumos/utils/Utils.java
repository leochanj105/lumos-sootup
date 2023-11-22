package com.lumos.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.lumos.App;

public class Utils {
    public static List<String> readFrom(String file) {
        List<String> list = new ArrayList<>();
        FileInputStream fstream;
        try {
            fstream = new FileInputStream(file);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String strLine;
            // Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                list.add(strLine);
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String trimSlash(String str) {
        if (str.length() > 0 && str.charAt(str.length() - 1) == '/') {
            return str.substring(0, str.length() - 1);
        }
        return str;
    }

    public static boolean isCompositeType(String tstr) {
        return (tstr.contains("List") || (tstr.contains("Set") || (tstr.contains("Map"))))
                || (tstr.contains("[]"));
    }

    public static String getReqTypeString(Object req) {
        String reqString = req.toString().toUpperCase();
        // System.out.println(reqString);
        if (reqString.contains("GET")) {
            return "GET";
        } else if (reqString.contains("POST")) {
            return "POST";
        } else if (reqString.contains("PUT")) {
            return "PUT";
        } else if (reqString.contains("DELETE")) {
            return "DELETE";
        } else if (reqString.contains("PATCH")) {
            return "PATCH";
        }
        return null;
    }

    public static boolean isCrossContext(String mname) {
        return mname.contains("exchange") || mname.contains("ForObject") ||
                mname.contains("boolean send(org.springframework.messaging.Message)");
    }

    public static String getDirectName(String cname) {
        return cname.substring(cname.lastIndexOf(".") + 1);
    }

    public static boolean typeMatch(String caller, String callee) {
        return caller.equals(callee) || specialMatch(caller, callee);
    }

    public static boolean specialMatch(String s1, String s2) {
        return (s1.equals("OutsidePaymentInfo") && s2.equals("PaymentInfo")) ||
                (s1.equals("Information") && s2.equals("Information3"));
    }

    public static String getSourceLine(String className, int target) {
        String ans = "";
        if (!App.sourceMap.containsKey(className)) {
            App.p(className + " does not exist");
            return ans;
        }
        try {
            Scanner myReader = new Scanner(App.sourceMap.get(className));
            int line = 1;
            // int target = 42;
            boolean beginFound = false;
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                // if (target == 360) {
                // App.p(data);
                // }
                if (beginFound) {
                    String tdata = data.trim();
                    ans += tdata;
                    // App.p(className + ", " + target + ", " + line + ", " + tdata + ", " + data);
                    if (tdata.charAt(tdata.length() - 1) == ';') {
                        break;
                    }
                } else if (line == target) {
                    // p(data);
                    ans = data.trim();
                    // ans.trim()
                    beginFound = true;
                    if (ans.charAt(ans.length() - 1) == ';') {
                        break;
                    }
                    // break;
                    // return data;
                }
                line += 1;
            }
            myReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ans;
    }
}
