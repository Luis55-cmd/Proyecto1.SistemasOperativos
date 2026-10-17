package com.avilaos.core.structures;


/**
 * Cola genérica propia (FIFO - First In, First Out).
 * 
 * Se utilizará para gestionar las distintas colas del sistema operativo:
 * nuevos, listos, bloqueados, terminados y colas de semáforos.
 * 
 * @param <T> Tipo de elemento almacenado en la cola.
 */
public class MyQueue<T> {
    Node<T> pFirst;
    Node<T> pLast;
    int size;

    public MyQueue() {
        this.pFirst = null;
        this.pLast = null;
        this.size = 0;
    }

    public boolean isEmpty() {
        return this.pFirst == null && this.size == 0;
    }

    public void Empty() {
        this.pFirst = null;
        this.pLast = null;
        this.size = 0;
    }

    public int size() {
        return this.size;
    }

    /**
     * Encola un elemento al final de la estructura (contrato oficial).
     *
     * @param item Elemento a encolar.
     */
    public void enqueue(T item) {
        queue(item);
    }

    public void queue(T data) {
        Node<T> pNew = new Node<>(data);
        if (isEmpty()) {
            this.pFirst = pNew;
            this.pLast = pNew;
        } else {
            this.pLast.pNext = pNew;
            this.pLast = pNew;
        }
        this.size++;
    }

    public T dequeue() {
        if (isEmpty()) {
            throw new EstructuraVaciaException("No se puede desencolar de una cola vacia");
        }
        T data = this.pFirst.data;
        this.pFirst = this.pFirst.pNext;
        this.size--;
        if (this.pFirst == null) {
            this.pLast = null;
        }
        return data;
    }

    public T peek() {
        if (isEmpty()) {
            throw new EstructuraVaciaException("No se puede consultar una cola vacia");
        }
        return this.pFirst.data;
    }
    
    public boolean removeElement(T data) {
        if (isEmpty()) {
            return false;
        }
        if (this.pFirst.data.equals(data)) {
            dequeue();
            return true;
        }
        Node<T> current = this.pFirst;
        while (current.pNext != null) {
            if (current.pNext.data.equals(data)) {
                current.pNext = current.pNext.pNext;
                if (current.pNext == null) {
                    this.pLast = current;
                }
                this.size--;
                return true;
            }
            current = current.pNext;
        }
    return false; 
}
}
