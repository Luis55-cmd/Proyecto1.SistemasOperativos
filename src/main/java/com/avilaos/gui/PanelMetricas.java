/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.avilaos.gui;

/**
 *
 * @author rafr
 */
import com.avilaos.simulation.SimulationController;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;

public class PanelMetricas extends JPanel {
    private final SimulationController controller;
    private final TimeSeriesCollection dataset;
    private TimeSeries[] seriesComputadores;
    private int[] idsComputadores;
   
    private final JLabel lblRendimientoGlobal = new JLabel("Throughput: 0 | Deadlines: 0% | Equidad: 0%");
    private final JLabel lblTiempos = new JLabel("Tiempo Respuesta Promedio: 0 ms");
    private final JPanel panelBuffers = new JPanel(new GridLayout(0, 1));

    public PanelMetricas(SimulationController controller) {
        this.controller = controller;
        this.dataset = new TimeSeriesCollection();
        this.seriesComputadores = new TimeSeries[0];
        this.idsComputadores = new int[0];
        
        setLayout(new BorderLayout());
        
        // 1. Configurar Gráfico de JFreeChart
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Utilización del Procesador",
                "Tiempo",
                "Utilización (%)",
                dataset,
                true, true, false
        );
        ChartPanel chartPanel = new ChartPanel(chart);
        add(chartPanel, BorderLayout.CENTER);
        
        // 2. Configurar Panel de KPIs Inferior
        JPanel pnlInferior = new JPanel(new BorderLayout());
        pnlInferior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel pnlGlobal = new JPanel(new GridLayout(2, 1, 5, 5));
        pnlGlobal.add(lblRendimientoGlobal);
        pnlGlobal.add(lblTiempos);
        
        pnlInferior.add(pnlGlobal, BorderLayout.NORTH);
        pnlInferior.add(new JScrollPane(panelBuffers), BorderLayout.CENTER);
        
        pnlInferior.setPreferredSize(new Dimension(800, 150));
        add(pnlInferior, BorderLayout.SOUTH);
    }

    public void actualizarMetricas() {
        int cicloActual = controller.getCurrentCycle();
        if (cicloActual == 0) return;

        var computadores = controller.getComputers();
        int cantidadComputadores = computadores.size();
        
        // Sincronizar arreglos si cambia la cantidad de computadores
        if (seriesComputadores.length != cantidadComputadores) {
            dataset.removeAllSeries();
            seriesComputadores = new TimeSeries[cantidadComputadores];
            idsComputadores = new int[cantidadComputadores];
            
            for (int i = 0; i < cantidadComputadores; i++) {
                var c = computadores.get(i);
                idsComputadores[i] = c.getId();
                seriesComputadores[i] = new TimeSeries(c.getName());
                dataset.addSeries(seriesComputadores[i]);
            }
        }

        int procesosCompletadosGlobal = 0;
        int deadlinesCumplidosGlobal = 0;
        int tiempoRespuestaTotalGlobal = 0;
        double maxUtil = -1.0;
        double minUtil = 101.0;
        
        Millisecond ahora = new Millisecond();
        
        // Actualizar datos de Computadores y Gráfico
        for (int i = 0; i < cantidadComputadores; i++) {
            var c = computadores.get(i);
            
            double utilizacion = ((double) c.getCiclosActivos() / cicloActual) * 100.0;
            seriesComputadores[i].addOrUpdate(ahora, utilizacion);
            
            if (utilizacion > maxUtil) maxUtil = utilizacion;
            if (utilizacion < minUtil) minUtil = utilizacion;
            
            procesosCompletadosGlobal += c.getProcesosCompletados();
            deadlinesCumplidosGlobal += c.getDeadlinesCumplidos();
            tiempoRespuestaTotalGlobal += c.getTotalTiempoRespuesta();
        }

        // Calcular KPIs del Sistema
        double throughput = (double) procesosCompletadosGlobal / cicloActual;
        double ratioDeadlines = procesosCompletadosGlobal > 0 
                ? ((double) deadlinesCumplidosGlobal / procesosCompletadosGlobal) * 100 : 0.0;
        double equidad = (cantidadComputadores > 1 && maxUtil >= 0) ? (maxUtil - minUtil) : 0.0;
        double tiempoRespuestaPromedio = procesosCompletadosGlobal > 0 
                ? (double) tiempoRespuestaTotalGlobal / procesosCompletadosGlobal : 0.0;

        lblRendimientoGlobal.setText(String.format("Throughput Global: %.4f proc/tick | Deadlines Cumplidos: %.1f%% | Equidad (Brecha Util): %.1f%%", 
                throughput, ratioDeadlines, equidad));
        
        lblTiempos.setText(String.format("Tiempo Respuesta Promedio: %.1f ticks", tiempoRespuestaPromedio));

        // Actualizar KPIs de Buffers usando iteración estándar sobre tu MyLinkedList
        panelBuffers.removeAll();
        var buffers = controller.getBuffers();
        for (int i = 0; i < buffers.size(); i++) {
            var b = buffers.get(i);
            String stats = String.format("Buffer %d -> Producidos: %d | Consumidos: %d", 
                    b.getId(), b.getProduced(), b.getComsumed());
            panelBuffers.add(new JLabel(stats));
        }
        
        panelBuffers.revalidate();
        panelBuffers.repaint();
    }
}