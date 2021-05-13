package org.sample.chapter11.balking;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 基于Balking模式实现路由表的异步存盘功能
 */
public class BalkingRouter {
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
         * 标识路由表是否发生过变化
         */
        volatile boolean changed = false;
        /**
         * 单线程调度器：用于异步保存路由表
         */
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

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
                change();
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
            change();
        }

        /**
         * 标识路由表发生了变化
         */
        public void change(){
            changed = true;
        }

        /**
         * 将路由表保存在本地文件
         */
        public void save2Local() {
            System.out.println("SAVE LOCAL FILE");
        }

        /**
         * 自动执行存盘操作
         */
        public void autoSave() {
            if (!changed) {
                System.out.println("NO CHANGED");
                return;
            }
            changed = false;
            this.save2Local();
        }

        /**
         * 启动定时任务，路由表发生变后，自动地异步存盘
         * @throws IOException
         */
        public void startLocalSaver() throws IOException {
            ses.scheduleWithFixedDelay(()->{
                autoSave();
            }, 1, 1, TimeUnit.SECONDS);
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

        rt.startLocalSaver();
        System.out.println(rt.get("Hello").size());
        Thread.sleep(3000);
        rt.add(r3);
    }
}
