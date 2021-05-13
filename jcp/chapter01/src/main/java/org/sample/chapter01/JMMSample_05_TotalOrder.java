/*
 * Copyright (c) 2016, Red Hat Inc.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.sample.chapter01;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;

import static org.openjdk.jcstress.annotations.Expect.*;

public class JMMSample_05_TotalOrder {

    /*
      ----------------------------------------------------------------------------------------------------------

        Another property comes for the inter-thread semantics deals not with
        partial, but total order. In JMM, synchronization order mandates that
        special "synchronization" actions always form a total order, consistent
        with program order.

        The most famous example that needs total order of operation is Dekker
        idiom, the building block of Dekker lock.

              [OK] org.openjdk.jcstress.samples.JMMSample_05_TotalOrder.PlainDekker
            (JVM args: [-server])
          Observed state   Occurrences              Expectation  Interpretation
                    0, 0    12,006,499   ACCEPTABLE_INTERESTING  Violates sequential consistency
                    0, 1    53,849,842               ACCEPTABLE  Trivial under sequential consistency
                    1, 0    39,405,818               ACCEPTABLE  Trivial under sequential consistency
                    1, 1            21               ACCEPTABLE  Trivial under sequential consistency
    */

    @JCStressTest
    @Outcome(id = {"0, 1", "1, 0", "1, 1"}, expect = ACCEPTABLE, desc = "预料支持")
    @Outcome(id = "0, 0",                   expect = ACCEPTABLE_INTERESTING,  desc = "预料之外")
    @State
    public static class PlainDekker {
        int x;
        int y;

        @Actor
        public void actor1(II_Result r) {
            x = 1;
            r.r1 = y;
        }

        @Actor
        public void actor2(II_Result r) {
            y = 1;
            r.r2 = x;
        }
    }

    /*
      ----------------------------------------------------------------------------------------------------------

        Adding volatile to both $x and $y bring them together into synchronization order,
        and thus require the results to be consistent with the case when reads/writes
        form a total order.

              [OK] org.openjdk.jcstress.samples.JMMSample_05_TotalOrder.VolatileDekker
            (JVM args: [-server])
          Observed state   Occurrences   Expectation  Interpretation
                    0, 0             0     FORBIDDEN  Violates sequential consistency
                    0, 1    52,228,833    ACCEPTABLE  Trivial under sequential consistency
                    1, 0    60,725,076    ACCEPTABLE  Trivial under sequential consistency
                    1, 1       313,541    ACCEPTABLE  Trivial under sequential consistency
     */

    @JCStressTest
    @Outcome(id = {"0, 1", "1, 0", "1, 1"}, expect = ACCEPTABLE, desc = "Trivial under sequential consistency")
    @Outcome(id = "0, 0",                   expect = FORBIDDEN,  desc = "Violates sequential consistency")
    @State
    public static class VolatileDekker {
        volatile int x;
        volatile int y;

        @Actor
        public void actor1(II_Result r) {
            x = 1;
            r.r1 = y;
        }

        @Actor
        public void actor2(II_Result r) {
            y = 1;
            r.r2 = x;
        }
    }

}
