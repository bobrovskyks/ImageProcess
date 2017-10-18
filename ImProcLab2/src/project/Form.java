package project;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Form {


    @FXML
    private ImageView imageView1, imageView2, imageView3;

    private Im imageProcessor;
    private BufferedImage normalImage, processedImage, binaryImage, grayScaleImage;
    private int[][] binaryImg;

    public Form() {}

    @FXML
    public void initialize() {
        imageProcessor = new Im();
        try {
            normalImage = ImageIO.read(new File("D:/Pictures/lab2/4.jpg"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
            normalImage = Filter.filter(normalImage);
            imageView1.setImage(SwingFXUtils.toFXImage(normalImage, null));
            grayScaleImage = imageProcessor.rgb2Grayscale(normalImage);
            binaryImg = imageProcessor.grayscale2Binary(grayScaleImage);
            binaryImage = imageProcessor.bin2img(binaryImg);
            imageView2.setImage(SwingFXUtils.toFXImage(binaryImage, null));
            processedImage = imageProcessor.findObjects(normalImage, binaryImg);
            imageView3.setImage(SwingFXUtils.toFXImage(processedImage, null)); }
}
