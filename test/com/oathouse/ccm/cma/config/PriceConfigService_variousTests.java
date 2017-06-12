/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.config;

import static com.oathouse.ccm.cma.VABoolean.asSet;
import com.oathouse.ccm.cma.VT;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.config.AgeStartBean;
import com.oathouse.ccm.cos.config.DayRangeBean;
import com.oathouse.ccm.cos.config.DayRangeManager;
import com.oathouse.ccm.cos.config.RoomConfigBean;
import com.oathouse.ccm.cos.config.education.ChildEducationTimetableBean;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.ccm.cos.config.finance.LoyaltyDiscountBean;
import com.oathouse.ccm.cos.config.finance.MultiRefEnum;
import com.oathouse.ccm.cos.config.finance.PriceListManager;
import com.oathouse.ccm.cos.config.finance.TariffManager;
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.exceptions.IllegalActionException;
import com.oathouse.oss.storage.exceptions.IllegalValueException;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.oathouse.oss.storage.valueholder.MRHolder;
import com.oathouse.oss.storage.valueholder.SDHolder;
import com.oathouse.oss.storage.valueholder.YWDHolder;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import mockit.Cascading;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Darryl Oatridge
 */
public class PriceConfigService_variousTests {

    //<editor-fold defaultstate="collapsed" desc="Setup and Declarations">
    private String sep = File.separator;
    private String rootStorePath = "." + sep + "oss" + sep + "data";
    private String owner = ObjectBean.SYSTEM_OWNED;

    private int priceTariffId = 101;
    private int loyaltyTariffId = 202;
    private int roomId = 1;
    private int AgeStartId = 3;
    private int priceRoomMr = MRHolder.getMR(MultiRefEnum.ROOM.type(), roomId);
    private int priceAgeMr = MRHolder.getMR(MultiRefEnum.AGE.type(), AgeStartId);

    private int pricelistId01 = 1;
    private int pricelistId02 = 2;
    private int priceAdjId01 = 1;
    private int priceAdjId02 = 2;
    private int priceAdjId03 = 3;
    private int loyaltyId01 = 1;
    private int loyaltyId02 = 2;

    private boolean[] workingDays = BeanBuilder.getDays(BeanBuilder.MON_TO_FRI);

    private int priceRoomDayRangeId;
    private int priceAgeDayRangeId01;
    private int priceAgeDayRangeId02;
    private int loyaltyRoomDayRangeId01;
    private int loyaltyRoomDayRangeId02;

    private @Cascading
            AgeRoomService ageRoomServiceMock;
    private @Cascading
            TimetableService timetableServiceMock;
    private @Cascading
            PropertiesService propertiesServiceMock;
    private @Cascading
            ChildService childServiceMock;

    private PriceConfigService service;

    @Before
    public void setUp() throws Exception {
        OssProperties props = OssProperties.getInstance();
        props.setConnection(OssProperties.Connection.FILE);
        props.setStorePath(rootStorePath);
        props.setAuthority(owner);
        props.setLogConfigFile(rootStorePath + sep + "conf" + sep + "oss_log4j.properties");

        new Expectations() {
            {
                AgeRoomService.getInstance();
                returns(ageRoomServiceMock);
                TimetableService.getInstance();
                returns(timetableServiceMock);
                ageRoomServiceMock.getRoomConfigManager().getAllIdentifier();
                returns(asSet(roomId));
                timetableServiceMock.getChildEducationTimetableManager().getAllIdentifier();
                returns(asSet());
            }
        };
        service = PriceConfigService.getInstance();
        assertEquals(true, service.clear());
        // set a couple of age groups
        service.setAgeStart(AgeStartId, "0-1 age", 0, owner);
    }

    //</editor-fold>


    @Test
    public void testMakePriceTariff() throws Exception {
        setAllConfig();
        setTariff();
        setDayRange();
    }

    /*
     * test to ensure this works
     */
    @Test
    public void isChildEducationPriceReductionRemovable() throws Exception {
        int ywd = CalendarStatic.getRelativeYW(0);
        boolean[] days = BeanBuilder.getDays(BeanBuilder.MON_ONLY);
        int dayRangeId = 13;
        int reductionId = 17;
        int cetId = 19;
        DayRangeBean dayRange = service.getChildEducationPriceReductionRangeManager().setObject(cetId, new DayRangeBean(dayRangeId, cetId, reductionId, ywd, ywd, days, owner));
        DayRangeBean testRange = service.getChildEducationPriceReductionRangeManager().getObject(cetId, dayRangeId);
        assertThat(dayRange, is(testRange));
        assertFalse(service.isChildEducationPriceReductionRemovable(reductionId));
    }

    /**
     * Unit test:
     */
    @Test
    public void test01_getAgeStartsWithActiveTariff() throws Exception {
        setAllConfig();
        setTariff();
        setDayRange();
        // price
        List<AgeStartBean> resultList = service.getAgeStartsWithActivePriceTariff();
        assertThat(resultList.size(),is(1));
        assertThat(resultList.get(0).getAgeStartId(), is (AgeStartId));
        // loyalty
        resultList = service.getAgeStartsWithActiveLoyaltyTariff();
        assertThat(resultList.size(),is(0));
    }

    /**
     * Unit test:
     */
    @Test
    public void test01_getRoomsWithActiveTariff() throws Exception {
        final RoomConfigBean room = new RoomConfigBean(roomId, AgeStartId, "room", 5, 2, 3, owner);
        setAllConfig();
        setTariff();
        setDayRange();

                // Record
        new NonStrictExpectations() {
            @Cascading
            AgeRoomService rs;
            {
                AgeRoomService.getInstance(); returns(rs);
                rs.getRoomConfigManager().getObject(roomId); returns(room);
            }
        };
        // Replay

        // price
        List<RoomConfigBean> resultList = service.getRoomsWithActivePriceTariff();
        assertThat(resultList.size(),is(1));
        assertThat(resultList.get(0).getRoomId(), is(roomId));
        // loyalty
        resultList = service.getRoomsWithActivePriceTariff();
        assertThat(resultList.size(),is(1));
        assertThat(resultList.get(0).getRoomId(), is(roomId));
    }

    /**
     * Unit test:
     */
    @Test(expected=NoSuchIdentifierException.class)
    public void setPriceTariffDayRange_wrongTariffId() throws Exception {
        int wrongTariffId = 254754;
        service.setPriceTariffDayRange(priceRoomMr, wrongTariffId, YWDHolder.getYW(2012, 40), YWDHolder.getYW(2012, 41), workingDays, owner).getDayRangeId();
    }

    /**
     * Unit test:
     */
    @Test
    public void hasPriceGroupGotTariff_noPriceGroupMr() throws Exception {
        int noOfWeeks = 1;
        int wrontMr = MRHolder.getMR(MultiRefEnum.ROOM.type(), AgeStartId);
        assertThat(service.hasPriceGroupGotTariff(wrontMr, YWDHolder.getYW(2012, 10), noOfWeeks, service.getPriceTariffRangeManager(), true), is (false));
    }

    /**
     * Unit test: straight run through to ensure the basic principle of the method works
     */
    @Test
    public void hasPriceGroupGotTariff_runThrough() throws Exception {
        setAllConfig();
        setTariff();
        setDayRange();

        int noOfWeeks = 1;
        new Expectations() {
            {
                TimetableService.getInstance(); returns(timetableServiceMock);
                // one weeks worth with closed days
                boolean[] days = BeanBuilder.getDays(BeanBuilder.MON_TO_FRI);
                for(int d = 0; d < YWDHolder.DAYS_IN_WEEK; d++) {
                    timetableServiceMock.isClosed(anyInt); result = !days[d];
                }
            }
        };
        assertThat(service.hasPriceGroupGotTariff(priceRoomMr, YWDHolder.getYW(2012, 10), noOfWeeks, service.getPriceTariffRangeManager(), true), is (true));
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_getPriceLists() throws Exception {
        testMakePriceTariff();
        assertThat(pricelistId01, is(service.getPriceList(priceTariffId, MultiRefEnum.PRICE_STANDARD).getPriceListId()));
        assertThat(pricelistId01, is(service.getPriceList(priceTariffId, MultiRefEnum.PRICE_SPECIAL).getPriceListId()));
        int wrongId = 234561;
        int result = service.getPriceList(wrongId, MultiRefEnum.PRICE_SPECIAL).getPriceListId();
        assertThat(result, is(PriceListManager.NO_PRICE_LIST));

        try {
            assertThat(PriceListManager.NO_PRICE_LIST, is(service.getPriceList(priceTariffId, MultiRefEnum.ADJUSTMENT_SPECIAL).getPriceListId()));
            fail("Exception Error: MultiRefEnum type is not of the right type");
        } catch(IllegalValueException ive) {
            // SUCCESS
        }
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_getTariffForProfile_room() throws Exception {
        final int profileId = 1; // any number
        final int groupMr = MRHolder.getMR(MultiRefEnum.ROOM.type(), roomId);
        final int ywd = CalendarStatic.getRelativeYWD(0); // todays date
        testMakePriceTariff();
        new Expectations() {
            @Cascading
            DayRangeManager tariffRange;
            @Mocked TariffManager priceTariffManager;
            {
                tariffRange.getDayRange(groupMr, ywd).getRightRef(); result = priceTariffId;
                priceTariffManager.getObject(priceTariffId);
            }
        };
        service.getPriceTariffForProfile(ywd, roomId, profileId);
    }
    /**
     * Unit test:
     */
    @Test
    public void unit01_getPriceListForProfile_ageStart() throws Exception {
        final int profileId = 1;
        testMakePriceTariff();
        new NonStrictExpectations() {
            @Mocked
            ChildBean childBean;
            {
                PropertiesService.getInstance(); returns(propertiesServiceMock);
                ChildService.getInstance(); returns(childServiceMock);
                childServiceMock.getChildManager().getObject(profileId); returns(childBean);
                childBean.getDateOfBirth();
                // 5 months so should be ageStart
            }
        };
        int resultId = service.getPriceTariffForProfile(YWDHolder.getYW(2012, 20), priceAgeMr, profileId).getTariffId();
        assertThat(priceTariffId, is(resultId));
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_RemovingPriceAdjustment() throws Exception {
        setAllConfig();
        setTariff();

        int priceAdjMr = MRHolder.getMR(MultiRefEnum.ADJUSTMENT_STANDARD.type(), priceAdjId01);
        assertTrue(service.getPriceTariffRelationship().getAllIdentifier(priceTariffId).contains(priceAdjMr));
        service.removePriceTariffRelationship(priceTariffId, priceAdjId01, MultiRefEnum.ADJUSTMENT_STANDARD);
        assertFalse(service.getPriceTariffRelationship().getAllIdentifier(priceTariffId).contains(priceAdjMr));

        setDayRange();
        try {
            service.removePriceTariffRelationship(priceTariffId, priceAdjId02, MultiRefEnum.ADJUSTMENT_STANDARD);
            fail("Exception ecpected: can't remove once in a DayRange");
        } catch(IllegalActionException iae) {
            // Success
        }
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_setPriceTariff() throws Exception {
        setAllConfig();
        setTariff();
        int priceListMr = MRHolder.getMR(MultiRefEnum.PRICE_STANDARD.type(), pricelistId01);
        assertTrue(service.getPriceTariffRelationship().getAllIdentifier(priceTariffId).contains(priceListMr));
        service.setPriceTariffRelationship(priceTariffId, -1, MultiRefEnum.PRICE_STANDARD, owner);
        assertFalse(service.getPriceTariffRelationship().getAllIdentifier(priceTariffId).contains(priceListMr));
        priceListMr = MRHolder.getMR(MultiRefEnum.PRICE_SPECIAL.type(), pricelistId01);
        assertTrue(service.getPriceTariffRelationship().getAllIdentifier(priceTariffId).contains(priceListMr));

        // replace an existing pricelist
        service.setPriceTariffRelationship(priceTariffId, pricelistId02, MultiRefEnum.PRICE_SPECIAL, owner);
        priceListMr = MRHolder.getMR(MultiRefEnum.PRICE_SPECIAL.type(), pricelistId02);
        assertTrue(service.getPriceTariffRelationship().getAllIdentifier(priceTariffId).contains(priceListMr));
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_ReverseOutTheSets() throws Exception {
        setAllConfig();
        setTariff();
        setDayRange();
        service.removePriceTariffDayRange(priceRoomMr, priceRoomDayRangeId);
        service.removePriceTariffDayRange(priceAgeMr, priceAgeDayRangeId01);
        service.removePriceTariffDayRange(priceAgeMr, priceAgeDayRangeId02);
        service.removePriceTariffRelationship(priceTariffId, priceAdjId01, MultiRefEnum.ADJUSTMENT_STANDARD);
        service.removePriceAdjustment(priceAdjId01);

    }
    /**
     * Unit test:
     */
    @Test
    public void unit01_getPriceGroupAssigned() throws Exception {
        final List<RoomConfigBean> roomList = new LinkedList<>();
        roomList.add(new RoomConfigBean(roomId, AgeStartId, "room", 5, 2, 3, owner));

        setAllConfig();
        setTariff();
        setDayRange();

        // Record
        new NonStrictExpectations() {
            @Cascading
            AgeRoomService rs;
            {
                AgeRoomService.getInstance(); returns(rs);
                rs.getRoomConfigManager().getAllObjects(); returns(roomList);
            }
        };
        // Replay
        List<ObjectBean> resultList = service.getPriceGroupAssignedToPriceTariff(priceTariffId);
        assertThat(resultList.size(), is(2));
        resultList = service.getPriceGroupAssignedToLoyaltyTariff(loyaltyTariffId);
        assertThat(resultList.size(), is(1));
    }

    /*
     *
     */
    @Test
    public void test01_defaultBillingBits() throws Exception {
        int testBits, billingBits;
        int percision = 1;
        long value = 100L;
        int rangeSd = SDHolder.addSD(10, 40);
        boolean repeated = false;
        int durationSd = 0;
        long discount = 20L;
        int start = -1;
        int duration = 49;
        boolean[] days = BeanBuilder.getDays(BeanBuilder.MON_TO_FRI);

        // PRICE ADJUSTMENTS
        testBits = getBillingBits(TYPE_ADJUSTMENT_ON_ALL, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_BOOKING);
        billingBits = 0;
        service.setPriceAdjustment(priceAdjId01, "", billingBits, value, percision, rangeSd, repeated, durationSd, owner);
        assertThat(service.getPriceAdjustmentManager().getObject(priceAdjId01).getBillingBits(), is(testBits));

        testBits = getBillingBits(TYPE_EARLY_DROPOFF, BILL_CHARGE, CALC_AS_PERCENT, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_BOOKING);
        billingBits = getBillingBits(TYPE_EARLY_DROPOFF,CALC_AS_PERCENT);
        service.setPriceAdjustment(priceAdjId01, "", billingBits, value, percision, rangeSd, repeated, durationSd, owner);
        assertThat(service.getPriceAdjustmentManager().getObject(priceAdjId01).getBillingBits(), is(testBits));

        // LOYALTY DISCOUNT
        testBits = getBillingBits(TYPE_LOYALTY, BILL_CREDIT, CALC_AS_VALUE, APPLY_NO_DISCOUNT, RANGE_AT_LEAST, GROUP_LOYALTY);
        billingBits = 0;
        service.setLoyaltyDiscount(loyaltyId01, "", billingBits, discount, start, duration, days, owner);
        assertThat(service.getLoyaltyDiscountManager().getObject(loyaltyId01).getBillingBits(), is(testBits));

        testBits = getBillingBits(TYPE_LOYALTY, BILL_CREDIT, CALC_AS_VALUE, APPLY_NO_DISCOUNT, RANGE_AT_LEAST, GROUP_LOYALTY);
        billingBits = getBillingBits(TYPE_FUNDED, BILL_CHARGE);
        service.setLoyaltyDiscount(loyaltyId01, "", billingBits, discount, start, duration, days, owner);
        assertThat(service.getLoyaltyDiscountManager().getObject(loyaltyId01).getBillingBits(), is(testBits));

        billingBits = getBillingBits(CALC_AS_PERCENT, RANGE_SUM_TOTAL);
        service.setLoyaltyDiscount(loyaltyId01, "", billingBits, discount, start, duration, days, owner);
        // set up loyaltyTariff
        service.setLoyaltyTariff(loyaltyTariffId, "LoyaltyTariff", "#cccccc", owner);
        service.setLoyaltyTariffRelationship(loyaltyTariffId, loyaltyId01, MultiRefEnum.LOYALTY_SPECIAL, owner);
    }


    //<editor-fold defaultstate="collapsed" desc="Private Setup Methods">
    private void setAllConfig() throws Exception {
        int billingBits, percision, rangeSd, durationSd, start, duration;
        long value, discount;
        boolean repeated, sequential;
        boolean[] workingDays;
        Map<Integer, Long> periodValueSet = new HashMap<>();
        periodValueSet.put(SDHolder.addSD(10, 19), 1000L);
        periodValueSet.put(SDHolder.addSD(30, 19), 1000L);

        // PRICE LIST
        service.setPriceList(pricelistId01, "Standard", "#cccccc", periodValueSet, owner);
        service.setPriceList(pricelistId02, "Standard2", "#cccccc", periodValueSet, owner);

        // PRICE ADJUSTMENTS
        billingBits = getBillingBits(TYPE_EARLY_DROPOFF, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_BOOKING);
        percision = 1;
        value = 100L;
        rangeSd = SDHolder.addSD(10, 40);
        repeated = false;
        durationSd = 0;
        service.setPriceAdjustment(priceAdjId01, "Early Drop Off Penalty", billingBits, value, percision, rangeSd, repeated, durationSd, owner);

        billingBits = getBillingBits(TYPE_LATE_PICKUP, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_BOOKING);
        repeated = true;
        durationSd = SDHolder.addSD(0, 10);;
        service.setPriceAdjustment(priceAdjId02, "Late Pickup Penalty", billingBits, value, percision, rangeSd, repeated, durationSd, owner);

        billingBits = getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CREDIT, CALC_AS_PERCENT, APPLY_DISCOUNT, RANGE_AT_LEAST, GROUP_BOOKING);
        percision = 100;
        value = 20L;
        rangeSd = SDHolder.addSD(10, 40);
        repeated = false;
        durationSd = 0;
        service.setPriceAdjustment(priceAdjId03, "Day Discount", billingBits, value, percision, rangeSd, repeated, durationSd, owner);

        // LOYALTY DISCOUNT
        billingBits = getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CREDIT, CALC_AS_PERCENT, APPLY_DISCOUNT, RANGE_AT_LEAST, GROUP_BOOKING);
        discount = 20L;
        start = -1;
        duration = 49;
        sequential = true;
        workingDays = BeanBuilder.getDays(BeanBuilder.MON_TO_FRI);
        service.setLoyaltyDiscount(loyaltyId01, "2 Day Special", billingBits, discount, start, duration, workingDays, owner);

        discount = 25L;
        service.setLoyaltyDiscount(loyaltyId02, "5 Day Discount", billingBits, discount, start, duration, workingDays, owner);

    }

    private void setTariff() throws Exception {
        // set up PriceTariff
        service.setPriceTariff(priceTariffId, "PriceTariff", "#cccccc", owner);

        service.setPriceTariffRelationship(priceTariffId, pricelistId01, MultiRefEnum.PRICE_STANDARD, owner);
        service.setPriceTariffRelationship(priceTariffId, pricelistId01, MultiRefEnum.PRICE_SPECIAL, owner);

        service.setPriceTariffRelationship(priceTariffId, priceAdjId01, MultiRefEnum.ADJUSTMENT_STANDARD, owner);
        service.setPriceTariffRelationship(priceTariffId, priceAdjId02, MultiRefEnum.ADJUSTMENT_STANDARD, owner);
        service.setPriceTariffRelationship(priceTariffId, priceAdjId03, MultiRefEnum.ADJUSTMENT_STANDARD, owner);
        service.setPriceTariffRelationship(priceTariffId, priceAdjId03, MultiRefEnum.ADJUSTMENT_SPECIAL, owner);

        // set up loyaltyTariff
        service.setLoyaltyTariff(loyaltyTariffId, "LoyaltyTariff", "#cccccc", owner);

        service.setLoyaltyTariffRelationship(loyaltyTariffId, loyaltyId01, MultiRefEnum.LOYALTY_SPECIAL, owner);
        service.setLoyaltyTariffRelationship(loyaltyTariffId, loyaltyId02, MultiRefEnum.LOYALTY_SPECIAL, owner);
        service.setLoyaltyTariffRelationship(loyaltyTariffId, loyaltyId02, MultiRefEnum.LOYALTY_STANDARD, owner);
    }

    private void setDayRange() throws Exception {
        //setup dayRange
        priceRoomDayRangeId = service.setPriceTariffDayRange(priceRoomMr, priceTariffId, YWDHolder.getYW(2012, 10), YWDHolder.getYW(2012, 11), workingDays, owner).getDayRangeId();
        priceAgeDayRangeId01 = service.setPriceTariffDayRange(priceAgeMr, priceTariffId, YWDHolder.getYW(2012, 20), YWDHolder.getYW(2012, 21), workingDays, owner).getDayRangeId();
        priceAgeDayRangeId02 = service.setPriceTariffDayRange(priceAgeMr, priceTariffId, YWDHolder.getYW(2012, 20), YWDHolder.getYW(2012, 21), workingDays, owner).getDayRangeId();

        //setup dayRange
        loyaltyRoomDayRangeId01 = service.setLoyaltyTariffDayRange(priceRoomMr, loyaltyTariffId, YWDHolder.getYW(2012, 10), YWDHolder.getYW(2012, 11), workingDays, owner).getDayRangeId();
        loyaltyRoomDayRangeId02 = service.setLoyaltyTariffDayRange(priceRoomMr, loyaltyTariffId, YWDHolder.getYW(2012, 20), YWDHolder.getYW(2012, 21), workingDays, owner).getDayRangeId();
    }
    //</editor-fold>
}