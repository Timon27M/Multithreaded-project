package org.example;

import org.example.threadpool.CustomThreadPool;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("========== СТАРТ ТЕСТА ==========\n");

        // 1. Инициализация пула
        CustomThreadPool pool = new CustomThreadPool(
                "MyPool",
                2,                   // corePoolSize
                4,                   // maxPoolSize
                5,                   // keepAliveTime
                TimeUnit.SECONDS,
                3,                   // queueSize
                1                    // minSpareThreads
        );

        // 2. Тест execute() — обычные задачи
        System.out.println("--- Тест 1: execute() с Runnable ---");
        for (int i = 1; i <= 8; i++) {
            final int taskId = i;
            try {
                pool.execute(() -> {
                    System.out.println("  >>> Task " + taskId + " START в " + Thread.currentThread().getName());
                    try {
                        Thread.sleep(2000); // имитация работы
                    } catch (InterruptedException e) {
                        System.out.println("  >>> Task " + taskId + " ПРЕРВАНА");
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("  <<< Task " + taskId + " END в " + Thread.currentThread().getName());
                });
            } catch (RuntimeException e) {
                System.out.println("  [ОТКАЗ] Task " + taskId + " отклонена: " + e.getMessage());
            }
            Thread.sleep(300);
        }

        Thread.sleep(8000);

        // 3. Тест submit() — задачи с возвратом результата
        System.out.println("\n--- Тест 2: submit() с Callable ---");
        try {
            Future<String> future = pool.submit(() -> {
                Thread.sleep(1500);
                return "Результат из потока " + Thread.currentThread().getName();
            });

            Future<Integer> futureCalc = pool.submit(() -> {
                Thread.sleep(1000);
                return 42 + 7;
            });

            System.out.println("  Результат future: " + future.get());
            System.out.println("  Результат futureCalc: " + futureCalc.get());
        } catch (Exception e) {
            System.out.println("  Ошибка submit: " + e.getMessage());
        }

        // 4. Тест перегрузки — много задач
        System.out.println("\n--- Тест 3: Перегрузка (много задач) ---");
        int rejected = 0;
        int accepted = 0;

        for (int i = 1; i <= 20; i++) {
            final int taskId = i;
            try {
                pool.execute(() -> {
                    System.out.println("  >>> Bulk Task " + taskId + " START в " + Thread.currentThread().getName());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("  <<< Bulk Task " + taskId + " END в " + Thread.currentThread().getName());
                });
                accepted++;
            } catch (RuntimeException e) {
                rejected++;
                System.out.println("  [ОТКАЗ] Bulk Task " + taskId + " отклонена");
            }
        }

        System.out.println("\n  Принято: " + accepted + ", Отклонено: " + rejected);

        // 5. Ждем завершения задач и вызываем shutdown
        Thread.sleep(10000);
        System.out.println("\n--- Тест 4: shutdown ---");
        pool.shutdown();
        Thread.sleep(3000);

        // 6. Проверка: после shutdown задачи должны отклоняться
        System.out.println("\n--- Тест 5: Попытка отправки после shutdown ---");
        try {
            pool.execute(() -> System.out.println("Эта задача не должна выполниться"));
        } catch (RuntimeException e) {
            System.out.println("  Задача после shutdown отклонена (так и должно быть)");
        }

        System.out.println("\n========== ТЕСТ ЗАВЕРШЕН ==========");
    }
}