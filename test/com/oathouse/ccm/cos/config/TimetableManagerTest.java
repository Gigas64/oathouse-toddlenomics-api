package com.oathouse.ccm.cos.config;

import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import java.util.concurrent.ConcurrentSkipListMap;
import com.oathouse.oss.storage.valueholder.SDHolder;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;
import java.util.Map;
import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import java.util.List;
import java.io.File;
import java.util.LinkedList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Darryl Oatridge
 */
public class TimetableManagerTest {

    private String owner;
    private String rootStorePath;
    private TimetableManager manager;

    public TimetableManagerTest() {
        owner = "tester";
        String sep = File.separator;
        rootStorePath = "." + sep + "oss" + sep + "data";
    }

    @Before
    public void setUp() throws Exception {
        String authority = "man00test";
        String sep = File.separator;
        OssProperties props = OssProperties.getInstance();
        props.setConnection(OssProperties.Connection.FILE);
        props.setStorePath(rootStorePath);
        props.setAuthority(authority);
        props.setLogConfigFile(rootStorePath + sep + "conf" + sep + "oss_log4j.properties");
        // reset
        ObjectDBMS.clearAuthority(authority);
        manager = null;
        // create new manager
        manager = new TimetableManager("TimetableManager");
        manager.init();

    }

    @After
    public void tearDown() throws Exception {
        // manager.clear();
    }

    /**
     * System test: ' is working ' system level test.
     */
    @Test
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public void system01_Timetable() throws Exception {
        Map<Integer, Set<Integer>> options = new ConcurrentSkipListMap<Integer, Set<Integer>>();
        // set up ttOne
        options.put(SDHolder.getSD(10, 9), new ConcurrentSkipListSet<Integer>());
        options.get(SDHolder.getSD(10, 9)).add(SDHolder.getSD(30, 9));
        options.put(SDHolder.getSD(30, 9), new ConcurrentSkipListSet<Integer>());
        TimetableBean timetable = new TimetableBean(1, "A", "AA", options, "tester");
        manager.setObject(timetable);
        assertEquals(1, manager.getAllIdentifier().size());
        assertEquals(timetable, manager.getObject(1));
        manager = null;
        // create new manager
        manager = new TimetableManager("TimetableManager");
        manager.init();
        assertEquals(1, manager.getAllIdentifier().size());
        assertEquals(timetable, manager.getObject(1));
    }

    /**
     * Unit test: Underlying bean is correctly formed.
     */
    @Test
    public void unit01_Timetable() throws Exception {
        List<String> exemptMethods = new LinkedList<String>();
        // we don't want a get on getPeriodSdStore
        exemptMethods.add("getPeriodSdStore");
        // setPeriodSdAndOptions has to take 2 paramenters periodSd and options
        exemptMethods.add("setPeriodSdAndOptions");

        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.config.TimetableBean", false, exemptMethods);
    }

    /**
     * Unit test: to test the getObject by name works
     */
    @Test
    public void unit02_Timetable() throws Exception {
        Map<Integer, Set<Integer>> options = new ConcurrentSkipListMap<Integer, Set<Integer>>();
        // set up ttOne
        options.put(SDHolder.getSD(10, 9), new ConcurrentSkipListSet<Integer>());
        options.get(SDHolder.getSD(10, 9)).add(SDHolder.getSD(30, 9));
        options.put(SDHolder.getSD(30, 9), new ConcurrentSkipListSet<Integer>());
        TimetableBean ttb = new TimetableBean(1, "A", "AA", options, "tester");
        manager.setObject(ttb);
        assertEquals(ttb, manager.getObject("A"));
        try {
            manager.getObject("Z");
            fail("Should have thrown an exception");
        } catch(NoSuchIdentifierException e) {
            // success
        }
    }

    /**
     * Unit test: to test the setTemporaryTimetableMerge works
     */
    @Test
    public void unit04_Timetable() throws Exception {
        TimetableBean timetable = timetableFactory();
        manager.setObject(timetable);
        // merge array
        Set<Integer> mergeSd = new ConcurrentSkipListSet<Integer>();
        // SENARIO 1: Exact fit to to timetable
        mergeSd.add(SDHolder.getSD(10, 29));
        TimetableBean merge = manager.setTemporaryTimetableMerge(timetable, mergeSd, owner);
        assertEquals(timetable.getAllPeriodSdAndEndOptions(), merge.getAllPeriodSdAndEndOptions());
        // SENARIO 2: Exact fit to to timetable span more than one periodSd
        timetable = timetableFactory();
        mergeSd.clear();
        mergeSd.add(SDHolder.getSD(20, 19));
        mergeSd.add(SDHolder.getSD(40, 9)); // go beyond the end of the day
        merge = manager.setTemporaryTimetableMerge(timetable, mergeSd, owner);
        assertEquals(timetable.getAllPeriodSdAndEndOptions(), merge.getAllPeriodSdAndEndOptions());
        // SENARIO 3: Start before start time and go after end time
        timetable = timetableFactory();
        mergeSd.clear();
        mergeSd.add(SDHolder.getSD(9, 69));
        merge = manager.setTemporaryTimetableMerge(timetable, mergeSd, owner);
        assertEquals(timetable.getAllPeriodSdAndEndOptions(), merge.getAllPeriodSdAndEndOptions());
        // SENARIO 4: start equal end not, end equal start not
        timetable = timetableFactory();
        mergeSd.clear();
        mergeSd.add(SDHolder.getSD(20, 7));
        mergeSd.add(SDHolder.getSD(41, 8));
        merge = manager.setTemporaryTimetableMerge(timetable, mergeSd, owner);
        assertEquals(unit04_Senario4(), merge.getAllPeriodSdAndEndOptions());

        // SENARIO 5: both in span 2 and both in span 1
        timetable = timetableFactory();
        mergeSd.clear();
        mergeSd.add(SDHolder.getSD(11, 17));
        mergeSd.add(SDHolder.getSD(31, 7));
        merge = manager.setTemporaryTimetableMerge(timetable, mergeSd, owner);
        assertEquals(unit04_Senario5(), merge.getAllPeriodSdAndEndOptions());

        // SENARIO 6: Exact fit to to timetable, span one periodSd so added end options
        timetable = timetableFactory();
        mergeSd.clear();
        mergeSd.add(SDHolder.getSD(10, 9));
        merge = manager.setTemporaryTimetableMerge(timetable, mergeSd, owner);
        // timetable should have an extra end option under 10
        Set<Integer> endOptions = timetable.getEndOptionsForTime(10);
        endOptions.add(SDHolder.getSD(19, 0));
        timetable.setPeriodSdAndOptions(SDHolder.getSD(10, 9), endOptions, owner);
        assertEquals(timetable.getAllPeriodSdAndEndOptions(), merge.getAllPeriodSdAndEndOptions());
    }

    /**
     * Unit test: Test the sets in TimetableBean
     */
    @Test
    public void unit05_Timetable() throws Exception {
        int id = 1;
        Map<Integer, Set<Integer>> periodSdAndOptions = new ConcurrentSkipListMap<Integer, Set<Integer>>();

        int periodSd = SDHolder.getSD(10, 9);
        Set<Integer> options = new ConcurrentSkipListSet<Integer>();

        TimetableBean timetable = new TimetableBean(id, "A", "AA", new ConcurrentSkipListMap<Integer, Set<Integer>>(), "tester");
        try {
            manager.setObject(timetable);
            fail("Should have thrown an exception as the periodSdAndOptions is empty");
        } catch(NullObjectException e) {
            // success
        }
        // set sd with no options
        periodSdAndOptions.put(periodSd, options);
        manager.getObject(id).setPeriodSdAndOptions(periodSd, options, owner);
        assertEquals(periodSdAndOptions, manager.getObject(id).getAllPeriodSdAndEndOptions());
        //setsd with one option
        options.add(SDHolder.getSD(30, 9));
        periodSdAndOptions.put(periodSd, options);
        manager.getObject(id).setPeriodSdAndOptions(periodSd, options, owner);
        assertEquals(periodSdAndOptions, manager.getObject(id).getAllPeriodSdAndEndOptions());
        //setsd with two option
        options.add(SDHolder.getSD(40, 9));
        periodSdAndOptions.put(periodSd, options);
        manager.getObject(id).setPeriodSdAndOptions(periodSd, options, owner);
        assertEquals(periodSdAndOptions, manager.getObject(id).getAllPeriodSdAndEndOptions());
        //put in a few options with one that overlaps
        periodSd = SDHolder.getSD(1, 9);
        periodSdAndOptions.put(periodSd, options);
        periodSd = SDHolder.getSD(20, 9);
        periodSdAndOptions.put(periodSd, options);
        periodSd = SDHolder.getSD(30, 9);
        periodSdAndOptions.put(periodSd, options);
        periodSd = SDHolder.getSD(40, 9);
        periodSdAndOptions.put(periodSd, options);
        timetable = new TimetableBean(id+1, "A", "AA", new ConcurrentSkipListMap<Integer, Set<Integer>>(), "tester");
        try {
            manager.setObject(timetable);
            fail("Should have thrown an exception");
        } catch(NullObjectException e) {
            // success
        }



    }

    /**
     * Unit test: test the gets for the periodSd and options
     */
    @Test
    public void unit06_Timetable() throws Exception {
        int id = 1;
        Map<Integer, Set<Integer>> options = new ConcurrentSkipListMap<Integer, Set<Integer>>();
        // set up ttOne
        options.put(SDHolder.getSD(10, 9), new ConcurrentSkipListSet<Integer>());
        options.get(SDHolder.getSD(10, 9)).add(SDHolder.getSD(29, 0));
        options.get(SDHolder.getSD(10, 9)).add(SDHolder.getSD(39, 0));
        options.put(SDHolder.getSD(30, 9), new ConcurrentSkipListSet<Integer>());
        options.get(SDHolder.getSD(30, 9)).add(SDHolder.getSD(39, 0));
        options.put(SDHolder.getSD(40, 9), new ConcurrentSkipListSet<Integer>());
        TimetableBean timetable = new TimetableBean(id, "A", "AA", options, "tester");
        manager.setObject(timetable);
        assertEquals(options, timetable.getAllPeriodSdAndEndOptions());
        assertEquals(options.keySet(), timetable.getAllPeriodSd());
        assertEquals(options.get(SDHolder.getSD(10, 9)), timetable.getEndOptionsForTime(10));
        options.remove(SDHolder.getSD(40, 9));
        assertEquals(options, timetable.getAllPeriodSdWithEndOptions());
    }

    /**
     * Unit test: Test the getPeriodSd call in the timetableBean
     */
    @Test
    public void unit07_getPeriodSdTest() throws Exception {
        TimetableBean timetable = timetableFactory();
        assertEquals(-1, timetable.getPeriodSd(9));
        assertEquals(SDHolder.getSD(10, 9), timetable.getPeriodSd(10));
        assertEquals(SDHolder.getSD(20, 9), timetable.getPeriodSd(22));
        assertEquals(SDHolder.getSD(60, 9), timetable.getPeriodSd(69));
        assertEquals(-3, timetable.getPeriodSd(70));
    }

    /**
     * Unit test:
     */
    @Test
    public void unit07_Timetable() throws Exception {
        TimetableBean timetable = timetableFactory();
        manager.setObject(timetable);

    }



    /*
     * utility private method when debugging to print all objects in a manger
     */
    private void printAll() throws Exception {
        for(TimetableBean bean : manager.getAllObjects()) {
            System.out.println(bean.toString());
        }
    }

    /*
     * utility private method to test the order of the beans in the manager
     */
    private void testOrder(int[] order) throws Exception {
        testOrder(order, manager.getAllObjects());
    }


    /*
     * utility private method to test the order of beans for a given list
     */
    private void testOrder(int[] order, List<TimetableBean> list) throws Exception {
        assertEquals("Testing manager size", order.length, list.size());
        for(int i = 0; i < order.length; i++) {
            assertEquals("Testing manager bean order [" + i + "]", order[i], list.get(i).getIdentifier());
        }
    }

    private TimetableBean timetableFactory() {
        int id = 1;
        Map<Integer, Set<Integer>> options = new ConcurrentSkipListMap<Integer, Set<Integer>>();
        options.put(SDHolder.getSD(10, 9), new ConcurrentSkipListSet<Integer>());
        options.get(SDHolder.getSD(10, 9)).add(SDHolder.getSD(29, 0));
        options.get(SDHolder.getSD(10, 9)).add(SDHolder.getSD(39, 0));
        options.get(SDHolder.getSD(10, 9)).add(SDHolder.getSD(49, 0));
        options.get(SDHolder.getSD(10, 9)).add(SDHolder.getSD(59, 0));
        options.get(SDHolder.getSD(10, 9)).add(SDHolder.getSD(69, 0));
        options.put(SDHolder.getSD(20, 9), new ConcurrentSkipListSet<Integer>());
        options.get(SDHolder.getSD(20, 9)).add(SDHolder.getSD(29, 0));
        options.get(SDHolder.getSD(20, 9)).add(SDHolder.getSD(39, 0));
        options.get(SDHolder.getSD(20, 9)).add(SDHolder.getSD(49, 0));
        options.get(SDHolder.getSD(20, 9)).add(SDHolder.getSD(59, 0));
        options.get(SDHolder.getSD(20, 9)).add(SDHolder.getSD(69, 0));
        options.put(SDHolder.getSD(30, 9), new ConcurrentSkipListSet<Integer>());
        options.get(SDHolder.getSD(30, 9)).add(SDHolder.getSD(39, 0));
        options.get(SDHolder.getSD(30, 9)).add(SDHolder.getSD(49, 0));
        options.get(SDHolder.getSD(30, 9)).add(SDHolder.getSD(59, 0));
        options.get(SDHolder.getSD(30, 9)).add(SDHolder.getSD(69, 0));
        options.put(SDHolder.getSD(40, 9), new ConcurrentSkipListSet<Integer>());
        options.get(SDHolder.getSD(40, 9)).add(SDHolder.getSD(49, 0));
        options.get(SDHolder.getSD(40, 9)).add(SDHolder.getSD(59, 0));
        options.get(SDHolder.getSD(40, 9)).add(SDHolder.getSD(69, 0));
        options.put(SDHolder.getSD(50, 9), new ConcurrentSkipListSet<Integer>());
        options.put(SDHolder.getSD(60, 9), new ConcurrentSkipListSet<Integer>());
        return new TimetableBean(id, "name", "label", options, "tester");
}
    private Map<Integer, Set<Integer>> unit04_Senario4() {
        // SDHolder.getSD(20, 7)
        // SDHolder.getSD(41, 8)
        Map<Integer, Set<Integer>> options = new ConcurrentSkipListMap<Integer, Set<Integer>>();
        options.put(SDHolder.getSD(10, 9), new ConcurrentSkipListSet<Integer>());
        options.get(SDHolder.getSD(10, 9)).add(SDHolder.getSD(27, 0));
        options.get(SDHolder.getSD(10, 9)).add(SDHolder.getSD(29, 0));
        options.get(SDHolder.getSD(10, 9)).add(SDHolder.getSD(39, 0));
        options.get(SDHolder.getSD(10, 9)).add(SDHolder.getSD(49, 0));
        options.get(SDHolder.getSD(10, 9)).add(SDHolder.getSD(59, 0));
        options.get(SDHolder.getSD(10, 9)).add(SDHolder.getSD(69, 0));
        options.put(SDHolder.getSD(20, 7), new ConcurrentSkipListSet<Integer>());
        options.get(SDHolder.getSD(20, 7)).add(SDHolder.getSD(27, 0));
        options.get(SDHolder.getSD(20, 7)).add(SDHolder.getSD(29, 0));
        options.get(SDHolder.getSD(20, 7)).add(SDHolder.getSD(39, 0));
        options.get(SDHolder.getSD(20, 7)).add(SDHolder.getSD(49, 0));
        options.get(SDHolder.getSD(20, 7)).add(SDHolder.getSD(59, 0));
        options.get(SDHolder.getSD(20, 7)).add(SDHolder.getSD(69, 0));
        options.put(SDHolder.getSD(28, 1), new ConcurrentSkipListSet<Integer>());
        options.put(SDHolder.getSD(30, 9), new ConcurrentSkipListSet<Integer>());
        options.get(SDHolder.getSD(30, 9)).add(SDHolder.getSD(39, 0));
        options.get(SDHolder.getSD(30, 9)).add(SDHolder.getSD(49, 0));
        options.get(SDHolder.getSD(30, 9)).add(SDHolder.getSD(59, 0));
        options.get(SDHolder.getSD(30, 9)).add(SDHolder.getSD(69, 0));
        options.put(SDHolder.getSD(40, 0), new ConcurrentSkipListSet<Integer>());
        options.get(SDHolder.getSD(40, 0)).add(SDHolder.getSD(49, 0));
        options.get(SDHolder.getSD(40, 0)).add(SDHolder.getSD(59, 0));
        options.get(SDHolder.getSD(40, 0)).add(SDHolder.getSD(69, 0));
        options.put(SDHolder.getSD(41, 8), new ConcurrentSkipListSet<Integer>());
        options.get(SDHolder.getSD(41, 8)).add(SDHolder.getSD(49, 0));
        options.get(SDHolder.getSD(41, 8)).add(SDHolder.getSD(59, 0));
        options.get(SDHolder.getSD(41, 8)).add(SDHolder.getSD(69, 0));
        options.put(SDHolder.getSD(50, 9), new ConcurrentSkipListSet<Integer>());
        options.put(SDHolder.getSD(60, 9), new ConcurrentSkipListSet<Integer>());
        return options;
    }

    private Map<Integer, Set<Integer>> unit04_Senario5() {
        //SDHolder.getSD(11, 16)
        //SDHolder.getSD(31, 7)
        int id = 1;
        Map<Integer, Set<Integer>> options = new ConcurrentSkipListMap<Integer, Set<Integer>>();
        options.put(SDHolder.getSD(10, 0), new ConcurrentSkipListSet<Integer>());
        options.get(SDHolder.getSD(10, 0)).add(SDHolder.getSD(28, 0));
        options.get(SDHolder.getSD(10, 0)).add(SDHolder.getSD(29, 0));
        options.get(SDHolder.getSD(10, 0)).add(SDHolder.getSD(38, 0));
        options.get(SDHolder.getSD(10, 0)).add(SDHolder.getSD(39, 0));
        options.get(SDHolder.getSD(10, 0)).add(SDHolder.getSD(49, 0));
        options.get(SDHolder.getSD(10, 0)).add(SDHolder.getSD(59, 0));
        options.get(SDHolder.getSD(10, 0)).add(SDHolder.getSD(69, 0));
        options.put(SDHolder.getSD(11, 8), new ConcurrentSkipListSet<Integer>());
        options.get(SDHolder.getSD(11, 8)).add(SDHolder.getSD(28, 0));
        options.get(SDHolder.getSD(11, 8)).add(SDHolder.getSD(29, 0));
        options.get(SDHolder.getSD(11, 8)).add(SDHolder.getSD(38, 0));
        options.get(SDHolder.getSD(11, 8)).add(SDHolder.getSD(39, 0));
        options.get(SDHolder.getSD(11, 8)).add(SDHolder.getSD(49, 0));
        options.get(SDHolder.getSD(11, 8)).add(SDHolder.getSD(59, 0));
        options.get(SDHolder.getSD(11, 8)).add(SDHolder.getSD(69, 0));
        options.put(SDHolder.getSD(20, 8), new ConcurrentSkipListSet<Integer>());
        options.get(SDHolder.getSD(20, 8)).add(SDHolder.getSD(28, 0));
        options.get(SDHolder.getSD(20, 8)).add(SDHolder.getSD(29, 0));
        options.get(SDHolder.getSD(20, 8)).add(SDHolder.getSD(38, 0));
        options.get(SDHolder.getSD(20, 8)).add(SDHolder.getSD(39, 0));
        options.get(SDHolder.getSD(20, 8)).add(SDHolder.getSD(49, 0));
        options.get(SDHolder.getSD(20, 8)).add(SDHolder.getSD(59, 0));
        options.get(SDHolder.getSD(20, 8)).add(SDHolder.getSD(69, 0));
        options.put(SDHolder.getSD(29, 0), new ConcurrentSkipListSet<Integer>());
        options.put(SDHolder.getSD(30, 0), new ConcurrentSkipListSet<Integer>());
        options.get(SDHolder.getSD(30, 0)).add(SDHolder.getSD(38, 0));
        options.get(SDHolder.getSD(30, 0)).add(SDHolder.getSD(39, 0));
        options.get(SDHolder.getSD(30, 0)).add(SDHolder.getSD(49, 0));
        options.get(SDHolder.getSD(30, 0)).add(SDHolder.getSD(59, 0));
        options.get(SDHolder.getSD(30, 0)).add(SDHolder.getSD(69, 0));
        options.put(SDHolder.getSD(31, 7), new ConcurrentSkipListSet<Integer>());
        options.get(SDHolder.getSD(31, 7)).add(SDHolder.getSD(38, 0));
        options.get(SDHolder.getSD(31, 7)).add(SDHolder.getSD(39, 0));
        options.get(SDHolder.getSD(31, 7)).add(SDHolder.getSD(49, 0));
        options.get(SDHolder.getSD(31, 7)).add(SDHolder.getSD(59, 0));
        options.get(SDHolder.getSD(31, 7)).add(SDHolder.getSD(69, 0));
        options.put(SDHolder.getSD(39, 0), new ConcurrentSkipListSet<Integer>());
        options.put(SDHolder.getSD(40, 9), new ConcurrentSkipListSet<Integer>());
        options.get(SDHolder.getSD(40, 9)).add(SDHolder.getSD(49, 0));
        options.get(SDHolder.getSD(40, 9)).add(SDHolder.getSD(59, 0));
        options.get(SDHolder.getSD(40, 9)).add(SDHolder.getSD(69, 0));
        options.put(SDHolder.getSD(50, 9), new ConcurrentSkipListSet<Integer>());
        options.put(SDHolder.getSD(60, 9), new ConcurrentSkipListSet<Integer>());
        return options;
    }
}