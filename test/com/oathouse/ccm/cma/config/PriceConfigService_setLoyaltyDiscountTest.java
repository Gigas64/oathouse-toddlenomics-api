
package com.oathouse.ccm.cma.config;

import com.oathouse.ccm.cos.bookings.BookingManager;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import com.oathouse.ccm.cos.config.finance.LoyaltyDiscountBean;
import com.oathouse.oss.storage.exceptions.IllegalActionException;
import com.oathouse.oss.storage.exceptions.IllegalValueException;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Darryl Oatridge
 */
public class PriceConfigService_setLoyaltyDiscountTest {

    // service
    private PriceConfigService service;

    // method parameters
    private int loyaltyDiscountId = -1;
    private String name = "name";
    private int billingBits = 0;
    private long discount = 23;
    private int start = -1;
    private int durationIn = 540;
    private boolean[] priorityDays = BeanBuilder.getDays(BeanBuilder.MON_TO_FRI);
    private String owner = ObjectBean.SYSTEM_OWNED;


    @Before
    public void setUp() throws Exception {
        service = PriceConfigService.getInstance();
    }

    /*
     * test loyalty discount removable
     */
    @Test
    public void test01_setLoyaltyDiscount() throws Exception {
        new NonStrictExpectations() {
            private @Mocked({"isLoyaltyDiscountRemovable"}) PriceConfigService serviceMock;
            {
                serviceMock.isLoyaltyDiscountRemovable(anyInt); result = false;
            }
        };
        try {
            service.setLoyaltyDiscount(loyaltyDiscountId, name, billingBits, discount, start, durationIn, priorityDays, owner);
            fail();
        } catch(IllegalActionException e) {
            assertThat(e.getMessage(), is("The Loyalty Discount is in use and can not be modified"));
        }
    }

    /*
     * test the startis less than -1
     */
    @Test
    public void test02_setLoyaltyDiscount() throws Exception {
        new NonStrictExpectations() {
            private @Mocked({"isLoyaltyDiscountRemovable"}) PriceConfigService serviceMock;
            {
                serviceMock.isLoyaltyDiscountRemovable(anyInt); result = true;
            }
        };
        try {
            start = -2;
            service.setLoyaltyDiscount(loyaltyDiscountId, name, billingBits, discount, start, durationIn, priorityDays, owner);
            fail();
        } catch(IllegalValueException e) {
            assertThat(e.getMessage(), is("The start value is out of range"));
        }
    }

    /*
     * test the start is > longest period
     */
    @Test
    public void test03_setLoyaltyDiscount() throws Exception {
        new NonStrictExpectations() {
            private @Mocked({"isLoyaltyDiscountRemovable"}) PriceConfigService serviceMock;
            {
                serviceMock.isLoyaltyDiscountRemovable(anyInt); result = true;
            }
        };
        try {
            start = BookingManager.MAX_PERIOD_LENGTH + 1;
            service.setLoyaltyDiscount(loyaltyDiscountId, name, billingBits, discount, start, durationIn, priorityDays, owner);
            fail();
        } catch(IllegalValueException e) {
            assertThat(e.getMessage(), is("The start value is out of range"));
        }
    }

    /*
     * test duration less than 0
     */
    @Test
    public void test04_setLoyaltyDiscount() throws Exception {
        new NonStrictExpectations() {
            private @Mocked({"isLoyaltyDiscountRemovable"}) PriceConfigService serviceMock;
            {
                serviceMock.isLoyaltyDiscountRemovable(anyInt); result = true;
            }
        };
        try {
            durationIn = -1;
            service.setLoyaltyDiscount(loyaltyDiscountId, name, billingBits, discount, start, durationIn, priorityDays, owner);
            fail();
        } catch(IllegalValueException e) {
            assertThat(e.getMessage(), is("The duration value must be a positive value"));
        }
    }

    /*
     * test duration can't exceed day
     */
    @Test
    public void test06_setLoyaltyDiscount() throws Exception {
        new NonStrictExpectations() {
            private @Mocked({"isLoyaltyDiscountRemovable"}) PriceConfigService serviceMock;
            {
                serviceMock.isLoyaltyDiscountRemovable(anyInt); result = true;
            }
        };
        billingBits = BillingEnum.getBillingBits(BillingEnum.RANGE_AT_MOST);
        // inside range
        durationIn = BookingManager.MAX_PERIOD_LENGTH;
        service.setLoyaltyDiscount(loyaltyDiscountId, name, billingBits, discount, start, durationIn, priorityDays, owner);
        // out of range
        durationIn = BookingManager.MAX_PERIOD_LENGTH + 2;
        try {
            service.setLoyaltyDiscount(loyaltyDiscountId, name, billingBits, discount, start, durationIn, priorityDays, owner);
            fail();
        } catch(IllegalValueException e) {
            assertThat(e.getMessage(), is("The duration is greater than the maximum period length in a day"));
        }
    }

    /*
     * test duration can't exceed week
     */
    @Test
    public void test07_setLoyaltyDiscount() throws Exception {
        new NonStrictExpectations() {
            private @Mocked({"isLoyaltyDiscountRemovable"}) PriceConfigService serviceMock;
            {
                serviceMock.isLoyaltyDiscountRemovable(anyInt); result = true;
            }
        };
        billingBits = BillingEnum.getBillingBits(BillingEnum.RANGE_SUM_TOTAL);
        //inside range
        durationIn = (BookingManager.MAX_PERIOD_LENGTH *  7);
        service.setLoyaltyDiscount(loyaltyDiscountId, name, billingBits, discount, start, durationIn, priorityDays, owner);
        //out of range
        durationIn = (BookingManager.MAX_PERIOD_LENGTH *  7)  + 2;
        try {
            service.setLoyaltyDiscount(loyaltyDiscountId, name, billingBits, discount, start, durationIn, priorityDays, owner);
            fail();
        } catch(IllegalValueException e) {
            assertThat(e.getMessage(), is("The duration is greater than the maximum period length in a week"));
        }
    }

    /*
     *
     */
    @Test
    public void setLoyaltyDiscount_bitSet() throws Exception {
        new NonStrictExpectations() {
            private @Mocked({"isLoyaltyDiscountRemovable"}) PriceConfigService serviceMock;
            {
                serviceMock.isLoyaltyDiscountRemovable(anyInt); result = true;
            }
        };
        billingBits = BillingEnum.getBillingBits(BillingEnum.RANGE_SUM_TOTAL, BillingEnum.APPLY_DISCOUNT);
        LoyaltyDiscountBean loyalty = service.setLoyaltyDiscount(loyaltyDiscountId, name, billingBits, discount, start, durationIn, priorityDays, owner);
        assertThat(loyalty.hasBillingBit(BillingEnum.APPLY_DISCOUNT), is(true));
    }


}