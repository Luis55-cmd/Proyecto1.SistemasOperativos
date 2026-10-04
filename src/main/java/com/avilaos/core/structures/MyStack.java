package com.avilaos.core.structures;

/**
 * Pila genérica propia (LIFO - Last In, First Out).
 * 
 * 
 * @param <T> 
 */
public class MyStack<T> {
    Node top;
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
    Node pNew = new Node(data);
    pNew.pNext = this.top;
    this.top = pNew;
    this.size++;
    }
    
    public void pop(){
    if(!isEmpty()){
    this.top = this.top.pNext;
    this.size--;
    }
    }
    
    public int size(){
        return this.size;
    }
    
}
