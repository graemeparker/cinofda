package com.adfonic.dto.publication.enums;

public enum Approval {

    ALL("All", null), AUTO("Auto", "1"), MANUAL("Manual", "0");

    private String approval;
    private String id;

    private Approval(String approval, String id) {
        this.approval = approval;
        this.id = id;
    }

    public String getapproval() {
        return approval;
    }

    public String getId() {
        return id;
    }

    public static Approval value(String id) {
        if (id == null) {
            return Approval.ALL;
        } else if (Approval.MANUAL.getId().equals(id)) {
            return Approval.MANUAL;
        } else {
            return Approval.AUTO;
        }
    }

}
