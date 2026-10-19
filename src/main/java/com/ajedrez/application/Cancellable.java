package com.ajedrez.application;

/** Manejador para cancelar una tarea en segundo plano (por ejemplo, la IA pensando). */
@FunctionalInterface
public interface Cancellable {

    public void cancel();
}
