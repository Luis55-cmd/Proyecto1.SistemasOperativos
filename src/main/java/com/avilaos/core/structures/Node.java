package com.avilaos.core.structures;

/**
 * @param <T> Tipo de dato almacenado en el nodo.
 */

public class Node<T> {
    T data;
    Node<T> pNext;
    
    public Node(T data){
    this.data = data;
    this.pNext = null;
    }
}
