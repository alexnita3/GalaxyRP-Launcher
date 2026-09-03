package com.galaxyrp.galaxyrplauncher.services;

import com.galaxyrp.galaxyrplauncher.GalaxyRPLauncherController;
import com.galaxyrp.galaxyrplauncher.enums.UserAction;
import javafx.application.Platform;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class AsyncActionService {
    @FunctionalInterface
    public interface AsyncTask {
        void run() throws Exception;
    }

    private final GalaxyRPLauncherController controller;
    private final AtomicBoolean actionRunning = new AtomicBoolean();

    public AsyncActionService(GalaxyRPLauncherController controller) {
        this.controller = controller;
    }

    public boolean run(
            UserAction action,
            AsyncTask task,
            Runnable onComplete,
            Consumer<Exception> onError) {
        if (!actionRunning.compareAndSet(false, true)) {
            return false;
        }

        InterfaceUpdateService.updateUserInterface(controller, action);

        Thread worker = new Thread(() -> {
            try {
                task.run();
                actionRunning.set(false);
                Platform.runLater(onComplete);
            } catch (Exception exception) {
                actionRunning.set(false);
                Platform.runLater(() -> onError.accept(exception));
            } finally {
                actionRunning.set(false);
            }
        }, action.name().toLowerCase());

        worker.setDaemon(true);
        worker.start();
        return true;
    }

    public boolean isActionRunning() {
        return actionRunning.get();
    }
}
