package org.example;

import java.util.Scanner;

import com.example.grpc.DroneServiceOuterClass;
import com.example.grpc.DroneServiceOuterClass.AutopilotModes;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        String ip = "";
        int port = 0;
        double latitude = 0.0;
        double longitude = 0.0;

        while (ip.isEmpty()) {
            System.out.print("Введите IP адрес: ");
            ip = scanner.nextLine();
            if (ip.isEmpty()) {
                System.out.println("IP адрес не может быть пустым. Пожалуйста, введите корректное значение.");
            }
        }

        while (port <= 0) {
            System.out.print("Введите порт: ");
            while (!scanner.hasNextInt()) {
                System.out.println("Неправильное значение порта. Пожалуйста, введите корректное значение.");
                scanner.next();
            }
            port = scanner.nextInt();
        }

        Commander commander = new Commander();
        try {
            commander = new Commander(ip, port);
        }
        catch (Exception exception) {
            System.out.println("Не удалось установить соединение!");
            System.exit(0);
        }

        while (true) {
            System.out.print("Попытка получить координаты дрона...");
            commander.UpdateDetailedData();
            DroneServiceOuterClass.DetailedData detailedData = commander.GetDetailedData();
            latitude = detailedData.getLatitude() / 1e7;
            longitude = detailedData.getLongitude() / 1e7;

            if (longitude == 0.0 || latitude == 0.0) {
                System.out.println("Значение координат выглядит неверным!");
            }
            else {
                System.out.println("Координаты приняты!");
                System.out.println("Внимание, подтвердите следующие координаты.\nНеправильные данные повлекут за собой необратимые последствия!" +
                        "\nLong: " + longitude + " Lat: " + latitude + "\nДля подтверждения пропишите \"YES\".");

                while (!scanner.hasNext()) {
                    System.out.println("Пожалуйста, введите корректное значение.");
                    scanner.next();
                }
                String answer = "";
                while (answer.isEmpty()) {
                    answer = scanner.nextLine();
                }
                if(answer.equals("YES")) break;
                else System.exit(0);
            }
        }
        System.out.println("Подтверждение получено!");
        int pointCounts = readIntegerInput(scanner, "Введите число геоточек: ");
        int alt = readIntegerInput(scanner, "Введите высоту полёта: ");
        int holdTime = readIntegerInput(scanner, "Введите время задержки в точке: ");
        int acceptRadius = readIntegerInput(scanner, "Введите допустимый радиус: ");
        int mode = readIntegerInput(scanner, "Введите режим дрона (STABILIZE=0; GUIDED=4; AUTO=3): ");
        int offsetMulty = readIntegerInput(scanner, "Введите множитель смещения между точками (По умолчанию: 1 = около 6 метров по долготе и 4 метров по широте за раз). \nАлгоритм каждый раз перемещается либо по широте, либо по долготе, змейкой. Т.е. по станадарту за один перелёт между точками перемещение происходит примерно на 5 метров.\n(Рекомендуется оставить 1):  ");
        double offset = 0.00005 * offsetMulty;

        double[][] geocodes = commander.GenerateGeoCodes(latitude, longitude, alt,
                offset, offset, pointCounts + 1);

        System.out.println("Миссия успешно сгенерирована!\nДля подтверждения её загрузки и переключения режима дрона пропишите \"YES\".");

        while (!scanner.hasNext()) {
            System.out.println("Пожалуйста, введите корректное значение.");
            scanner.next();
        }
        String answer = "";
        while (answer.isEmpty()) {
            answer = scanner.nextLine();
        }
        if(!answer.equals("YES")) System.exit(0);

        commander.SendFlightPlan(commander.GetGeocodesFromArray(geocodes), holdTime, acceptRadius);
        commander.ChangeFlightMode(AutopilotModes.forNumber(mode));

        System.out.println("WARNING! CHECK EVERYTHING! THIS IS POINT OF NO RETURN!!!!\nДля подтверждения начала миссии пропишите \"YES\".");

        while (!scanner.hasNext()) {
            System.out.println("Пожалуйста, введите корректное значение.");
            scanner.next();
        }

        answer = "";
        while (answer.isEmpty()) {
            answer = scanner.nextLine();
        }
        if(!answer.equals("YES")) System.exit(0);

        commander.ArmDisarm(1);
        TimeUnit.SECONDS.sleep(1);
        commander.StartMission();
        System.out.println("Работа завершена!");
    }
    private static int readIntegerInput(Scanner scanner, String message) {
        int input;
        do {
            System.out.print(message);
            while (!scanner.hasNextInt()) {
                System.out.println("Неправильное значение. Пожалуйста, введите целое число.");
                scanner.next();
            }
            input = scanner.nextInt();
        } while (input < 0);
        return input;
    }
}