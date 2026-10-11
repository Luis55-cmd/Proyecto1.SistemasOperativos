package com.avilaos.core.kernel;
import com.avilaos.Computer;
import com.avilaos.core.structures.*;
import com.avilaos.core.model.ProcessControlBlock;
import com.avilaos.core.model.ProcessState;
import com.avilaos.core.model.SchedulingPolicyType;
import com.avilaos.core.scheduler.*;
/**
 
 */
public class ComputerKernel {
    
    MyLinkedList<ProcessControlBlock> processList;
    MyQueue<ProcessControlBlock> blockedQueue;
    MyQueue<ProcessControlBlock> newQueue;
    MyQueue<ProcessControlBlock> readyQueue;
    MyQueue<ProcessControlBlock> endedQueue;
    Dispatcher dispatcher;
    GlobalClock clock;
    
    public ComputerKernel(MyLinkedList<ProcessControlBlock> processList,
    MyQueue<ProcessControlBlock> blockedQueue,
    MyQueue<ProcessControlBlock> newQueue,
    MyQueue<ProcessControlBlock> readyQueue,
    MyQueue<ProcessControlBlock> endedQueue,
    Dispatcher dispatcher,
    GlobalClock clock){
    this.blockedQueue = blockedQueue;
    this.processList = processList;
    this.newQueue = newQueue;
    this.readyQueue = readyQueue;
    this.endedQueue = endedQueue;
    this.dispatcher = dispatcher;
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
        // Luego se hace
        
        
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
        }
    }
    

    
    
   
    
}