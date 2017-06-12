package com.oathouse.ccm.cos.accounts;

/**
 * Utility to deal with repeated tax and discount calculations
 *
 * <p>&copy; 2011 oathouse.com ltd</p>
 * <p>author: Darryl Oatridge</p>
 * <p>version: 1.00 31 Jan 2011</p>
 */
public class TDCalc {

    /**
     * Calculates tax to 0.1p, rounded down to nearest 0.1p
     * See HMRC VAT Guide section 17.5
     *
     * @param value in pennies to tenths of a penny.  Eg £1.43.2 = 1432L
     * @param taxRate in percent to 2 dp as a int.  Eg 17.5% = 1750L
     * @return tax value in same format as value
     */
    public static long getTax(long value, int taxRate) {
        return (value * taxRate)/10000;
    }

    /**
     * Calculates tax to 0.1p, rounded down to nearest 0.1p
     * See HMRC VAT Guide section 17.5
     *
     * @param value in pennies to tenths of a penny.  Eg £1.43.2 = 1432L
     * @param taxRate in percent to 2 dp as a int.  Eg 17.5% = 1750L
     * @return tax value in same format as value
     */
    public static long getValueIncTax(long value, int taxRate) {
        return (value * (10000 + taxRate))/10000;
    }

    /**
     * Returns a tax-exclusive value from a tax-inclusive value
     * Rounds up calculated value to nearest 0.1p, so that the tax is rounded down to
     * the nearest 0.1p (long values are in 10ths of pennies)
     * See HMRC VAT Guide section 17.5
     * @param valueIncTax
     * @param taxRate
     * @return
     */
    public static long getBeforeTaxValue(long valueIncTax, int taxRate) {
        return ( (long) Math.ceil((( (double)valueIncTax * 10000d) / (10000d + (double)taxRate ))) );
    }

    /**
     * Calculates a discount value as a long in tenths of pennies based on the
     * discount rate being an int to 2dp (eg 3.25% = 325).
     * Rounds the discount DOWN to the WHOLE penny closest to zero -
     * ie returns a long ending in zero
     * @param fullValue in pennies to tenths of a penny.  Eg £1.43.2 = 1432L
     * @param discountRate in percent to 2 dp as a int.  Eg 3.0% = 300
     * @return
     */
    public static long getDiscountValue(long fullValue, int discountRate) {
        long discountValue = (fullValue * discountRate)/10000;
        return(roundDown(discountValue));
    }

    /**
     * Calculates a precision value, correctly rounding (5 is up). The fullValue
     * must be in 10th Eg £1.43.2 = 1432L. The precision must be 0 or greater.
     * A precision of 0 does not round, 1 to the nearest penny, 3 to the nearest pound,
     * etc. Note that if the precision is greater than the fullValue then 0 is returned.
     *
     * @param fullValue in pennies to tenths of a penny.  Eg £1.43.2 = 1432L
     * @param precision value 0 returns the fullValue, 1 to the nearest penny, 3 to the nearest pound, etc
     * @return a rounded value according to the precision
     */
    public static long getPrecisionValue(long fullValue, int precision) {
        if(precision < 1) {
            return fullValue;
        }
        int power = (int) Math.pow(10, precision - 1);
        // this adds 5 to just before the cut so as to round
        return ((fullValue/power) + 5)/10 * (power * 10);
    }

    /* *****************************
     * P R I V A T E   M E T H O D S
     * *****************************/
    private static long roundDown(long money) {
        boolean neg = false;
        if(money < 0) {
            neg = true;
        }
        money = Math.abs(money);
        long whole = money / 10;
        money = whole * 10;
        if(neg) {
            return(money * -1);
        }
        return(money);
    }

    private TDCalc() {
    }

}
