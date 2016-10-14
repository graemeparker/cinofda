package com.adfonic.domain;

import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(FirstPartyAudienceDeviceIdsUploadHistory.class)
public abstract class FirstPartyAudienceDeviceIdsUploadHistory_ {

	public static volatile SingularAttribute<FirstPartyAudienceDeviceIdsUploadHistory, FirstPartyAudience> firstPartyAudience;
	public static volatile SingularAttribute<FirstPartyAudienceDeviceIdsUploadHistory, String> filename;
	public static volatile SingularAttribute<FirstPartyAudienceDeviceIdsUploadHistory, Date> dateTimeUploaded;
	public static volatile SingularAttribute<FirstPartyAudienceDeviceIdsUploadHistory, Long> numInsertedRecords;
	public static volatile SingularAttribute<FirstPartyAudienceDeviceIdsUploadHistory, Long> totalNumRecords;
	public static volatile SingularAttribute<FirstPartyAudienceDeviceIdsUploadHistory, DeviceIdentifierType> deviceIdentifierType;
	public static volatile SingularAttribute<FirstPartyAudienceDeviceIdsUploadHistory, Long> id;
	public static volatile SingularAttribute<FirstPartyAudienceDeviceIdsUploadHistory, Long> numValidatedRecords;

}

