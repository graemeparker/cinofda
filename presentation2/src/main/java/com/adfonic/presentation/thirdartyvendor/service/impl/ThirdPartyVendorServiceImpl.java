package com.adfonic.presentation.thirdartyvendor.service.impl;

import static com.byyd.middleware.iface.dao.SortOrder.asc;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.ThirdPartyVendor;
import com.adfonic.dto.campaign.campaignbid.ThirdPartyVendorDto;
import com.adfonic.presentation.thirdartyvendor.service.ThirdPartyVendorService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.common.filter.ThirdPartyVendorFilter;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.iface.dao.Sorting;

@Service("thirdPartyVendorService")
public class ThirdPartyVendorServiceImpl extends GenericServiceImpl implements ThirdPartyVendorService {

	@Autowired
	private CommonManager commonManager;

	@Override
	@Transactional(readOnly = true)
	public ThirdPartyVendorDto getThirdPartyVendorDtoById(long thirdPartyVendorId) {
		return getDtoObject(ThirdPartyVendorDto.class, commonManager.getObjectById(ThirdPartyVendor.class, thirdPartyVendorId));
	}

	@Override
	@Transactional(readOnly = true)
	public List<ThirdPartyVendorDto> getAllThirdPartyVendors() {
		List<ThirdPartyVendor> thirdPartyVendors = commonManager.getAllThirdPartyVendors(null, new Sorting(asc(ThirdPartyVendor.class, "name")));
		return getDtoList(ThirdPartyVendorDto.class, thirdPartyVendors);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<ThirdPartyVendorDto> getAllThirdPartyVendorsByTypeIds(Set<Long> thirdPartyVendorTypeIds) {
		List<ThirdPartyVendor> thirdPartyVendors = commonManager.getAllThirdPartyVendors(
				new ThirdPartyVendorFilter().setThirdPartyVendorTypeIds(thirdPartyVendorTypeIds), new Sorting(asc(ThirdPartyVendor.class, "name")));
		return getDtoList(ThirdPartyVendorDto.class, thirdPartyVendors);
	}
	
}