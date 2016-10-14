package com.adfonic.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="EXTENDED_CREATIVE_TEMPLATE" /*, uniqueConstraints={@UniqueConstraint(columnNames="CREATIVE_ID, CONTENT_FORM")} */)
public class ExtendedCreativeTemplate extends BusinessKey {
	
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    
    public static final String beaconTemplate = "#if($renderBeacons && $adComponents.components.beacons)\n" +
			    								"#set($Integer = 0)\n" +
			    								"#foreach ($idx in [1..$Integer.parseInt($adComponents.components.beacons.numBeacons)])\n" +
			    								"<!-- Beacon ${idx} -->\n" +
			    								"<img width=1 height=1 src=\"#evaluate(\"$adComponents.components.beacons.beacon$idx\")\"/>\n" +
			    								"#end\n" +
			    								"#end\n";
    
    public static final String htmlMediaTypeDynamiTemplateHrefPrependString = "${adComponents.destinationUrl}?redir=";
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CREATIVE_ID",nullable=false)
    private Creative creative;
    
    @Column(name="CONTENT_FORM",nullable=false)
    @Enumerated(EnumType.STRING)
    private ContentForm contentForm;

    @Column(name="TEMPLATE_ORIGINAL",length=30000,nullable=false)
    private String templateOriginal;

    @Column(name="TEMPLATE_PREPROCESSED",length=30000,nullable=false)
    private String templatePreprocessed;

    public long getId() { return id; }

	public Creative getCreative() {
		return creative;
	}

	public void setCreative(Creative creative) {
		this.creative = creative;
	}

	public ContentForm getContentForm() {
		return contentForm;
	}

	public void setContentForm(ContentForm contentForm) {
		this.contentForm = contentForm;
	}

	public String getTemplateOriginal() {
		return templateOriginal;
	}

	public void setTemplateOriginal(String templateOriginal) {
		this.templateOriginal = templateOriginal;
	}

	public String getTemplatePreprocessed() {
		return templatePreprocessed;
	}

	public void setTemplatePreprocessed(String templatePreprocessed) {
		this.templatePreprocessed = templatePreprocessed;
	};

    public String getBeaconTemplate() {
    	return beaconTemplate;
    }
    
}
