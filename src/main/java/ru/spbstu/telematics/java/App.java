package ru.spbstu.telematics.java;

import java.util.Random;

public class App {
    public static void main(String[] args) {

        TrafficLight trafficLight = new TrafficLight();
        Random random = new Random();

        Thread trafficLightThread = new Thread(new TrafficLightController(trafficLight));
        trafficLightThread.start();

        Thread carGeneratorThread = new Thread(new CarGenerator(trafficLight, random));
        carGeneratorThread.start();
    }
}


class TrafficLightController implements Runnable {

    private final TrafficLight trafficLight;

    public TrafficLightController(TrafficLight trafficLight) {
        this.trafficLight = trafficLight;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            trafficLight.turnOnGreen();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); 
            }
        }
    }
}


class CarGenerator implements Runnable {

    private final TrafficLight trafficLight;
    private final Random random;

    public CarGenerator(TrafficLight trafficLight, Random random) {
        this.trafficLight = trafficLight;
        this.random = random;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Direction direction = Direction.values()[random.nextInt(Direction.values().length)];
            new Thread(new Car(direction, trafficLight)).start();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
