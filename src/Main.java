import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static int newWidth = 800; // новая ширина изображения
    private static int attempt = 0; // счётчик попыток

    public static void main(String[] args) {

        while (attempt == 0) { // пока количество попыток равно нулю (т.е. не выполнено ни одного успешного сжатия)
            try {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Введите путь до папки, в которой хранятся изображения для сжатия (Например: C://Users/User/Desktop/folder): ");
                String srcFolder = scanner.next();
                legalOrNotPath(srcFolder); // отправляем полученный путь из консоли на проверку
                System.out.println("Введите путь до папки, в которой будут храниться изображения после сжатия: ");
                String dstFolder = scanner.next();
                legalOrNotPath(dstFolder); // так же отправляем на проверку и этот путь

                long start = System.currentTimeMillis(); // метка времени для замера длительности выполнения программы
                System.out.println("Программа выполняется.............");
                //String srcFolder = "C://Users/Георгий/Desktop/откуда"; // путь для папки, из которой будем сжимать изображение
                //String dstFolder = "C://Users/Георгий/Desktop/куда"; // путь для папки, в которую будем сохранять изображения

                File srcDirectory = new File(srcFolder); // папка с картинками изначальная
                File[] files = srcDirectory.listFiles(); // массив с картинками из изначальной папки
                int cores = Runtime.getRuntime().availableProcessors(); // такой простой командой узнаем количество ядер процессора

                int parts = files.length / cores; // например 54 файла - /16 = 3, остается 6 файлов
                int remainder = files.length % cores; // 6
                int counter = 0; // счетчик отсчета файлов в исходном массиве
                ArrayList<Thread> threads = new ArrayList<>(); // сюда запишем все потоки (чтобы потом их соединить)

                for (int i = 0; i < cores; i++) { // проходим циклом по каждому ядру процессора
                    if (remainder > 0) { // если остаток от деления исходного массива на части не равен 0, то прибавляем к части файлов ещё один
                        parts++;
                    }
                    File[] partFiles = new File[parts]; // создаем новый массив файлов, длиной с одну часть
                    System.arraycopy(files, counter, partFiles, 0, partFiles.length); // копируем - (исходный массив, начиная с какого берём, куда, начиная откуда, докуда)
                    ImageResizer imageResizer = new ImageResizer(partFiles, newWidth, dstFolder); // создаем имейджРесайзер, даём ему необходимые параметры
                    Thread thread = new Thread(imageResizer); // создаём новый поток, передаём ему имейджресайзер
                    thread.start(); // запускаем поток
                    threads.add(thread); // добавляем поток в список потоков
                    counter = counter + parts; // записываем в счётчик сколько файлов мы уже записали
                    if (remainder > 0) { // если остаток так и не равен нулю
                        remainder--; // уменьшаем количество остатка на один
                        parts--; // так же возвращаем количество файлов в одной части
                    }
                }
                for (Thread thread : threads) { // перебираем все потоки и соединяем (чтобы корректно работал подсчёт времени выполнения программы)
                    thread.join();
                }
                double time = (double) (System.currentTimeMillis() - start) / 1000; // время выполнения программы в сек
                time = Math.round(time * 10.0) / 10.0; // округляем до одного знака после запятой
                System.out.println("Сжатие изображений закончено успешно.");
                System.out.println("Время выполнения программы: " + time + "с.");
                attempt++; // если дошли досюда, значит сжатие выполнено, бросаем в счётчик 1, программа завершается
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
    private static boolean correctFormatPath(String path) { // метод для проверки корректности пути к папке
        String[] chars = path.split("[A-Za-z]:([[/\\\\]\\D\\d\\s])+"); // вот такое регулярное выражение придумал
        if (chars.length == 0) { // если в итоге массив символов пустой, значит путь корректный, иначе некорректен
            return true;
        }
        return false;
    }
    private static void legalOrNotPath(String path) { // метод для обработки пути к папке
        if (!correctFormatPath(path)) { // если предыдущий метод возвращает false, значит бросаем исключение
            throw new IllegalArgumentException("Неверно введён путь к папке! ");
        }
        File folder = new File(path);
        File[] files = folder.listFiles();
        if (files == null) { // создаем папку, читаем её, если выбрасывает null - значит папки по этому пути не найдено
            throw new NullPointerException("Такой папки не существует! ");
        }
    }
}