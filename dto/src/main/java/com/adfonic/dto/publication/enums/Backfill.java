package com.adfonic.dto.publication.enums;

public enum Backfill {
    ALL("All", null), YES("Yes", "1"), NO("No", "0");

    private String backfill;
    private String id;

    private Backfill(String backfill, String id) {
        this.backfill = backfill;
        this.id = id;
    }

    public String getbackfill() {
        return backfill;
    }

    public String getId() {
        return id;
    }

    public static Backfill value(String id) {
        if (id == null) {
            return Backfill.ALL;
        } else if (id.equals(Backfill.NO.getId())) {
            return Backfill.NO;
        } else {
            return Backfill.YES;
        }
    }
}
