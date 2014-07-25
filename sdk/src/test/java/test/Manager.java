package test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Manager {

    private final Map<Request, Set<RequestListener>> map = new HashMap<Request, Set<RequestListener>>();

    public void execute(Request request, RequestListener requestListener) {
        addRequestListenerToListOfRequestListeners(request, requestListener);

    }

    private void addRequestListenerToListOfRequestListeners(Request request, RequestListener requestListener) {
        synchronized (map) {
            Set<RequestListener> listeners = map.get(request);
            if (listeners == null) {
                listeners = new HashSet<RequestListener>();
                map.put(request, listeners);
            }
            listeners.add(requestListener);
        }
    }
}
