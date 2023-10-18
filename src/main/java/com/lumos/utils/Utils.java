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

}
