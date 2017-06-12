package com.oathouse.ccm.cma;

import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import java.util.List;

/**
 * Utility to allow easy set up of testing
 * @author Darryl Oatridge
 */
public class ServicePoolConfig {
    // Singleton Instance
    private volatile static ServicePool engine;
    private static final long serialVersionUID = 1L;

    /*
     * Singleton so constructor is private
     */
    private ServicePoolConfig() {
    }

    public static ServicePool getInstance(String authority) {
        if(engine == null) {
            synchronized (ServicePool.class) {
                // Check again just incase before we synchronised an instance was created
                if(engine == null) {
                    engine = new ServicePool("./oss/data", authority, "./oss/conf/jugla_log4j.properties");
//                    engine = new ServicePool(21021, authority);
                }
            }
        }
        return engine;
    }

    public static void printList(List<? extends ObjectBean> oList) {
        for(ObjectBean objectBean : oList) {
            System.out.println(objectBean.toXML(ObjectDataOptionsEnum.COMPACTED, ObjectDataOptionsEnum.TRIMMED));
        }
    }

    public static void compareObjects(ObjectBean o1, ObjectBean o2) {
        StringBuilder sb =new StringBuilder();
        sb.append(o1.toXML(ObjectDataOptionsEnum.COMPACTED, ObjectDataOptionsEnum.TRIMMED));
        sb.append(o2.toXML(ObjectDataOptionsEnum.COMPACTED, ObjectDataOptionsEnum.TRIMMED));
        System.out.println(sb.toString());
    }


}
