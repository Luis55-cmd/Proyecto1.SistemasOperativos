package com.avilaos.core.structures;

import java.util.NoSuchElementException;

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
        return (this.pFirst == null && this.pLast==null) && this.size == 0;
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
            this.pFirst = pNew; 
        }
        this.size++;
    }

    public void deleteFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("No se puede borrar de una lista vacia");
        }
        this.pFirst = this.pFirst.pNext;
        this.size--;
        if (this.pFirst == null) {
            this.pLast = null;
        }
    }

    public void deleteLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("No se puede borrar de una lista vacia");
        }
        if (this.size == 1) {
            deleteFirst();
            return;
        }
        Node<T> aux = this.pFirst;
        while (aux.pNext != this.pLast) {
            aux = aux.pNext;
        }
        this.pLast = aux;
        this.pLast.pNext = null;
        this.size--;
        
    }

    public void insertPos(int pos, T data) {
        if (pos < 0 || pos > this.size) {
            throw new IndexOutOfBoundsException("Posicion de insercion invalida: " + pos);
        }
        if (pos == 0) {
            insertFirst(data);
            return;
        }
        if (pos == this.size) {
            append(data);
            return;
        }
        Node<T> aux = this.pFirst;
        for (int i = 0; i < pos - 1; i++) {
            aux = aux.pNext;
        }
        Node<T> pNew = new Node<>(data);
        pNew.pNext = aux.pNext;
        aux.pNext = pNew;
        this.size++;
    }

    public void deletePos(int pos) {
        if (pos < 0 || pos >= this.size) {
            throw new IndexOutOfBoundsException("Posicion de borrado invalida: " + pos);
        }
        if (pos == 0) {
            deleteFirst();
            return;
        }
        if (pos == this.size - 1) {
           deleteLast();
           return;
        }
        Node<T> aux = this.pFirst;
        for (int i = 0; i < pos - 1; i++) {
            aux = aux.pNext;
        }
        aux.pNext = aux.pNext.pNext;
        this.size--;
    }

    public int size() {
        return this.size;
    }
}
