/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cma.config;

// common imports
import com.oathouse.ccm.cma.VABoolean;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cma.profile.ChildServiceTest;
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import java.nio.file.Paths;
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
public class MaintenanceServiceTest_tidyChildCustodianRelation {
    private final String owner = ObjectBean.SYSTEM_OWNED;

    @Before
    public void setUp() throws Exception {

    }

    /*
     *
     */
    @Test
    public void tidyChildCustodianRelation_RelationEmpty() throws Exception {
        // result values
        final int childId = 13;
        final Set<Integer> custodialKeySet = VABoolean.asSet(childId);
        final List<ChildBean> childList = Arrays.asList(ChildServiceTest.setChild(11, childId, 3, false));

        new NonStrictExpectations() {
            @Cascading private ChildService cs;
            {
              ChildService.getInstance(); returns(cs);
              cs.getCustodialRelationship().getAllKeys(); result = custodialKeySet;
              cs.getChildManager().getAllObjects(); result = childList;
            }
        };

        MaintenanceService.tidyChildCustodianRelation();
        new Verifications() {
            @Cascading private ChildService cs;
            {
                cs.getLiableContactsForAccount(childId); times = 1;
            }
        };

    }

    /*
     *
     */
    @Test
    public void tidyChildCustodianRelation_NoChildCustodian() throws Exception {
        // result values
        final int childId = 13;
        final List<ChildBean> childList = Arrays.asList(ChildServiceTest.setChild(11, childId, 3, false));

        new NonStrictExpectations() {
            @Cascading private ChildService cs;
            {
              cs.getChildManager().getAllObjects(); result = childList;
            }
        };

        MaintenanceService.tidyChildCustodianRelation();
        new Verifications() {
            @Cascading private ChildService cs;
            {
                cs.getLiableContactsForAccount(childId); times = 1;
            }
        };

    }


}
