package com.avilaos.core.structures;

/**
 * Lista enlazada genérica propia sin usar java.util.
 * 
 * Responsabilidades:
 * - Inserción al inicio y al final.
 * - Eliminación y búsqueda por valor o índice.
 * - Soporte para ordenamiento propio mediante comparadores.
 * - Iteración propia.
 * 
 * @param <T> Tipo de elemento almacenado.
 */
public class MyLinkedList<T> {


    Node<T> pFirst;
    Node<T> pLast;
    int size;

    public MyLinkedList() {
        this.pFirst = null;
        this.pLast = null;
        this.size = 0;
    }

    public boolean isEmpty() {
        return this.pFirst == null && this.size == 0;
    }

    public void append(T data) {
        Node<T> pNew = new Node<>(data);
        if (isEmpty()) {
            this.pFirst = this.pLast = pNew;
        } else {
            this.pLast.pNext = pNew;
            this.pLast = pNew;
        }
        this.size++;
    }

    public void insertFirst(T data) {
        Node<T> pNew = new Node<>(data);
        if (isEmpty()) {
            this.pFirst = this.pLast = pNew;
        } else {
            pNew.pNext = this.pFirst;
            this.pFirst = pNew; // Corregido: antes tenías this.pLast = pNew
        }
        this.size++;
    }

    public void deleteFirst() {
        if (isEmpty()) {
            throw new IllegalStateException("La lista está vacía");
        }
        this.pFirst = this.pFirst.pNext;
        this.size--;
        if (this.pFirst == null) {
            this.pLast = null;
        }
    }

    public void deleteLast() {
        if (isEmpty()) {
        }
        if (this.size == 1) {
            deleteFirst();
        }
        Node<T> current = this.pFirst;
        while (current.pNext != this.pLast) {
            current = current.pNext;
        }
        this.pLast = current;
        this.pLast.pNext = null;
        this.size--;
        
    }

    public void insertPos(int pos, T data) {
        if (pos < 0 || pos > this.size) {
        }
        if (pos == 0) {
            insertFirst(data);
        }
        if (pos == this.size) {
            append(data);
            return;
        }
        Node<T> current = this.pFirst;
        for (int i = 0; i < pos - 1; i++) {
            current = current.pNext;
        }
        Node<T> pNew = new Node<>(data);
        pNew.pNext = current.pNext;
        current.pNext = pNew;
        this.size++;
    }

    public void deletePos(int pos) {
        if (pos < 0 || pos >= this.size) {
        }
        if (pos == 0) {
            deleteFirst();
        }
        if (pos == this.size - 1) {
           deleteLast();
        }
        Node<T> current = this.pFirst;
        for (int i = 0; i < pos - 1; i++) {
            current = current.pNext;
        }
        current.pNext = current.pNext.pNext;
        this.size--;
    }

    public int size() {
        return this.size;
    }
}
