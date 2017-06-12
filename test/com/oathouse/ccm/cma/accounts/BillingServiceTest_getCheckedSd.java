/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.accounts;

// common imports
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cos.properties.SystemPropertiesBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.valueholder.SDHolder;
import java.io.File;
import java.util.*;
import static java.util.Arrays.*;
// Test Imports
import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class BillingServiceTest_getCheckedSd {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private BillingService service;

    @Before
    public void setUp() throws Exception {
        String authority = ObjectBean.SYSTEM_OWNED;
        String sep = File.separator;
        String rootStorePath = "." + sep + "oss" + sep + "data";
        OssProperties props = OssProperties.getInstance();
        props.setConnection(OssProperties.Connection.FILE);
        props.setStorePath(rootStorePath);
        props.setAuthority(authority);
        props.setLogConfigFile(rootStorePath + sep + "conf" + sep + "oss_log4j.properties");
        // reset
        ObjectDBMS.clearAuthority(authority);
        // global instances
        service = BillingService.getInstance();
        service.clear();

    }

    /*
     *
     */
    @Test
    public void getCheckedSd_noChargeMargin_noDifference() throws Exception {
        final int periodSd = SDHolder.getSD(10, 19);
        final int actualSd = periodSd;
        setPropertiesMock(false);
        int result = service.getCheckedSd(periodSd, actualSd);
        assertThat(result, is(periodSd));
    }

    /*
     *
     */
    @Test
    public void getCheckedSd_noChargeMargin_startLessEndLess() throws Exception {
        final int periodSd = SDHolder.getSD(10, 19);
        final int actualSd = SDHolder.getSD(9, 19);
        final int controlSd = SDHolder.getSD(9, 20);
        setPropertiesMock(false);
        int result = service.getCheckedSd(periodSd, actualSd);
        assertThat(result, is(controlSd));
    }

    /*
     *
     */
    @Test
    public void getCheckedSd_noChargeMargin_startLessEndMore() throws Exception {
        final int periodSd = SDHolder.getSD(10, 19);
        final int actualSd = SDHolder.getSD(9, 19);
        final int controlSd = SDHolder.getSD(9, 20);
        setPropertiesMock(false);
        int result = service.getCheckedSd(periodSd, actualSd);
        assertThat(result, is(controlSd));
    }

    /*
     *
     */
    @Test
    public void getCheckedSd_noChargeMargin_startMoreEndMore() throws Exception {
        final int periodSd = SDHolder.getSD(10, 19);
        final int actualSd = SDHolder.getSD(11, 19);
        final int controlSd = SDHolder.getSD(10, 20);
        setPropertiesMock(false);
        int result = service.getCheckedSd(periodSd, actualSd);
        assertThat(result, is(controlSd));
    }

    /*
     *
     */
    @Test
    public void getCheckedSd_noChargeMargin_startMoreEndLess() throws Exception {
        final int periodSd = SDHolder.getSD(10, 19);
        final int actualSd = SDHolder.getSD(11, 17);
        final int controlSd = SDHolder.getSD(10, 19);
        setPropertiesMock(false);
        int result = service.getCheckedSd(periodSd, actualSd);
        assertThat(result, is(controlSd));
    }

    /*
     *
     */
    @Test
    public void getCheckedSd_withChargeMargin_noDifference() throws Exception {
        final int periodSd = SDHolder.getSD(10, 19);
        final int actualSd = periodSd;
        final int controlSd = periodSd;
        setPropertiesMock(true);
        int result = service.getCheckedSd(periodSd, actualSd);
        assertThat(result, is(controlSd));
    }

    /*
     *
     */
    @Test
    public void getCheckedSd_withChargeMargin_startInEndIn() throws Exception {
        // margins are drop-off 2, pick-up 2
        final int periodSd = SDHolder.buildSD(10, 20);
        final int actualSd = SDHolder.buildSD(9, 21);
        final int controlSd = SDHolder.buildSD(10, 20);
        setPropertiesMock(true);
        int result = service.getCheckedSd(periodSd, actualSd);
        assertThat(result, is(controlSd));
    }

    /*
     *
     */
    @Test
    public void getCheckedSd_withChargeMargin_startOutEndOut() throws Exception {
        // margins are drop-off 2, pick-up 2
        final int periodSd = SDHolder.buildSD(10, 20);
        final int actualSd = SDHolder.buildSD(7, 23);
        final int controlSd = SDHolder.buildSD(7, 23);
        setPropertiesMock(true);
        int result = service.getCheckedSd(periodSd, actualSd);
        assertThat(result, is(controlSd));
    }

    //<editor-fold defaultstate="collapsed" desc="Mock Beans">
    private void setPropertiesMock(boolean chargeMargin) throws Exception {
        int seed = 1;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(1));
        if(!chargeMargin){
            fieldSet.put("chargeMargin", Boolean.toString(false));
        }
        fieldSet.put("dropOffChargeMargin", Integer.toString(2));
        fieldSet.put("pickupChargeMargin", Integer.toString(2));
        final SystemPropertiesBean properties = (SystemPropertiesBean) BeanBuilder.addBeanValues(new SystemPropertiesBean(), seed++, fieldSet);
        new Expectations() {
            private @Cascading PropertiesService propertiesServiceMock;
            {
                PropertiesService.getInstance(); returns(propertiesServiceMock);
                propertiesServiceMock.getSystemProperties(); result = properties;
            }
        };
    }
    //</editor-fold>
}
