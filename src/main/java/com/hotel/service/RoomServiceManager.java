package com.hotel.service;

import com.hotel.model.ServiceRequestRecord;
import com.hotel.model.ServiceRequestStatus;
import com.hotel.model.ServiceType;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class RoomServiceManager {
    private final Map<ServiceType, ExecutorService> serviceExecutors = new EnumMap<>(ServiceType.class);

    public RoomServiceManager() {
        for (ServiceType serviceType : ServiceType.values()) {
            serviceExecutors.put(serviceType, Executors.newFixedThreadPool(3, daemonThreadFactory(serviceType)));
        }
    }

    public void submitRequest(ServiceRequestRecord request,
                              Consumer<ServiceRequestRecord> stateConsumer,
                              Runnable onComplete) {
        serviceExecutors.get(request.getServiceType()).submit(() -> runLifecycle(request, stateConsumer, onComplete));
    }

    private void runLifecycle(ServiceRequestRecord request,
                              Consumer<ServiceRequestRecord> stateConsumer,
                              Runnable onComplete) {
        try {
            request.setStatus(ServiceRequestStatus.IN_PROGRESS);
            emitState(request, stateConsumer);

            for (int index = 1; index < request.getServiceType().getStages().size(); index++) {
                Thread.sleep(request.getServiceType().getStageDurationMillis());
                request.setCurrentStage(request.getServiceType().getStages().get(index));
                if (index == request.getServiceType().getStages().size() - 1) {
                    request.setStatus(ServiceRequestStatus.COMPLETED);
                }
                emitState(request, stateConsumer);
            }

            if (onComplete != null) {
                onComplete.run();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            request.setCurrentStage("Interrupted");
            emitState(request, stateConsumer);
        }
    }

    private void emitState(ServiceRequestRecord request, Consumer<ServiceRequestRecord> stateConsumer) {
        if (stateConsumer != null) {
            stateConsumer.accept(request);
        }
    }

    public void shutdown() {
        for (ExecutorService executorService : serviceExecutors.values()) {
            executorService.shutdownNow();
        }
        for (ExecutorService executorService : serviceExecutors.values()) {
            try {
                executorService.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private ThreadFactory daemonThreadFactory(ServiceType serviceType) {
        AtomicInteger threadCount = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("hotel-" + serviceType.name().toLowerCase() + "-" + threadCount.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
    }
}
