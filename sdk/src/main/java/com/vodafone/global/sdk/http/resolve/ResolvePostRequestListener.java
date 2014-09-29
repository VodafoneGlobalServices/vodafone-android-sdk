package com.vodafone.global.sdk.http.resolve;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.vodafone.global.sdk.RequestBuilderProvider;
import com.vodafone.global.sdk.ResolveCallback;
import com.vodafone.global.sdk.ResolutionStatus;
import timber.log.Timber;

import java.util.Set;

public class ResolvePostRequestListener implements RequestListener<UserDetailsDTO> {
    private final SpiceManager spiceManager;
    private final Set<ResolveCallback> resolveCallbacks;

    public ResolvePostRequestListener(SpiceManager spiceManager, Set<ResolveCallback> resolveCallbacks) {
        this.spiceManager = spiceManager;
        this.resolveCallbacks = resolveCallbacks;
    }

    @Override
    public void onRequestFailure(SpiceException e) {
        Timber.e(e, e.getMessage());

        for (ResolveCallback callback : resolveCallbacks) {
            //callback.onError(new VodafoneException(e.getMessage(), e));
        }
    }

    @Override
    public void onRequestSuccess(UserDetailsDTO userDetailsDTO) {
        boolean stillRunning = userDetailsDTO.status == ResolutionStatus.STILL_RUNNING;
        if (stillRunning)
            loop(userDetailsDTO);

        for (ResolveCallback callback : resolveCallbacks)
            callback.onCompleted(userDetailsDTO.userDetails);
    }

    private void loop(UserDetailsDTO userDetailsDTO) {
        // TODO timeout
        RequestBuilderProvider requestBuilderProvider = new RequestBuilderProvider("", "", "", "");
        ResolveGetRequest request = ResolveGetRequest.builder()
                .url("")
                .accessToken("")
                .userDetaildDTO(userDetailsDTO)
                .requestBuilderProvider(requestBuilderProvider)
                .build();
        ResolveGetRequestListener requestListener = new ResolveGetRequestListener(spiceManager, resolveCallbacks, requestBuilderProvider);
        spiceManager.execute(request, requestListener);
    }
}
