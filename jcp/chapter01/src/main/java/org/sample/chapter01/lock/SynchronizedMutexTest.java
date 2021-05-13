package org.sample.chapter01.lock;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;

@JCStressTest

// 验证输出.
@Outcome(id = "1, 1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "两个线程结果都是1: 原子性失败.")
@Outcome(id = "1, 2", expect = Expect.ACCEPTABLE, desc = "线程1先于线程2执行")
@Outcome(id = "2, 1", expect = Expect.ACCEPTABLE, desc = "线程2先于线程1执行")
@State
public class SynchronizedMutexTest {

    final Object lock = new Object();
    int v;

    @Actor
    public void actor1(II_Result r) {
        synchronized (lock) {
            r.r1 = ++v; // 记录actor1的结果
        }
    }

    @Actor
    public void actor2(II_Result r) {
        synchronized (lock) {
            r.r2 = ++v; // 记录actor2的结果
        }
    }
}

