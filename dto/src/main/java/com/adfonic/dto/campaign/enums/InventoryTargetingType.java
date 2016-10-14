package com.adfonic.dto.campaign.enums;

import com.adfonic.domain.Campaign;

public enum InventoryTargetingType {
    EXCHANGE_INVENTORY(Campaign.InventoryTargetingType.RUN_OF_NETWORK), WHITELIST(Campaign.InventoryTargetingType.WHITELIST), CATEGORY(Campaign.InventoryTargetingType.CATEGORY), PRIVATE_MARKET_PLACE(
            Campaign.InventoryTargetingType.PRIVATE_MARKET_PLACE);

    private Campaign.InventoryTargetingType inventoryTargetingType;

    private InventoryTargetingType(Campaign.InventoryTargetingType inventoryTargetingType) {
        this.inventoryTargetingType = inventoryTargetingType;
    }

    public Campaign.InventoryTargetingType getInventoryTargetingType() {
        return inventoryTargetingType;
    }

    public static InventoryTargetingType fromString(String domainEnumName) {
        if (domainEnumName != null) {
            for (InventoryTargetingType b : InventoryTargetingType.values()) {
                if (domainEnumName.equalsIgnoreCase(b.inventoryTargetingType.name())) {
                    return b;
                }
            }
        }
        return null;
    }
}
