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

public class JMMSample_03_Coherence {

    /*
      ----------------------------------------------------------------------------------------------------------

        另一个微妙而直观的特性来自于对程序如何工作的天真理解。
        在Java内存模型下，如果没有同步， 独立读的顺序没有定义。即便是读取*相同*的变量!

        Yet another subtle and intuitive property comes from the naive understanding
        of how programs work. Under Java Memory Model, in absence of synchronization,
        the order of independent reads is undefined. That includes reads of the *same*
        variable!

              [OK] org.openjdk.jcstress.samples.JMMSample_03_Coherence.SameRead
            (JVM args: [-server])
          Observed state   Occurrences              Expectation  Interpretation
                    0, 0     4,593,916               ACCEPTABLE  Doing both reads early.
                    0, 1         2,507               ACCEPTABLE  Doing first read early, not surprising.
                    1, 0        48,132   ACCEPTABLE_INTERESTING  First read seen racy value early, and the second one did ...
                    1, 1    88,146,175               ACCEPTABLE  Doing both reads late.
    */

    @JCStressTest
    @Outcome(id = "0, 0", expect = ACCEPTABLE, desc = "Doing both reads early.")
    @Outcome(id = "1, 1", expect = ACCEPTABLE, desc = "Doing both reads late.")
    @Outcome(id = "0, 1", expect = ACCEPTABLE, desc = "Doing first read early, not surprising.")
    @Outcome(id = "1, 0", expect = ACCEPTABLE_INTERESTING, desc = "First read seen racy value early, and the second one did not.")
    @State
    public static class SameRead {

        private final Holder h1 = new Holder();
        private final Holder h2 = h1;

        private static class Holder {
            int a;
            int trap;
        }

        @Actor
        public void actor1() {
            h1.a = 1;
        }

        @Actor
        public void actor2(II_Result r) {
            Holder h1 = this.h1;
            Holder h2 = this.h2;

            // Spam null-pointer check folding: try to step on NPEs early.
            // Doing this early frees compiler from moving h1.a and h2.a loads
            // around, because it would not have to maintain exception order anymore.
            h1.trap = 0;
            h2.trap = 0;

            // Spam alias analysis: the code effectively reads the same field twice,
            // but compiler does not know (h1 == h2) (i.e. does not check it, as
            // this is not a profitable opt for real code), so it issues two independent
            // loads.
            r.r1 = h1.a;
            r.r2 = h2.a;
        }
    }

    /*
      ----------------------------------------------------------------------------------------------------------

        The stronger property -- coherence -- mandates that the writes to the same
        variable to be observed in a total order (that implies that _observers_ are
        also ordered). Java "volatile" assumes this property.

              [OK] org.openjdk.jcstress.samples.JMMSample_03_Coherence.SameVolatileRead
            (JVM args: [-server])
          Observed state   Occurrences   Expectation  Interpretation
                    0, 0    66,401,704    ACCEPTABLE  Doing both reads early.
                    0, 1       102,587    ACCEPTABLE  Doing first read early, not surprising.
                    1, 0             0     FORBIDDEN  Violates coherence.
                    1, 1    15,507,759    ACCEPTABLE  Doing both reads late.
     */

    @JCStressTest
    @Outcome(id = "0, 0", expect = ACCEPTABLE, desc = "Doing both reads early.")
    @Outcome(id = "1, 1", expect = ACCEPTABLE, desc = "Doing both reads late.")
    @Outcome(id = "0, 1", expect = ACCEPTABLE, desc = "Doing first read early, not surprising.")
    @Outcome(id = "1, 0", expect = FORBIDDEN, desc = "Violates coherence.")
    @State
    public static class SameVolatileRead {

        private final Holder h1 = new Holder();
        private final Holder h2 = h1;

        private static class Holder {
            volatile int a;
            int trap;
        }

        @Actor
        public void actor1() {
            h1.a = 1;
        }

        @Actor
        public void actor2(II_Result r) {
            Holder h1 = this.h1;
            Holder h2 = this.h2;

            h1.trap = 0;
            h2.trap = 0;

            r.r1 = h1.a;
            r.r2 = h2.a;
        }
    }

}
