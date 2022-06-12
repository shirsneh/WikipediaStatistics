package com.magicalpipelines.model;

import org.jetbrains.annotations.NotNull;

public class WikiActive implements Comparable<WikiActive> {
    protected String name;
    protected int counter;

    public WikiActive(String name) {
        this.name = name;
        this.counter = 1;
    }

    public WikiActive(WikiEvent event) {
        this.name = event.getTitle();
        this.counter = 1;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "WikiActive{" +
                "name='" + name + '\'' +
                ", counter=" + counter +
                '}';
    }

    @Override
    public int compareTo(@NotNull WikiActive wikiActive) {
        return this.name.compareTo(wikiActive.name);
    }
}
