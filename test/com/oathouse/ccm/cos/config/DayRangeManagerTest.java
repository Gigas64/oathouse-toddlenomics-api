/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.config;

import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.oathouse.oss.storage.valueholder.YWDHolder;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Darryl Oatridge
 */
public class DayRangeManagerTest {

    private String owner;
    private String rootStorePath;
    private DayRangeManager manager;
    private int leftRef;
    private DayRangeBean def;

    public DayRangeManagerTest() {
        owner = "tester";
        String sep = File.separator;
        rootStorePath = "." + sep + "oss" + sep + "data";
        leftRef = 100;
        def = null;
    }

    @Before
    public void setUp() throws Exception {
        String sep = File.separator;
        OssProperties props = OssProperties.getInstance();
        props.setConnection(OssProperties.Connection.FILE);
        props.setStorePath(rootStorePath);
        props.setAuthority("man00test");
        props.setLogConfigFile(rootStorePath + sep + "conf" + sep + "oss_log4j.properties");
        manager = new DayRangeManager("DayRangeManager");
        manager.init(new ConcurrentSkipListSet<Integer>());
        def = manager.getDefaultObject();
    }

    @After
    public void tearDown() throws Exception {
        manager.clear();
    }

    /**
     * System test: ' is working ' system level test.
     */
    @Test
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public void system01_DayRange() throws Exception {
        // create bean
        int id = 1;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(id++));
        fieldSet.put("owner", owner);
        DayRangeBean bean1 = (DayRangeBean) BeanBuilder.addBeanValues(new DayRangeBean(), id, fieldSet);
        fieldSet.put("id", Integer.toString(id++));
        DayRangeBean bean2 = (DayRangeBean) BeanBuilder.addBeanValues(new DayRangeBean(), id, fieldSet);
        // test manager
        assertEquals(0, manager.getAllIdentifier().size());
        manager.setObject(99, bean1);
        manager.setObject(99, bean2);
        manager.setObject(33, bean2);
        assertEquals(3, manager.getAllObjects().size());
        assertEquals(2, manager.getAllObjects(99).size());
        assertEquals(1, manager.getAllObjects(33).size());
        manager = null;
        manager = new DayRangeManager("DayRangeManager");
        manager.init(new ConcurrentSkipListSet<Integer>());
        assertEquals(3, manager.getAllObjects().size());
        assertEquals(2, manager.getAllObjects(99).size());
        assertEquals(1, manager.getAllObjects(33).size());
        assertEquals(bean1, manager.getObject(99, 1));
        assertEquals(bean2, manager.getObject(2));
        LinkedList<DayRangeBean> resultList = (LinkedList<DayRangeBean>) manager.getAllObjects(99);
        // remeber they are in reverse order
        assertEquals(bean2,resultList.getFirst());
        assertEquals(bean1,resultList.getLast());

    }

    /**
     * Unit test: Underlying bean is correctly formed. (can use to look at bean by setting
     *            the false to true.
     */
    @Test
    public void unit01_DayRange() throws Exception {
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.config.DayRangeBean", false);
    }

    /*
     * Included a check to ensure no duplication of the same booking request
     */
    @Test
    public void dayRangeManager_setObject() throws Exception {
        int start = YWDHolder.MIN_YWD;
        int end = YWDHolder.MAX_YWD;
        DayRangeBean r1 = new DayRangeBean(1, leftRef, 1, start, end, BeanBuilder.getDays(127), owner); // Mon - Sun
        manager.setObject(leftRef, r1);
        assertEquals(1, manager.getAllObjects(leftRef).size());
        DayRangeBean r2 = new DayRangeBean(2, leftRef, 1, start, end, BeanBuilder.getDays(127), owner); // Mon - Sun
        // setObject
        manager.setObject(leftRef, r2);
        assertEquals(1, manager.getAllObjects(leftRef).size());
        assertEquals(r2.getDayRangeId(), manager.getAllObjects(leftRef).get(0).getDayRangeId());
        // setFirstObject
        manager.setFirstObject(leftRef, r1);
        assertEquals(1, manager.getAllObjects(leftRef).size());
        assertEquals(r1.getDayRangeId(), manager.getAllObjects(leftRef).get(0).getDayRangeId());
        // setLastObject
        manager.setLastObject(leftRef, r2);
        assertEquals(1, manager.getAllObjects(leftRef).size());
        assertEquals(r2.getDayRangeId(), manager.getAllObjects(leftRef).get(0).getDayRangeId());
        // setObjectAt
        manager.setObjectAt(leftRef, r1, 0);
        assertEquals(1, manager.getAllObjects(leftRef).size());
        assertEquals(r1.getDayRangeId(), manager.getAllObjects(leftRef).get(0).getDayRangeId());
    }


    /**
     * Unit test: Set the default object
     */
    @Test
    public void unit02_DayRange() throws Exception {
        DayRangeBean d = new DayRangeBean(ObjectEnum.DEFAULT_ID.value(), ObjectEnum.DEFAULT_KEY.value(),
                    ObjectEnum.DEFAULT_ID.value(), 2010213, 9999010, BeanBuilder.getDays(127), "system.default");
        assertEquals(def, manager.getDefaultObject());
        assertNotSame(d, manager.getDefaultObject());
        manager.resetDefaultObject(d);
        assertEquals(d, manager.getDefaultObject());
    }

    /**
     * Unit test: Test the get all particularly when the day range is forever
     */
    @Test
    public void unit03_DayRange() throws Exception {
        // get calender helper to sort out dates
        // create bean
        int id = 0;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(++id));
        fieldSet.put("owner", owner);
        fieldSet.put("startYwd", Integer.toString(CalendarStatic.getRelativeYWD(0)));
        fieldSet.put("endYwd", Integer.toString(9999526));
        DayRangeBean bean01 = (DayRangeBean) BeanBuilder.addBeanValues(new DayRangeBean(), id, fieldSet);
        fieldSet.put("id", Integer.toString(++id));
        fieldSet.put("startYwd", Integer.toString(CalendarStatic.getRelativeYWD(-1)));
        fieldSet.put("endYwd", Integer.toString(CalendarStatic.getRelativeYW(+100)));
        DayRangeBean bean02 = (DayRangeBean) BeanBuilder.addBeanValues(new DayRangeBean(), id, fieldSet);
        manager.setObject(99, bean01);
        manager.setObject(99, bean02);
        manager.getAllObjects(99);


    }

    /**
     * Unit test: set up a couple of day range then try getting out a week at a time
     */
    @Test
    public void unit04_DayRange() throws Exception {
        // get calender helper to sort out dates
        // create bean
        DayRangeBean r1 = new DayRangeBean(1, leftRef, 1, CalendarStatic.getRelativeYWD(0), 9999526, BeanBuilder.getDays(31), owner); // Mon - Fri
        manager.setLastObject(leftRef, r1);
        testWeek(new int[]{1,1,1,1,1,0,0}, manager.getRightRefForWeekLeftRef(leftRef, CalendarStatic.getRelativeYW(1)));
        testWeek(new int[]{1,1,1,1,1,0,0}, manager.getRightRefForWeekLeftRef(leftRef, CalendarStatic.getRelativeYW(2)));
        testWeek(new int[]{1,1,1,1,1,0,0}, manager.getRightRefForWeekLeftRef(leftRef, CalendarStatic.getRelativeYW(8)));
        testWeek(new int[]{1,1,1,1,1,0,0}, manager.getRightRefForWeekLeftRef(leftRef, CalendarStatic.getRelativeYW(100)));
    }

    /**
     * Unit test: This is an additional to testMoveDayRange() consecutive ranges and a single underlying
     */
    @Test
    public void unit05_DayRange() throws Exception {
        List<Integer> moveList;
        // 1-2 to 2-1
        DayRangeBean r1 = new DayRangeBean(1, leftRef, 1, 2011012, 2011021, BeanBuilder.getDays(127), owner); // Mon - Sun
        manager.setLastObject(leftRef, r1);
        // 2-2 to 3-2
        DayRangeBean r2 = new DayRangeBean(2, leftRef, 2, 2011022, 2011031, BeanBuilder.getDays(127), owner); // Mon - Sun
        manager.setLastObject(leftRef, r2);
        // 1-0 to 4-4
        DayRangeBean r3 = new DayRangeBean(3, leftRef, 3, 2011010, 2011044, BeanBuilder.getDays(127), owner); // Mon - Sun
        manager.setLastObject(leftRef, r3);
        /*
         * Default start:
         *   01234560123456012345601234560123456
         *     |r1---|
         *            |r2---|
         *   |r3----------------------|
         *   |m1---||m2---||m3---||m4---||m5---|
         */
        testOrder(new int[]{1,2,3});
        /*
         * Move(m1) r3 down (No Change)
         *   01234560123456012345601234560123456
         *     |r1---|
         *            |r2---|
         *   |r3----------------------|
         *   |m1---||m2---||m3---||m4---||m5---|
         */
        moveList = manager.moveObject(leftRef, r3.getDayRangeId(), 2011010, 2011020, false, owner);
        testOrder(new int[]{1,2,3});
        testWeek(new int[]{3,3,1,1,1,1,1}, moveList);
        assertEquals(2011010,manager.getObject(leftRef, 3).getStartYwd());
        assertEquals(2011044,manager.getObject(leftRef, 3).getEndYwd());
        /*
         * Move(m2) r3 up
         *   01234560123456012345601234560123456
         *     |r1---|
         *          |r3(4)------------|
         *            |r2---|
         *   |r3---|
         *   |m1---||m2---||m3---||m4---||m5---|
         */
        moveList = manager.moveObject(leftRef, r3.getDayRangeId(), 2011020, 2011020, true, owner);
        testOrder(new int[]{1,4,2,3});
        testWeek(new int[]{1,1,3,3,3,3,3}, moveList);
        testWeek(new int[]{3,3,1,1,1,1,1}, manager.getRightRefForWeekLeftRef(leftRef, 2011010));
        assertEquals(2011010,manager.getObject(leftRef, 3).getStartYwd());
        assertEquals(2011016,manager.getObject(leftRef, 3).getEndYwd());
        assertEquals(2011020,manager.getObject(leftRef, 4).getStartYwd());
        assertEquals(2011044,manager.getObject(leftRef, 4).getEndYwd());
        /*
         * Move(m2) r3(4) up again keep the same cut
         *   01234560123456012345601234560123456
         *          |r3(4)------------|
         *     |r1---|
         *            |r2---|
         *   |r3---|
         *   |m1---||m2---||m3---||m4---||m5---|
         */
        moveList = manager.moveObject(leftRef, 4, 2011020, 2011020, true, owner);
        testOrder(new int[]{4,1,2,3});
        testWeek(new int[]{3,3,3,3,3,3,3}, moveList);
        assertEquals(2011020,manager.getObject(leftRef, 4).getStartYwd());
        assertEquals(2011044,manager.getObject(leftRef, 4).getEndYwd());
        /*
         * Move(m2) r3(4) up again should do nothing
         *   01234560123456012345601234560123456
         *          |r3(4)------------|
         *     |r1---|
         *            |r2---|
         *   |r3---|
         *   |m1---||m2---||m3---||m4---||m5---|
         */
        moveList = manager.moveObject(leftRef, 4, 2011020, 2011020, true, owner);
        testOrder(new int[]{4,1,2,3});
        testWeek(new int[]{3,3,3,3,3,3,3}, moveList);
        assertEquals(2011020,manager.getObject(leftRef, 4).getStartYwd());
        assertEquals(2011044,manager.getObject(leftRef, 4).getEndYwd());
       /*
         * Move(m2) r3(4) down
         *   01234560123456012345601234560123456
         *     |r1---|
         *          |r3(4)------------|
         *            |r2---|
         *   |r3---|
         *   |m1---||m2---||m3---||m4---||m5---|
         */
        moveList = manager.moveObject(leftRef, 4, 2011020, 2011020, false, owner);
        testOrder(new int[]{1,4,2,3});
        testWeek(new int[]{1,1,3,3,3,3,3}, moveList);
        assertEquals(2011020,manager.getObject(leftRef, 4).getStartYwd());
        assertEquals(2011044,manager.getObject(leftRef, 4).getEndYwd());
       /*
         * Move(m2) r3(4) down again
         *   01234560123456012345601234560123456
         *     |r1---|
         *            |r2---|
         *          |r3(4)------------|
         *   |r3---|
         *   |m1---||m2---||m3---||m4---||m5---|
         */
        moveList = manager.moveObject(leftRef, 4, 2011020, 2011020, false, owner);
        testOrder(new int[]{1,2,4,3});
        testWeek(new int[]{1,1,2,2,2,2,2}, moveList);
        assertEquals(2011020,manager.getObject(leftRef, 4).getStartYwd());
        assertEquals(2011044,manager.getObject(leftRef, 4).getEndYwd());
       /*
         * Move(m2) r3(4) down again but at bottom for this week so stays the same
         *   01234560123456012345601234560123456
         *     |r1---|
         *            |r2---|
         *          |r3(4)------------|
         *   |r3---|
         *   |m1---||m2---||m3---||m4---||m5---|
         */
        moveList = manager.moveObject(leftRef, 4, 2011020, 2011020, false, owner);
        testOrder(new int[]{1,2,4,3});
        testWeek(new int[]{1,1,2,2,2,2,2}, moveList);
        assertEquals(2011020,manager.getObject(leftRef, 4).getStartYwd());
        assertEquals(2011044,manager.getObject(leftRef, 4).getEndYwd());
        /*
         * Move(m3) r3(4)
         *   01234560123456012345601234560123456
         *     |r1---|
         *                 |r3(5)-----|
         *            |r2---|
         *          |r3(4)|
         *   |r3---|
         *   |m1---||m2---||m3---||m4---||m5---|
         */
        moveList = manager.moveObject(leftRef, 4, 2011030, 2011030, true, owner);
        testOrder(new int[]{1,5,2,4,3});
        testWeek(new int[]{3,3,3,3,3,3,3}, moveList);
        assertEquals(2011020,manager.getObject(leftRef, 4).getStartYwd());
        assertEquals(2011026,manager.getObject(leftRef, 4).getEndYwd());
        assertEquals(2011030,manager.getObject(leftRef, 5).getStartYwd());
        assertEquals(2011044,manager.getObject(leftRef, 5).getEndYwd());
        /*
         * Move(m3) r3(5) should stay the same as r1 in differnet view week
         *   01234560123456012345601234560123456
         *     |r1---|
         *                 |r3(5)-----|
         *            |r2---|
         *          |r3(4)|
         *   |r3---|
         *   |m1---||m2---||m3---||m4---||m5---|
         */

        testOrder(new int[]{1,5,2,4,3});
        testWeek(new int[]{3,3,3,3,3,3,3}, moveList);
        assertEquals(2011020,manager.getObject(leftRef, 4).getStartYwd());
        assertEquals(2011026,manager.getObject(leftRef, 4).getEndYwd());
        assertEquals(2011030,manager.getObject(leftRef, 5).getStartYwd());
        assertEquals(2011044,manager.getObject(leftRef, 5).getEndYwd());
        /*
         * Move(m4) r3(5) down (Nothing happend as nothing to jump in this week)
         *   01234560123456012345601234560123456
         *     |r1---|
         *                 |r3(5)-----|
         *            |r2---|
         *          |r3(4)|
         *   |r3---|
         *   |m1---||m2---||m3---||m4---||m5---|
         */
        moveList = manager.moveObject(leftRef, 5, 2011040, -1, false, owner);
        testOrder(new int[]{1,5,2,4,3});
        testWeek(new int[]{3,3,3,3,3,0,0}, moveList);
        assertEquals(2011030,manager.getObject(leftRef, 5).getStartYwd());
        assertEquals(2011044,manager.getObject(leftRef, 5).getEndYwd());
        /*
         * Move(m5) r1 up (r1 does not exist in this week. week is empty, should remain same)
         *   01234560123456012345601234560123456
         *     |r1---|
         *                 |r3(5)-----|
         *            |r2---|
         *          |r3(4)|
         *   |r3---|
         *   |m1---||m2---||m3---||m4---||m5---|
         */
        moveList = manager.moveObject(leftRef, r1.getRangeId(), 2011050, -1, true, owner);
        testOrder(new int[]{1,5,2,4,3});
        testWeek(new int[]{0,0,0,0,0,0,0}, moveList);
        assertEquals(2011012,manager.getObject(leftRef, r1.getRangeId()).getStartYwd());
        assertEquals(2011021,manager.getObject(leftRef, r1.getRangeId()).getEndYwd());
        /*
         * Move(m3) r3(5) down: The Gotcha here is that r3 is like humpty dumpty
         * and can't be put back together again. This means unlike at the start
         * where r3 can be used to cover r1 and r2, this is now impossible, move
         * with a split is not reversable.
         *   01234560123456012345601234560123456
         *     |r1---|
         *            |r2---|
         *                 |r3(5)-----|
         *          |r3(4)|
         *   |r3---|
         *   |m1---||m2---||m3---||m4---||m5---|
         */
        moveList = manager.moveObject(leftRef, 5, 2011030, -1, false, owner);
        testOrder(new int[]{1,2,5,4,3});
        testWeek(new int[]{2,2,3,3,3,3,3}, moveList);
    }

    /**
     * Unit test: Gotcha testing for moving up and covering an unrelated drb
     */
    @Test
    public void unit06_DayRange() throws Exception {
        List<Integer> moveList;
        DayRangeBean r1 = new DayRangeBean(1, leftRef, 1, 2011020, 2011026, BeanBuilder.getDays(127), owner); // Mon - Sun
        manager.setLastObject(leftRef, r1);
        DayRangeBean r2 = new DayRangeBean(2, leftRef, 2, 2011030, 2011036, BeanBuilder.getDays(127), owner); // Mon - Sun
        manager.setLastObject(leftRef, r2);
        DayRangeBean r3 = new DayRangeBean(3, leftRef, 3, 2011010, 2011046, BeanBuilder.getDays(127), owner); // Mon - Sun
        manager.setLastObject(leftRef, r3);
        /*
         * Default start:
         *   01234560123456012345601234560123456
         *          |r1---|
         *                 |r2---|
         *   |r3------------------------|
         *   |m1---||m2---||m3---||m4---||m5---|
         */
        testOrder(new int[]{1,2,3});
        /*
         * Move(m3) r3 up: I am looking to replace r1 with r3 so within this week I move
         * r3 up with a cut off of week 2 start. The result is as indicated. Gotcha: Not
         * only have I now replaced r1 but I have also replaced r2 with r3
         *   01234560123456012345601234560123456
         *          |r3(4)--------------|
         *          |r1---|
         *                 |r2---|
         *   |r3---|
         *   |m1---||m2---||m3---||m4---||m5---|
         */
        moveList = manager.moveObject(leftRef, r3.getDayRangeId(), 2011020, 2011020, true, owner);
        testOrder(new int[]{4,1,2,3});
        testWeek(new int[]{3,3,3,3,3,3,3}, moveList);
        // now look at week 3 and r2 has also been replaced with r3
        moveList = manager.getRightRefForWeekLeftRef(leftRef, 2011030);
        testWeek(new int[]{3,3,3,3,3,3,3}, moveList);
        /*
         * Move(m3) r2 up: This means that you then need to additionally go back and move the
         * other layers up, in this case r2
         *   01234560123456012345601234560123456
         *                 |r2---|
         *          |r3(4)--------------|
         *          |r1---|
         *   |r3---|
         *   |m1---||m2---||m3---||m4---||m5---|
         */
        moveList = manager.moveObject(leftRef, r2.getDayRangeId(), 2011030, -1, true, owner);
        testOrder(new int[]{2,4,1,3});
        testWeek(new int[]{2,2,2,2,2,2,2}, moveList);
    }

    /**
     * Unit test: Gotcha testing for moving up and providing a cut unrelated to the viewed
     */
    @Test
    public void unit07_DayRange() throws Exception {
        List<Integer> moveList;
        DayRangeBean r1 = new DayRangeBean(1, leftRef, 1, 2011020, 2011026, BeanBuilder.getDays(127), owner); // Mon - Sun
        manager.setLastObject(leftRef, r1);
        DayRangeBean r2 = new DayRangeBean(2, leftRef, 2, 2011030, 2011036, BeanBuilder.getDays(127), owner); // Mon - Sun
        manager.setLastObject(leftRef, r2);
        DayRangeBean r3 = new DayRangeBean(3, leftRef, 3, 2011010, 2011046, BeanBuilder.getDays(127), owner); // Mon - Sun
        manager.setLastObject(leftRef, r3);
        /*
         * Default start:
         *   01234560123456012345601234560123456
         *          |r1---|
         *                 |r2---|
         *   |r3------------------------|
         *   |m1---||m2---||m3---||m4---||m5---|
         */
        testOrder(new int[]{1,2,3});
        /*
         * Move(m3) r3 up: we are looking to move r3 above r2 but to view m2 so we put a cut date to
         * be the next week. As an example view m2 but set cut to be m3
         *   01234560123456012345601234560123456
         *          |r1---|
         *                 |r3(4)-------|
         *                 |r2---|
         *   |r3----------|
         *   |m1---||m2---||m3---||m4---||m5---|
         *
         * What actually happens is that the cut date is set to -1 to avoid this gotcha and moved up
         *   01234560123456012345601234560123456
         *   |r3------------------------|
         *          |r1---|
         *                 |r2---|
         *   |m1---||m2---||m3---||m4---||m5---|
         */
        moveList = manager.moveObject(leftRef, r3.getDayRangeId(), 2011020, 2011030, true, owner);
        testOrder(new int[]{3,1,2});
        testWeek(new int[]{3,3,3,3,3,3,3}, moveList);
        assertEquals(2011010,manager.getObject(leftRef, 3).getStartYwd());
        assertEquals(2011046,manager.getObject(leftRef, 3).getEndYwd());
    }


    /**
     * Test of init method, of class DayRangeManager.
     */
    @Test
    public void testInit() throws Exception {
        DayRangeBean r1 = new DayRangeBean(1, leftRef, 200, 2009110, 2009142, BeanBuilder.getDays(31), owner); // Mon - Fri
        manager.setObject(leftRef, r1);
        DayRangeBean r2 = new DayRangeBean(2, leftRef, 201, 2009120, 2009132, BeanBuilder.getDays(21), owner); // Mon, Wed, Fri
        manager.setObject(leftRef, r2);
        assertEquals(2, manager.getAllObjects(leftRef).size());
        List<DayRangeBean> testBeans = manager.getAllObjects(leftRef);
        manager = null;
        manager = new DayRangeManager("dayRangeManager");
        manager.init(new ConcurrentSkipListSet<Integer>());
        assertEquals(2, manager.getAllObjects(leftRef).size());
        manager.clear();
        assertEquals(0, manager.getAllObjects(leftRef).size());
        manager = null;
        manager = new DayRangeManager("dayRangeManager");
        manager.init(new ConcurrentSkipListSet<Integer>());
        assertEquals(0, manager.getAllObjects(leftRef).size());
    }

    /**
     * Test of getDayRange method, of class DayRangeManager.
     */
    @Test
    public void testGetDayRange() throws Exception {
        DayRangeBean r1 = new DayRangeBean(1, leftRef, 7, 110, 142, BeanBuilder.getDays(31), owner); // Mon - Fri
        manager.setObjectAt(leftRef, r1, 0);
        DayRangeBean r2 = new DayRangeBean(2, leftRef, 8, 120, 132, BeanBuilder.getDays(21), owner); // Mon, Wed, Fri
        manager.setObjectAt(leftRef, r2, 0);
        // extream testing
        assertEquals(def, manager.getDayRange(leftRef, 106));
        assertEquals(r1, manager.getDayRange(leftRef, 110));
        assertEquals(r2, manager.getDayRange(leftRef, 132));
        assertEquals(r1, manager.getDayRange(leftRef, 133));
        assertEquals(r1, manager.getDayRange(leftRef, 142));
        assertEquals(def, manager.getDayRange(leftRef, 143));
        //single week test.
        assertEquals(r2, manager.getDayRange(leftRef, 120));
        assertEquals(r1, manager.getDayRange(leftRef, 121));
        assertEquals(r2, manager.getDayRange(leftRef, 122));
        assertEquals(r1, manager.getDayRange(leftRef, 123));
        assertEquals(r2, manager.getDayRange(leftRef, 124));
        assertEquals(def, manager.getDayRange(leftRef, 125));
        assertEquals(def, manager.getDayRange(leftRef, 126));
    }

    /**
     * Test of getRightRefForWeekLeftRef method, of class DayRangeManager.
     */
    @Test
    public void testGetRightRefForWeekLeftRef() throws Exception {
        DayRangeBean r1 = new DayRangeBean(1, leftRef, 200, 2009110, 2009142, BeanBuilder.getDays(31), owner); // Mon - Fri
        manager.setObjectAt(leftRef, r1, 0);
        DayRangeBean r2 = new DayRangeBean(2, leftRef, 201, 2009120, 2009132, BeanBuilder.getDays(21), owner); // Mon, Wed, Fri
        manager.setObjectAt(leftRef, r2, 0);
        List<Integer> configIds = new LinkedList<Integer>();
        configIds.add(r2.getRightRef());
        configIds.add(r1.getRightRef());
        configIds.add(r2.getRightRef());
        configIds.add(r1.getRightRef());
        configIds.add(r2.getRightRef());
        configIds.add(0);
        configIds.add(0);
        assertEquals(configIds, manager.getRightRefForWeekLeftRef(leftRef, 2009120));
    }

    /**
     * Test of getAllObjects method, of class DayRangeManager.
     */
    @Test
    public void testGetAllDayRange() throws Exception {
        DayRangeBean r1 = new DayRangeBean(1, leftRef, 200, 2009110, 2009142, BeanBuilder.getDays(31), owner); // Mon - Fri
        manager.setObjectAt(leftRef, r1, 0);
        DayRangeBean r2 = new DayRangeBean(2, leftRef, 201, 2009120, 2009132, BeanBuilder.getDays(21), owner); // Mon, Wed, Fri
        manager.setObjectAt(leftRef, r2, 0);
        List<DayRangeBean> ranges;
        ranges = manager.getAllObjects(leftRef, 2009100, 2009106);
        assertEquals(0, ranges.size());
        ranges = manager.getAllObjects(leftRef, 2009100, 2009116);
        assertEquals(1, ranges.size());
        assertEquals(r1, ranges.get(0));
        ranges = manager.getAllObjects(leftRef, 2009120, 2009126);
        assertEquals(2, ranges.size());
        assertEquals(r2, ranges.get(0));
        assertEquals(r1, ranges.get(1));
        ranges = manager.getAllObjects(leftRef, 2009132, 2009136);
        assertEquals(2, ranges.size());
        assertEquals(r2, ranges.get(0));
        assertEquals(r1, ranges.get(1));
        ranges = manager.getAllObjects(leftRef, 2009133, 2009136);
        assertEquals(1, ranges.size());
        assertEquals(r1, ranges.get(0));
    }

    /**
     * Test of moveObject method, of class DayRangeManager.
     */
    @Test
    public void testMoveDayRange() throws Exception {
        List<Integer> moveVector;
        DayRangeBean r1 = new DayRangeBean(1, leftRef, 200, 2009110, 2009142, BeanBuilder.getDays(31), owner); // Mon - Fri
        manager.setObject(leftRef, r1);
        DayRangeBean r2 = new DayRangeBean(2, leftRef, 201, 2009120, 2009132, BeanBuilder.getDays(21), owner); // Mon, Wed, Fri
        manager.setObject(leftRef, r2);
        DayRangeBean r3 = new DayRangeBean(3, leftRef, 202, 2009110, 2009126, BeanBuilder.getDays(6), owner); // Tues - Wed
        manager.setObject(leftRef, r3);
        assertEquals(r2, manager.getDayRange(leftRef, 2009120));
        assertEquals(r3, manager.getDayRange(leftRef, 2009121));
        assertEquals(r3, manager.getDayRange(leftRef, 2009122));
        assertEquals(r1, manager.getDayRange(leftRef, 2009123));
        assertEquals(r2, manager.getDayRange(leftRef, 2009124));
        assertEquals(def, manager.getDayRange(leftRef, 2009125));
        assertEquals(def, manager.getDayRange(leftRef, 2009126));

        assertEquals(3, manager.getAllObjects(leftRef).get(0).getDayRangeId());
        assertEquals(2, manager.getAllObjects(leftRef).get(1).getDayRangeId());
        assertEquals(1, manager.getAllObjects(leftRef).get(2).getDayRangeId());

        moveVector = manager.moveObject(leftRef, r3.getDayRangeId(), 2009120, 2009100, false, owner);
        assertEquals(r2.getRightRef(), (int) moveVector.get(0));
        assertEquals(r3.getRightRef(), (int) moveVector.get(1));
        assertEquals(r2.getRightRef(), (int) moveVector.get(2));
        assertEquals(r1.getRightRef(), (int) moveVector.get(3));
        assertEquals(r2.getRightRef(), (int) moveVector.get(4));
        assertEquals(def.getRightRef(), (int) moveVector.get(5));
        assertEquals(def.getRightRef(), (int) moveVector.get(6));

        assertEquals(2, manager.getAllObjects(leftRef).get(0).getDayRangeId());
        assertEquals(3, manager.getAllObjects(leftRef).get(1).getDayRangeId());
        assertEquals(1, manager.getAllObjects(leftRef).get(2).getDayRangeId());

        manager = null;
        manager = new DayRangeManager("DayRangeManager");
        manager.init(new ConcurrentSkipListSet<Integer>());
        assertEquals(2, manager.getAllObjects(leftRef).get(0).getDayRangeId());
        assertEquals(3, manager.getAllObjects(leftRef).get(1).getDayRangeId());
        assertEquals(1, manager.getAllObjects(leftRef).get(2).getDayRangeId());

        moveVector = manager.moveObject(leftRef, r3.getDayRangeId(), 2009120, 2009100, false, owner);
        assertEquals(r2.getRightRef(), (int) moveVector.get(0));
        assertEquals(r1.getRightRef(), (int) moveVector.get(1));
        assertEquals(r2.getRightRef(), (int) moveVector.get(2));
        assertEquals(r1.getRightRef(), (int) moveVector.get(3));
        assertEquals(r2.getRightRef(), (int) moveVector.get(4));

        assertEquals(2, manager.getAllObjects(leftRef).get(0).getDayRangeId());
        assertEquals(1, manager.getAllObjects(leftRef).get(1).getDayRangeId());
        assertEquals(3, manager.getAllObjects(leftRef).get(2).getDayRangeId());

        moveVector = manager.moveObject(leftRef, r2.getDayRangeId(), 2009120, 2009100, false, owner);
        assertEquals(r1.getRightRef(), (int) moveVector.get(0));
        assertEquals(r1.getRightRef(), (int) moveVector.get(1));
        assertEquals(r1.getRightRef(), (int) moveVector.get(2));
        assertEquals(r1.getRightRef(), (int) moveVector.get(3));
        assertEquals(r1.getRightRef(), (int) moveVector.get(4));

        assertEquals(1, manager.getAllObjects(leftRef).get(0).getDayRangeId());
        assertEquals(2, manager.getAllObjects(leftRef).get(1).getDayRangeId());
        assertEquals(3, manager.getAllObjects(leftRef).get(2).getDayRangeId());

        moveVector = manager.moveObject(leftRef, r3.getDayRangeId(), 2009120, 2009100, true, owner);
        assertEquals(r1.getRightRef(), (int) moveVector.get(0));
        assertEquals(r1.getRightRef(), (int) moveVector.get(1));
        assertEquals(r1.getRightRef(), (int) moveVector.get(2));
        assertEquals(r1.getRightRef(), (int) moveVector.get(3));
        assertEquals(r1.getRightRef(), (int) moveVector.get(4));

        assertEquals(1, manager.getAllObjects(leftRef).get(0).getDayRangeId());
        assertEquals(3, manager.getAllObjects(leftRef).get(1).getDayRangeId());
        assertEquals(2, manager.getAllObjects(leftRef).get(2).getDayRangeId());



        moveVector = manager.moveObject(leftRef, r1.getDayRangeId(), 2009120, 2009100, true, owner);
        assertEquals(r1.getRightRef(), (int) moveVector.get(0));
        assertEquals(r1.getRightRef(), (int) moveVector.get(1));
        assertEquals(r1.getRightRef(), (int) moveVector.get(2));
        assertEquals(r1.getRightRef(), (int) moveVector.get(3));
        assertEquals(r1.getRightRef(), (int) moveVector.get(4));

        assertEquals(1, manager.getAllObjects(leftRef).get(0).getDayRangeId());
        assertEquals(3, manager.getAllObjects(leftRef).get(1).getDayRangeId());
        assertEquals(2, manager.getAllObjects(leftRef).get(2).getDayRangeId());

        moveVector = manager.moveObject(leftRef, r1.getDayRangeId(), 2009120, 2009100, false, owner);
        assertEquals(r1.getRightRef(), (int) moveVector.get(0));
        assertEquals(r3.getRightRef(), (int) moveVector.get(1));
        assertEquals(r3.getRightRef(), (int) moveVector.get(2));
        assertEquals(r1.getRightRef(), (int) moveVector.get(3));
        assertEquals(r1.getRightRef(), (int) moveVector.get(4));

        assertEquals(3, manager.getAllObjects(leftRef).get(0).getDayRangeId());
        assertEquals(1, manager.getAllObjects(leftRef).get(1).getDayRangeId());
        assertEquals(2, manager.getAllObjects(leftRef).get(2).getDayRangeId());

        moveVector = manager.moveObject(leftRef, r1.getDayRangeId(), 2009120, 2009100, false, owner);
        assertEquals(r2.getRightRef(), (int) moveVector.get(0));
        assertEquals(r3.getRightRef(), (int) moveVector.get(1));
        assertEquals(r3.getRightRef(), (int) moveVector.get(2));
        assertEquals(r1.getRightRef(), (int) moveVector.get(3));
        assertEquals(r2.getRightRef(), (int) moveVector.get(4));

        assertEquals(3, manager.getAllObjects(leftRef).get(0).getDayRangeId());
        assertEquals(2, manager.getAllObjects(leftRef).get(1).getDayRangeId());
        assertEquals(1, manager.getAllObjects(leftRef).get(2).getDayRangeId());

        assertEquals(r2, manager.getDayRange(leftRef, 2009120));
        assertEquals(r3, manager.getDayRange(leftRef, 2009121));
        assertEquals(r3, manager.getDayRange(leftRef, 2009122));
        assertEquals(r1, manager.getDayRange(leftRef, 2009123));
        assertEquals(r2, manager.getDayRange(leftRef, 2009124));
        assertEquals(def, manager.getDayRange(leftRef, 2009125));
        assertEquals(def, manager.getDayRange(leftRef, 2009126));
    }

    /**
     * Test of tidyDayRangeObjects method, of class DayRangeManager.
     */
    @Test
    public void testCleanDayRange() throws Exception {
        DayRangeBean r1 = new DayRangeBean(1, leftRef, 200, 2009110, 2009142, BeanBuilder.getDays(31), owner); // Mon - Fri
        manager.setObjectAt(leftRef, r1, 0);
        DayRangeBean r2 = new DayRangeBean(2, leftRef, 201, 2009120, 2009132, BeanBuilder.getDays(21), owner); // Mon, Wed, Fri
        manager.setObject(leftRef, r2);
        assertEquals(2, manager.getAllObjects(leftRef).size());
        manager.tidyDayRangeObjects(2009130);
        assertEquals(2, manager.getAllObjects(leftRef).size());
        manager.tidyDayRangeObjects(2009136);
        assertEquals(2, manager.getAllObjects(leftRef).size());
        manager.tidyDayRangeObjects(2009140);
        assertEquals(1, manager.getAllObjects(leftRef).size());
        assertTrue(manager.getAllObjects(leftRef).contains(r1));
        manager.tidyDayRangeObjects(2009150);
        assertEquals(0, manager.getAllObjects(leftRef).size());

    }

    /**
     * Test of removeAllObjectsForRightRef method, of class DayRangeManager.
     */
    @Test
    public void testRemoveDayRangesForRightRef() throws Exception {
        DayRangeBean r1 = new DayRangeBean(1, leftRef, 200, 2009110, 2009142, BeanBuilder.getDays(31), owner); // Mon - Fri
        manager.setObjectAt(leftRef, r1, 0);
        DayRangeBean r2 = new DayRangeBean(2, leftRef, 201, 2009120, 2009132, BeanBuilder.getDays(21), owner); // Mon, Wed, Fri
        manager.setObject(leftRef, r2);
        assertEquals(2, manager.getAllObjects(leftRef).size());
        manager.removeAllObjectsForRightRef(201); //remove r2
        assertEquals(1, manager.getAllObjects(leftRef).size());
        assertTrue(manager.getAllObjects(leftRef).contains(r1));
        manager.removeAllObjectsForRightRef(200); //remove r2
        assertEquals(0, manager.getAllObjects(leftRef).size());
    }

    @Test
    public void testResetLeftRef() throws Exception {
        DayRangeBean r1 = new DayRangeBean(1, leftRef, 200, 2009110, 2009142, BeanBuilder.getDays(31), owner); // Mon - Fri
        manager.setObjectAt(leftRef, r1, 0);
        DayRangeBean r2 = new DayRangeBean(2, leftRef, 201, 2009120, 2009132, BeanBuilder.getDays(21), owner); // Mon, Wed, Fri
        manager.setObjectAt(leftRef, r2, 0);
        assertEquals(2, manager.getAllObjects(leftRef).size());
        assertTrue(manager.getAllObjects(leftRef).contains(r1));
        assertTrue(manager.getAllObjects(leftRef).contains(r2));
        DayRangeBean b = manager.resetLeftRef(leftRef, 102, owner);
        assertEquals(1, manager.getAllObjects(leftRef).size());
        assertTrue(manager.getAllObjects(leftRef).contains(b));
    }

    /**
     * Test class DayRangeManager.
     */
    @Test
    public void testGetLeftRefForRightRef() throws Exception {
        int leftRef2 = leftRef + 1;
        Set<Integer> test =new ConcurrentSkipListSet<Integer>();

        DayRangeBean r1 = new DayRangeBean(1, leftRef, 200, 2009110, 2009142, BeanBuilder.getDays(31), owner); // Mon - Fri
        manager.setObjectAt(leftRef, r1, 0);
        DayRangeBean r2 = new DayRangeBean(2, leftRef, 201, 2009120, 2009132, BeanBuilder.getDays(21), owner); // Mon, Wed, Fri
        manager.setObjectAt(leftRef, r2, 0);
        DayRangeBean r3 = new DayRangeBean(3, leftRef2, 202, 2009110, 2009126, BeanBuilder.getDays(6), owner); // Tues - Wed
        manager.setObject(leftRef2, r3);
        DayRangeBean r4 = new DayRangeBean(4, leftRef2, 201, 2009110, 2009126, BeanBuilder.getDays(6), owner); // Tues - Wed
        manager.setObject(leftRef2, r4);
        test.add(leftRef);
        assertEquals(test, manager.getLeftRefForRightRef(200));
        test.add(leftRef2);
        assertEquals(test, manager.getLeftRefForRightRef(201));
        test.remove(leftRef);
        assertEquals(test, manager.getLeftRefForRightRef(202));
    }

    /**
     * Unit test:
     */
    @Test
    public void test_static_inDayRange() throws Exception {
        // create bean
        String owner = ObjectBean.SYSTEM_OWNED;
        int leftRef = 99;
        int rightRef = 0;
        int rangeId = 0;
        int startYwd = 0;
        int endYwd = 0;
        boolean[] days = BeanBuilder.getDays(BeanBuilder.MON_TO_FRI);

        rangeId = manager.generateIdentifier();
        rightRef = 1;
        startYwd = CalendarStatic.getRelativeYW(-4);
        endYwd = CalendarStatic.getRelativeYW(-3);
        DayRangeBean bean01 = new DayRangeBean(rangeId, leftRef, rightRef, startYwd, endYwd, days, owner);
        manager.setObject(99, bean01);

        rangeId = manager.generateIdentifier();
        rightRef = 1;
        startYwd = CalendarStatic.getRelativeYW(-1);
        endYwd = CalendarStatic.getRelativeYW(1);
        DayRangeBean bean02 = new DayRangeBean(rangeId, leftRef, rightRef, startYwd, endYwd, days, owner);
        manager.setObject(99, bean02);

        rangeId = manager.generateIdentifier();
        rightRef = 1;
        startYwd = CalendarStatic.getRelativeYW(3);
        endYwd = CalendarStatic.getRelativeYW(4);
        DayRangeBean bean03 = new DayRangeBean(rangeId, leftRef, rightRef, startYwd, endYwd, days, owner);
        manager.setObject(99, bean03);

        rangeId = manager.generateIdentifier();
        rightRef = 2;
        startYwd = CalendarStatic.getRelativeYW(-100);
        endYwd = CalendarStatic.getRelativeYW(100);
        DayRangeBean bean04 = new DayRangeBean(rangeId, leftRef, rightRef, startYwd, endYwd, days, owner);
        manager.setObject(99, bean04);

        rightRef = 1;

        startYwd = CalendarStatic.getRelativeYW(-6);
        endYwd = CalendarStatic.getRelativeYW(-5);
        assertFalse(DayRangeManager.inDayRange(rightRef, startYwd, endYwd, manager));

        startYwd = CalendarStatic.getRelativeYW(-4);
        endYwd = CalendarStatic.getRelativeYW(-4);
        assertTrue(DayRangeManager.inDayRange(rightRef, startYwd, endYwd, manager));

        startYwd = CalendarStatic.getRelativeYW(-3);
        endYwd = CalendarStatic.getRelativeYW(-2);
        assertTrue(DayRangeManager.inDayRange(rightRef, startYwd, endYwd, manager));

        startYwd = CalendarStatic.getRelativeYW(-2);
        endYwd = CalendarStatic.getRelativeYW(-2);
        assertFalse(DayRangeManager.inDayRange(rightRef, startYwd, endYwd, manager));

        startYwd = CalendarStatic.getRelativeYW(0);
        endYwd = CalendarStatic.getRelativeYW(0);
        assertTrue(DayRangeManager.inDayRange(rightRef, startYwd, endYwd, manager));

        startYwd = CalendarStatic.getRelativeYW(1);
        endYwd = CalendarStatic.getRelativeYW(2);
        assertTrue(DayRangeManager.inDayRange(rightRef, startYwd, endYwd, manager));

        startYwd = CalendarStatic.getRelativeYW(2);
        endYwd = CalendarStatic.getRelativeYW(2);
        assertFalse(DayRangeManager.inDayRange(rightRef, startYwd, endYwd, manager));

        startYwd = CalendarStatic.getRelativeYW(-100);
        endYwd = CalendarStatic.getRelativeYW(200);
        assertTrue(DayRangeManager.inDayRange(rightRef, startYwd, endYwd, manager));


    }

    /*
     * utility private method when debugging to print all objects in a manger
     */
    private void printAll() throws Exception {
        for(DayRangeBean bean : manager.getAllObjects()) {
            System.out.println(bean.toString());
        }
    }

    private void testOrder(int[] order) throws PersistenceException {
        assertEquals("manager size different than order array",order.length,manager.getAllObjects(leftRef).size());
        for(int i = 0; i < order.length; i++) {
            assertEquals("Testing order, layer [" + i + "]",order[i], manager.getAllObjects(leftRef).get(i).getDayRangeId());
        }
    }

    private void testWeek(int[] week, List<Integer> result) throws PersistenceException {
        assertEquals(7, week.length);
        for(int day = 0; day < week.length; day++) {
            assertEquals("Testing week, day [" + day + "]",week[day], (int) result.get(day));
        }
    }
}
