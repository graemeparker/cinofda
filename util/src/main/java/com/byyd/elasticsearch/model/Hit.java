package com.byyd.elasticsearch.model;

import java.util.Map;

public class Hit {

    private String id;
    private Long version = 0L;
    private Map<String, Object> source;

    public Hit(String id, Long version, Map<String, Object> source) {
        super();
        this.id = id;
        this.version = version;
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public Map<String, Object> getSource() {
        return source;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Hit other = (Hit) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (source == null) {
            if (other.source != null) {
                return false;
            }
        } else if (!source.equals(other.source)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Hit [id=").append(id).append(", version=").append(version).append(", source=").append(source).append("]");
        return builder.toString();
    }
}
