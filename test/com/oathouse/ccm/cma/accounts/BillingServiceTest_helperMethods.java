
package com.oathouse.ccm.cma.accounts;

import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cos.bookings.BTBits;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingState;
import static com.oathouse.ccm.cos.config.finance.MultiRefEnum.*;
import com.oathouse.ccm.cos.properties.SystemPropertiesBean;
import com.oathouse.oss.storage.exceptions.IllegalValueException;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.valueholder.SDHolder;
import java.util.HashMap;
import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class BillingServiceTest_helperMethods {
    BillingService service;

    @Before
    public void setUp() throws Exception {
        service = BillingService.getInstance();
    }

    /*
     * bookingSd = spanSd = actualSd
     * chargeMargin = false;
     */

    @Test
    public void test01_getChargeSd() throws Exception {
                // create bean
        int id = 1;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(id++));
        fieldSet.put("owner", ObjectBean.SYSTEM_OWNED);
        fieldSet.put("bookingSd", Integer.toString(SDHolder.getSD(10, 20)));
        fieldSet.put("spanSd", Integer.toString(SDHolder.getSD(10, 20)));
        fieldSet.put("actualStart", Integer.toString(10));
        fieldSet.put("actualEnd", Integer.toString(30));
        fieldSet.put("state", BookingState.AUTHORISED.toString());
        BookingBean booking = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), id, fieldSet);

        new NonStrictExpectations() {
            PropertiesService serviceMock;
            SystemPropertiesBean beanMock;
            {
                PropertiesService.getInstance(); returns(serviceMock);
                serviceMock.getSystemProperties(); returns(beanMock);
                beanMock.isChargeMargin(); result = false;
            }
        };
        int arraySd[] = service.getBookingActuals(booking);
        int actualSd = arraySd[0];
        int chargeSd = arraySd[1];
        assertThat(actualSd, is(SDHolder.getSD(10, 20)));
        assertThat(chargeSd, is(SDHolder.getSD(10, 20)));
        new Verifications() {
            PropertiesService serviceMock;
            SystemPropertiesBean beanMock;
            {
                beanMock.getDropOffChargeMargin(); times = 0;
            }
        };
    }

    /*
     * bookingSd = spanSd
     * actualStart = -1
     * actualEnd = -1
     * chargeMargin = false;
     */

    @Test
    public void test02_getChargeSd() throws Exception {
                // create bean
        int id = 1;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(id++));
        fieldSet.put("owner", ObjectBean.SYSTEM_OWNED);
        fieldSet.put("bookingSd", Integer.toString(SDHolder.getSD(10, 20)));
        fieldSet.put("spanSd", Integer.toString(SDHolder.getSD(10, 20)));
        fieldSet.put("actualStart", Integer.toString(-1));
        fieldSet.put("actualEnd", Integer.toString(-1));
        fieldSet.put("state", BookingState.AUTHORISED.toString());
        BookingBean booking = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), id, fieldSet);

        new NonStrictExpectations() {
            PropertiesService serviceMock;
            SystemPropertiesBean beanMock;
            {
                PropertiesService.getInstance(); returns(serviceMock);
                serviceMock.getSystemProperties(); returns(beanMock);
                beanMock.isChargeMargin(); result = false;
            }
        };
        int arraySd[] = service.getBookingActuals(booking);
        int actualSd = arraySd[0];
        int chargeSd = arraySd[1];
        assertThat(actualSd, is(SDHolder.getSD(10, 20)));
        assertThat(chargeSd, is(SDHolder.getSD(10, 20)));
    }

    /*
     * bookingSd = spanSd
     * actualStart = 9
     * actualEnd = 31
     * chargeMargin = false;
     */

    @Test
    public void test03_getChargeSd() throws Exception {
                // create bean
        int id = 1;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(id++));
        fieldSet.put("owner", ObjectBean.SYSTEM_OWNED);
        fieldSet.put("bookingSd", Integer.toString(SDHolder.getSD(10, 20)));
        fieldSet.put("spanSd", Integer.toString(SDHolder.getSD(10, 20)));
        fieldSet.put("actualStart", Integer.toString(9));
        fieldSet.put("actualEnd", Integer.toString(31));
        fieldSet.put("state", BookingState.AUTHORISED.toString());
        BookingBean booking = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), id, fieldSet);

        new NonStrictExpectations() {
            PropertiesService serviceMock;
            SystemPropertiesBean beanMock;
            {
                PropertiesService.getInstance(); returns(serviceMock);
                serviceMock.getSystemProperties(); returns(beanMock);
                beanMock.isChargeMargin(); result = false;
            }
        };
        int arraySd[] = service.getBookingActuals(booking);
        int actualSd = arraySd[0];
        int chargeSd = arraySd[1];
        assertThat(actualSd, is(SDHolder.getSD(9, 22)));
        assertThat(chargeSd, is(SDHolder.getSD(9, 22)));
    }

    /*
     * bookingSd = spanSd
     * actualStart = 7
     * actualEnd = 35
     * chargeMargin = true;
     * dropoff = 3;
     * pickup = 5;
     */

    @Test
    public void test04_getChargeSd() throws Exception {
                // create bean
        int id = 1;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(id++));
        fieldSet.put("owner", ObjectBean.SYSTEM_OWNED);
        fieldSet.put("bookingSd", Integer.toString(SDHolder.getSD(10, 20)));
        fieldSet.put("spanSd", Integer.toString(SDHolder.getSD(10, 20)));
        fieldSet.put("actualStart", Integer.toString(7));
        fieldSet.put("actualEnd", Integer.toString(35));
        fieldSet.put("state", BookingState.AUTHORISED.toString());
        BookingBean booking = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), id, fieldSet);

        new NonStrictExpectations() {
            PropertiesService serviceMock;
            SystemPropertiesBean beanMock;
            {
                PropertiesService.getInstance(); returns(serviceMock);
                serviceMock.getSystemProperties(); returns(beanMock);
                beanMock.isChargeMargin(); result = true;
                beanMock.getDropOffChargeMargin(); result = 3;
                beanMock.getPickupChargeMargin(); result = 5;
            }
        };
        int arraySd[] = service.getBookingActuals(booking);
        int actualSd = arraySd[0];
        int chargeSd = arraySd[1];
        assertThat(actualSd, is(SDHolder.getSD(10, 20)));
        assertThat(chargeSd, is(SDHolder.getSD(10, 20)));
    }

    /*
     * bookingSd = spanSd
     * actualStart = 6
     * actualEnd = 36
     * chargeMargin = true;
     * dropoff = 3;
     * pickup = 5;
     */

    @Test
    public void test05_getChargeSd() throws Exception {
                // create bean
        int id = 1;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(id++));
        fieldSet.put("owner", ObjectBean.SYSTEM_OWNED);
        fieldSet.put("bookingSd", Integer.toString(SDHolder.getSD(10, 20)));
        fieldSet.put("spanSd", Integer.toString(SDHolder.getSD(10, 20)));
        fieldSet.put("actualStart", Integer.toString(6));
        fieldSet.put("actualEnd", Integer.toString(36));
        fieldSet.put("state", BookingState.AUTHORISED.toString());
        BookingBean booking = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), id, fieldSet);

        new NonStrictExpectations() {
            PropertiesService serviceMock;
            SystemPropertiesBean beanMock;
            {
                PropertiesService.getInstance(); returns(serviceMock);
                serviceMock.getSystemProperties(); returns(beanMock);
                beanMock.isChargeMargin(); result = true;
                beanMock.getDropOffChargeMargin(); result = 3;
                beanMock.getPickupChargeMargin(); result = 5;
            }
        };
        int arraySd[] = service.getBookingActuals(booking);
        int actualSd = arraySd[0];
        int chargeSd = arraySd[1];
        assertThat(actualSd, is(SDHolder.getSD(6, 30)));
        assertThat(chargeSd, is(SDHolder.getSD(6, 30)));
    }

    /*
     * bookingSd = spanSd
     * actualStart = 15
     * actualEnd = 20
     * chargeMargin = false;
     */

    @Test
    public void test06_getChargeSd() throws Exception {
                // create bean
        int id = 1;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(id++));
        fieldSet.put("owner", ObjectBean.SYSTEM_OWNED);
        fieldSet.put("bookingSd", Integer.toString(SDHolder.getSD(10, 20)));
        fieldSet.put("spanSd", Integer.toString(SDHolder.getSD(10, 20)));
        fieldSet.put("actualStart", Integer.toString(15));
        fieldSet.put("actualEnd", Integer.toString(20));
        fieldSet.put("state", BookingState.AUTHORISED.toString());
        BookingBean booking = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), id, fieldSet);

        new NonStrictExpectations() {
            PropertiesService serviceMock;
            SystemPropertiesBean beanMock;
            {
                PropertiesService.getInstance(); returns(serviceMock);
                serviceMock.getSystemProperties(); returns(beanMock);
                beanMock.isChargeMargin(); result = false;
            }
        };
        int arraySd[] = service.getBookingActuals(booking);
        int actualSd = arraySd[0];
        int chargeSd = arraySd[1];
        assertThat(actualSd, is(SDHolder.getSD(10, 20)));
        assertThat(chargeSd, is(SDHolder.getSD(10, 20)));
    }

    /*
     * bookingSd = 10, 19
     * spanSd= 10, 20
     * actualStart = 7
     * actualEnd = 35
     * chargeMargin = true;
     * dropoff = 3;
     * pickup = 5;
     */

    @Test
    public void test07_getChargeSd() throws Exception {
                // create bean
        int id = 1;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(id++));
        fieldSet.put("owner", ObjectBean.SYSTEM_OWNED);
        fieldSet.put("bookingSd", Integer.toString(SDHolder.getSD(10, 19)));
        fieldSet.put("spanSd", Integer.toString(SDHolder.getSD(10, 20)));
        fieldSet.put("actualStart", Integer.toString(7));
        fieldSet.put("actualEnd", Integer.toString(35));
        fieldSet.put("state", BookingState.AUTHORISED.toString());
        BookingBean booking = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), id, fieldSet);

        new NonStrictExpectations() {
            PropertiesService serviceMock;
            SystemPropertiesBean beanMock;
            {
                PropertiesService.getInstance(); returns(serviceMock);
                serviceMock.getSystemProperties(); returns(beanMock);
                beanMock.isChargeMargin(); result = true;
                beanMock.getDropOffChargeMargin(); result = 3;
                beanMock.getPickupChargeMargin(); result = 5;
            }
        };
        int arraySd[] = service.getBookingActuals(booking);
        int actualSd = arraySd[0];
        int chargeSd = arraySd[1];
        assertThat(actualSd, is(SDHolder.getSD(10, 25)));
        assertThat(chargeSd, is(SDHolder.getSD(10, 20)));
    }

    /*
     * check that the convertion to MultiRefEnum from a charge bookingType works
     */

    @Test
    public void test01_getMultiRefFromBookingType() throws Exception {
        int bookingTypeId;
        bookingTypeId = BTIdBits.ATTENDING_STANDARD;
        assertThat(BillingService.getMultiRefFromBookingType(bookingTypeId, PRICE), is(PRICE_STANDARD));
        assertThat(BillingService.getMultiRefFromBookingType(bookingTypeId, ADJUSTMENT), is(ADJUSTMENT_STANDARD));
        assertThat(BillingService.getMultiRefFromBookingType(bookingTypeId, LOYALTY), is(LOYALTY_STANDARD));
        assertThat(BillingService.getMultiRefFromBookingType(bookingTypeId, FIXED), is(NO_VALUE));
        bookingTypeId = BTIdBits.ATTENDING_SPECIAL;
        assertThat(BillingService.getMultiRefFromBookingType(bookingTypeId, PRICE), is(PRICE_SPECIAL));
        assertThat(BillingService.getMultiRefFromBookingType(bookingTypeId, ADJUSTMENT), is(ADJUSTMENT_SPECIAL));
        assertThat(BillingService.getMultiRefFromBookingType(bookingTypeId, LOYALTY), is(LOYALTY_SPECIAL));
        assertThat(BillingService.getMultiRefFromBookingType(bookingTypeId, FIXED), is(NO_VALUE));
        bookingTypeId = BTIdBits.ATTENDING_NOCHARGE;
        assertThat(BillingService.getMultiRefFromBookingType(bookingTypeId, PRICE), is(NO_VALUE));
        assertThat(BillingService.getMultiRefFromBookingType(bookingTypeId, ADJUSTMENT), is(NO_VALUE));
        assertThat(BillingService.getMultiRefFromBookingType(bookingTypeId, LOYALTY), is(NO_VALUE));
        assertThat(BillingService.getMultiRefFromBookingType(bookingTypeId, FIXED), is(NO_VALUE));
        bookingTypeId = BTBits.ATTENDING_BIT;
        try {
            BillingService.getMultiRefFromBookingType(bookingTypeId, PRICE);
            fail("There is no ChargeType so should throw an exception");
        } catch(IllegalValueException ive) {
            // SUCCESS
        }
        bookingTypeId = 0;
        try {
            BillingService.getMultiRefFromBookingType(bookingTypeId, ADJUSTMENT);
            fail("There is no ChargeType so should throw an exception");
        } catch(IllegalValueException ive) {
            // SUCCESS
        }
        bookingTypeId = -1;
        try {
            BillingService.getMultiRefFromBookingType(bookingTypeId, LOYALTY);
            fail("There is no ChargeType so should throw an exception");
        } catch(IllegalValueException ive) {
            // SUCCESS
        }
    }

}