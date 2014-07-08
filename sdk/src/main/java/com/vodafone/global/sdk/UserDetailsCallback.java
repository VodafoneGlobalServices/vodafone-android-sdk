package com.vodafone.global.sdk;

/**
 * Interface for receiving events about retrieving UserDetails.
 */
public interface UserDetailsCallback extends VodafoneCallback {
    /**
     * Called when UserDetails get updated.
     * @param userDetails current version of UserDetails
     */
    void onUserDetailsUpdate(UserDetails userDetails);

    /**
     * Called when error occurred during retrieving UserDetails.
     * @param ex exception detailing type of error
     */
    void onUserDetailsError(VodafoneException ex);
}
