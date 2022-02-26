package com.jladder.lang;

import junit.framework.TestCase;

import java.util.Date;

public class TimesTest extends TestCase {

    public void testMinuteDiff() {

        long times = Times.ams("2021-10-30 19:59:29");

        System.out.println((int)(times/1000));

        long diff = Times.minuteDiff(new Date(), Times.addMinute(5));
        System.out.println(diff);
    }
}