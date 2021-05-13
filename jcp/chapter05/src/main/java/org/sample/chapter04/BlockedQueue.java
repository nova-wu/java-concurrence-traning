package org.sample.chapter04;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于Lock实现的阻塞队列
 * @param <T>
 */
public class BlockedQueue<T> {
    private T[] items;
    private int head;
    private int tail;
    private Lock lock;

    public BlockedQueue(int capacity) {
        items = (T[]) new Object[capacity];
        head = 0;
        tail = 0;
        lock = new ReentrantLock();
    }

    /**
     * 入队
     * @param item
     */
    public void enq(T item) {
        lock.lock();
        try {
            if (tail - head == items.length) {
                throw new RuntimeException("Full");
            }

            items[tail % items.length] = item;
            tail++;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 出队
     * @return
     */
    public T deq() {
        lock.lock();
        try {
            if (tail == head) {
                throw new RuntimeException("Empty");
            }

            T item = items[head % items.length];
            head++;

            return item;
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws Exception {
        final BlockedQueue<Integer> queue = new BlockedQueue<>(10);

        Thread[] threads = new Thread[10];
        for (int idx=0; idx<10; idx++) {
            threads[idx] = new Thread(()->{
                try {
                    String name = Thread.currentThread().getName();
                    queue.enq(Integer.parseInt(name));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, String.valueOf(idx));
        }

        for (int idx=0; idx<10; idx++) {
            threads[idx].start();
        }

        for (int idx=0; idx<10; idx++) {
            threads[idx].join();
        }

        for (int idx=0; idx<10; idx++) {
            Integer item = queue.deq();
            System.out.println("deq:" + item);
        }
    }
}
