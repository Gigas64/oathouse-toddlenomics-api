package com.oathouse.ccm.cos.properties;

import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.*;

/**
 *
 * @author Administrator
 */
public class AccountHolderManagerTest {

    private AccountHolderManager manager;
    private AccountHolderBean bean;
    private AccountHolderBean def;
    private String contact = "A";
    private String businessName = "B";
    private String line1 = "C";
    private String line2 = "D";
    private String line3 = "E";
    private String city = "F";
    private String province = "G";
    private String postcode = "H";
    private String country = "I";
    private String phone = "J";
    private String currency = "L";
    private String accountId = "M";
    private String priceUrl = "U";
    private String superUsername = "O";
    private String superPassword = "P";
    private String returnEmailAddress = "Q";
    private String returnEmailDisplayName = "R";
    private String billingEmail = "S";
    private boolean validated = true;
    private boolean suspended = true;
    private String timeZone = "U";
    private String language = "V";
    private int chargeRate = 50;
    private int registeredCapacity = 99;
    private String owner = "tester";

    @Before
    public void setUp() throws Exception {
        manager = new AccountHolderManager("accountHolderManager");
        manager.init();
        def = new AccountHolderBean();
    }

    @After
    public void tearDown() throws Exception {
        manager.setObject(def);
    }

    /**
     * Unit test: Underlying bean is correctly formed.
     */
    @Test
    public void unit01_SystemProperties() throws Exception {
        List<String> exemptSetMethodList = new LinkedList<>();
        exemptSetMethodList.add("setAccountHolder");
        exemptSetMethodList.add("setSuperUser");
        exemptSetMethodList.add("setReturnEmail");
        exemptSetMethodList.add("setBilling");
        exemptSetMethodList.add("setLocale");
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.properties.AccountHolderBean", false, exemptSetMethodList);
    }

    /**
     * Test of setAccountHolder method, of class AccountHolderProperties.
     */
    @Test
    public void testcreateAccountHolder() throws Exception {
        bean = new AccountHolderBean(contact, businessName, line1, line2, line3, city, province, postcode,
                    country, phone, accountId, priceUrl, superUsername,
                    superPassword, returnEmailAddress, returnEmailDisplayName, billingEmail,
                    validated, suspended, timeZone, language, currency, chargeRate, registeredCapacity, owner);
        manager.setObject(bean);
        assertEquals(bean, manager.getObject());
    }

    /**
     * Test of setAccountHolder method, of class AccountHolderProperties.
     */
    @Test
    public void testSetAccountHolder() throws Exception {
        bean = new AccountHolderBean(contact, businessName, line1, line2, line3, city, province, postcode,
                    country, phone, accountId, priceUrl, "superuser",
                    "superPass", "", "", "", false, false, timeZone, language, currency, chargeRate, registeredCapacity, owner);
        manager.setObjectAccountHolder(contact, businessName, line1, line2, line3, city, province, postcode,
                    country, phone, accountId, owner);

        assertEquals(bean.getContact(), manager.getObject().getContact());
        assertEquals(bean.getBusinessName(), manager.getObject().getBusinessName());
        assertEquals(bean.getLine1(), manager.getObject().getLine1());
        assertEquals(bean.getLine2(), manager.getObject().getLine2());
        assertEquals(bean.getLine3(), manager.getObject().getLine3());
        assertEquals(bean.getCity(), manager.getObject().getCity());
        assertEquals(bean.getProvince(), manager.getObject().getProvince());
        assertEquals(bean.getPostcode(), manager.getObject().getPostcode());
        assertEquals(bean.getCountry(), manager.getObject().getCountry());
        assertEquals(bean.getPhone(), manager.getObject().getPhone());
        assertEquals(bean.getAccountId(), manager.getObject().getAccountId());
    }

    /**
     * Test of setSuperUser method, of class AccountHolderProperties.
     */
    @Test
    public void testSetSuperUser() throws Exception {
        bean = new AccountHolderBean("", "", "", "", "", "", "", "", "", "", "", "",
                    superUsername, superPassword, "", "",
                    "", false, false, "", "", "", -1, -1,owner);
        manager.setObjectSuperUser(superUsername, superPassword, owner);

        assertEquals(bean.getSuperUsername(), manager.getObject().getSuperUsername());
        assertEquals(bean.getSuperPassword(), manager.getObject().getSuperPassword());
    }

    /**
     * Test of setObjectReturnEmail method, of class AccountHolderProperties.
     */
    @Test
    public void testSetReturnEmail() throws Exception {
        bean = new AccountHolderBean("", "", "", "", "", "", "", "", "", "", "", "",
                    "", "", returnEmailAddress, returnEmailDisplayName,
                    "", false, false, "", "", "", -1, -1,owner);
        manager.setObjectReturnEmail(returnEmailAddress, returnEmailDisplayName, owner);

        assertEquals(bean.getReturnEmailAddress(), manager.getObject().getReturnEmailAddress());
        assertEquals(bean.getReturnEmailDisplayName(), manager.getObject().getReturnEmailDisplayName());
    }

    /**
     * Test of setObjectBilling method, of class AccountHolderProperties.
     */
    @Test
    public void testSetBilling() throws Exception {
        bean = new AccountHolderBean("", "", "", "", "", "", "", "", "", "", "", priceUrl,
                    "", "", "", "",
                    billingEmail, validated, false, "", "", "", -1, -1,owner);
        manager.setObjectBilling(priceUrl, validated, billingEmail, owner);

        assertEquals(bean.getPriceFile(), manager.getObject().getPriceFile());
        assertEquals(bean.isValidated(), manager.getObject().isValidated());
        assertEquals(bean.getBillingEmail(), manager.getObject().getBillingEmail());
    }

    /**
     * Test of setObjectSuspended method, of class AccountHolderProperties.
     */
    @Test
    public void testSetSuspended() throws Exception {
        bean = new AccountHolderBean("", "", "", "", "", "", "", "", "", "", "", "",
                    "", "", "", "",
                    "", false, suspended, "", "", "", -1, -1,owner);
        manager.setObjectSuspended(suspended, owner);

        assertEquals(bean.isSuspended(), manager.getObject().isSuspended());
    }

    @Test
    public void testSetLocale() throws Exception {
        bean = new AccountHolderBean("", "", "", "", "", "", "", "", "", "", "", "",
                    "", "", "", "",
                    "", false, false, timeZone, language, currency, -1, -1,owner);
        manager.setObjectLocale(timeZone, language, currency,owner);

        assertEquals(bean.getTimeZone(), manager.getObject().getTimeZone());
        assertEquals(bean.getLanguage(), manager.getObject().getLanguage());
        assertEquals(bean.getCurrency(), manager.getObject().getCurrency());
    }

    @Test
    public void testSetRegisteredCapacity() throws Exception {
        bean = new AccountHolderBean("", "", "", "", "", "", "", "", "", "", "", "",
                    "", "", "", "",
                    "", false, false, "", "", "", -1, registeredCapacity, owner);
        manager.setObjectRegisteredCapacity(registeredCapacity, owner);

        assertEquals(bean.getRegisteredCapacity(), manager.getObject().getRegisteredCapacity());
    }

    @Test
    public void testSetChargeRate() throws Exception {
        bean = new AccountHolderBean("", "", "", "", "", "", "", "", "", "", "", "",
                    "", "", "", "",
                    "", false, false, "", "", "", chargeRate, -1,owner);
        manager.setObjectChargeRate(chargeRate,owner);

        assertEquals(bean.getChargeRate(), manager.getObject().getChargeRate());
    }

}
