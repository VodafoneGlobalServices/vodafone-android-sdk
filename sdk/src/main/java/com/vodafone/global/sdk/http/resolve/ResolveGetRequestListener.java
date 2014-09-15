package com.vodafone.global.sdk.http.resolve;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.vodafone.global.sdk.UserDetailsCallback;

import java.util.Set;

import timber.log.Timber;

public class ResolveGetRequestListener implements RequestListener<UserDetailsDTO> {
    private final SpiceManager spiceManager;
    private final Set<UserDetailsCallback> userDetailsCallbacks;

    public ResolveGetRequestListener(SpiceManager spiceManager, Set<UserDetailsCallback> userDetailsCallbacks) {
        this.spiceManager = spiceManager;
        this.userDetailsCallbacks = userDetailsCallbacks;
    }

    @Override
    public void onRequestSuccess(UserDetailsDTO userDetailsDTO) {
        boolean stillRunning = userDetailsDTO.userDetails.stillRunning;
        if (stillRunning)
            loop(userDetailsDTO);

        for (UserDetailsCallback callback : userDetailsCallbacks)
            callback.onUserDetailsUpdate(userDetailsDTO.userDetails);
    }

    @Override
    public void onRequestFailure(SpiceException e) {
        Timber.e(e, e.getMessage());

        for (UserDetailsCallback callback : userDetailsCallbacks) {
        //callback.onUserDetailsError(new VodafoneException(e.getMessage(), e));
        }
    }

    private void loop(UserDetailsDTO userDetailsDTO) {
        // TODO timeout
        ResolveGetRequest request = ResolveGetRequest.builder()
                .userDetaildDTO(userDetailsDTO)
                .build();
        ResolveGetRequestListener requestListener = new ResolveGetRequestListener(spiceManager, userDetailsCallbacks);
        spiceManager.execute(request, requestListener);
    }
}
