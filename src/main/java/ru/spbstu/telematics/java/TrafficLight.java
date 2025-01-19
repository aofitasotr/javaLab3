package ru.spbstu.telematics.java;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TrafficLight {
    private final ReentrantLock lock;
    private final Condition condition;
    private final LinkedList<Direction> directionPriorities;
    private final EnumMap<Direction, Integer> carsCount;
    private Direction allowedToMove1;
    private Direction allowedToMove2;

    public TrafficLight() {
        lock = new ReentrantLock();
        condition = lock.newCondition();
        directionPriorities = new LinkedList<>();
        carsCount = new EnumMap<>(Direction.class);
        allowedToMove1 = null;
        allowedToMove2 = null;

        List<Direction> directionsList = Arrays.asList(Direction.values());
        Collections.shuffle(directionsList, new Random());
        directionPriorities.addAll(directionsList);

        for (Direction direction : Direction.values()) {
            carsCount.put(direction, 0);
        }
    }

    public void addCar(Direction direction) {
        lock.lock();
        try {
            carsCount.put(direction, carsCount.get(direction) + 1);
            while (!canMove(direction)) {
                condition.await();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
        } finally {
            lock.unlock();
        }
    }

    public void removeCar(Direction direction) {
        lock.lock();
        try {
            if (carsCount.get(direction) > 0) {
                carsCount.put(direction, carsCount.get(direction) - 1);
                System.out.println("Уехала машина " 
                    + "№" + Thread.currentThread().getId() + " по траектории " + direction);
            }
        } finally {
            lock.unlock();
        }
    }

    public void turnOnGreen() {
        lock.lock();
        try {
            allowedToMove1 = directionPriorities.removeFirst();
            for (Direction direction : directionPriorities) {
                if (allowedToMove1.doesNotIntersect(direction)) {
                    allowedToMove2 = direction;
                    directionPriorities.remove(direction);

                    System.out.println("ЗЕЛЕНЫЙ СВЕТ направлениям " + allowedToMove1 
                        + " и " + allowedToMove2);
                    condition.signalAll(); // Разрешаем движение всем ожидающим машинам
                    Thread.sleep(1000); // Имитация времени зеленого света

                    directionPriorities.add(allowedToMove2);
                    directionPriorities.add(allowedToMove1); 
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
        } finally {
            lock.unlock();
        }
    }

    // Методы для тестирования
    public int getCarsCount(Direction direction) {
        lock.lock();
        try {
            return carsCount.get(direction);
        } finally {
            lock.unlock();
        }
    }

    public boolean canMove(Direction direction) {
        lock.lock();
        try {
            return allowedToMove1 == direction || allowedToMove2 == direction;
        } finally {
            lock.unlock();
        }
    }
}
