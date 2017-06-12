package com.oathouse.ccm.builders;

import com.oathouse.ccm.cma.ServicePool;
import com.oathouse.ccm.cma.ServicePoolConfig;
import com.oathouse.ccm.cma.VT;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.ccm.cos.concessions.HolidayConcessionBean;
import com.oathouse.ccm.cos.properties.AccountHolderBean;
import com.oathouse.ccm.cos.properties.AccountHolderManager;
import com.oathouse.ccm.cos.properties.RelativeDate;
import com.oathouse.ccm.cos.properties.SystemPropertiesBean;
import com.oathouse.ccm.cos.properties.SystemPropertiesManager;
import com.oathouse.ccm.cos.config.AgeRangeBean;
import com.oathouse.ccm.cos.config.AgeRangeManager;
import com.oathouse.ccm.cos.config.RoomConfigBean;
import com.oathouse.ccm.cos.config.RoomConfigManager;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectSingleStore;
import java.util.HashMap;

/**
 *
 * @author Darryl
 */
public class RSBuilder {

    private static final String owner = ObjectBean.SYSTEM_OWNED;
    private static final ServicePool engine = ServicePoolConfig.getInstance(owner);
//
    public static void setUp() throws Exception {
        // clear out the old data
        engine.clearAll();
        // set up the default properties
        setupSystemProperties();
    }

    public static String getOwner() {
        return owner;
    }

    public static ServicePool getEngine() {
        return engine;
    }

    /* ****************************************************
     * C o n f i g   S e r v i c e   s e t u p s
     * ****************************************************/
    public static int setupRoom() throws Exception {
        RoomConfigBean room = null;
        // set an ageRange
        AgeRangeManager arm = new AgeRangeManager(VT.AGE_RANGE.manager());
        arm.init();
        int seed = arm.generateIdentifier(1, 1000);

        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(seed));
        fieldSet.put("dobAdd", Integer.toString(5));
        fieldSet.put("specialNoticeDays", Integer.toString(25));
        fieldSet.put("standardNoticeDays", Integer.toString(18));
        AgeRangeBean arb = (AgeRangeBean) BeanBuilder.addBeanValues(new AgeRangeBean(), seed, fieldSet);
        arm.setObject(arb);
        // set a room
        RoomConfigManager rcm = new RoomConfigManager(VT.ROOM_CONFIG.manager());
        rcm.init();
        seed = rcm.generateIdentifier(1, 1000);
        fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(seed));
        fieldSet.put("capacity", Integer.toString(5));
        fieldSet.put("ageRangeId", Integer.toString(arb.getAgeRangeId()));
        room = (RoomConfigBean) BeanBuilder.addBeanValues(new RoomConfigBean(), seed, fieldSet);
        rcm.setObject(room);
        //print(room, false);
        return room.getRoomId();
    }

    public static SystemPropertiesBean setupSystemProperties() throws Exception {
        return setupSystemProperties(null);
    }

    public static SystemPropertiesBean setupSystemProperties(HashMap<String, String> propertySet) throws Exception {
        SystemPropertiesBean systemProperties = null;
        int id = 1;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(id));
        fieldSet.put("confirmedPeriodWeeks", Integer.toString(8));
        fieldSet.put("actualsPeriodWeeks", Integer.toString(5));
        fieldSet.put("maxAgeMonths", Integer.toString(168));
        fieldSet.put("adminSuspended", Boolean.toString(false));
        fieldSet.put("actualDropOffIdMandatory", Boolean.toString(false));
        fieldSet.put("actualPickupIdMandatory", Boolean.toString(false));
        fieldSet.put("reinstateBookingTypeId", Integer.toString(BTIdBits.ATTENDING_STANDARD));
        fieldSet.put("timeZone", "Europe/London");
        fieldSet.put("language", "EN_gb");
        fieldSet.put("bookingActualLimit", Integer.toString(15));
        fieldSet.put("toExceedCapacityWhenInsertingRequests", Boolean.toString(false));
        fieldSet.put("bookingChargeLimit", Integer.toString(10));
        fieldSet.put("taxRate", Integer.toString(0));
        fieldSet.put("bookingsTaxable", Boolean.toString(false));
        fieldSet.put("firstInvoiceYwd", Integer.toString(2010010));
        fieldSet.put("creditCardFeeRate", Integer.toString(0));
        fieldSet.put("paymentInstructions", "");
        fieldSet.put("defaultInvoiceLastYwd", RelativeDate.END_NEXT_MONTH.toString());
        fieldSet.put("defaultInvoiceDueYwd", RelativeDate.START_NEXT_MONTH.toString());
        // replace any defaults with the passed field values
        if(propertySet != null && !propertySet.isEmpty()) {
            fieldSet.putAll(propertySet);
        }
        systemProperties = (SystemPropertiesBean) BeanBuilder.addBeanValues(new SystemPropertiesBean(), id, fieldSet);
        SystemPropertiesManager sp = new SystemPropertiesManager(VT.PROPERTIES.manager());
        sp.init();
        sp.setObject(systemProperties);
        //print(systemProperties, false);
        return systemProperties;
    }

    public static AccountHolderBean setupAccountHolder() throws Exception {
        return setupAccountHolder(null);
    }

    public static AccountHolderBean setupAccountHolder(HashMap<String, String> propertySet) throws Exception {
        AccountHolderBean accountHolder = null;
        int id = 1;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(id));
        fieldSet.put("contact", "A");
        fieldSet.put("businessName", "B");
        fieldSet.put("line1", "C");
        fieldSet.put("line2", "D");
        fieldSet.put("line3", "E");
        fieldSet.put("city", "F");
        fieldSet.put("province", "G");
        fieldSet.put("postcode", "H");
        fieldSet.put("country", "I");
        fieldSet.put("phone", "J");
        fieldSet.put("accountId", "K");
        fieldSet.put("priceUrl", "L");
        fieldSet.put("superUsername", "M");
        fieldSet.put("superPassword", "N");
        fieldSet.put("returnEmailAddress", "O");
        fieldSet.put("returnEmailDisplayName", "P");
        fieldSet.put("billingEmail", "Q");
        fieldSet.put("validated", "true");
        fieldSet.put("suspended", "false");
        fieldSet.put("timeZone", "Europe/London");
        fieldSet.put("language", "EN_gb");
        fieldSet.put("currency", "GBP");

        // replace any defaults with the passed field values
        if(propertySet != null && !propertySet.isEmpty()) {
            fieldSet.putAll(propertySet);
        }
        accountHolder = (AccountHolderBean) BeanBuilder.addBeanValues(new AccountHolderBean(), id, fieldSet);
        AccountHolderManager ah = new AccountHolderManager(VT.ACCOUNT_HOLDER.manager());
        ah.init();
        ah.setObject(accountHolder);
//        print(accountHolder, false);
        return accountHolder;
    }

    public static HolidayConcessionBean setupHolidayConcession() throws Exception {
        HolidayConcessionBean holidayConcession = null;
        int id = 1;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(id));
        fieldSet.put("defaultAllocation", Integer.toString(1));
        fieldSet.put("countFrom", Integer.toString(-1));
        holidayConcession = (HolidayConcessionBean) BeanBuilder.addBeanValues(new HolidayConcessionBean(), id, fieldSet);
        ObjectSingleStore<HolidayConcessionBean> hc = new ObjectSingleStore<HolidayConcessionBean>("holidayConcession");
        hc.init();
        hc.setObject(holidayConcession);
//        print(holidayConcession, false);
        return holidayConcession;
    }


    /* ****************************************************
     * U S E F U L   M E T H O D S
     * ****************************************************/

    /*
     * Just to make things quick to print and look at when setting attributes
     */
    public static void print(ObjectBean ob, boolean toXml) {
        String output = toXml ? ob.toXML() : ob.toString();
        System.out.println(output);
    }
}
