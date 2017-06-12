package com.oathouse.ccm.cma.booking;

import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.oss.storage.valueholder.*;
import com.oathouse.util.*;
import java.util.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 * @author Darryl Oatridge
 */
public class TestChildBookingHistory_SetBookingHistory {

    ChildBookingHistory bookingHistory;
    BookingBean booking;
    BookingBean history;
    PropertiesService ps;

    @Before
    public void setUp() throws Exception {
        bookingHistory = ChildBookingHistory.getInstance();
        bookingHistory.init();
        bookingHistory.clear();
    }


    @After
    public void tearDown() throws Exception {
        bookingHistory.clear();
        bookingHistory = null;
        ps = null;
    }

    @Test
    public void savesBookingInCorrectKeyAndKeepsArchiveYwdZero() throws Exception {
        booking = new BookingBean(99, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, BookingState.OPEN, 15, "A", "B");
        history = bookingHistory.setBookingHistory(booking);
        assertEquals(1, history.getIdentifier());
//        System.out.println(history.toXML());
        List<BookingBean> histories = bookingHistory.getBookingHistory(99);
        assertEquals(1, histories.size());
        BookingBean hOut = histories.get(0);
        assertEquals(history, hOut);
        assertEquals(0, hOut.getArchiveYwd());


    }
}
