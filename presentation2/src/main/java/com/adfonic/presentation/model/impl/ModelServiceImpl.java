package com.adfonic.presentation.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.domain.Model;
import com.adfonic.domain.Model_;
import com.adfonic.dto.devicegroup.DeviceGroupDto;
import com.adfonic.dto.model.ModelDto;
import com.adfonic.dto.publication.platform.PlatformDto;
import com.adfonic.presentation.model.ModelService;
import com.adfonic.presentation.util.DtoToDomainMapperUtils;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@Service("modelService")
public class ModelServiceImpl extends GenericServiceImpl implements ModelService {

	@Autowired
	private DeviceManager deviceManager;
	
    @Autowired
    private org.dozer.Mapper mapper;
    
	private FetchStrategy MODEL_FS = new FetchStrategyBuilder()
         .addLeft(Model_.vendor)
         .addLeft(Model_.deviceGroup)
         .addLeft(Model_.platforms)
     .build();

    public Collection<ModelDto> doQuery(String search,
            List<PlatformDto> platformsDto, 
            DeviceGroupDto deviceGroupDto) {
         FetchStrategy MODEL_FS = new FetchStrategyBuilder()
            .addLeft(Model_.vendor)
            .addLeft(Model_.deviceGroup)
            .addLeft(Model_.platforms)
        .build();
        
        List<Model> models = deviceManager.getModelsByNameAndPlatformAndDeviceGroup(
                search, // name,
                LikeSpec.CONTAINS, // like,
                false, // caseSensitive,
                false, // deleted,
                false, // hidden, @todo need to uncomment when we move to middleware 1.19.x
                true, // prependVendorName,
                DtoToDomainMapperUtils.mapPlatformsDtoToPlatforms(platformsDto, deviceManager), // platforms
                DtoToDomainMapperUtils.mapDeviceGroupDtoToDeviceGroup(deviceGroupDto, deviceManager), // deviceGroup
                MODEL_FS);

        Collection<ModelDto> list = getList(ModelDto.class, models);

        return list;
    }
		
	public ModelDto getModelByName(String name) {

		 FetchStrategy MODEL_FS = new FetchStrategyBuilder()
		.addLeft(Model_.vendor)
		.addLeft(Model_.deviceGroup)
		.addLeft(Model_.platforms)
		.build();
		
		Model entity = deviceManager.getModelByName(name,MODEL_FS);
		if(entity==null){
            return null;
        }
		ModelDto dto = getObjectDto(ModelDto.class, entity);
		return dto;
	}
	
	public ModelDto getModelById(Long id){
	    Model entity = deviceManager.getModelById(id,MODEL_FS);
        ModelDto dto = getObjectDto(ModelDto.class, entity);
        return dto;
	}
	
	public List<ModelDto> getModelsByName(String modelName){
	    FetchStrategy MODEL_FS = new FetchStrategyBuilder()
        .addLeft(Model_.vendor)
        .addLeft(Model_.deviceGroup)
        .addLeft(Model_.platforms)
        .build();
        
	    List<Model> entities = deviceManager.getModelsByName(modelName, null, false, MODEL_FS);

        List<ModelDto> models = new ArrayList<ModelDto>();
        for(Model m : entities){
            models.add(getObjectDto(ModelDto.class, m));
        }
        return models;
	}        
	
	public List<ModelDto> getModelsByNameAndVendor(String vendorModelName){
	    FetchStrategy MODEL_FS = new FetchStrategyBuilder()
        .addLeft(Model_.vendor)
        .addLeft(Model_.deviceGroup)
        .addLeft(Model_.platforms)
        .build();
        
        List<Model> entities = deviceManager.getModelsByName(vendorModelName, null, false, false, false, true, MODEL_FS);

        List<ModelDto> models = new ArrayList<ModelDto>();
        for(Model m : entities){
            models.add(getObjectDto(ModelDto.class, m));
        }
        
        return models;
	}
	
    public List<ModelDto> getModelsByVendorNameAndPlatformAndDeviceGroup(String vendorName, List<PlatformDto> platformsDto, DeviceGroupDto deviceGroupDto) {
        FetchStrategy MODEL_FS = new FetchStrategyBuilder().addLeft(Model_.vendor).addLeft(Model_.deviceGroup).addLeft(Model_.platforms).build();
        
        List<Model> entities = deviceManager.getModelsByVendorNameAndPlatformAndDeviceGroup(vendorName,
                DtoToDomainMapperUtils.mapPlatformsDtoToPlatforms(platformsDto, deviceManager),
                DtoToDomainMapperUtils.mapDeviceGroupDtoToDeviceGroup(deviceGroupDto, deviceManager), MODEL_FS);

        List<ModelDto> models = new ArrayList<ModelDto>();
        for (Model m : entities) {
            models.add(getObjectDto(ModelDto.class, m));
        }

        return models;
    }
    
    public <T> T getObjectDto(final Class<T> type, final Object source) {
        // Mapper mapper = new DozerBeanMapper();
        return mapper.map(source, type);
    }

}
