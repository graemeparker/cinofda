package com.adfonic.presentation.thirdartyvendor.service.impl;

import static com.byyd.middleware.iface.dao.SortOrder.asc;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.ThirdPartyVendorType;
import com.adfonic.dto.campaign.campaignbid.ThirdPartyVendorTypeDto;
import com.adfonic.presentation.thirdartyvendor.service.ThirdPartyVendorTypeService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.iface.dao.Sorting;

@Service("thirdPartyVendorTypeService")
public class ThirdPartyVendorTypeServiceImpl extends GenericServiceImpl implements ThirdPartyVendorTypeService {

	@Autowired
	private CommonManager commonManager;

	@Override
	@Transactional(readOnly = true)
	public ThirdPartyVendorTypeDto getThirdPartyVendorTypeDtoById(long thirdPartyVendorTypeId) {
		return getDtoObject(ThirdPartyVendorTypeDto.class, commonManager.getObjectById(ThirdPartyVendorType.class, thirdPartyVendorTypeId));
	}

	@Override
	@Transactional(readOnly = true)
	public List<ThirdPartyVendorTypeDto> getAllThirdPartyVendorTypes() {
		List<ThirdPartyVendorType> thirdPartyVendorTypes = commonManager.getAllThirdPartyVendorTypes(null, new Sorting(asc(ThirdPartyVendorType.class, "name")));
		return getDtoList(ThirdPartyVendorTypeDto.class, thirdPartyVendorTypes);
	}
}