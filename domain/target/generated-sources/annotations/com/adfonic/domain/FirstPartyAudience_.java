package com.adfonic.domain;

import com.adfonic.domain.FirstPartyAudience.Type;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(FirstPartyAudience.class)
public abstract class FirstPartyAudience_ {

	public static volatile SingularAttribute<FirstPartyAudience, Audience> audience;
	public static volatile SetAttribute<FirstPartyAudience, Campaign> campaigns;
	public static volatile SingularAttribute<FirstPartyAudience, Long> muidSegmentId;
	public static volatile SingularAttribute<FirstPartyAudience, Boolean> active;
	public static volatile SetAttribute<FirstPartyAudience, FirstPartyAudienceDeviceIdsUploadHistory> deviceIdsUploadHistory;
	public static volatile SingularAttribute<FirstPartyAudience, Long> id;
	public static volatile SingularAttribute<FirstPartyAudience, Type> type;

}

