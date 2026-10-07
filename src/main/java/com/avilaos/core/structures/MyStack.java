package com.avilaos.core.structures;


/**
 * Pila genérica propia (LIFO - Last In, First Out).
 * 
 * 
 * @param <T> 
 */
public class MyStack<T> {
    Node<T> top;
    int size;
    
    public MyStack(){
        this.top = null;
        this.size = 0;
    }
    
    public boolean isEmpty(){
        return this.top == null && this.size == 0;
    }
    
    public void putEmpty(){
        this.top = null;
        this.size = 0;
    }
    
    public void push(T data){
    Node<T> pNew = new Node<>(data);
    pNew.pNext = this.top;
    this.top = pNew;
    this.size++;
    }
    
    public T pop(){
    if(isEmpty()){
        throw new EstructuraVaciaException("No se puede desempilar de una pila vacia");
    }
    T data = this.top.data;
    this.top = this.top.pNext;
    this.size--;
    return data;
    }
    
    public int size(){
        return this.size;
    }
    
}
