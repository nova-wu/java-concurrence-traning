package org.sample.chapter09.queue;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 无锁无界队列
 * 参考：《多处理器编程的艺术》10.5节、《Java并发编程实战》15.4.2节
 * @param <T>
 */
public class LockFreeQueue<T> {
    private static class Node<T> {
        final T value;
        final AtomicReference<Node<T>> next;

        public Node(T value) {
            this.value = value;
            next = new AtomicReference<>(null);
        }
    }

    private final Node<T> sentinel = new Node<T>(null);
    private final AtomicReference<Node<T>> head = new AtomicReference<>(sentinel);
    private final AtomicReference<Node<T>> tail = new AtomicReference<>(sentinel);

    /**
     * 入队
     * 入队操作需要顺序更新以下两个指针：
     *   1、最后一个节点的next指针
     *   2、tail指针
     * 当仅更新了一个指针时，我们称为中间状态
     * @param value
     */
    public void enq(T value) {
        Node<T> node = new Node<>(value);
        while (true) {
            Node<T> last = tail.get();
            Node<T> next = last.next.get();
            // 读取last、next期间，无其他线程入队、出队
            if (last == tail.get()) {
                if (next == null) {
                    // 更新最后一个节点的next指针
                    if (last.next.compareAndSet(next, node)) {
                        // 更新tail指针，可能成功，也可能失败
                        // 如果更新失败，说明其他线程已经帮助自己更新过了
                        tail.compareAndSet(last, node);
                        // 无论更新tail指针成功、失败，入队操作都成功
                        return;
                    }
                } else {
                    // 处于中间状态，帮助其他线程更新tail指针
                    // 之后循环执行自己的入队操作
                    tail.compareAndSet(last, next);
                }
            }
        }
    }

    /**
     * 出队
     * 出队操作需更新head指针
     * 更新head指针之前，必须确认tail没有指向被删除的哨兵节点
     * @return
     * @throws Exception
     */
    public T deq() throws Exception {
        while (true) {
            Node<T> first = head.get();
            Node<T> last = tail.get();
            Node<T> next = first.next.get();
            // 读取first、last、next期间，无出队操作
            if (first == head.get()) {
                // 如果head等于tail，并且他们指向的节点（哨兵）的next指针非null
                // 则认为tail指针落后了，需要调整tail指针
                if (first == last) {
                    if (next == null) {
                        throw new Exception("Empty");
                    }
                    tail.compareAndSet(last, next);
                } else {
                    // 更新head指针
                    T value = next.value;;
                    if (head.compareAndSet(first, next)) {
                        return value;
                    }
                }
            }
        }
    }
}
