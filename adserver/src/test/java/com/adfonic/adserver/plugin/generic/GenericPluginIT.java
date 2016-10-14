package com.adfonic.adserver.plugin.generic;

import org.junit.Test;

import com.adfonic.adserver.plugin.AbstractPluginIT;

public class GenericPluginIT extends AbstractPluginIT {
    private final GenericPlugin genericPlugin = new GenericPlugin();
        
    @Test
    public void test_banner() {
        //testPlugin(genericPlugin, "mpu", "iab300x250", "plugin://generic?click=http%3A%2F%2Feu-ad.sam4m.com%2F2-7-1-23-AbZ98%2Fclick&image=http%3A%2F%2Feu-ad.sam4m.com%2F2-7-1-23-AbZ98%2Fdisplay");
        //testPlugin(genericPlugin,  "xl",     "300x50", "plugin://generic?click=http%3A%2F%2Fservedby.flashtalking.com%2Fclick%2F1%2F23973%3B471030%3B0%3B209%3B0%2F%3Fft_width%3D300%26ft_height%3D50%26url%3D2893621&image=http%3A%2F%2Fservedby.flashtalking.com%2Fimp%2F1%2F23973%3B471030%3B205%3Bgif%3BAdfonic%3B300x50%2F%3F");
        
        testPlugin(genericPlugin,  "xl",     "300x50", "plugin://generic?ord=123&click=http%3A%2F%2Fad.doubleclick.net%2Fjump%2FN7312.480832.ADFONIC.COM1%2FB7594507%3Bsz%3D300x50%3Bdcove%3Dr&image=http%3A%2F%2Fad.doubleclick.net%2Fad%2FN7312.480832.ADFONIC.COM1%2FB7594507%3Bsz%3D300x50%3Bdcove%3Dr");
    }
}
