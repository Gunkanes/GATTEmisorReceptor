package es.magtel.idi.gattemisorreceptor.patronOrden;

import java.util.LinkedList;

/**
 * Created by SAMUAN on 09/08/2016.
 *
 * Gestiona la cola de ordenes. El procesamiento es FIFO.
 */
public class GestorDeOrdenes {

    private LinkedList<IOrden> listaOrdenes;

    public GestorDeOrdenes() {
        listaOrdenes = new LinkedList<>();
    }

    /**
     * Añade una orden a la lista.
     *
     * @param orden orden a añadir en la lista.
     */
    public void add(IOrden orden) {
        listaOrdenes.add(orden);
    }

    /**
     * Ejecuta la primera orden de la lista y la quita de la cola.
     */
    public void ejecutarSiguiente() {
        if (listaOrdenes.size() > 0) {
            IOrden orden = listaOrdenes.getFirst();
            orden.ejecutar();
        }
    }

}
