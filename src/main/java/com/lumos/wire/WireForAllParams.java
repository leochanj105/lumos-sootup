package com.lumos.wire;

import java.util.HashSet;
import java.util.Set;

public class WireForAllParams {
    private static Set<String> names = null;

    public static Set<String> getNames() {
        if (names == null) {
            names = new HashSet<>();
            names.add("java.lang.String: boolean equals(java.lang.Object)");
            names.add("java.lang.Boolean: ");
            names.add("java.util.concurrent.Future: java.lang.Object get()");
            names.add("org.springframework.scheduling.'annotation'.AsyncResult: void <init>");
            names.add("org.springframework.http.ResponseEntity: java.lang.Object getBody()");
        }
        return names;
    }
}
