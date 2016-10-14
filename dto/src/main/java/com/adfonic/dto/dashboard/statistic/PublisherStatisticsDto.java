package com.adfonic.dto.dashboard.statistic;

import com.adfonic.dto.publication.enums.Approval;
import com.adfonic.dto.publication.enums.Backfill;
import com.adfonic.dto.publication.publicationtype.PublicationtypeDto;

public class PublisherStatisticsDto extends PublisherHeadlineStatsDto {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String status;
    private PublicationtypeDto platform;
    private String publicationName;
    private Approval approval;
    private Backfill backfill;
    private long publicationId;
    private boolean selected;
    private long clicks;
    private double ctr;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPublicationName() {
        return publicationName;
    }

    public void setPublicationName(String publicationName) {
        this.publicationName = publicationName;
    }

    public PublicationtypeDto getPlatform() {
        return platform;
    }

    public void setPlatform(PublicationtypeDto platform) {
        this.platform = platform;
    }

    public Approval getApproval() {
        return approval;
    }

    public void setApproval(Approval approval) {
        this.approval = approval;
    }

    public Backfill getBackfill() {
        return backfill;
    }

    public void setBackfill(Backfill backfill) {
        this.backfill = backfill;
    }

    public long getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(long publicationId) {
        this.publicationId = publicationId;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public long getClicks() {
        return clicks;
    }

    public void setClicks(long clicks) {
        this.clicks = clicks;
    }

    public double getCtr() {
        return ctr;
    }

    public void setCtr(double ctr) {
        this.ctr = ctr;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PublisherStatisticsDto [status=");
        builder.append(status);
        builder.append(", publicationName=");
        builder.append(publicationName);
        builder.append(", publicationId=");
        builder.append(publicationId);
        builder.append(", platform=");
        builder.append(platform);
        builder.append(", aproval=");
        builder.append(approval);
        builder.append(", backfill=");
        builder.append(backfill);
        builder.append(", requests=");
        builder.append(requests);
        builder.append(", fillRate=");
        builder.append(fillRate);
        builder.append(", impressions=");
        builder.append(impressions);
        builder.append(", revenue=");
        builder.append(revenue);
        builder.append("]");
        return builder.toString();
    }

}
