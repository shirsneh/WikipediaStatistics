package com.magicalpipelines.model;

public class WikiActive {
    protected String name;
    protected int counter;

    public WikiActive(String name) {
        this.name = name;
        this.counter = 1;
    }

    public WikiActive(WikiEvent event) {
        this.name = event.getPageTitle();
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
}
