package org.sample.chapter10.cow;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 基于CopyOnWrite模式实现的路由表
 */
public class CopyOnWriteRouter {
    /**
     * 路由项
     */
    public static final class RouterItem {
        private final String  ip;
        private final Integer port;
        private final String  iface;

        public RouterItem(String ip, Integer port, String iface){
            this.ip = ip;
            this.port = port;
            this.iface = iface;
        }

        public boolean equals(Object obj) {
            if (obj instanceof RouterItem) {
                RouterItem r = (RouterItem)obj;
                return this.iface.equals(r.iface) &&
                        this.ip.equals(r.ip) &&
                        this.port.equals(r.port);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = ip.hashCode();
            result = 31 * result + Integer.hashCode(port);
            result = 31 * result + iface.hashCode();
            return result;
        }
    }

    /**
     * 路由表
     */
    public static class RouteTable {
        /**
         * 用于在内存中持有路由表
         */
        ConcurrentHashMap<String, CopyOnWriteArraySet<RouterItem>> rt = new ConcurrentHashMap<>();

        /**
         * 获取接口对应的所有路由信息
         * @param iface 接口
         * @return 接口对应的路由信息
         */
        public Set<RouterItem> get(String iface) {
            return rt.get(iface);
        }

        /**
         * 移除路由项
         * @param routerItem 路由项
         */
        public void remove(RouterItem routerItem) {
            Set<RouterItem> set = rt.get(routerItem.iface);
            if (set != null) {
                set.remove(routerItem);
            }
        }

        /**
         * 增加路由项
         * @param routerItem 路由项
         */
        public void add(RouterItem routerItem) {
            Set<RouterItem> set = rt.computeIfAbsent(
                    routerItem.iface, r -> new CopyOnWriteArraySet<>());
            set.add(routerItem);
        }
    }

    public static void main(String [] args) throws IOException, InterruptedException {
        RouterItem r1 = new RouterItem("192.168.1.1", 7777, "Hello");
        RouterItem r2 = new RouterItem("192.168.1.2", 7777, "Hello");
        RouterItem r3 = new RouterItem("192.168.1.1", 7777, "Hello");

        RouteTable rt = new RouteTable();
        rt.add(r1);
        rt.add(r2);
        rt.add(r1);
        rt.remove(r3);

        System.out.println(rt.get("Hello").size());
    }
}
