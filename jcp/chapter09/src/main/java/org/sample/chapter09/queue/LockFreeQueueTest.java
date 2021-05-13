package org.sample.chapter09.queue;

public class LockFreeQueueTest {
    public static void main(String [] args) throws Exception {
        LockFreeQueue<Integer> queue = new LockFreeQueue<>();

        queue.enq(23);
        queue.enq(34);

        System.out.println(queue.deq());
        System.out.println(queue.deq());

        System.out.println(queue.deq());

    }
}
