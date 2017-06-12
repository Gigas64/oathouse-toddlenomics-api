package com.oathouse.ccm.cma.booking;

import com.oathouse.ccm.cos.bookings.BookingRequestBean;
import com.oathouse.ccm.cos.config.DayRangeBean;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * @author Darryl Oatridge
 */
public class BookingModelService {

    // Singleton Instance
    private volatile static BookingModelService INSTANCE;
    // to stop initialising when initialised
    private volatile boolean initialise = true;
    //
    private volatile ConcurrentSkipListMap<Integer, ConcurrentSkipListSet<BookingModelBean>> bookingYwdMap;
    private volatile ConcurrentSkipListMap<Integer, ConcurrentSkipListSet<BookingModelBean>> bookingChildMap;
    private volatile ConcurrentSkipListMap<Integer, ConcurrentSkipListSet<BookingModelBean>> bookingAccountMap;
    private volatile ConcurrentSkipListMap<Integer, ConcurrentSkipListSet<BookingModelBean>> bookingRoomMap;

    private final ConcurrentSkipListMap<Integer, BookingModelBean> bookingModelManager = new ConcurrentSkipListMap<>();

    /**
     * Singleton pattern to get the instance of the {@literal BillingService} class
     * @return instance of the {@literal BillingService}
     */
    public static BookingModelService getInstance() {
        if(INSTANCE == null) {
            synchronized (BookingModelService.class) {
                // Check again just incase before we synchronised an instance was created
                if(INSTANCE == null) {
                    INSTANCE = new BookingModelService().init();
                }
            }
        }
        return INSTANCE;
    }

    public BookingModelService init() {
        if(initialise) {
            bookingYwdMap = new ConcurrentSkipListMap<>();
            bookingChildMap = new ConcurrentSkipListMap<>();
            bookingAccountMap = new ConcurrentSkipListMap<>();
            bookingRoomMap = new ConcurrentSkipListMap<>();
        }
        initialise = true;
        return(this);
    }

    /**
     * validation method: checks the timestamp associated with an id is the same.
     * @param id
     * @param timestamp
     * @return
     */
    public boolean isModified(int id, long timestamp) {
        return !(bookingModelManager.containsKey(id) && bookingModelManager.get(id).isModified(timestamp));
    }

    public BookingModelBean setBooking(BookingRequestBean request, DayRangeBean dayRange) {
        if(isModified(dayRange.getDayRangeId(), dayRange.getModified())) {
            return null;
        }

        return null;
    }
}
