package ru.spbstu.telematics.java;

public class Car implements Runnable{
    private final Direction direction;
    private final TrafficLight trLight;

    public Car(Direction dir, TrafficLight trl){
        direction = dir;
        trLight = trl;
    }

    @Override
    public void run(){
        try {
            System.out.println("Подъехала машина " + "№" 
                + Thread.currentThread().getId() + " c траекторией " + direction);
            trLight.addCar(direction);
            Thread.sleep(200);
            trLight.removeCar(direction);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}