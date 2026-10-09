package com.avilaos.core.kernel;

import com.avilaos.core.structures.MyLinkedList;

/**
 * Reloj Global centralizado del simulador ÁvilaOS.
 * 
 * Implementa el patrón Singleton y corre sobre un hilo independiente de Java (Runnable).
 * Sincroniza temporalmente a todos los computadores del clúster avanzando ciclo por ciclo.
 * 
 * Requisitos: RF §1, ARC-11, ARC-13, ARC-14, D-07.
 */
public class GlobalClock implements Runnable {

    private static volatile GlobalClock instance;

    private final MyLinkedList<IClockTickable> subscribers;
    private int cycleCounter;
    private volatile int cycleDurationMs;
    private volatile boolean running;
    private volatile boolean paused;

    private Thread clockThread;
    private final Object pauseLock = new Object();
    private final Object tickLock = new Object();

    /**
     * Constructor privado del Singleton.
     */
    private GlobalClock() {
        this.subscribers = new MyLinkedList<>();
        this.cycleCounter = 0;
        this.cycleDurationMs = 1000; // Por defecto: 1 ciclo por segundo
        this.running = false;
        this.paused = false;
    }

    /**
     * Retorna la instancia única del Reloj Global (hilo seguro).
     *
     * @return Instancia única de GlobalClock.
     */
    public static GlobalClock getInstance() {
        if (instance == null) {
            synchronized (GlobalClock.class) {
                if (instance == null) {
                    instance = new GlobalClock();
                }
            }
        }
        return instance;
    }

    /**
     * Inicia la ejecución del hilo del reloj.
     * Si ya estaba corriendo pero pausado, lo reanuda.
     */
    public synchronized void start() {
        if (!running) {
            running = true;
            paused = false;
            clockThread = new Thread(this, "AvilaOS-GlobalClock");
            clockThread.setDaemon(true);
            clockThread.start();
        } else if (paused) {
            resume();
        }
    }

    /**
     * Pausa el avance temporal del reloj sin destruir el hilo.
     */
    public void pause() {
        this.paused = true;
    }

    /**
     * Reanuda el avance temporal del reloj tras una pausa.
     */
    public void resume() {
        synchronized (pauseLock) {
            this.paused = false;
            pauseLock.notifyAll();
        }
    }

    /**
     * Detiene por completo el reloj global.
     */
    public synchronized void stop() {
        running = false;
        paused = false;
        synchronized (pauseLock) {
            pauseLock.notifyAll();
        }
        if (clockThread != null) {
            clockThread.interrupt();
            clockThread = null;
        }
    }

    /**
     * Ejecuta manualmente un único ciclo de reloj (modo paso a paso).
     */
    public void step() {
        tick();
    }

    /**
     * Pulso interno de reloj: incrementa el contador de ciclos y notifica a los suscriptores.
     */
    private void tick() {
        int current;
        synchronized (tickLock) {
            cycleCounter++;
            current = cycleCounter;
        }

        synchronized (subscribers) {
            for (IClockTickable subscriber : subscribers) {
                try {
                    subscriber.tick(current);
                } catch (Exception e) {
                    System.err.println("[GlobalClock] Error al notificar suscriptor en ciclo " 
                            + current + ": " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void run() {
        while (running) {
            synchronized (pauseLock) {
                while (paused && running) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            if (!running) {
                break;
            }

            tick();

            try {
                Thread.sleep(cycleDurationMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Suscribe un componente (computador/CPU/dispositivo) al pulso del reloj.
     *
     * @param subscriber Componente que implementa IClockTickable.
     */
    public void addSubscriber(IClockTickable subscriber) {
        if (subscriber != null) {
            synchronized (subscribers) {
                if (!subscribers.contains(subscriber)) {
                    subscribers.add(subscriber);
                }
            }
        }
    }

    /**
     * Desuscribe un componente del reloj global.
     *
     * @param subscriber Componente a remover.
     */
    public void removeSubscriber(IClockTickable subscriber) {
        if (subscriber != null) {
            synchronized (subscribers) {
                subscribers.remove(subscriber);
            }
        }
    }

    /**
     * Reinicia el reloj global a su estado inicial (ciclo 0 y sin suscriptores).
     */
    public synchronized void reset() {
        stop();
        synchronized (tickLock) {
            this.cycleCounter = 0;
        }
        synchronized (subscribers) {
            while (!subscribers.isEmpty()) {
                subscribers.remove(subscribers.get(0));
            }
        }
    }

    // --- GETTERS Y SETTERS ---

    public int getCycleCounter() {
        synchronized (tickLock) {
            return cycleCounter;
        }
    }

    public int getCycleDurationMs() {
        return cycleDurationMs;
    }

    public void setCycleDurationMs(int cycleDurationMs) {
        if (cycleDurationMs > 0) {
            this.cycleDurationMs = cycleDurationMs;
        }
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isPaused() {
        return paused;
    }

    public MyLinkedList<IClockTickable> getSubscribers() {
        return subscribers;
    }
}
