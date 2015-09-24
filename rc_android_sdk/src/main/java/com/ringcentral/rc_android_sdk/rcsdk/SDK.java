package com.ringcentral.rc_android_sdk.rcsdk;

import com.ringcentral.rc_android_sdk.rcsdk.platform.Helpers;
import com.ringcentral.rc_android_sdk.rcsdk.platform.Platform;

import java.io.Serializable;

/**
 * Created by andrew.pang on 6/26/15.
 */
public class SDK implements Serializable{

    Platform platform;
    Helpers helpers;

    public SDK(String appKey, String appSecret, Platform.Server server){
        helpers = new Helpers(appKey, appSecret, server);
        platform = new Platform(appKey, appSecret, server);
    }

    public Helpers getHelpers() {
        return this.helpers;
    }

    public Platform getPlatform() {
        return platform;
    }

}
