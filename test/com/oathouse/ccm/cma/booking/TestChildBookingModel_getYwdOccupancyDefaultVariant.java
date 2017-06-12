/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cma.booking;

import com.oathouse.ccm.cma.VABoolean;
import com.oathouse.ccm.cma.VT;
import com.oathouse.ccm.cma.config.AgeRoomService;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cos.bookings.BTBits;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.ccm.cos.bookings.BookingManager;
import com.oathouse.ccm.cos.config.RoomConfigBean;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.oathouse.oss.storage.valueholder.SDHolder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
/**
 *
 * @author Darryl Oatridge
 */
public class TestChildBookingModel_getYwdOccupancyDefaultVariant {

    private int ywd = CalendarStatic.getRelativeYW(1);
    private int contactId = 99;
    private int bookingTypeId = BTIdBits.ATTENDING_STANDARD;
    private int actionBits = 0;

    private BookingManager manager;

    private Set<Integer> occupancySdSet;

    @Mocked
    AgeRoomService roomServiceMock;
    @Cascading
    PropertiesService propertiesMock;

    @Before
    public void setUp() throws Exception {
        manager = new BookingManager("bookingModel", ObjectDataOptionsEnum.MEMORY);
        manager.init();
        int periodSd = SDHolder.getSD(10,20);
        int profileId = 1;
        int roomId = 1;
        manager.setCreateBooking(ywd, periodSd, profileId, roomId, contactId, contactId, contactId, bookingTypeId, actionBits, ywd, "", ObjectBean.SYSTEM_OWNED);
        periodSd = SDHolder.getSD(20,20);
        profileId = 2;
        manager.setCreateBooking(ywd, periodSd, profileId, roomId, contactId, contactId, contactId, bookingTypeId, actionBits, ywd, "", ObjectBean.SYSTEM_OWNED);
        periodSd = SDHolder.getSD(10,20);
        profileId = 3;
        roomId = 2;
        manager.setCreateBooking(ywd, periodSd, profileId, roomId, contactId, contactId, contactId, bookingTypeId, actionBits, ywd, "", ObjectBean.SYSTEM_OWNED);
        periodSd = SDHolder.getSD(20,20);
        profileId = 4;
        manager.setCreateBooking(ywd, periodSd, profileId, roomId, contactId, contactId, contactId, bookingTypeId, actionBits, ywd, "", ObjectBean.SYSTEM_OWNED);

        occupancySdSet = VABoolean.asSet(SDHolder.getSD(15, 0), SDHolder.getSD(25, 0));
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_roomOccupancy() throws Exception {

        // Record
        new Expectations() {
            {
                AgeRoomService.getInstance(); returns(roomServiceMock);
                roomServiceMock.isId(anyInt, VT.ROOM_CONFIG); result = true;
                PropertiesService.getInstance(); returns(propertiesMock);
                propertiesMock.getSystemProperties().getOccupancySdSet(); returns(occupancySdSet);
            }
        };
        // Replay
        Map<Integer,Integer> rtnMap = ChildBookingModel.getYwdRoomOccupancyDefaultVariant(manager, ywd, 1, BTBits.ATTENDING_BIT);
        assertTrue(rtnMap.containsKey(SDHolder.getSD(15, 0)));
        assertTrue(rtnMap.containsKey(SDHolder.getSD(25, 0)));

        assertEquals(1, (int) rtnMap.get(SDHolder.getSD(15, 0)));
        assertEquals(2, (int) rtnMap.get(SDHolder.getSD(25, 0)));
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_occupancy() throws Exception {
        // Record
        new NonStrictExpectations() {
            RoomConfigBean roomMock;
            {
                AgeRoomService.getInstance(); returns(roomServiceMock);
                roomServiceMock.getOpenRooms(ywd); returns(Arrays.asList(roomMock, roomMock));
                roomMock.getRoomId(); returns(1,2);
                AgeRoomService.getInstance(); returns(roomServiceMock);
                roomServiceMock.isId(anyInt, VT.ROOM_CONFIG); result = true;
                PropertiesService.getInstance(); returns(propertiesMock);
                propertiesMock.getSystemProperties().getOccupancySdSet(); returns(occupancySdSet);
            }
        };
        // Replay
        Map<Integer,Integer> rtnMap = ChildBookingModel.getYwdOccupancyDefaultVariant(manager, ywd, BTBits.ATTENDING_BIT);
        assertTrue(rtnMap.containsKey(SDHolder.getSD(15, 0)));
        assertTrue(rtnMap.containsKey(SDHolder.getSD(25, 0)));

        assertEquals(2, (int) rtnMap.get(SDHolder.getSD(15, 0)));
        assertEquals(4, (int) rtnMap.get(SDHolder.getSD(25, 0)));
    }


}