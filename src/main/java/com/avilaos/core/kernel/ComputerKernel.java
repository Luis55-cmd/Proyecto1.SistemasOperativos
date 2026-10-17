package com.avilaos.core.kernel;
import com.avilaos.Computer;
import com.avilaos.core.structures.*;
import com.avilaos.core.model.ProcessControlBlock;
import com.avilaos.core.model.ProcessState;
import com.avilaos.core.model.SchedulingPolicyType;
import com.avilaos.core.scheduler.*;
import com.avilaos.core.model.Buffer;
/**
 
 */
public class ComputerKernel {
    
    MyLinkedList<ProcessControlBlock> processList;
    MyQueue<ProcessControlBlock> blockedQueue;
    MyQueue<ProcessControlBlock> newQueue;
    MyQueue<ProcessControlBlock> readyQueue;
    MyQueue<ProcessControlBlock> readyQueue2;
    MyQueue<ProcessControlBlock> readyQueue3;
    MyQueue<ProcessControlBlock> readyQueue4;
    MyQueue<ProcessControlBlock> endedQueue;
    GlobalClock clock;
    
    public ComputerKernel(MyLinkedList<ProcessControlBlock> processList,
    MyQueue<ProcessControlBlock> blockedQueue,
    MyQueue<ProcessControlBlock> newQueue,
    MyQueue<ProcessControlBlock> readyQueue,
    MyQueue<ProcessControlBlock> readyQueue2,
    MyQueue<ProcessControlBlock> readyQueue3,
    MyQueue<ProcessControlBlock> readyQueue4,
    MyQueue<ProcessControlBlock> endedQueue,
    GlobalClock clock){
    this.blockedQueue = blockedQueue;
    this.processList = processList;
    this.newQueue = newQueue;
    this.readyQueue = readyQueue;
    this.endedQueue = endedQueue;
    this.clock = clock;
    }
    
    public String gestionCreateProcess(Computer idComp, boolean asignacionManual, int memoryPt){
        
        int idProcess = 1;
        if(!this.processList.isEmpty()){
            idProcess = this.processList.size() + 1;
        }
        ProcessControlBlock p = new ProcessControlBlock();
        if(asignacionManual){
            int memoryAssigned = p.getAssignedMemory();
            if(isMemoryTotallyUsed(idComp,memoryAssigned)){
                p.setComputerId(idComp.getId());
                for (int i=p.getMemoryPointer(); i <= p.getMemoryPointer()+p.getTotalInstructions(); i++){
                    if(idComp.getRam().getSpace() == null){
                        idComp.getRam().getSpace()[i] = p.getId();
                    }}
                idComp.getRam().setUsedSize(idComp.getRam().getUsedSize()- p.getAssignedMemory());
                this.processList.append(p);
                p.setMemoryPointer(memoryPt);
                switch(idComp.getPolitica()){
                case FCFS:
                    FcfsScheduler scheduler = new FcfsScheduler();
                    scheduler.addProcess(p);
            
                case ROUND_ROBIN:
                    RoundRobinScheduler scheduler1 = new RoundRobinScheduler();
                    scheduler1.addProcess(p);
                    
                case EDF:
                    EdfScheduler scheduler2 = new EdfScheduler();
                    scheduler2.addProcess(p);
                    
                case PRIORITY_PREEMPTIVE:
                    PreemptivePriorityScheduler scheduler3 = new PreemptivePriorityScheduler();
                    scheduler3.addProcess(p);
            }
                return "Se ha creado el proceso";
            }
            return "Falta de memoria. Intente de nuevo.";
        
        }else{
        
        
        
        }
    
        return "Se ha añadido el proceso a la cola de nuevo";
    
    }
    
    public boolean isMemoryTotallyUsed(Computer com, int memoria){
        int memoryNotUsed = com.getRam().getTotalSize() - com.getRam().getUsedSize();
        return memoryNotUsed >= memoria;
    }
    
    public void verificateRAMSpaceForNew(Computer com, ProcessControlBlock p){
        int memoryNotUsed = com.getRam().getTotalSize() - com.getRam().getUsedSize();
        if(memoryNotUsed >= p.getAssignedMemory()){
            this.readyQueue.enqueue(p);
            p.setState(ProcessState.LISTO);
        }
    }
    
    public void dispatch(Computer com, ProcessControlBlock p){
        if(this.readyQueue.isEmpty()){
            if(!this.blockedQueue.isEmpty()){
                this.blockedQueue.peek().setState(ProcessState.EJECUTANDO);
                this.blockedQueue.dequeue();
            }
        }else{
            switch(com.getPolitica()){
                case FCFS:
                    FcfsScheduler scheduler = new FcfsScheduler();
                    scheduler.scheduleNext(this.readyQueue, p);
            
                case ROUND_ROBIN:
                    RoundRobinScheduler scheduler1 = new RoundRobinScheduler();
                    scheduler1.scheduleNext(this.readyQueue, p);
                    
                case EDF:
                    EdfScheduler scheduler2 = new EdfScheduler();
                    scheduler2.scheduleNext(this.readyQueue, p);
                    
                case PRIORITY_PREEMPTIVE:
                    PreemptivePriorityScheduler scheduler3 = new PreemptivePriorityScheduler();
                    scheduler3.scheduleNext(this.readyQueue, p);
            }
            com.getCpu().setPCB(p);
        }
    }
    
    
    
    public void terminateFatherProcess(int fatherId){
        for (int i = 1; i <= this.processList.size(); i++) {
            ProcessControlBlock proceso = this.processList.get(i);
            if (proceso != null && proceso.getIdFather() == fatherId) {
                ProcessState estadoActual = proceso.getState();
                if(estadoActual == ProcessState.BLOQUEADO){
                    this.blockedQueue.removeElement(proceso);
                } else if(estadoActual == ProcessState.LISTO){
                    this.readyQueue.removeElement(proceso);
                } else if(estadoActual == ProcessState.NUEVO){
                    this.newQueue.removeElement(proceso);
                }
                
                proceso.setState(ProcessState.SALIENTE);
                this.endedQueue.enqueue(proceso);
                
                this.processList.deletePos(i);
                i--; 

            }
        }
    
    }
    
    public void terminateProcess(ProcessControlBlock p, Computer com){
        if(p.getState() == ProcessState.BLOQUEADO){
            this.blockedQueue.removeElement(p);
        }else if(p.getState() == ProcessState.LISTO){
            this.readyQueue.removeElement(p);
        }else if(p.getState() == ProcessState.NUEVO){
            this.newQueue.removeElement(p);
        }
        this.processList.deletePos(p.getId());
        p.setState(ProcessState.SALIENTE);
        terminateFatherProcess(p.getId());
        for (int i=p.getMemoryPointer(); i <= p.getMemoryPointer()+p.getTotalInstructions(); i++){
                    if(com.getRam().getSpace() == null){
                        com.getRam().getSpace()[i] = 0;
                    }}
        com.getRam().setUsedSize(com.getRam().getUsedSize()- p.getAssignedMemory());
    }
    
    public void blockProcess(ProcessControlBlock p){
        


    }
    
    public void blockedToReady(ProcessControlBlock p){
        this.blockedQueue.removeElement(p);
        this.readyQueue.enqueue(p);
        p.setState(ProcessState.LISTO);
    
    }
    
    public String placeBufferRAM(Buffer buffer, Computer com){
    int cantidad = buffer.getStorage();
    if (com.getRam().getSpace() == null) {
        return "Error: El espacio de la RAM no está inicializado.";
    }
    int ramTotalSize = com.getRam().getSpace().length;
    int consecutiveFreeBlocks = 0;
    int startIndex = -1;
    for (int i = 0; i < ramTotalSize; i++) {
        if (com.getRam().getSpace()[i] == 0) { 
            if (consecutiveFreeBlocks == 0) {
                startIndex = i; 
            }
            consecutiveFreeBlocks++;
            if (consecutiveFreeBlocks == cantidad) {
                break;
            }
        } else {
            consecutiveFreeBlocks = 0;
            startIndex = -1;
        }
    }
    if (consecutiveFreeBlocks >= cantidad) {
        for (int i = startIndex; i < startIndex + cantidad; i++) {
            com.getRam().getSpace()[i] = buffer.getId()+1000;  //ESTO SE CAMBIARA LUEGO
        }
        com.getRam().setUsedSize(com.getRam().getUsedSize() + buffer.getSizeInMemory());
        return "Búfer ID " + buffer.getId() + " ubicado exitosamente en la RAM";
    }
    return "Falta de memoria contigua.";
    }
    
}
    
    
    


    
   
    
