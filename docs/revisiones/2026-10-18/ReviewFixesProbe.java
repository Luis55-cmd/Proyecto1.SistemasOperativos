import com.avilaos.simulation.SimulationController;
import com.avilaos.core.model.*;
import com.avilaos.hardware.Computer;
import com.avilaos.sync.Buffer;

/** Verificación aislada de los tres arreglos de Luis; no oculta los dos bugs pendientes. */
public class ReviewFixesProbe {
 static final SimulationController c=SimulationController.getInstance();
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 static Computer computer(int ram){return c.createComputer(ram,SchedulingPolicyType.FCFS);}
 static ProcessControlBlock process(int host,ProcessType type,int deadline){
  var p=c.createProcess("probe",host,type,30,2,1,deadline,"1",1,1);
  return p;
 }
 public static void main(String[] args){
  c.reset();var host=computer(32);
  check(c.createBuffer(1,64)==null,"Rechazo por RAM");
  check(c.createBuffer(1,0)==null && c.createBuffer(1,-1)==null,"Capacidad invalida");
  check(c.createBuffer(99,1)==null,"Host inexistente");
  check(c.getBuffers().size()==0 && host.getMemoryManager().getUsedMemory()==0,"Fallo altera estado");
  var b=c.createBuffer(1,32);check(b!=null && b.getId()==1,"Ajuste exacto/ID");
  check(c.createBuffer(1,1)==null && c.getBuffers().size()==1 && host.getMemoryManager().getUsedMemory()==32,"Sobreasignacion");
  c.reset();host=computer(8);
  for(int i=0;i<8;i+=2)host.getRam().getSpace()[i]=99;
  check(c.createBuffer(1,2)==null && host.getMemoryManager().getUsedMemory()==0,"Fallo contiguo deja reserva");
  System.out.println("PASS: buffer invalido/sin RAM/exacto/fragmentado sin registro parcial");

  c.reset();computer(128);b=c.createBuffer(1,1);
  var consumidor=process(1,ProcessType.CONSUMER,100);process(1,ProcessType.PRODUCER,100);
  for(int i=0;i<15;i++)c.stepSimulation();
  check(consumidor.getState()==ProcessState.SALIENTE && b.getComsumed()==1,"Consumidor no despierta");
  c.reset();computer(128);b=c.createBuffer(1,1);b.produce(7);b.getVacios().tryAcquire();b.getLlenos().release();
  var productor=process(1,ProcessType.PRODUCER,100);process(1,ProcessType.CONSUMER,100);
  for(int i=0;i<15;i++)c.stepSimulation();
  check(productor.getState()==ProcessState.SALIENTE && b.getProduced()==2,"Productor sobre lleno no despierta");
  c.reset();computer(128);computer(128);b=c.createBuffer(1,4);c.setNetworkLatency(0);
  var p1=process(1,ProcessType.PRODUCER,100);var p2=process(2,ProcessType.PRODUCER,100);
  c.stepSimulation();c.stepSimulation();
  check(p2.getBlockReason()==BlockReason.WAITING_MUTEX,"No reprodujo espera mutex");
  for(int i=0;i<12;i++)c.stepSimulation();
  check(p1.getState()==ProcessState.SALIENTE && p2.getState()==ProcessState.SALIENTE && b.getProduced()==2,"Mutex entre computadores");
  System.out.println("PASS: despertar por elementos, espacios y mutex entre computadores");

  for(int latency=0;latency<=2;latency++){
   c.reset();computer(128);computer(128);computer(128);b=c.createBuffer(1,4);c.setNetworkLatency(latency);
   var p=process(2,ProcessType.PRODUCER,100);var q=process(3,ProcessType.PRODUCER,100);
   c.stepSimulation();
   check(p.getNetworkDelayRemaining()==latency,"Inicio red");
   for(int remaining=latency-1;remaining>=0;remaining--){
    c.stepSimulation();check(p.getNetworkDelayRemaining()==remaining && q.getNetworkDelayRemaining()==remaining,"Red no avanza una vez por tick");
   }
   for(int i=0;i<15;i++)c.stepSimulation();
   check(p.getState()==ProcessState.SALIENTE && q.getState()==ProcessState.SALIENTE,"Red no completa");
  }
  c.reset();computer(128);computer(128);b=c.createBuffer(1,4);c.setNetworkLatency(5);
  var expirado=process(2,ProcessType.PRODUCER,2);
  for(int i=0;i<10;i++)c.stepSimulation();
  check(expirado.getState()==ProcessState.SALIENTE && b.getProduced()==0,"Deadline reactiva solicitud");
  System.out.println("PASS: latencias 0/1/2 simultaneas y cancelacion por deadline");
 }
}
