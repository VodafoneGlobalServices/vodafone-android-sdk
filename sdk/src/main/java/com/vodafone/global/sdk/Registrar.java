package com.vodafone.global.sdk;

/**
 * An interface responsible for registering and unregistering callbacks.
 */
interface Registrar {
    void register(VodafoneCallback callback);

    void unregister(VodafoneCallback callback);
}
