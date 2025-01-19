package ru.spbstu.telematics.java;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class AppTest extends TestCase {

    private TrafficLight trafficLight;

    public AppTest(String testName) {
        super(testName);
    }

    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(AppTest.class);
        return suite;
    }

    @Before
    public void setUp() {
        trafficLight = new TrafficLight();
    }

    private void submitCarTask(ExecutorService executor, CountDownLatch latch, Direction direction) {
        executor.submit(() -> {
            try {
                latch.await();
                Car car = new Car(direction, trafficLight);
                car.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private Direction getRandomDirection() {
        Random random = new Random();
        return Direction.values()[random.nextInt(Direction.values().length)];
    }

    @Test
    public void testRaceCondition() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Thread trafficLightThread = new Thread(new TrafficLightController(trafficLight));
        trafficLightThread.start();

        for (int i = 0; i < 10; i++) {
            Direction direction = getRandomDirection();
            submitCarTask(executor, latch, direction);
        }

        latch.countDown();
        executor.shutdown();

        assertTrue("Обнаружено состояние гонки: пул потоков не завершился вовремя",
            executor.awaitTermination(10, TimeUnit.SECONDS));

        for (Direction dir : Direction.values()) {
            int count = trafficLight.getCarsCount(dir);
            assertTrue("Количество машин для направления " + dir + " должно быть неотрицательным", count >= 0);
        }
    }

    @Test
    public void testDeadlock() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Thread trafficLightThread = new Thread(new TrafficLightController(trafficLight));
        trafficLightThread.start();
        Direction direction = getRandomDirection();

        submitCarTask(executor, latch, direction);
        submitCarTask(executor, latch, direction);

        latch.countDown(); // Разрешаем потокам начать выполнение
        executor.shutdown();

        assertTrue("Обнаружена взаимная блокировка: потоки не завершились вовремя",
            executor.awaitTermination(10, TimeUnit.SECONDS));
    }


    @Test
    public void testNotIntersects() {
        assertTrue(Direction.NS.doesNotIntersect(Direction.SN));
        assertTrue(Direction.EW.doesNotIntersect(Direction.WE));
        assertTrue(Direction.ES.doesNotIntersect(Direction.NS));
    }
}
