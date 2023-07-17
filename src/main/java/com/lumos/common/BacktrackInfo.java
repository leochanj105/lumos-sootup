package com.lumos.common;

import java.util.List;
import java.util.Set;

public class BacktrackInfo {
    public Set<Query> unresolvedRequeries;
    public List<InstrumentPoint> insPoints;

    public BacktrackInfo(Set<Query> queries, List<InstrumentPoint> points) {
        this.unresolvedRequeries = queries;
        this.insPoints = points;
    }
}
