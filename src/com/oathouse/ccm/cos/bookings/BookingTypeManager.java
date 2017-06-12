/*
 * @(#)BookingTypeManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.bookings;

import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectSetStore;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import java.util.LinkedList;
import java.util.List;

/**
 * The {@code BookingTypeManager} Class extends the methods of the parent class.
 * The key feature of this class is the unique nature of the identifiers of the
 * underlying {@code BookingTypeBean} objects. The {@code BookingTypeBean} is make
 * up of a number of bit values held in an Integer that are both unique to each other
 * but also hold common bit elements that allow the identifier to be interrogated and
 * thus define the behaviour of the booking both as a booking and a charge
 *
 *
 * @author Darryl Oatridge
 * @version 1.00 16-Oct-2011
 */
public class BookingTypeManager extends ObjectSetStore<BookingTypeBean> {
    /**
     * Constructs a {@code BookingTypeManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions
     */
    public BookingTypeManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        // this must be held in persistence so make usre persistence is one of the options
        super(managerName, ObjectDataOptionsEnum.addToArray(dataOptions, ObjectDataOptionsEnum.PERSIST, ObjectDataOptionsEnum.MEMORY));
    }

    /**
     * Overridden init() to allow the addition of a set of default types
     * @return instance of the BookingTypeManager
     * @throws PersistenceException
     */
    @Override
    public BookingTypeManager init() throws PersistenceException {
        super.init();
        // remove any that are not BookingTypeIds
        int defaultCount = 0;
        try {
            // set the default objects
            defaultCount = resetAllDefaultObjects(BTBits.ALL_OFF);
        } catch(NullObjectException ex) {
            throw new PersistenceException("Error loading the default BookingTypeBeans: " + ex.getMessage());
        }
        if(this.getAllObjects().size() != defaultCount) {
            throw new PersistenceException("There are " + defaultCount + " BookingTypeBean objects but " + this.getAllObjects().size() + " in persistence");
        }
        return(this);
    }

    /**
     * Helper method to enhance BTIdBits.getAllStrings() and return an exception when not found.
     * This only returns one flag name, therfore if there are several bits set in the bitFlag
     * only the first will be returned.
     *
     * @param flagBit
     * @return the string value of the bit flag
     * @throws NoSuchIdentifierException
     */
    public String getFlagName(int flagBit) throws NoSuchIdentifierException {
        if(BTFlagBits.getAllStrings(flagBit).isEmpty()) {
            throw new NoSuchIdentifierException("There is no flag for the presented flag bit");
        }
        return(BTFlagBits.getAllStrings(flagBit).get(0));
    }

    /**
     * Returns a {@code BookingTypeBean} with the given name. This allows a {@code BookingTypeBean} to be
     * reference by name as well as by id
     *
     * @param name the name of the BookingTypeBean
     * @return The BookingTypeBean with the given name
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public BookingTypeBean getObject(String name) throws NoSuchIdentifierException, PersistenceException {
        for(BookingTypeBean bean : getAllObjects()) {
            if(bean.getName().equalsIgnoreCase(name)) {
                return (bean);
            }
        }
        throw new NoSuchIdentifierException("A BookingTypeBean with name '" + name + "' does not exist");
    }

    /**
     * Provides a list of {@code BookingTypeBean} objects based on the bit mask values passed.
     * The two bits masks relate to the BTBits and the BTFlagBits providing a logical filter to
     * retrieve a list of {@code BookingTypeBean} objects that fulfil both bit masks.
     * For example:
     *
     * <blockquote>
     * <pre>
     *     int bitMask = BTBits.ATTENDING | BTBits.PENDING;
     *     int bitFlagMask = PRECHARGE
     *     List BookingTypeBean myList = getAllObjects(bitMask, );
     * </pre>
     * </blockquote>
     *
     * <p>
     * this would return a list of all {@code BookingTypeBean} objects that were both attending
     * and Pending with the pre-charge flag bit set.
     * </p>
     *
     * @param bookingTypeMask the bit mask to apply to the BookingType identifier
     * @param bookingTypeFlagMask the bit mask to apply to the BookingType flags
     * @return a list of BookingTypeBean filtered by the bookingTypeMask and bookingTypeFlagMask
     * @throws PersistenceException
     * @see BTBits
     */
    public List<BookingTypeBean> getAllObjects(int bookingTypeMask, int bookingTypeFlagMask) throws PersistenceException {
        LinkedList<BookingTypeBean> rtnSet = new LinkedList<>();

        for(BookingTypeBean bookingType : getAllObjects()) {
            if(BTIdBits.isFilter(bookingType.getBookingTypeId(), bookingTypeMask)
               && BTFlagIdBits.isFlag(bookingType.getFlagBits(), bookingTypeFlagMask)) {
                    rtnSet.add(bookingType);
            }
        }
        return rtnSet;
    }

    /**
     * Useful method for testing to see what all the currently held BookingType Objects are with the
     * bit values converted to English text.
     *
     * @param bookingTypeMask
     * @param bookingTypeFlagMask
     * @return a string representation of all bookingType objects
     * @throws PersistenceException
     */
    public String getAllObjectsAsString(int bookingTypeMask, int bookingTypeFlagMask) throws PersistenceException {
        StringBuilder sb = new StringBuilder();
        for(BookingTypeBean bookingType : this.getAllObjects(bookingTypeMask, bookingTypeFlagMask)) {
            sb.append("Name          : ").append(bookingType.getName()).append("\n");
            sb.append("BTIdBits      : ");
            for(String BTBit : BTBits.getAllStrings(bookingType.getBookingTypeId())) {
                sb.append(BTBit).append("  ");
            }
            sb.append("\n");
            sb.append("BTFlagsBits   : ");
            for(String BTFlagBit : BTFlagBits.getAllStrings(bookingType.getFlagBits())) {
                sb.append(BTFlagBit).append("  ");
            }
            sb.append("\n");
            sb.append("ChargePercent : ").append(bookingType.getChargePercent()).append("%");
            sb.append("\n\n");
        }
        return sb.toString();
    }

    /* **********************************************************************
     * Overridden Set & remove Methods as BookingTypeBeans are either loaded
     * from the setAllDefaultObjects as predefined or manualy added as xml
     * files in the data store specific to the authority.
     * ***********************************************************************/

    /**
     * Allows the dynamic changing of a BookingTypeBean flagBits and chargePercent value. This
     * values will be saved to file and not replaced unless reset using setObjectToDefault()
     *
     * @param bookingTypeId
     * @param flagBits
     * @param isOn
     * @param owner
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public BookingTypeBean setObjectFlagBits(int bookingTypeId, int flagBits, boolean isOn, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        if(!this.isIdentifier(bookingTypeId)) {
            throw new NoSuchIdentifierException("The bookingTypeId " + bookingTypeId + " does not exist");
        }
        BookingTypeBean bookingType = this.getObject(bookingTypeId);

        int newFlags = isOn ? BTFlagBits.turnOn(bookingType.getFlagBits(), flagBits)
                            : BTFlagBits.turnOff(bookingType.getFlagBits(), flagBits);
        bookingType.setFlagBits(newFlags, owner);
        return super.setObject(bookingType);
    }

    /**
     * Allows the dynamic changing of a BookingTypeBean  chargePercent value. This
     * values will be saved to file and not replaced unless reset using setObjectToDefault()
     *
     * @param bookingTypeId
     * @param chargePercent
     * @param owner
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     */
    public BookingTypeBean setObjectChargePercent(int bookingTypeId, int chargePercent, String owner) throws PersistenceException, NoSuchIdentifierException, NullObjectException {
        BookingTypeBean bookingType = this.getObject(bookingTypeId);
        bookingType.setChargePercent(chargePercent, owner);
        return super.setObject(bookingType);
    }

    /**
     * Allows a BookingTypeBean objects to be set back to their default values.
     * The BookingTypeBean objects to reset should be listed
     *
     * @param bookingTypeId
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     */
    public void setObjectToDefault(int bookingTypeId) throws PersistenceException, NullObjectException, NoSuchIdentifierException {
        if(!this.isIdentifier(bookingTypeId)) {
            throw new NoSuchIdentifierException("The bookingTypeId " + bookingTypeId + " does not exist");
        }
        resetAllDefaultObjects(bookingTypeId);
    }

    /**
     * Overrides the super.setObject() to stop the ability to set a {@code BookingTypeBean}.
     * {@code BookingTypeBean} are set by default for known types or added as an additional XML file
     * for specific authorities by hand.
     *
     * @param ob
     * @return
     * @throws PersistenceException
     * @throws NullObjectException
     */
    @Override
    public BookingTypeBean setObject(BookingTypeBean ob) throws PersistenceException, NullObjectException {
        throw new PersistenceException("You can not set a BookingTypeBean. BookingTypes are manually set");
    }

    /**
     * Overrides the super.removeObject() to avoid deleting any {@code BookingTypeBean}.
     * {@code BookingTypeBean} are set by default for known types or added as an additional XML file
     * for specific authorities by hand.
     *
     * @param bookingTypeId
     * @return the removed Object
     * @throws PersistenceException
     */
    @Override
    public BookingTypeBean removeObject(int bookingTypeId) throws PersistenceException {
        throw new PersistenceException("You can not remove a BookingTypeBean");
    }

    /* Private set method used for the default BookingTypeBean objects */
    private void setObject(int bookingTypeId, String name, int flagBits, int chargePercent, String owner, int resetbookingTypeId) throws PersistenceException, NullObjectException {
        // must have an identifier in the bookingTypeId
        if(!BTIdBits.isFilter(bookingTypeId, BTIdBits.TYPE_IDENTIFIER)) {
            throw new IllegalArgumentException("The bookingTypeId " + bookingTypeId + " for BookingType " + name + " does not have an Identifier");
        }
        // must have acharge type in the bookingTypeId
        if(!BTIdBits.isFilter(bookingTypeId, BTIdBits.TYPE_CHARGE)) {
            throw new IllegalArgumentException("The bookingTypeId " + bookingTypeId + " for BookingType " + name + " does not have an Charge");
        }
        // checxk it is not already an identifier (never overwrite) OR it is a reset
        if(!isIdentifier(bookingTypeId) || BTIdBits.isFilter(bookingTypeId, resetbookingTypeId)) {
            BookingTypeBean bookingType = new BookingTypeBean(bookingTypeId, name, flagBits, chargePercent, owner);
            // now set the object
            super.setObject(bookingType);
        }
    }

    /* ********************************************************
     * This automatically sets up a default set of BookingTypes
     * ********************************************************/
    private int resetAllDefaultObjects(int resetBookingTypeId) throws PersistenceException, NullObjectException {
        int count = 0;
        setObject(BTIdBits.ATTENDING_STANDARD, "ATTENDING_STANDARD",
                BTFlagIdBits.STANDARD_FLAGS
                | BTFlagBits.PREBOOKING_FLAG
                | BTFlagBits.POSTBOOKING_FLAG
                | BTFlagBits.ACTIVEBOOKING_FLAG
                | BTFlagBits.SD_CHANGEABLE_FLAG,
                100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.ATTENDING_NOCHARGE, "ATTENDING_NOCHARGE",
                BTFlagIdBits.NOCHARGE_FLAGS
                | BTFlagBits.PREBOOKING_FLAG
                | BTFlagBits.POSTBOOKING_FLAG
                | BTFlagBits.ACTIVEBOOKING_FLAG
                | BTFlagBits.SD_CHANGEABLE_FLAG,
                0, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.ATTENDING_SPECIAL, "ATTENDING_SPECIAL",
                BTFlagIdBits.STANDARD_FLAGS
                | BTFlagBits.PREBOOKING_FLAG
                | BTFlagBits.POSTBOOKING_FLAG
                | BTFlagBits.ACTIVEBOOKING_FLAG
                | BTFlagBits.SD_CHANGEABLE_FLAG,
                100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.WAITING_STANDARD, "WAITING_STANDARD",
                BTFlagIdBits.STANDARD_FLAGS
                | BTFlagBits.PREBOOKING_FLAG
                | BTFlagBits.SD_CHANGEABLE_FLAG,
                100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.WAITING_NOCHARGE, "WAITING_NOCHARGE",
                BTFlagIdBits.NOCHARGE_FLAGS
                | BTFlagBits.PREBOOKING_FLAG
                | BTFlagBits.SD_CHANGEABLE_FLAG,
                0, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.WAITING_SPECIAL, "WAITING_SPECIAL",
                BTFlagIdBits.STANDARD_FLAGS
                | BTFlagBits.PREBOOKING_FLAG
                | BTFlagBits.SD_CHANGEABLE_FLAG,
                100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.HOLIDAY_STANDARD, "HOLIDAY_STANDARD",
                BTFlagIdBits.STANDARD_FLAGS
                | BTFlagBits.PREBOOKING_FLAG,
                100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.HOLIDAY_NOCHARGE, "HOLIDAY_NOCHARGE",
                BTFlagIdBits.NOCHARGE_FLAGS
                | BTFlagBits.PREBOOKING_FLAG,
                0, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.HOLIDAY_SPECIAL, "HOLIDAY_SPECIAL",
                BTFlagIdBits.STANDARD_FLAGS
                | BTFlagBits.PREBOOKING_FLAG,
                100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.LEAVE_STANDARD, "LEAVE_STANDARD",
                BTFlagIdBits.STANDARD_FLAGS
                | BTFlagBits.PREBOOKING_FLAG
                | BTFlagBits.ALLOWANCE_FLAG,
                100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.LEAVE_NOCHARGE, "LEAVE_NOCHARGE",
                BTFlagIdBits.NOCHARGE_FLAGS
                | BTFlagBits.PREBOOKING_FLAG
                | BTFlagBits.ALLOWANCE_FLAG,
                0, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.LEAVE_SPECIAL, "LEAVE_SPECIAL",
                BTFlagIdBits.STANDARD_FLAGS
                | BTFlagBits.PREBOOKING_FLAG
                | BTFlagBits.ALLOWANCE_FLAG,
                100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.SUSPENDED_STANDARD, "SUSPENDED_STANDARD",
                BTFlagIdBits.STANDARD_FLAGS,
                100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.SUSPENDED_NOCHARGE, "SUSPENDED_NOCHARGE",
                BTFlagIdBits.NOCHARGE_FLAGS,
                0, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.SUSPENDED_SPECIAL, "SUSPENDED_SPECIAL",
                BTFlagIdBits.STANDARD_FLAGS,
                100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.SICK_STANDARD, "SICK_STANDARD",
                BTFlagIdBits.STANDARD_FLAGS
                | BTFlagBits.POSTBOOKING_FLAG
                | BTFlagBits.ACTIVEBOOKING_FLAG,
                100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.SICK_NOCHARGE, "SICK_NOCHARGE",
                BTFlagIdBits.NOCHARGE_FLAGS
                | BTFlagBits.POSTBOOKING_FLAG
                | BTFlagBits.ACTIVEBOOKING_FLAG,
                0, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.SICK_SPECIAL, "SICK_SPECIAL",
                BTFlagIdBits.STANDARD_FLAGS
                | BTFlagBits.POSTBOOKING_FLAG
                | BTFlagBits.ACTIVEBOOKING_FLAG,
                100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.ABSENT_STANDARD, "ABSENT_STANDARD",
                BTFlagIdBits.STANDARD_FLAGS
                ^ BTFlagBits.BOOKING_FLAG
                | BTFlagBits.ACTIVEBOOKING_FLAG
                ^ BTFlagBits.PRECHARGE_FLAG,
                100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.ABSENT_NOCHARGE, "ABSENT_NOCHARGE",
                BTFlagIdBits.NOCHARGE_FLAGS
                ^ BTFlagBits.BOOKING_FLAG
                | BTFlagBits.ACTIVEBOOKING_FLAG
                ^ BTFlagBits.PRECHARGE_FLAG,
                0, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.ABSENT_SPECIAL, "ABSENT_SPECIAL",
                BTFlagIdBits.STANDARD_FLAGS
                ^ BTFlagBits.BOOKING_FLAG
                | BTFlagBits.ACTIVEBOOKING_FLAG
                ^ BTFlagBits.PRECHARGE_FLAG,
                100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.CANCELLED_STANDARD, "CANCELLED_STANDARD",
                BTFlagIdBits.STANDARD_FLAGS
                | BTFlagBits.ACTIVEBOOKING_FLAG,
                100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.CANCELLED_NOCHARGE, "CANCELLED_NOCHARGE",
                BTFlagIdBits.NOCHARGE_FLAGS
                | BTFlagBits.ACTIVEBOOKING_FLAG,
                0, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.CANCELLED_SPECIAL, "CANCELLED_SPECIAL",
               BTFlagIdBits.STANDARD_FLAGS
                | BTFlagBits.ACTIVEBOOKING_FLAG,
               100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.REFUND_STANDARD, "REFUND_STANDARD",
                BTFlagIdBits.LAYER_FLAGS,
                100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.REFUND_SPECIAL, "REFUND_SPECIAL",
                BTFlagIdBits.LAYER_FLAGS,
                100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.DISCOUNT_STANDARD, "DISCOUNT_STANDARD",
                BTFlagIdBits.LAYER_FLAGS,
                100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.DISCOUNT_SPECIAL, "DISCOUNT_SPECIAL",
                BTFlagIdBits.LAYER_FLAGS,
                100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.PENALTY_STANDARD, "PENALTY_STANDARD",
                BTFlagIdBits.LAYER_FLAGS,
                100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        setObject(BTIdBits.PENALTY_SPECIAL, "PENALTY_SPECIAL",
                BTFlagIdBits.LAYER_FLAGS,
                100, ObjectBean.SYSTEM_OWNED, resetBookingTypeId);
        count++;
        return(count);
    }
}
