package com.vodafone.he.sdk.android;

/**
 * Interface for receiving events about SMS validation process.
 */
public interface ValidateSmsCallback extends VodafoneCallback {
    /**
     * Called when SMS validation was successful.
     */
    void onSmsValidationSuccessful();

    /**
     * Called when SMS validation was unsuccessful.
     */
    void onSmsValidationFailure();

    /**
     * Called when error occurred during SMS validation.
     * @param ex exception detailing type of error
     */
    void onSmsValidationError(VodafoneException ex);
}
