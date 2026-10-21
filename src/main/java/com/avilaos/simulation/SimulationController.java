package com.avilaos.simulation;

import com.avilaos.core.kernel.ComputerKernel;
import com.avilaos.core.kernel.GlobalClock;
import com.avilaos.core.kernel.IClockTickable;
import com.avilaos.core.model.ExecutionMode;
import com.avilaos.core.model.ProcessControlBlock;
import com.avilaos.core.model.ProcessType;
import com.avilaos.core.model.SchedulingPolicyType;
import com.avilaos.core.structures.MyLinkedList;
import com.avilaos.core.structures.MyQueue;
import com.avilaos.hardware.CPU;
import com.avilaos.hardware.Computer;
import com.avilaos.hardware.DMA;
import com.avilaos.hardware.RAM;
import com.avilaos.sync.Buffer;
import com.avilaos.sync.ProducerConsumerDriver;

import javax.swing.SwingUtilities;

/**
 * Controlador y Fachada Central de la Simulación (Capa 3: Control y Simulación).
 * Coordina la simulación del clúster ÁvilaOS, orquesta el avance temporal
 * sincronizado con GlobalClock, y expone una API limpia y desacoplada para la GUI.
 */
public class SimulationController implements IClockTickable {

    private static volatile SimulationController instance;

    private final MyLinkedList<Computer> computers;
    private final MyLinkedList<Buffer> buffers;
    private final GlobalClock clock;
    private final ProducerConsumerDriver syncDriver;
    
    // Lista de observadores de la interfaz gráfica
    private final MyLinkedList<Runnable> guiListeners;

    private int nextComputerId = 1;
    private int nextBufferId = 1;

    private SimulationController() {
        this.computers = new MyLinkedList<>();
        this.buffers = new MyLinkedList<>();
        this.guiListeners = new MyLinkedList<>();
        this.clock = GlobalClock.getInstance();
        this.syncDriver = new ProducerConsumerDriver(2); // Latencia por defecto: 2 ciclos
        this.clock.addSubscriber(this);
    }

    public static SimulationController getInstance() {
        if (instance == null) {
            synchronized (SimulationController.class) {
                if (instance == null) {
                    instance = new SimulationController();
                }
            }
        }
        return instance;
    }

    // =========================================================================
    // CONEXIÓN CON LA INTERFAZ GRÁFICA (Patrón Observador)
    // =========================================================================
    
    /** Registra un panel de la GUI para que se actualice cuando cambie la simulación. */
    public void addGUIListener(Runnable listener) {
        if (listener != null) {
            this.guiListeners.append(listener);
        }
    }

    /** Dispara la actualización en todos los paneles registrados de forma segura en Swing. */
    private void notifyGUI() {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < guiListeners.size(); i++) {
                Runnable listener = guiListeners.get(i);
                if (listener != null) {
                    listener.run();
                }
            }
        });
    }

    // =========================================================================
    // MÉTODOS PARA BOTONES DE LA GUI (Gestión de Clúster, Procesos y Buffers)
    // =========================================================================

    public synchronized Computer createComputer(int ramSize, SchedulingPolicyType policy) {
        int id = nextComputerId++;
        RAM ram = new RAM(ramSize, 0, new int[ramSize]);
        CPU cpu = new CPU(0, 0, 0, null, ExecutionMode.USER);
        DMA dma = new DMA();

        ComputerKernel kernel = new ComputerKernel(
                new MyLinkedList<>(),
                new MyQueue<>(),
                new MyQueue<>(),
                new MyQueue<>(),
                null, null, null,
                new MyQueue<>(),
                this.clock
        );

        Computer comp = new Computer(id, cpu, ram, dma, kernel, policy != null ? policy : SchedulingPolicyType.FCFS);
        this.computers.append(comp);
        
        notifyGUI(); // <- Avisa a la GUI que hay un nuevo computador
        return comp;
    }

    public synchronized Computer createComputer(String name, int ramSize, SchedulingPolicyType policy) {
        if (name == null || name.trim().isEmpty() || ramSize <= 0 || policy == null) {
            throw new IllegalArgumentException("Indica nombre, RAM positiva y política.");
        }
        String normalizedName = name.trim();
        for (int i = 0; i < computers.size(); i++) {
            if (computers.get(i).getName().equalsIgnoreCase(normalizedName)) {
                throw new IllegalArgumentException("Asegúrate de usar un nombre único");
            }
        }
        Computer computer = createComputer(ramSize, policy);
        computer.setName(name);
        
        notifyGUI(); // <- Avisa a la GUI
        return computer;
    }

    public synchronized ProcessControlBlock createProcess(String name, int computerId, ProcessType type,
                                                          int instructions, int memory, int priority, int deadline) {
        return createProcess(name, computerId, type, instructions, memory, priority, deadline, null, 1, 0);
    }

    public synchronized ProcessControlBlock createProcess(String name, int computerId, ProcessType type,
                                                          int instructions, int memory, int priority, int deadline,
                                                          String targetBufferId, int cyclesPerOperation, int requiredElements) {
        Computer comp = getComputerById(computerId);
        if (comp == null) {
            return null;
        }

        ProcessControlBlock pcb = new ProcessControlBlock(
                name, computerId, type != null ? type : ProcessType.CPU_BOUND,
                instructions, memory, priority, deadline,
                targetBufferId, cyclesPerOperation, requiredElements
        );

        comp.getKernel().gestionCreateProcess(comp, pcb);
        
        notifyGUI(); // <- Avisa a la GUI de un nuevo proceso
        return pcb;
    }

    public synchronized Buffer createBuffer(int hostComputerId, int storage) {
        if (storage <= 0) {
            return null;
        }
        Computer host = getComputerById(hostComputerId);
        if (host == null || host.getKernel() == null) {
            return null;
        }

        int id = nextBufferId;
        Buffer buffer = new Buffer(null, null, null, hostComputerId, storage, id, new MyQueue<>(), storage);
        boolean success = host.getKernel().placeBufferRAM(buffer, host);
        if (!success) {
            return null;
        }

        nextBufferId++;
        this.buffers.append(buffer);
        
        notifyGUI(); // <- Avisa a la GUI
        return buffer;
    }

    // =========================================================================
    // CONTROL DEL RELOJ GLOBAL (Play, Pausa, Step, Velocidad)
    // =========================================================================

    public void startSimulation() {
        this.clock.start();
        notifyGUI();
    }

    public void pauseSimulation() {
        this.clock.pause();
        notifyGUI();
    }

    public void stepSimulation() {
        this.clock.step();
        notifyGUI();
    }

    public void setCycleDurationMs(int ms) {
        this.clock.setCycleDurationMs(ms);
        notifyGUI();
    }

    public int getCycleDurationMs() {
        return this.clock.getCycleDurationMs();
    }

    public int getCurrentCycle() {
        return this.clock.getCycleCounter();
    }

    public boolean isRunning() {
        return this.clock.isRunning();
    }

    public boolean isPaused() {
        return this.clock.isPaused();
    }

    public void setNetworkLatency(int latencyCycles) {
        this.syncDriver.setLatencia(latencyCycles);
    }

    public int getNetworkLatency() {
        return this.syncDriver.getLatencia();
    }

    // =========================================================================
    // AVANCE SINCRONIZADO POR CICLO (IClockTickable)
    // =========================================================================

    @Override
    public synchronized void tick(int currentCycle) {
        for (int i = 0; i < this.computers.size(); i++) {
            Computer comp = this.computers.get(i);
            if (comp != null && comp.getKernel() != null) {
                comp.getKernel().executeCycle(comp, currentCycle, this.syncDriver, this.buffers);
            }
        }
        
        notifyGUI(); // <- Actualiza visualmente los paneles en cada ciclo de reloj
    }

    // =========================================================================
    // GETTERS PARA CONSULTA DE ESTADO DESDE LA GUI (Observabilidad)
    // =========================================================================

    public MyLinkedList<Computer> getComputers() {
        return computers;
    }

    public MyLinkedList<Buffer> getBuffers() {
        return buffers;
    }

    public Computer getComputerById(int id) {
        for (int i = 0; i < this.computers.size(); i++) {
            Computer c = this.computers.get(i);
            if (c != null && c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    public Buffer getBufferById(int id) {
        for (int i = 0; i < this.buffers.size(); i++) {
            Buffer b = this.buffers.get(i);
            if (b != null && b.getId() == id) {
                return b;
            }
        }
        return null;
    }

    public synchronized void reset() {
        this.clock.reset();
        this.clock.addSubscriber(this);
        while (!this.computers.isEmpty()) {
            this.computers.deletePos(0);
        }
        while (!this.buffers.isEmpty()) {
            this.buffers.deletePos(0);
        }
        this.nextComputerId = 1;
        this.nextBufferId = 1;
        
        notifyGUI(); // <- Refresca la vista tras un reset
    }
}