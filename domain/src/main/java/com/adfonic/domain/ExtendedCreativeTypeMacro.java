package com.adfonic.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="EXTENDED_CREATIVE_TYPE_MACRO")
public class ExtendedCreativeTypeMacro extends BusinessKey {
	   private static final long serialVersionUID = 1L;

	    @Id @GeneratedValue @Column(name="ID")
	    private long id;

	    @ManyToOne(fetch=FetchType.LAZY)
	    @JoinColumn(name="EXTENDED_CREATIVE_TYPE_ID",nullable=false)
	    private ExtendedCreativeType extendedCreativeType;
	    
	    @Column(name="MATCH_STRING",length=64,nullable=false)
	    private String matchString;

	    @Column(name="REPLACEMENT_STRING",length=128,nullable=false)
	    private String replacementString;

	    public long getId() { return id; }

		public ExtendedCreativeType getExtendedCreativeType() {
			return extendedCreativeType;
		}

		public void setExtendedCreativeType(ExtendedCreativeType extendedCreativeType) {
			this.extendedCreativeType = extendedCreativeType;
		}

		public String getMatchString() {
			return matchString;
		}

		public void setMatchString(String matchString) {
			this.matchString = matchString;
		}

		public String getReplacementString() {
			return replacementString;
		}

		public void setReplacementString(String replacementString) {
			this.replacementString = replacementString;
		};

	    
}
