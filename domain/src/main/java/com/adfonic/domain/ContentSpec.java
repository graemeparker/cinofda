package com.adfonic.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.*;
import org.apache.commons.lang.StringUtils;

/**
 * Groups together a number of attributes defining a content
 * specification that is part of an ad format.
 */

@Entity
@Table(name="CONTENT_SPEC")
public class ContentSpec extends BusinessKey implements Named {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="CONTENT_SPEC_CONTENT_TYPE",joinColumns=@JoinColumn(name="CONTENT_SPEC_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="CONTENT_TYPE_ID",referencedColumnName="ID"))
    private Set<ContentType> contentTypes;
    @Column(name="MANIFEST",length=4096,nullable=true)
    private String manifest;

    {
	contentTypes = new HashSet<ContentType>();
    }
    
    ContentSpec() {}

    public ContentSpec(String name, String manifest) {
	this.name = name;
	this.manifest = manifest;
    }

    public long getId() { return id; };
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Set<ContentType> getContentTypes() { return contentTypes; }

    /**
     * The manifest is an application-specific formatted value that describes
     * how the application is to identify content that fits the specification.
     * For instance, name/value pairs like "height=300;width=50".
     */
    public String getManifest() { return manifest; }
    public void setManifest(String manifest) { 
	this.manifest = manifest; 
	manifestProps = null;
    }

    // Manifest helpers
    private transient Map<String,String> manifestProps;
    public synchronized Map<String,String> getManifestProperties() {
	if (manifestProps == null) {
            manifestProps = parseManifestProperties(manifest);
	}
	return manifestProps;
    }

    public static Map<String,String> parseManifestProperties(String semiColonSeparated) {
    	Map<String,String> props = new HashMap<String,String>();
        for (String kv : StringUtils.split(semiColonSeparated, ';')) {
            String[] keyAndVal = StringUtils.split(kv, '=');
            if (keyAndVal.length == 2) {
                props.put(keyAndVal[0].trim(), keyAndVal[1].trim());
            }
        }
        return props;
    }

    public boolean isText() { 
	for (ContentType ct : contentTypes) {
	    if (ct.getMIMEType().startsWith("text/")) {
		return true;
	    }
	}
	return false;
    }
}
