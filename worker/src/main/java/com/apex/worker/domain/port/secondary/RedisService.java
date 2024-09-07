package com.apex.worker.domain.port.secondary;

import java.time.Duration;

public interface RedisService {

    /**
     * Intenta adquirir un bloqueo para el procesamiento de una orden.
     *
     * @param orderId ID de la orden
     * @param timeout Tiempo máximo de espera para adquirir el bloqueo
     * @return true si el bloqueo se adquirió con éxito, false en caso contrario
     */
    boolean acquireLock(String orderId, Duration timeout);

    /**
     * Libera el bloqueo de una orden.
     *
     * @param orderId ID de la orden
     */
    void releaseLock(String orderId);

    /**
     * Incrementa y obtiene el contador de intentos para una orden.
     *
     * @param orderId ID de la orden
     * @return El número actual de intentos después de incrementar
     */
    int incrementRetryCount(String orderId);

    /**
     * Obtiene el contador de intentos actual para una orden.
     *
     * @param orderId ID de la orden
     * @return El número actual de intentos
     */
    int getRetryCount(String orderId);

    /**
     * Almacena información de error para una orden.
     *
     * @param orderId      ID de la orden
     * @param errorMessage Mensaje de error
     */
    void storeError(String orderId, String errorMessage);

    /**
     * Obtiene la información de error almacenada para una orden.
     *
     * @param orderId ID de la orden
     * @return El mensaje de error almacenado, o null si no hay error
     */
    String getError(String orderId);

    /**
     * Limpia todos los datos relacionados con una orden (bloqueo, contador de
     * intentos, error).
     *
     * @param orderId ID de la orden
     */
    void clearOrderData(String orderId);
}
