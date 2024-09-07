package com.apex.worker.domain.port.secondary;

import com.apex.worker.domain.entity.Product;

import java.util.ArrayList;

public interface ProductService {
    /**
     * Verifica si un producto está disponible en stock.
     *
     * @param productId El ID del producto a verificar.
     * @return true si el producto está disponible, false en caso contrario.
     */
    boolean areAllProductsAvailable(ArrayList<String> productIds);

    /**
     * Obtiene una lista de productos por sus IDs.
     *
     * @param productIds Lista de IDs de productos a buscar.
     * @return Lista de objetos Product encontrados.
     */
    ArrayList<Product> getProducts(ArrayList<String> productIds);
}
