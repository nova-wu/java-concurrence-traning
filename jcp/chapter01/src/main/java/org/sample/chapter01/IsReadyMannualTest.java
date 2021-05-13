/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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


public class IsReadyMannualTest {

    static boolean isReady = false;
    static int data = 0;
    static int result;

    public static void main(String[] args)
            throws InterruptedException {
        for (int idx=0; idx<10000; idx++) {
            Thread thread1 = new Thread(()-> {
                data = 666;
                isReady = true;
            });
            Thread thread2 = new Thread(()->{
                while (!isReady) {};
                result = data + 222;
            });

            thread2.start();
            thread1.start();

            thread2.join();
            if (result != 888) {
                System.out.println(result);
            }
        }
    }
}
