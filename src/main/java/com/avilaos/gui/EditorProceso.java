package com.avilaos.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import static com.avilaos.gui.PanelPrincipal.*;

/** Borrador visual de programa. No representa PCB, imagen cargada ni proceso admitido. */
public class EditorProceso extends JPanel {
    // Arreglos propios del borrador: una fila es un bloque, no una instrucción ejecutada.
    private final String[] operaciones = new String[100];
    private final int[] duraciones = new int[100];
    private int cantidad;
    private final JComboBox<String> operacion = new JComboBox<>(new String[]{"CPU", "E/S"});
    private final JSpinner ticks = new JSpinner(new SpinnerNumberModel(5, 1, 10000, 1));
    private final JLabel resumen = texto("0 bloques  ·  CPU: 0 ticks  ·  E/S: 0 ticks", 13, TINTA, true);
    private final JLabel mensaje = texto("Agrega el primer bloque para comenzar.", 12, GRIS, false);
    private final ModeloBloques modelo = new ModeloBloques();
    private final JTable tabla = new JTable(modelo);

    /** Construye formulario y conecta listeners; no ejecuta operaciones al construirlo. */
    public EditorProceso() {
        super(new BorderLayout());
        JPanel pagina = pagina("PROCESOS", "Compón tu programa", "Define una secuencia de CPU y E/S, y el computador que la ejecutará.");
        JPanel identidad = tarjeta();
        JPanel campos = new JPanel(new GridLayout(1, 2, 24, 0));
        campos.setOpaque(false);
        campos.add(campo("Nombre del proceso", new JTextField()));
        campos.add(campo("Computador destino · datos de ejemplo", new JComboBox<>(
                new String[]{"Computador 1", "Computador 2"})));
        identidad.add(campos);
        pagina.add(identidad);
        pagina.add(Box.createVerticalStrut(18));

        JPanel editor = tarjeta();
        editor.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        editor.add(texto("Secuencia del programa", 19, TINTA, true));
        editor.add(Box.createVerticalStrut(8));
        editor.add(texto("CPU: trabajo de cálculo. E/S: duración de una operación de espera.", 12, GRIS, false));
        editor.add(Box.createVerticalStrut(18));
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        controles.setOpaque(false); controles.setAlignmentX(LEFT_ALIGNMENT);
        operacion.setPreferredSize(new Dimension(90, 36));
        ticks.setPreferredSize(new Dimension(90, 36));
        controles.add(operacion); controles.add(Box.createHorizontalStrut(12));
        controles.add(ticks); controles.add(texto("  ticks   ", 13, GRIS, false));
        JButton agregar = accion("+ Agregar bloque", true);
        agregar.addActionListener(e -> agregarBloque());
        controles.add(agregar);
        controles.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        editor.add(controles); editor.add(Box.createVerticalStrut(16));

        tabla.setRowHeight(36);
        tabla.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        tabla.setForeground(TINTA); tabla.setBackground(PAPEL);
        tabla.setSelectionBackground(new Color(225, 233, 209));
        tabla.setSelectionForeground(TINTA);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(new Color(235, 230, 215));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 32));
        tabla.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setColumnHeaderView(tabla.getTableHeader());
        scroll.setAlignmentX(LEFT_ALIGNMENT);
        scroll.setPreferredSize(new Dimension(500, 160));
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        editor.add(scroll); editor.add(Box.createVerticalStrut(12));
        JPanel acciones = new JPanel(new BorderLayout()); acciones.setOpaque(false);
        acciones.setAlignmentX(LEFT_ALIGNMENT);
        JButton quitar = accion("Quitar seleccionado", false);
        quitar.addActionListener(e -> quitarBloque());
        acciones.add(quitar, BorderLayout.WEST); acciones.add(resumen, BorderLayout.EAST);
        acciones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        mensaje.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        editor.add(acciones); editor.add(Box.createVerticalStrut(10)); editor.add(mensaje);
        pagina.add(editor); pagina.add(Box.createVerticalStrut(14));
        pagina.add(texto("Borrador local · Hasta 100 bloques de 1–10 000 ticks. No se envía al kernel.", 12, GRIS, false));
        pagina.add(Box.createVerticalStrut(6));
        pagina.add(texto("La memoria calculada y los protocolos de sincronización se conectarán después.", 12, GRIS, false));
        add(pagina);
    }

    /**
     * Entradas: operación seleccionada y ticks del spinner. Salida void.
     * Efecto: agrega una fila a los arreglos y actualiza tabla/resumen; no modifica RAM.
     * Condiciones: máximo 100 bloques; ticks enteros entre 1 y 10 000.
     * Error: muestra mensaje, sin modificar el borrador. Sin ciclos propios.
     * Conexiones: botón → esta función → modelo Swing + actualizarResumen.
     * Normal: CPU,5 agrega UN bloque CPU de cinco ticks; aún no expande instrucciones.
     */
    private void agregarBloque() {
        if (cantidad == operaciones.length) {
            mensaje.setText("Límite del prototipo: 100 bloques. Quita uno antes de agregar otro."); return;
        }
        try { ticks.commitEdit(); }
        catch (java.text.ParseException e) { mensaje.setText("Escribe un número entero entre 1 y 10 000."); return; }
        int valor = ((Number) ticks.getValue()).intValue();
        if (valor < 1 || valor > 10000) { mensaje.setText("Los ticks deben estar entre 1 y 10 000."); return; }
        operaciones[cantidad] = (String) operacion.getSelectedItem();
        duraciones[cantidad] = valor;
        cantidad++;
        modelo.fireTableRowsInserted(cantidad - 1, cantidad - 1);
        tabla.setRowSelectionInterval(cantidad - 1, cantidad - 1);
        actualizarResumen();
        mensaje.setText("Bloque agregado. La secuencia se lee de arriba hacia abajo.");
    }

    /**
     * Entrada: fila seleccionada. Salida void. Sin selección: mensaje sin mutación.
     * Desplaza las filas posteriores una posición; ciclo termina en cantidad-1.
     * Limpia la última celda y actualiza tabla/resumen. No toca procesos reales.
     */
    private void quitarBloque() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { mensaje.setText("Selecciona el bloque que quieres quitar."); return; }
        for (int i = fila; i < cantidad - 1; i++) {
            operaciones[i] = operaciones[i + 1]; duraciones[i] = duraciones[i + 1];
        }
        cantidad--;
        operaciones[cantidad] = null; duraciones[cantidad] = 0;
        modelo.fireTableDataChanged();
        actualizarResumen();
        mensaje.setText("Bloque eliminado. El resto conserva su orden.");
    }

    /** Lee el borrador; suma CPU/E/S por separado hasta cantidad. Sólo cambia el rótulo.
     * No calcula tiempo final (esperas y costos SO no están modelados) ni memoria. */
    private void actualizarResumen() {
        int cpu = 0, es = 0;
        for (int i = 0; i < cantidad; i++) {
            if ("CPU".equals(operaciones[i])) cpu += duraciones[i]; else es += duraciones[i];
        }
        resumen.setText(cantidad + " bloques  ·  CPU: " + cpu + " ticks  ·  E/S: " + es + " ticks");
    }

    /** Adaptador de lectura entre arreglos propios y JTable; no es el modelo del proceso.
     * JTable consulta filas/columnas válidas. Celdas no editables; no muta el borrador. */
    private class ModeloBloques extends AbstractTableModel {
        @Override public int getRowCount() { return cantidad; }
        @Override public int getColumnCount() { return 3; }
        @Override public String getColumnName(int columna) {
            return switch (columna) { case 0 -> "ORDEN"; case 1 -> "OPERACIÓN"; default -> "DURACIÓN"; };
        }
        @Override public Object getValueAt(int fila, int columna) {
            return switch (columna) { case 0 -> fila + 1; case 1 -> operaciones[fila]; default -> duraciones[fila] + " ticks"; };
        }
    }
}
