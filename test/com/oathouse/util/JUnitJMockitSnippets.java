/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.util;

// utility imports
import com.oathouse.oss.storage.objectstore.*;
import com.oathouse.oss.storage.objectstore.example.ExampleBean;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import static java.util.Arrays.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
// test imports
import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
/**
 *
 * @author Darryl Oatridge
 */
public class JUnitJMockitSnippets {

    public void beanBuilder() throws Exception {
        // code template bb
        int id = 1;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(id++));
        fieldSet.put("owner", ObjectBean.SYSTEM_OWNED);
        ExampleBean example = (ExampleBean) BeanBuilder.addBeanValues(new ExampleBean(), id, fieldSet);
    }

    public void managerContentBuilder() throws Exception {
        ObjectSetStore<ExampleBean> manager = new ObjectSetStore<>("ExampleManager", ObjectDataOptionsEnum.PERSIST);

        int id = 1;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(id));
        fieldSet.put("owner", ObjectBean.SYSTEM_OWNED);
        ExampleBean example = (ExampleBean) BeanBuilder.addBeanValues(new ExampleBean(), id, fieldSet);
        manager.setObject(example);

        // reinitialise the service so as to include the new additions
        // service.reInitialise();
    }

    public void listContentBuilder() throws Exception {
        final List<ExampleBean> exampleList = new LinkedList<>();
        final HashMap<String, String> fieldSet = new HashMap<>();
        int seed = 1;
        fieldSet.put("id", Integer.toString(1));
        exampleList.add((ExampleBean) BeanBuilder.addBeanValues(new ExampleBean(), seed++, fieldSet));
        fieldSet.put("id", Integer.toString(2));
        exampleList.add((ExampleBean) BeanBuilder.addBeanValues(new ExampleBean(), seed++, fieldSet));
    }

    private static void compareObjects(ObjectBean o1, ObjectBean o2) {
        StringBuilder sb =new StringBuilder();
        sb.append(o1.toXML(ObjectDataOptionsEnum.COMPACTED, ObjectDataOptionsEnum.TRIMMED));
        sb.append(o2.toXML(ObjectDataOptionsEnum.COMPACTED, ObjectDataOptionsEnum.TRIMMED));
        System.out.println(sb.toString());
    }

}