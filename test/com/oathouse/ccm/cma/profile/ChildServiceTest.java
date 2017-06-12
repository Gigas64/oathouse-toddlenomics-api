/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.profile;

import com.oathouse.ccm.cos.profile.AccountBean;
import com.oathouse.ccm.cos.profile.AccountStatus;
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import java.util.HashMap;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Darryl Oatridge
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ChildServiceTest_checkChildRemovable.class,
    ChildServiceTest_setAccountPaymentPeriod.class,
    ChildServiceTest_checkAccountRemovable.class,
    ChildServiceTest_createChild.class,
    ChildServiceTest_getMedicalBeansForChild.class,
})
public class ChildServiceTest {

    public static ChildBean setChild(int accountId, int childId, int years, boolean departed) throws Exception {
        // clear out any that are there
        int dob = CalendarStatic.getRelativeYW(-(years * 52 + 10));
        int departYwd = departed ? CalendarStatic.getRelativeYW(-1) : CalendarStatic.getRelativeYW(dob, 12 * 52);
        //set account
        int seed = 1;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(accountId));
        fieldSet.put("status", AccountStatus.ACTIVE.toString());
        AccountBean account = (AccountBean) BeanBuilder.addBeanValues(new AccountBean(), seed++, fieldSet);
        ChildService.getInstance().getAccountManager().setObject(account);
        fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(childId));
        fieldSet.put("accountId", Integer.toString(accountId));
        fieldSet.put("dateOfBirth", Integer.toString(dob));
        fieldSet.put("departYwd", Integer.toString(departYwd));
        ChildBean child = (ChildBean) BeanBuilder.addBeanValues(new ChildBean(), fieldSet);
        ChildService.getInstance().getChildManager().setObject(child);
        return child;
    }

}