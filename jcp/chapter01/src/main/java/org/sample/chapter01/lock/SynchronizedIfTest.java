package org.sample.chapter01.lock;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.II_Result;

import java.util.ArrayList;
import java.util.Arrays;

@JCStressTest

// 验证输出.
@Outcome(id = "1, 1", expect = Expect.ACCEPTABLE, desc = "正确")
@Outcome(id = "1, 2", expect = Expect.ACCEPTABLE_INTERESTING, desc = "异常")
@Outcome(id = "2, 1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "异常")
@Outcome(id = "2, 2", expect = Expect.ACCEPTABLE_INTERESTING, desc = "异常")
@State
public class SynchronizedIfTest {
    final ArrayList<Integer> array = new ArrayList<>();

    @Actor
    public void actor1(II_Result r) {
        addIfNotExist();
        r.r1 = array.size();
    }

    @Actor
    public void actor2(II_Result r) {
        addIfNotExist();
        r.r2 = array.size();
    }

    private synchronized void addIfNotExist() {
        if (!array.contains(1)) {
            array.add(1);
        }
    }
}

