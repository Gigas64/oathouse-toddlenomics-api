package com.oathouse.ccm.builders;

import com.oathouse.oss.storage.exceptions.MaxCountReachedException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.valueholder.SDHolder;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.ccm.cma.ServicePool;
import com.oathouse.ccm.cos.config.education.ChildEducationTimetableBean;
import com.oathouse.ccm.cos.config.education.ChildEducationTimetableManager;
import com.oathouse.ccm.cos.config.DayRangeBean;
import com.oathouse.ccm.cos.config.DayRangeManager;
import com.oathouse.ccm.cma.ServicePoolConfig;
import com.oathouse.ccm.cma.config.TimetableService;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.config.TimetableBean;
import com.oathouse.ccm.cos.config.TimetableManager;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.Assert.*;

/**
 *
 * @author Darryl
 */
public class TSBuilder {

    private static final String owner = ObjectBean.SYSTEM_OWNED;
    private static final ServicePool engine = ServicePoolConfig.getInstance(owner);

    public static void setUp() throws Exception {
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

    /* *****************************************************************
     * T I M E T A B L E   S e r v i c e   S e t u p s
     *
     * TIME      10  15  20  25  30  35  40  45  50  55  60  65  70  75  80
     * Periods   |--||--||--------------||--||--||--------------||--||--|
     *
     * AM drop   +   +   +
     * AM pickup                        -   -
     * PM drop                           +   +   +
     * PM pickup                                                -   -   -
     *
     * *****************************************************************/
    public static int setupTimetables(int roomId) throws Exception {
        TimetableService ts = engine.getTimetableService().reInitialise();
        int timetableId = ts.getTimetableManager().generateIdentifier();
        boolean[] days = BeanBuilder.getDays(BeanBuilder.MON_TO_FRI);
        try {
            Map<Integer, Set<Integer>> periodAndOptions = new ConcurrentSkipListMap<Integer, Set<Integer>>();

            Set<Integer> morningEndOptions = new ConcurrentSkipListSet<Integer>();
            morningEndOptions.add(SDHolder.getSD(39, 0));
            morningEndOptions.add(SDHolder.getSD(44, 0));
            morningEndOptions.add(SDHolder.getSD(69, 0));
            morningEndOptions.add(SDHolder.getSD(74, 0));
            morningEndOptions.add(SDHolder.getSD(79, 0));

            Set<Integer> midEndOptions = new ConcurrentSkipListSet<Integer>();
            morningEndOptions.add(SDHolder.getSD(69, 0));
            morningEndOptions.add(SDHolder.getSD(74, 0));
            morningEndOptions.add(SDHolder.getSD(79, 0));

            Set<Integer> afternoonEndOptions = new ConcurrentSkipListSet<Integer>();

            periodAndOptions.put(SDHolder.getSD(10, 4), morningEndOptions);
            periodAndOptions.put(SDHolder.getSD(15, 4), morningEndOptions);
            periodAndOptions.put(SDHolder.getSD(20, 19), morningEndOptions);
            periodAndOptions.put(SDHolder.getSD(40, 4), midEndOptions);
            periodAndOptions.put(SDHolder.getSD(45, 4), midEndOptions);
            periodAndOptions.put(SDHolder.getSD(50, 19), midEndOptions);
            periodAndOptions.put(SDHolder.getSD(70, 4), afternoonEndOptions);
            periodAndOptions.put(SDHolder.getSD(75, 4), afternoonEndOptions);
            TimetableBean tt = new TimetableBean(timetableId, "Timetable" + timetableId, "label", periodAndOptions, TSBuilder.getOwner());

            TimetableManager tm = ts.getTimetableManager();
            tm.setObject(tt);
        } catch(Exception ex) {
            fail("Could not create timetable: " + ex.getMessage());
        }
        try {
            DayRangeManager drm = ts.getDayRangeManager();
            Set<Integer> initSet = new ConcurrentSkipListSet<Integer>();
            initSet.add(roomId);
            drm.init(initSet);
            drm.setObject(roomId, new DayRangeBean(1, roomId, timetableId, 2000010, 3000010, days, TSBuilder.getOwner()));
        } catch(Exception ex) {
            fail("Could not create timetable day range: " + ex.getMessage());
        }
        // sets up education days for the nursery as a whole
        try {
            DayRangeManager erm = ts.getEducationRangeManager();
            Set<Integer> initSet = new ConcurrentSkipListSet<Integer>();
            initSet.add(ChildEducationTimetableManager.ALL_ROOMS);
            erm.init(initSet);
            erm.setObject(ChildEducationTimetableManager.ALL_ROOMS, new DayRangeBean(1, ChildEducationTimetableManager.ALL_ROOMS, ChildEducationTimetableManager.ALL_ROOMS, 2000010, 3000010, days, TSBuilder.getOwner()));
        } catch(Exception ex) {
            fail("Could not create education day range: " + ex.getMessage());
        }
        return(timetableId);
    }

    public static int setupEducationTimetable(int roomId, int childId) throws PersistenceException, MaxCountReachedException {
        // sets up education for a child
        TimetableService ts = engine.getTimetableService().reInitialise();
        ChildService cs = engine.getChildService().reInitialise();
        int timetableId = ts.getChildEducationTimetableManager().generateIdentifier();
        boolean[] days = BeanBuilder.getDays(BeanBuilder.MON_TO_FRI);
        try {
            Set<Integer> educationSd = new ConcurrentSkipListSet<Integer>();
            educationSd.add(SDHolder.getSD(20, 14));
            educationSd.add(SDHolder.getSD(50, 24));
            ChildEducationTimetableBean cet = new ChildEducationTimetableBean(timetableId, "Education" + timetableId, "lable", roomId, educationSd, TSBuilder.getOwner());

            ChildEducationTimetableManager cetm = ts.getChildEducationTimetableManager();
            cetm.init();
            cetm.setObject(cet);
        } catch(Exception ex) {
            fail("Could not create education timetable: " + ex.getMessage());
        }
        try {
            DayRangeManager cerm = cs.getChildEducationRangeManager();
            Set<Integer> initSet = new ConcurrentSkipListSet<Integer>();
            initSet.add(childId);
            cerm.init(initSet);
            cerm.setObject(childId, new DayRangeBean(1, childId, timetableId, 2000010, 3000010, days, TSBuilder.getOwner()));
        } catch(Exception ex) {
            fail("Could not create child education day range: " + ex.getMessage());
        }
        return(timetableId);
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
