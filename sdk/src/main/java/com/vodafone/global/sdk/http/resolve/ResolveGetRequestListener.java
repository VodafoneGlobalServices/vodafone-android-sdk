package com.vodafone.global.sdk.http.resolve;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.vodafone.global.sdk.RequestBuilderProvider;
import com.vodafone.global.sdk.ResolveCallback;
import com.vodafone.global.sdk.ResolutionStatus;
import timber.log.Timber;

import java.util.Set;

public class ResolveGetRequestListener implements RequestListener<UserDetailsDTO> {
    private final SpiceManager spiceManager;
    private final Set<ResolveCallback> resolveCallbacks;
    private RequestBuilderProvider requestBuilderProvider;

    public ResolveGetRequestListener(
            SpiceManager spiceManager,
            Set<ResolveCallback> resolveCallbacks,
            RequestBuilderProvider requestBuilderProvider
    ) {
        this.spiceManager = spiceManager;
        this.resolveCallbacks = resolveCallbacks;
        this.requestBuilderProvider = requestBuilderProvider;
    }

    @Override
    public void onRequestSuccess(UserDetailsDTO userDetailsDTO) {
        boolean stillRunning = userDetailsDTO.status == ResolutionStatus.STILL_RUNNING;
        if (stillRunning)
            loop(userDetailsDTO);

        for (ResolveCallback callback : resolveCallbacks)
            callback.onCompleted(userDetailsDTO.userDetails);
    }

    @Override
    public void onRequestFailure(SpiceException e) {
        Timber.e(e, e.getMessage());

        for (ResolveCallback callback : resolveCallbacks) {
        //callback.onError(new VodafoneException(e.getMessage(), e));
        }
    }

    private void loop(UserDetailsDTO userDetailsDTO) {
        // TODO timeout
        ResolveGetRequest request = ResolveGetRequest.builder()
                .userDetaildDTO(userDetailsDTO)
                .requestBuilderProvider(requestBuilderProvider)
                .build();
        ResolveGetRequestListener requestListener = new ResolveGetRequestListener(spiceManager, resolveCallbacks, requestBuilderProvider);
        spiceManager.execute(request, requestListener);
    }
}
