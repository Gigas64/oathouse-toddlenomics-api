package com.oathouse.ccm.builders;

import com.oathouse.ccm.cma.ServicePool;
import com.oathouse.ccm.cma.ServicePoolConfig;
import com.oathouse.ccm.cos.profile.AccountBean;
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.ccm.cos.profile.ContactBean;
import com.oathouse.ccm.cos.profile.RelationType;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Darryl
 */
public class BSBuilder {

    private static final String owner = ObjectBean.SYSTEM_OWNED;
    private static final ServicePool engine = ServicePoolConfig.getInstance(owner);

    public static void setUp() throws Exception {
        // clear out the old data
        engine.clearAll();
        // set up the default properties
        RSBuilder.setupSystemProperties();
        RSBuilder.setupAccountHolder();
    }

    public static String getOwner() {
        return owner;
    }

    public static ServicePool getEngine() {
        return engine;
    }

    /* ****************************************************
     * B o o k i n g   S e r v i c e   S e t u p s
     * ****************************************************/
    public static AccountBean setupAccount(String accountRef, int liableId) throws Exception {
        return engine.getChildService().createAccount(accountRef, liableId, "", owner);
    }

    public static AccountBean setupAccount(int liableId) throws Exception {
        return setupAccount(ObjectBean.SYSTEM_OWNED, liableId);
    }

    public static ContactBean setupContact() throws Exception {
        List<String> names = new LinkedList<String>();
        names.add("Joe");
        return engine.getChildService().createContact("Mr", names, "Bloggs", "", "line1", "line2", "line3", "city", "province", "postcode", "country", "business", "phone", "phone2", "mobile", "email", 10, 10, 50, RelationType.NON_PROFESSIONAL, "", owner);
    }

    public static ChildBean setupChild(int accountId, int dob, int departYwd) throws Exception {
        List<String> names = new LinkedList<String>();
        names.add("Jimmy" + dob);
        String surname = "Surname" + accountId;
        return engine.getChildService().createChild(accountId, "", names, surname, "", "line1", "line2", "line3", "city", "province", "postcode", "country", "phone", "gender", dob, departYwd, 10, "", owner);
    }

    public static ChildBean setupChild(int accountId, int age) throws Exception {
        int departIn = (7 * 52) - age;

        int dob = CalendarStatic.getRelativeYW(-age);
        int departYwd = CalendarStatic.getRelativeYW(departIn);
        return setupChild(accountId, dob, departYwd);
    }

    public static void setupBookingWeek(int roomId, int[] childId, int[] dayMask, int[] periodSd, int[] contactId, int[] bookingTypeId) throws Exception {
        int ywd = CalendarStatic.getRelativeYW(6);
        int requestedYwd = ywd;
        int bookingSd;
        for(int day = 0; day < 5; day++) {
            for(int index = 0; index < childId.length ; index++) {
                if(!BeanBuilder.getDays(dayMask[index])[day]) {
                    continue;
                }
                ywd += index;
                engine.getChildBookingService().setBooking(ywd, roomId, periodSd[index], childId[index],
                        contactId[index], -1, contactId[index], bookingTypeId[index], "", requestedYwd, ObjectBean.SYSTEM_OWNED);
            }
        }
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
