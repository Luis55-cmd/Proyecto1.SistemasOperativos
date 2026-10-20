package com.avilaos;
import com.avilaos.core.structures.*;
import com.avilaos.hardware.Computer;
import com.avilaos.ui.Vista1;


/**
 * Punto de entrada principal de ÁvilaOS.
 * Inicializa la aplicación y la interfaz gráfica.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("ÁvilaOS Simulator - Inicializando sistema...");
        // TODO: Inicializar vistas y componentes del simulador
        MyLinkedList<Computer> listComputer = new MyLinkedList<>();
        
        Vista1 vista = new Vista1();
        vista.setVisible(true);
        
        
        
    }
}
