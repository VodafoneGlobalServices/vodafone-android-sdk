package com.vodafone.global.sdk.http.resolve;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.vodafone.global.sdk.RequestBuilderProvider;
import com.vodafone.global.sdk.ResolutionCallback;
import com.vodafone.global.sdk.ResolutionStatus;
import timber.log.Timber;

import java.util.Set;

public class ResolveGetRequestListener implements RequestListener<UserDetailsDTO> {
    private final SpiceManager spiceManager;
    private final Set<ResolutionCallback> resolutionCallbacks;
    private RequestBuilderProvider requestBuilderProvider;

    public ResolveGetRequestListener(
            SpiceManager spiceManager,
            Set<ResolutionCallback> resolutionCallbacks,
            RequestBuilderProvider requestBuilderProvider
    ) {
        this.spiceManager = spiceManager;
        this.resolutionCallbacks = resolutionCallbacks;
        this.requestBuilderProvider = requestBuilderProvider;
    }

    @Override
    public void onRequestSuccess(UserDetailsDTO userDetailsDTO) {
        boolean stillRunning = userDetailsDTO.userDetails.status == ResolutionStatus.STILL_RUNNING;
        if (stillRunning)
            loop(userDetailsDTO);

        for (ResolutionCallback callback : resolutionCallbacks)
            callback.onCompleted(userDetailsDTO.userDetails);
    }

    @Override
    public void onRequestFailure(SpiceException e) {
        Timber.e(e, e.getMessage());

        for (ResolutionCallback callback : resolutionCallbacks) {
        //callback.onError(new VodafoneException(e.getMessage(), e));
        }
    }

    private void loop(UserDetailsDTO userDetailsDTO) {
        // TODO timeout
        ResolveGetRequest request = ResolveGetRequest.builder()
                .userDetaildDTO(userDetailsDTO)
                .requestBuilderProvider(requestBuilderProvider)
                .build();
        ResolveGetRequestListener requestListener = new ResolveGetRequestListener(spiceManager, resolutionCallbacks, requestBuilderProvider);
        spiceManager.execute(request, requestListener);
    }
}
