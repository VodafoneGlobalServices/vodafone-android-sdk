package com.vodafone.global.sdk;

/**
 * Interface for receiving events about retrieving UserDetails.
 */
public interface ResolutionCallback extends VodafoneCallback {
    /**
     * Called when UserDetails get updated.
     * @param userDetails current version of UserDetails
     */
    void onCompleted(UserDetails userDetails);

    void onValidationRequired();

    void onFailed();

    /**
     * Called when error occurred during retrieving UserDetails.
     * @param ex exception detailing type of error
     */
    void onError(VodafoneException ex);
}
