package org.sample.chapter01.lock;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.II_Result;

import java.util.ArrayList;


@JCStressTest(Mode.Termination)
@Outcome(id = "TERMINATED", expect = Expect.ACCEPTABLE, desc = "正常结束")
@Outcome(id = "STALE", expect = Expect.ACCEPTABLE_INTERESTING, desc = "异常")
@State
public class SynchronizedSetTest {
    int v;

    @Actor
    public void actor1() {
        while (getV() == 0) {
            // spin
        }
    }

    @Signal
    public void actor2() {
        addOne();
    }

    private synchronized void addOne() {
        v += 1;
    }

    private int getV() {
        return v;
    }
}

