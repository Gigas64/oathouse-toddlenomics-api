/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.profile;

// common imports
import com.oathouse.ccm.cos.profile.MedicalBean;
import com.oathouse.ccm.cos.profile.MedicalType;
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
public class ChildServiceTest_getMedicalBeansForChild {
    private final String owner = ObjectBean.SYSTEM_OWNED;
    private ChildService service;

    @Before
    public void setUp() throws Exception {
        String authority = ObjectBean.SYSTEM_OWNED;
        String rootStorePath = Paths.get("./oss/data").toString();
        OssProperties props = OssProperties.getInstance();
        props.setConnection(OssProperties.Connection.FILE);
        props.setStorePath(rootStorePath);
        props.setAuthority(authority);
        props.setLogConfigFile(Paths.get(rootStorePath + "/conf/oss_log4j.properties").toString());
        // reset
        ObjectDBMS.clearAuthority(authority);
        // global instances
        service = ChildService.getInstance();
        service.clear();

    }

    /*
     *
     */
    @Test
    public void getMedicalBeansForChild_multipleEntries() throws Exception {
        int childId = 13;
        int controlId = 99;
        service.createMedicalBean(childId, "Nuts", MedicalType.ALLERGY, "Nuts Desc", "Nuts Instruct", -1, -1, owner);
        service.createMedicalBean(childId, "Meat", MedicalType.DIET, "Meat Desc", "Meat Instruct", -1, -1, owner);
        service.createMedicalBean(childId, "Fish", MedicalType.DIET, "Fish Desc", "Fish Instruct", -1, -1, owner);
        service.createMedicalBean(childId, "SPEC", MedicalType.MED_SPECIFIC, "SPEC Desc", "SPEC Instruct", -1, -1, owner);
        // control
        service.createMedicalBean(controlId, "Milk", MedicalType.DIET, "Milk Desc", "Milk Instruct", -1, -1, owner);
        assertThat(service.getMedicalBeansForChild(childId).size(), is(4));
        assertThat(service.getMedicalBeansForChild(childId, MedicalType.ALLERGY).size(), is(1));
        assertThat(service.getMedicalBeansForChild(childId, MedicalType.MED_SPECIFIC).size(), is(1));
        assertThat(service.getMedicalBeansForChild(childId, MedicalType.DIET).size(), is(2));
        assertThat(service.getMedicalBeansForChild(childId, MedicalType.DIET, MedicalType.ALLERGY, MedicalType.MED_SPECIFIC).size(), is(4));
    }


}