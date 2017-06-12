 /*
 * @(#)PriceListBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config.finance;

import com.oathouse.ccm.cma.VT;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.valueholder.SDHolder;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import org.jdom2.Element;

/**
 * The {@code PriceListBean} Class creates a timetable map of pricing periods.
 *
 * @author Darryl Oatridge
 * @version 1.00 07-Aug-2010
 */
public class PriceListBean extends ObjectBean {

    private static final long serialVersionUID = 20100807100L;
    private volatile String name;
    private volatile String label;
    private final ConcurrentSkipListMap<Integer, Long> periodSdValues; // periodSd -> value

    /**
     * Price configuration constructor
     *
     * @param priceListId
     * @param name
     * @param label
     * @param periodSdValues a map of periodSd - long value
     * @param owner
     */
    public PriceListBean(int priceListId, String name, String label, Map<Integer, Long> periodSdValues,
            String owner) {
        super(priceListId, owner);
        this.name = name;
        this.label = label;
        this.periodSdValues = new ConcurrentSkipListMap<>();
        if(periodSdValues != null) {
            synchronized (periodSdValues) {
                this.periodSdValues.putAll(periodSdValues);
            }
        }
    }

    public PriceListBean() {
        super();
        this.name = "";
        this.label = "";
        this.periodSdValues = new ConcurrentSkipListMap<>();
    }

    /**
     * returns the id for the PriceListBean
     */
    public int getPriceListId() {
        return super.getIdentifier();
    }

    /**
     * returns the name of the PriceListBean
     */
    public String getName() {
        return name;
    }

    /**
     * returns the label of the PriceListBean
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns a price for a time where the time falls within a period. If no period exists then zero is returned
     *
     * @param time the time for the charge
     * @return a charge value, or 0 if no period found
     */
    public long getPeriodSdValue(int time) {
        for(int valueSd : periodSdValues.keySet()) {
            if(SDHolder.inRange(valueSd, SDHolder.getSD(time, 1))) {
                return (periodSdValues.get(valueSd));
            }
        }
        return 0;
    }

    /**
     * provides a sum of prices for all pricing periods that fall within the passed periodSd. if the periodSd overlaps
     * any price periodSd even by a single minute, it will be included in the price.
     *
     * @param periodSd the period to calculated.
     * @return the sum of charges covered by the period passed
     */
    public long getPeriodSdSum(int periodSd) {
        long total = 0;
        for(int valueSd : periodSdValues.keySet()) {
            if(SDHolder.inRange(valueSd, periodSd)) {
                total += periodSdValues.get(valueSd);
            }
        }
        return (total);
    }

    /**
     * returns a map of periodSd-value that fall within the passed periodSd
     *
     * @param periodSd
     * @return map of periodSd - value
     */
    public Map<Integer, Long> getPeriodSdMap(int periodSd) {
        Map<Integer, Long> rtnMap = new ConcurrentSkipListMap<>();
        for(int valueSd : periodSdValues.keySet()) {
            if(SDHolder.inRange(valueSd, periodSd)) {
                rtnMap.put(valueSd, periodSdValues.get(valueSd));
            }
        }
        return (rtnMap);
    }

    /**
     * the stored map of periods and charges.
     * @return map of periodSd - value
     */
    public Map<Integer, Long> getPeriodSdValues() {
        return new ConcurrentSkipListMap<>(periodSdValues);
    }

    /**
     * Determines the start of the first period
     * @return the start
     */
    public int getFirstPeriodStartTime() {
        return (SDHolder.getStart(periodSdValues.firstKey()));
    }

    public static final Comparator<PriceListBean> CASE_INSENSITIVE_NAME_ORDER = new Comparator<PriceListBean>() {
        @Override
        public int compare(PriceListBean p1, PriceListBean p2) {
            if(p1 == null && p2 == null) {
                return 0;
            }
            // just in case there are null object values show them last
            if(p1 != null && p2 == null) {
                return -1;
            }
            if(p1 == null && p2 != null) {
                return 1;
            }
            // compare
            int result = (p1.getName().toLowerCase()).compareTo(p2.getName().toLowerCase());
            if(result != 0) {
                return result;
            }
            // dob not unique so violates the equals comparability. Can cause disappearing objects in Sets
            return (((Integer) p1.getIdentifier()).compareTo((Integer) p2.getIdentifier()));
        }
    };

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final PriceListBean other = (PriceListBean) obj;
        if((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if((this.label == null) ? (other.label != null) : !this.label.equals(other.label)) {
            return false;
        }
        if(this.periodSdValues != other.periodSdValues && (this.periodSdValues == null || !this.periodSdValues.equals(other.periodSdValues))) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 59 * hash + (this.label != null ? this.label.hashCode() : 0);
        hash = 59 * hash + (this.periodSdValues != null ? this.periodSdValues.hashCode() : 0);
        return hash + super.hashCode();
    }

    /**
     * crates all the elements that represent this bean at this level.
     *
     * @return List of elements in order
     */
    @Override
    public List<Element> getXMLElement() {
        List<Element> rtnList = new LinkedList<>();
        // create and add the content Element
        for(Element e : super.getXMLElement()) {
            rtnList.add(e);
        }
        Element bean = new Element(VT.PRICE_LIST.bean());
        rtnList.add(bean);
        // set the data
        bean.setAttribute("name", name);
        bean.setAttribute("label", label);
        Element allElements = new Element("periodSdValues_map");
        bean.addContent(allElements);
        for(int period : periodSdValues.keySet()) {
            Element eachKey = new Element("periodSdValues");
            allElements.addContent(eachKey);
            eachKey.setAttribute("key", Integer.toString(period));
            eachKey.setAttribute("value", Long.toString(periodSdValues.get(period)));
        }
        bean.setAttribute("serialVersionUID", Long.toString(serialVersionUID));
        return (rtnList);
    }

    /**
     * sets all the values in the bean from the XML. Remember to put default values in getAttribute() and check the
     * content of getText() if you are parsing to a value.
     *
     * @param root element of the DOM
     */
    @Override
    public void setXMLDOM(Element root) {
        // extract the super meta data
        super.setXMLDOM(root);
        // extract the bean data
        Element bean = root.getChild(VT.PRICE_LIST.bean());
        // set up the data
        name = bean.getAttributeValue("name", "");
        label = bean.getAttributeValue("label", "");
        periodSdValues.clear();
        for(Object o : bean.getChild("periodSdValues_map").getChildren("periodSdValues")) {
            Element eachElement = (Element) o;
            int periodSd = Integer.valueOf(eachElement.getAttributeValue("key", "-1"));
            long value = Long.valueOf(eachElement.getAttributeValue("value", "-1"));
            periodSdValues.put(periodSd, value);
        }
    }
}
