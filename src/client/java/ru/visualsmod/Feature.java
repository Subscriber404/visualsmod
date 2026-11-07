package ru.visualsmod;

public final class Feature {
    public final String id, name, desc;
    public final int cat, key;
    public boolean enabled;

    public Feature(String id, String name, String desc, int cat, int key, boolean enabled) {
        this.id = id; this.name = name; this.desc = desc;
        this.cat = cat; this.key = key; this.enabled = enabled;
    }
}
