package com.adfonic.tools.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import com.adfonic.dto.campaign.search.CampaignSearchDto;
import com.adfonic.dto.campaign.typeahead.CampaignTypeAheadDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.tools.beans.campaign.navigation.CampaignNavigationSessionBean;
import com.adfonic.tools.beans.user.UserSessionBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.Utils;

public class DuplicatedNameValidator extends GenericValidator {

    @Override
    public void validate(FacesContext context, UIComponent componet, Object value) throws ValidatorException {

        CampaignNavigationSessionBean bean = Utils.findBean(context, Constants.CAMPAIGN_NAVIGATION_BEAN);
        // validation is only for new campaigns
        if (bean.isSchedulingDisabled()) {
            if (!isValid((String) value, context)) {
                FacesMessage fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, null,
                        "page.error.validation.duplicatednamme");

                throw new ValidatorException(fm);
            }
        }
    }

    private boolean isValid(String name, FacesContext context) {
        if (name != null) {
            CampaignService service = getSpringService(context, com.adfonic.presentation.campaign.CampaignService.class);
            CampaignSearchDto dto = new CampaignSearchDto();
            dto.setName(name);
            UserSessionBean bean = Utils.findBean(context, Constants.USER_SESSION_BEAN);
            UserDTO userDto = (UserDTO) bean.getMap().get(Constants.USERDTO);
            dto.setAdvertiser(userDto.getAdvertiserDto());
            CampaignTypeAheadDto obj = service.getCampaignWithName(dto);
            if (obj != null && obj.getId() != null && obj.getId() > 0) {
                return false;
            }
        }
        return true;
    }

}
