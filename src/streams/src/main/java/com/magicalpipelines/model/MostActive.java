package com.magicalpipelines.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class MostActive<T extends WikiActive> {
    private final TreeSet<T> activity = new TreeSet<>();

    public MostActive<T> add(final T active) {
        activity.add(active);

        // keep only the top 5 most active
        if (activity.size() > 5) {
            activity.remove(activity.last());
        }

        return this;
    }

    public List<T> toList() {

        Iterator<T> counts = activity.iterator();
        List<T> activeCounts = new ArrayList<>();
        while (counts.hasNext()) {
            activeCounts.add(counts.next());
        }

        return activeCounts;
    }
}