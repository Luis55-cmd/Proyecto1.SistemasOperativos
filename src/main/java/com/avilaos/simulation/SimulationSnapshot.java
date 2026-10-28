package com.avilaos.simulation;
import com.avilaos.core.model.*;
import com.avilaos.core.structures.MyLinkedList;
import com.avilaos.hardware.Computer;
import com.avilaos.sync.Buffer;

/** Copia de valores, sin referencias al modelo vivo. Se publica con volatile.
 * Las listas y arrays se encapsulan; sus elementos son registros inmutables.
 */
public final class SimulationSnapshot {
    public final long session;
    public final int cycle;
    public final boolean running, paused;
    public final int interval;
    public final Values<Node> computers;
    public final Values<BufferView> buffers;
    public record Usage(int computerId, double percent) { }
    public record Sample(int cycle, Values<Usage> usage) { }
    public final Values<Sample> history;
    public static final class Values<T> {
        private final Object[] items;
        private Values(Object[] items) { this.items = items; }
        public int size() { return items.length; }
        @SuppressWarnings("unchecked") public T get(int index) { return (T) items[index]; }
    }
    public record ProcessView(String description, int memoryAddressRegister, int priority, int remainingDeadline) {
        public String getDescription() { return description; }
        public int getMemoryAddressRegister() { return memoryAddressRegister; }
        public int getPriority() { return priority; }
        public int getRemainingDeadline() { return remainingDeadline; }
        @Override public String toString() { return description; }
    }
    public record CpuView(ProcessView PCB, ExecutionMode mode, int PC) {
        public ProcessView getPCB() { return PCB; }
        public ExecutionMode getMode() { return mode; }
        public int getPC() { return PC; }
    }
    public record MemoryView(int totalMemory, int usedMemory) {
        public int getTotalMemory() { return totalMemory; }
        public int getUsedMemory() { return usedMemory; }
    }
    public record RamView(int totalSize) {
        public int getTotalSize() { return totalSize; }
    }
    public record QueueView(int count, String text) {
        public int getCount() { return count; }
        public String getText() { return text; }
        public int size() { return count; }
        @Override public String toString() { return text; }
    }
    public record KernelView(QueueView newQueue, QueueView readyQueue, QueueView blockedQueue, QueueView endedQueue) {
        public QueueView getNewQueue() { return newQueue; }
        public QueueView getReadyQueue() { return readyQueue; }
        public QueueView getBlockedQueue() { return blockedQueue; }
        public QueueView getEndedQueue() { return endedQueue; }
    }
    public record Node(int id, String name, SchedulingPolicyType politica, int quantum, CpuView cpu, MemoryView memoryManager, RamView ram, KernelView kernel, int ciclosActivos, int ciclosObservados, int procesosCompletados, int procesosFinalizados, int deadlinesCumplidos, int totalTiempoRespuesta) {
        public int getId() { return id; }
        public String getName() { return name; }
        public SchedulingPolicyType getPolitica() { return politica; }
        public int getQuantum() { return quantum; }
        public CpuView getCpu() { return cpu; }
        public MemoryView getMemoryManager() { return memoryManager; }
        public RamView getRam() { return ram; }
        public KernelView getKernel() { return kernel; }
        public int getCiclosActivos() { return ciclosActivos; }
        public int getCiclosObservados() { return ciclosObservados; }
        public int getProcesosCompletados() { return procesosCompletados; }
        public int getProcesosFinalizados() { return procesosFinalizados; }
        public int getDeadlinesCumplidos() { return deadlinesCumplidos; }
        public int getTotalTiempoRespuesta() { return totalTiempoRespuesta; }
    }
    public record PermitView(int count) {
        public int getCount() { return count; }
        public int availablePermits() { return count; }
    }
    public record BufferView(int id, int idComp, int storage, int sizeInMemory, int produced, int comsumed, QueueView datos, PermitView mutex, PermitView llenos, PermitView vacios, String blockedProcessesAsString) {
        public int getId() { return id; }
        public int getIdComp() { return idComp; }
        public int getStorage() { return storage; }
        public int getSizeInMemory() { return sizeInMemory; }
        public int getProduced() { return produced; }
        public int getComsumed() { return comsumed; }
        public QueueView getDatos() { return datos; }
        public PermitView getMutex() { return mutex; }
        public PermitView getLlenos() { return llenos; }
        public PermitView getVacios() { return vacios; }
        public String getBlockedProcessesAsString() { return blockedProcessesAsString; }
    }
    public SimulationSnapshot(long session, int cycle, boolean running, boolean paused, int interval,
            MyLinkedList<Computer> nodes, MyLinkedList<Buffer> sources, SimulationSnapshot previous) {
        this.session=session; this.cycle=cycle; this.running=running; this.paused=paused; this.interval=interval;
        Object[] copiedNodes = new Object[nodes.size()];
        for (int i=0; i<nodes.size(); i++) {
            var c=nodes.get(i); var p=c.getCpu().getPCB(); var k=c.getKernel();
            var cpu=new CpuView(p==null ? null : new ProcessView(p.toString(),p.getMemoryAddressRegister(),p.getPriority(),p.getRemainingDeadline()), c.getCpu().getMode(), c.getCpu().getPC());
            var kernel=new KernelView(new QueueView(k.getNewQueue().size(),k.getNewQueue().toString()),
                    new QueueView(k.getReadyQueue().size(),k.getReadyQueue().toString()),
                    new QueueView(k.getBlockedQueue().size(),k.getBlockedQueue().toString()),
                    new QueueView(k.getEndedQueue().size(),k.getEndedQueue().toString()));
            copiedNodes[i]=new Node(c.getId(),c.getName(),c.getPolitica(),c.getQuantum(),cpu,
                    new MemoryView(c.getMemoryManager().getTotalMemory(),c.getMemoryManager().getUsedMemory()),
                    new RamView(c.getRam().getTotalSize()),kernel,c.getCiclosActivos(),c.getCiclosObservados(),
                    c.getProcesosCompletados(),c.getProcesosFinalizados(),c.getDeadlinesCumplidos(),c.getTotalTiempoRespuesta());
        }
        Object[] copiedBuffers=new Object[sources.size()];
        for(int i=0;i<sources.size();i++) {
            var b=sources.get(i);
            copiedBuffers[i]=new BufferView(b.getId(),b.getIdComp(),b.getStorage(),b.getSizeInMemory(),b.getProduced(),b.getComsumed(),
                    new QueueView(b.getDatos().size(),""),new PermitView(b.getMutex().availablePermits()),
                    new PermitView(b.getLlenos().availablePermits()),new PermitView(b.getVacios().availablePermits()),b.getBlockedProcessesAsString());
        }
        computers=new Values<>(copiedNodes); buffers=new Values<>(copiedBuffers);
        // Mantener muestras aunque el EDT omita refrescos. Ventana acotada a 2000 ciclos.
        Object[] usages=new Object[computers.size()];
        for(int i=0;i<usages.length;i++) {
            Node node=computers.get(i);
            usages[i]=new Usage(node.id(),node.ciclosObservados()==0 ? 0.0 : 100.0*node.ciclosActivos()/node.ciclosObservados());
        }
        int retained=previous!=null && previous.session==session && previous.cycle<=cycle ? previous.history.size() : 0;
        if(retained>0 && previous.cycle==cycle) retained--;
        int count=Math.min(1999,retained);
        Object[] samples=new Object[count+1];
        for(int i=0;i<count;i++) samples[i]=previous.history.get(retained-count+i);
        samples[count]=new Sample(cycle,new Values<>(usages));
        history=new Values<>(samples);
    }
}
