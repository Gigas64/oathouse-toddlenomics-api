/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.util;

// common imports
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import java.io.File;
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
public class BitType {

    @Test
    public void BookingBitType() throws Exception {
        int bits = 4352;
        System.out.println("BookingBits : "  + bits + " = " + BTIdBits.getAllStrings(bits));
    }

    @Test
    public void BillingBits() throws Exception {
        int bits =  27;
        System.out.println("BillingBits : "  + bits + " = " + BillingEnum.getAllStrings(bits));
    }


}
