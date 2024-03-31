import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
public class ImageResizer implements Runnable{
    private File[] files;
    private int newWidth;
    private String dstFolder;
    public ImageResizer(File[] files, int newWidth, String dstFolder) {
        this.files = files;
        this.newWidth = newWidth;
        this.dstFolder = dstFolder;
    }
    @Override // чтобы заработал поток, нужно переопределить метод ран
    public void run() {
        try {
            for (File file : files) {
                BufferedImage image = ImageIO.read(file); // читаем изображение из файла
                if (image == null) { // если файл не прочитался, то продолжаем цикл
                    continue;
                }
                int newHeight = (int) Math.round(image.getHeight() / (image.getWidth() / (double) newWidth)); // например 720 / (1280 / 300) = 169 - высота
                BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB); // создаем новое изображение
                int widthStep = image.getWidth() / newWidth; // шаг по ширине. например 3000 / 300 = 10
                int heightStep = image.getHeight() / newHeight; // шаг по длине
                for (int x = 0; x < newWidth; x++) {
                    for (int y = 0; y < newHeight; y++) {
                        int rgb = image.getRGB(x * widthStep, y * heightStep); // берём эти пиксели
                        newImage.setRGB(x, y, rgb); // и вставляем в это изображение
                    }
                }
                File newFile = new File(dstFolder + "/" + file.getName()); // создаем файл со старым названием в новой директории
                ImageIO.write(newImage, "jpg", newFile); // записываем изображение в файл
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
