/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.accounts;

// common imports
import com.oathouse.ccm.cma.VABoolean;
import com.oathouse.ccm.cma.config.PriceConfigService;
import com.oathouse.ccm.cma.config.TimetableService;
import com.oathouse.ccm.cos.config.TimetableBean;
import com.oathouse.ccm.cos.config.TimetableManager;
import com.oathouse.ccm.cos.config.education.ChildEducationTimetableBean;
import com.oathouse.ccm.cos.config.education.ChildEducationTimetableManager;
import com.oathouse.ccm.cos.config.finance.MultiRefEnum;
import com.oathouse.ccm.cos.config.finance.PriceListBean;
import com.oathouse.ccm.cos.config.finance.TariffBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
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
public class BillingServiceTest_verifyTotalValue {
    private final String owner = ObjectBean.SYSTEM_OWNED;
    private BillingService service;

    @Mocked
    private PriceConfigService priceConfigServiceMock;
    @Mocked
    private  TimetableService timetableServiceMock;

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

        new Expectations() {
            {
                PriceConfigService.getInstance(); returns(priceConfigServiceMock);
                TimetableService.getInstance(); returns(timetableServiceMock);
            }
        };

    }

    /*
     *
     */
    @Test
    public void verifyTotalValue_runThrough_noEducation() throws Exception {
        final int ywd = 11;
        final int priceGroupMr = 13;
        final int chargeSd = 17;
        final int childEducationTimetableId = -1;
        final MultiRefEnum priceListType = MultiRefEnum.PRICE;
        // internal
        final TariffBean priceTariff = (TariffBean) BeanBuilder.addBeanValues(new TariffBean());
        final long sessionValue = 23;
        new Expectations() {
            @Mocked PriceListBean priceListMock;
            {
                priceConfigServiceMock.getPriceTariff(ywd, priceGroupMr); returns(priceTariff);
                priceConfigServiceMock.getPriceList(priceTariff.getTariffId(), priceListType); returns(priceListMock);
                priceListMock.getPeriodSdSum(chargeSd); result = sessionValue;
            }
        };
        // run
        long result = service.verifyTotalValue(ywd, priceGroupMr, chargeSd, childEducationTimetableId, priceListType);
        assertThat(result, is(sessionValue));
    }

    /*
     *
     */
    @Test
    public void verifyTotalValue_runThrough_withEducation() throws Exception {
        final int ywd = 11;
        final int priceGroupMr = 13;
        final int chargeSd = SDHolder.getSD(10, 20);
        final int childEducationTimetableId = 23;
        final MultiRefEnum priceListType = MultiRefEnum.PRICE;
        // internal
        final TariffBean priceTariff = (TariffBean) BeanBuilder.addBeanValues(new TariffBean());
        final long edValue = 23;
        final long sessionValue = 29;
        final Set<Integer> educationSdSet = VABoolean.asSet(chargeSd);
        new Expectations() {
            @Mocked PriceListBean priceListMock;
            @Mocked ChildEducationTimetableManager timetableManagerMock;
            @Mocked ChildEducationTimetableBean timetableMock;
            {
                priceConfigServiceMock.getPriceTariff(ywd, priceGroupMr); returns(priceTariff);
                priceConfigServiceMock.getPriceList(priceTariff.getTariffId(), priceListType); returns(priceListMock);
                priceListMock.getPeriodSdSum(chargeSd); result = sessionValue;
                timetableServiceMock.getChildEducationTimetableManager(); returns(timetableManagerMock);
                timetableManagerMock.getObject(childEducationTimetableId); returns(timetableMock);
                timetableMock.getAllEducationSd(); result = educationSdSet;
                priceConfigServiceMock.getChildEducationPriceReduction(ywd, childEducationTimetableId); returns(priceListMock);
                priceListMock.getPeriodSdSum(chargeSd); result = edValue;
            }
        };
        // run
        long result = service.verifyTotalValue(ywd, priceGroupMr, chargeSd, childEducationTimetableId, priceListType);
        assertThat(result, is(sessionValue - edValue));
    }

}
