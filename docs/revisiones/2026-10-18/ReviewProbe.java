import com.avilaos.simulation.*;
import com.avilaos.core.model.*;
import com.avilaos.hardware.*;
import com.avilaos.sync.*;
public class ReviewProbe {
 public static void main(String[] args) {
  SimulationController c=SimulationController.getInstance();c.reset();
  Computer pc=c.createComputer(128,SchedulingPolicyType.ROUND_ROBIN);
  ProcessControlBlock a=c.createProcess("A",1,ProcessType.CPU_BOUND,20,10,1,100);
  ProcessControlBlock b=c.createProcess("B",1,ProcessType.CPU_BOUND,20,10,1,100);
  for(int i=0;i<8;i++)c.stepSimulation();
  System.out.println("RR tras 8 ticks: A="+a.getRemainingTime()+" B="+b.getRemainingTime());
  c.reset();pc=c.createComputer(128,SchedulingPolicyType.FCFS);Buffer buf=c.createBuffer(1,5);
  a=c.createProcess("P",1,ProcessType.PRODUCER,10,20,1,100);
  a.setRequiredElements(1);a.setTargetBufferId("1");
  b=c.createProcess("B",1,ProcessType.CPU_BOUND,30,30,1,100);
  for(int i=0;i<5;i++)c.stepSimulation();
  System.out.println("Fin productor: ended="+pc.getKernel().getEndedQueue().size()+" RAM usada="+pc.getMemoryManager().getUsedMemory()+" esperado 35");
  c.reset();pc=c.createComputer(128,SchedulingPolicyType.FCFS);buf=c.createBuffer(1,5);
  a=c.createProcess("C",1,ProcessType.CONSUMER,10,10,1,100);a.setRequiredElements(1);a.setTargetBufferId("1");
  b=c.createProcess("P",1,ProcessType.PRODUCER,10,10,1,100);b.setRequiredElements(1);b.setTargetBufferId("1");
  for(int i=0;i<15;i++)c.stepSimulation();
  System.out.println("Consumidor tras productor: estado="+a.getState()+" elementos="+buf.getDatos().size()+" consumidos="+buf.getComsumed());
  c.reset();c.createComputer(128,SchedulingPolicyType.FCFS);pc=c.createComputer(128,SchedulingPolicyType.FCFS);c.createBuffer(1,5);
  a=c.createProcess("Remoto",2,ProcessType.PRODUCER,10,10,1,100);a.setRequiredElements(1);a.setTargetBufferId("1");
  for(int i=0;i<10;i++)c.stepSimulation();
  System.out.println("Red tras 10 ticks: estado="+a.getState()+" demora="+a.getNetworkDelayRemaining());
  c.reset();pc=c.createComputer(32,SchedulingPolicyType.FCFS);buf=c.createBuffer(1,64);
  System.out.println("Buffer no cabe: registrado="+(c.getBufferById(buf.getId())!=null)+" RAM usada="+pc.getMemoryManager().getUsedMemory());
 }
}
