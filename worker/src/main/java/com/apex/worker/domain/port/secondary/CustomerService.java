package com.apex.worker.domain.port.secondary;

import com.apex.worker.domain.entity.Customer;

public interface CustomerService {
    /**
     * Verifica si un cliente es válido basado en su ID.
     *
     * @param customerId El ID del cliente a verificar.
     * @return true si el cliente es válido, false en caso contrario.
     */
    boolean isValidCustomer(String customerId);

    /**
     * Obtiene los detalles de un cliente por su ID.
     *
     * @param customerId El ID del cliente a buscar.
     * @return El objeto Customer si se encuentra, null en caso contrario.
     */
    Customer getCustomerDetails(String customerId);
}
