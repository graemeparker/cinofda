package com.adfonic.domain;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.*;

@Entity
@Table(name="ASSET_BUNDLE")
public class AssetBundle extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CREATIVE_ID",nullable=true)
    private Creative creative;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="DISPLAY_TYPE_ID",nullable=true)
    private DisplayType displayType;
    @OneToMany(fetch=FetchType.LAZY)
    @JoinTable(name="ASSET_BUNDLE_ASSET_MAP",joinColumns=@JoinColumn(name="ASSET_BUNDLE_ID"),inverseJoinColumns=@JoinColumn(name="ASSET_ID"))
    @MapKeyJoinColumn(name="COMPONENT_ID",referencedColumnName="ID")
    private Map<Component,Asset> assetMap;

    {
	assetMap = new HashMap<Component,Asset>();
    }
    
    AssetBundle() {}

    AssetBundle(Creative creative, DisplayType displayType) {
	this.creative = creative;
	this.displayType = displayType;
    }

    public long getId() { return id; };
    
    public Creative getCreative() { return creative; }

    public DisplayType getDisplayType() { return displayType; }

    public Asset getAsset(Component component) {
	return assetMap.get(component);
    }

    public void putAsset(Component component, Asset asset) {
	assetMap.put(component, asset);
    }

    public Map<Component,Asset> getAssetMap() { return assetMap; }
}
