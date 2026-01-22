import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class JantarDosFilosofos {

    static class Filosofo implements Runnable {
        private final int id;
        private final Semaphore garfoEsquerdo;
        private final Semaphore garfoDireito;
        private final Semaphore limiteComensais;
        private int contadorRefeicoes = 0;

        public Filosofo(int id, Semaphore garfoEsquerdo, Semaphore garfoDireito, Semaphore limiteComensais) {
            this.id = id;
            this.garfoEsquerdo = garfoEsquerdo;
            this.garfoDireito = garfoDireito;
            this.limiteComensais = limiteComensais;
        }

        private void pensar() throws InterruptedException {
            System.out.println(System.currentTimeMillis() + " | Filósofo " + id + " está pensando.");
            TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(500, 2000));
        }

        private void comer() throws InterruptedException {
            System.out.println(System.currentTimeMillis() + " | Filósofo " + id + " está comendo. (Refeição #" + (++contadorRefeicoes) + ")");
            TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(500, 2000));
        }

        private void pegarGarfos() throws InterruptedException {
            limiteComensais.acquire(); // Limita número de filósofos tentando comer

            garfoEsquerdo.acquire();
            System.out.println(System.currentTimeMillis() + " | Filósofo " + id + " pegou o garfo esquerdo.");

            garfoDireito.acquire();
            System.out.println(System.currentTimeMillis() + " | Filósofo " + id + " pegou o garfo direito.");
        }

        private void largarGarfos() {
            garfoDireito.release();
            garfoEsquerdo.release();
            limiteComensais.release();
            System.out.println(System.currentTimeMillis() + " | Filósofo " + id + " largou os garfos.");
        }

        @Override
        public void run() {
            try {
                while (true) {
                    pensar();
                    pegarGarfos();
                    comer();
                    largarGarfos();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(System.currentTimeMillis() + " | Filósofo " + id + " interrompido.");
            }
        }
    }

    public static void main(String[] args) {
        int numeroDeFilosofos = 5;
        Semaphore[] garfos = new Semaphore[numeroDeFilosofos];
        Semaphore limiteComensais = new Semaphore(numeroDeFilosofos - 1); // Evita deadlock

        // Inicializa os garfos (semáforos binários)
        for (int i = 0; i < numeroDeFilosofos; i++) {
            garfos[i] = new Semaphore(1);
        }

        // Cria e inicia os filósofos
        Thread[] filosofos = new Thread[numeroDeFilosofos];
        for (int i = 0; i < numeroDeFilosofos; i++) {
            Semaphore garfoEsquerdo = garfos[i];
            Semaphore garfoDireito = garfos[(i + 1) % numeroDeFilosofos];
            filosofos[i] = new Thread(new Filosofo(i, garfoEsquerdo, garfoDireito, limiteComensais));
            filosofos[i].start();
        }


        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Interrompe todos os filósofos
        for (Thread filosofo : filosofos) {
            filosofo.interrupt();
        }

        // Aguarda todos terminarem
        for (Thread filosofo : filosofos) {
            try {
                filosofo.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Fim.");
    }
}