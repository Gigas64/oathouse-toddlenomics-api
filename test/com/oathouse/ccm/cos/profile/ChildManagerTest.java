package com.oathouse.ccm.cos.profile;

import com.oathouse.oss.storage.objectstore.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 * @author Darryl Oatridge
 */
public class ChildManagerTest {

    private String owner;
    private String rootStorePath;
    private ChildManager manager;

    public ChildManagerTest() {
        owner = ObjectBean.SYSTEM_OWNED;
        String sep = File.separator;
        rootStorePath = "." + sep + "oss" + sep + "data";
    }

    @Before
    public void setUp() throws Exception {
        manager = new ChildManager("ChildManager");
        manager.init();

    }

    @After
    public void tearDown() throws Exception {
        manager.clear();
    }

    @Test
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public void testManager() throws Exception {
        // create bean
        int id = 1;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(id++));
        fieldSet.put("owner", owner);
        ChildBean bean1 = (ChildBean) BeanBuilder.addBeanValues(new ChildBean(), id, fieldSet);
        fieldSet.put("id", Integer.toString(id++));
        ChildBean bean2 = (ChildBean) BeanBuilder.addBeanValues(new ChildBean(), id, fieldSet);
        // test manager
        assertEquals(0, manager.getAllIdentifier().size());
        manager.setObject(bean1);
        manager.setObject(bean2);
        assertEquals(2, manager.getAllIdentifier().size());
        manager = null;
        manager = new ChildManager("ChildManager");
        manager.init();
        assertEquals(2, manager.getAllIdentifier().size());
        assertEquals(bean1, manager.getObject(1));
        assertEquals(bean2, manager.getObject(2));
    }

    @Test
    public void testBean() throws Exception {
        List<String> exemptList = new LinkedList<>();
        exemptList.add("setPersonalDetails");
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.profile.ChildBean", false, exemptList);
    }

    @Test
    public void testFinds() throws Exception {
        // surname="surname31"
        // forenames value="One30"
        // forenames value="Two30"
        // commonName="commonName17"
        ChildBean bean1 = (ChildBean) BeanBuilder.addBeanValues(new ChildBean());
        // System.out.println(bean1.toString());
        assertTrue(bean1.hasName("One32"));
        assertTrue(bean1.hasName("ne3"));
        assertTrue(bean1.hasName("o32"));
        assertTrue(bean1.hasName("surname"));
        assertTrue(bean1.hasName("comm"));
        assertTrue(bean1.hasName("7"));
        assertFalse(bean1.hasName("ABCDE"));
    }

    @Test
    public void testComparator() throws Exception {
        List<ChildBean> childSet = new LinkedList<>();
        int id = 1;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(id++));
        fieldSet.put("dateOfBirth", Integer.toString(10));
        ChildBean bean1 = (ChildBean) BeanBuilder.addBeanValues(new ChildBean(), id, fieldSet);
        childSet.add(bean1);
        fieldSet.put("id", Integer.toString(id++));
        fieldSet.put("dateOfBirth", Integer.toString(10));
        ChildBean bean2 = (ChildBean) BeanBuilder.addBeanValues(new ChildBean(), id, fieldSet);
        childSet.add(bean2);
        fieldSet.put("id", Integer.toString(id++));
        fieldSet.put("dateOfBirth", Integer.toString(11));
        ChildBean bean3 = (ChildBean) BeanBuilder.addBeanValues(new ChildBean(), id, fieldSet);
        childSet.add(bean3);
        fieldSet.put("id", Integer.toString(id++));
        fieldSet.put("dateOfBirth", Integer.toString(9));
        ChildBean bean4 = (ChildBean) BeanBuilder.addBeanValues(new ChildBean(), id, fieldSet);
        childSet.add(bean4);
        fieldSet.put("id", Integer.toString(id++));
        fieldSet.put("dateOfBirth", Integer.toString(-1));
        ChildBean bean5 = (ChildBean) BeanBuilder.addBeanValues(new ChildBean(), id, fieldSet);
        childSet.add(bean5);

        assertEquals(0, ChildBean.DOB_ORDER.compare(bean1, bean1));
        assertEquals(-1, ChildBean.DOB_ORDER.compare(bean1, bean2));
        assertEquals(-1, ChildBean.DOB_ORDER.compare(bean1, bean3));
        assertEquals(1, ChildBean.DOB_ORDER.compare(bean1, bean4));
        assertEquals(1, ChildBean.DOB_ORDER.compare(bean1, bean5));
        assertEquals(1, ChildBean.DOB_ORDER.compare(null, bean5));
        assertEquals(-1, ChildBean.DOB_ORDER.compare(bean1, null));
        assertEquals(0, ChildBean.DOB_ORDER.compare(null, null));


        int[] order = {1,2,3,4,5};
        for(int index = 0; index < childSet.size(); index++) {
            assertEquals(childSet.get(index).getChildId(), order[index]);
        }
        Collections.sort(childSet, ChildBean.DOB_ORDER);
        int[] ordered = {5,4,1,2,3};
        for(int index = 0; index < childSet.size(); index++) {
            assertEquals(childSet.get(index).getChildId(), ordered[index]);
        }
        Collections.sort(childSet, ChildBean.REVERSE_DOB_ORDER);
        int[] reverse_ordered = {3,2,1,4,5};
        for(int index = 0; index < childSet.size(); index++) {
            assertEquals(childSet.get(index).getChildId(), reverse_ordered[index]);
        }
    }

}