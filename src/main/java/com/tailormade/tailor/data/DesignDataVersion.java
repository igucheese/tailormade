package com.tailormade.tailor.data;

public enum DesignDataVersion {
    INITIAL  (10000),
    OLD_DATA_MERGED  (10001);

    private final int version;

    DesignDataVersion(int version) {
        this.version= version;
    }

    public int getVersion() { return this.version; }
}
