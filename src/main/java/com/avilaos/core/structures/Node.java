package com.avilaos.core.structures;

/**
 * @param <T> Tipo de dato almacenado en el nodo.
 */

public class Node<T> {
    T data;
    Node pNext;
    
    public Node(T data){
    this.data = data;
    this.pNext = null;
    }
}
