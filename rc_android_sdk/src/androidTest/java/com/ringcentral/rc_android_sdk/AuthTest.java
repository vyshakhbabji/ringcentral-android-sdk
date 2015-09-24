package com.ringcentral.rc_android_sdk;

import android.test.InstrumentationTestCase;

import com.ringcentral.rc_android_sdk.rcsdk.platform.Auth;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrew.pang on 7/20/15.
 */
public class AuthTest extends InstrumentationTestCase {

    public void testSetDataAndGetData() throws Exception{
        Map<String, String> data = new HashMap<>();
        data.put("access_token", "a");
        data.put("refresh_token", "b");
        data.put("token_type", "c");
        Auth a = new Auth();
        a.setData(data);
        assertEquals("a", a.getAccessToken());
        assertEquals("b", a.getRefreshToken());
        assertEquals("c", a.getTokenType());
        assertNotSame("d", a.getAccessToken());
    }

    public void testIsTokenDateValid() throws Exception{
        Auth a = new Auth();
        GregorianCalendar invalidDate = new GregorianCalendar(1994, 11, 9);
        assertFalse(a.isTokenDateValid(invalidDate));
        GregorianCalendar futureDate = new GregorianCalendar(2200, 1, 1);
        assertTrue(a.isTokenDateValid(futureDate));
        GregorianCalendar validDate = new GregorianCalendar();
        validDate.add(Calendar.HOUR_OF_DAY, 1);
        assertTrue(a.isTokenDateValid(validDate));
    }

    public void testIsAccessTokenValidAndIsRefreshTokenValid() throws Exception{
        Map<String, String> data = new HashMap<>();
        data.put("expires_in", "36000");
        data.put("refresh_token_expires_in", "36000");
        Auth a = new Auth();
        a.setData(data);
        assertTrue(a.isAccessTokenValid());
        assertTrue(a.isRefreshTokenValid());
        data.put("expires_in", "-1000");
        data.put("refresh_token_expires_in", "-1000");
        Auth b = new Auth();
        b.setData(data);
        assertFalse(b.isAccessTokenValid());
        assertFalse(b.isRefreshTokenValid());
    }
}
