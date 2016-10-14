package com.adfonic.domain;

import com.adfonic.domain.RtbConfig.AdmProfile;
import com.adfonic.domain.RtbConfig.DecryptionScheme;
import com.adfonic.domain.RtbConfig.RtbAdMode;
import com.adfonic.domain.RtbConfig.RtbAuctionType;
import com.adfonic.domain.RtbConfig.RtbImpTrackMode;
import com.adfonic.domain.RtbConfig.RtbWinNoticeMode;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(RtbConfig.class)
public abstract class RtbConfig_ {

	public static volatile SingularAttribute<RtbConfig, String> bidCurrency;
	public static volatile SingularAttribute<RtbConfig, String> integrationTypePrefix;
	public static volatile SingularAttribute<RtbConfig, RtbWinNoticeMode> winNoticeMode;
	public static volatile SingularAttribute<RtbConfig, AdmProfile> admProfile;
	public static volatile SingularAttribute<RtbConfig, String> settlementPriceMacro;
	public static volatile SingularAttribute<RtbConfig, RtbAdMode> adMode;
	public static volatile SingularAttribute<RtbConfig, String> secAlias;
	public static volatile SingularAttribute<RtbConfig, String> dpidFallback;
	public static volatile SingularAttribute<RtbConfig, Boolean> sslRequired;
	public static volatile SingularAttribute<RtbConfig, RtbImpTrackMode> impTrackMode;
	public static volatile SingularAttribute<RtbConfig, String> escapedClickForwardUrl;
	public static volatile SingularAttribute<RtbConfig, String> clickForwardValidationPattern;
	public static volatile SingularAttribute<RtbConfig, Integer> rtbExpirySeconds;
	public static volatile SingularAttribute<RtbConfig, String> escapedUrlPrefix;
	public static volatile SingularAttribute<RtbConfig, Long> id;
	public static volatile SingularAttribute<RtbConfig, RtbAuctionType> auctionType;
	public static volatile SingularAttribute<RtbConfig, DecryptionScheme> decryptionScheme;

}

