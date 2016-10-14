package com.byyd.middleware.campaign.filter;

import java.util.Set;

import com.adfonic.domain.Country;
import com.adfonic.domain.GeotargetType;
import com.byyd.middleware.iface.dao.LikeSpec;

public class GeotargetFilter {
    private GeotargetType geotargetType;
    private String countryIsoCode;
    private Country country;
    private Set<String> names;
    private boolean namesCaseSensitive;
    private String nameLike;
    private boolean nameLikeCaseSensitive;

    public GeotargetType getType() {
        return geotargetType;
    }
    public GeotargetFilter setType(GeotargetType type) {
        this.geotargetType = type;
        return this;
    }

    public String getCountryIsoCode() {
        return countryIsoCode;
    }
    public GeotargetFilter setCountryIsoCode(String countryIsoCode) {
        this.countryIsoCode = countryIsoCode;
        return this;
    }

    public Country getCountry() {
        return country;
    }
    public GeotargetFilter setCountry(Country country) {
        this.country = country;
        return this;
    }

    public Set<String> getNames() {
        return names;
    }
    public GeotargetFilter setNames(Set<String> names, boolean namesCaseSensitive) {
        this.names = names;
        this.namesCaseSensitive = namesCaseSensitive;
        return this;
    }

    public boolean isNamesCaseSensitive() {
        return namesCaseSensitive;
    }
    public GeotargetFilter setNamesCaseSensitive(boolean namesCaseSensitive) {
        this.namesCaseSensitive = namesCaseSensitive;
        return this;
    }

    public String getNameLike() {
        return nameLike;
    }
    public GeotargetFilter setNameLike(String nameLike, LikeSpec likeSpec, boolean caseSensitive) {
        this.nameLike = (likeSpec == null ? LikeSpec.CONTAINS : likeSpec).getPattern(nameLike);
        this.nameLikeCaseSensitive = caseSensitive;
        return this;
    }

    public boolean isNameLikeCaseSensitive() {
        return nameLikeCaseSensitive;
    }
    public GeotargetFilter setNameLikeCaseSensitive(boolean nameLikeCaseSensitive) {
        this.nameLikeCaseSensitive = nameLikeCaseSensitive;
        return this;
    }
}
