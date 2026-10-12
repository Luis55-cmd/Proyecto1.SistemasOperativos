/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.avilaos.core.model;
import com.avilaos.core.structures.*;
import java.util.concurrent.Semaphore;
/**
 *
 * @author rafr
 */
public class Buffer {
    Semaphore mutex;
    Semaphore vacios;
    Semaphore llenos;
    int idComp;
    int storage;
    int sizeInMemory;
    int id;
    MyQueue<Object> datos;
    private int produced;
    private int comsumed;

    public Buffer(Semaphore mutex, Semaphore vacios, Semaphore llenos, int idComp, int storage, int sizeInMemory) {
        this.mutex = new Semaphore(1, true);
        this.vacios = new Semaphore(storage, true);
        this.llenos = new Semaphore(0, true);
        this.idComp = idComp;
        this.storage = storage;
        this.sizeInMemory = sizeInMemory;
        this.id = id;
        this.datos = datos;
        this.produced = 0;
        this.comsumed = 0;
    }

    public Semaphore getMutex() {
        return mutex;
    }

    public void setMutex(Semaphore mutex) {
        this.mutex = mutex;
    }

    public Semaphore getVacios() {
        return vacios;
    }

    public void setVacios(Semaphore vacios) {
        this.vacios = vacios;
    }

    public Semaphore getLlenos() {
        return llenos;
    }

    public void setLlenos(Semaphore llenos) {
        this.llenos = llenos;
    }

    public int getIdComp() {
        return idComp;
    }

    public void setIdComp(int idComp) {
        this.idComp = idComp;
    }

    public int getStorage() {
        return storage;
    }

    public void setStorage(int storage) {
        this.storage = storage;
    }

    public int getSizeInMemory() {
        return sizeInMemory;
    }

    public void setSizeInMemory(int sizeInMemory) {
        this.sizeInMemory = sizeInMemory;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MyQueue<Object> getDatos() {
        return datos;
    }

    public void setDatos(MyQueue<Object> datos) {
        this.datos = datos;
    }

    public int getProduced() {
        return produced;
    }

    public void setProduced(int produced) {
        this.produced = produced;
    }

    public int getComsumed() {
        return comsumed;
    }

    public void setComsumed(int comsumed) {
        this.comsumed = comsumed;
    }
    
    
    
}
