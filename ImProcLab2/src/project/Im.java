package project;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Stack;

public class Im {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static final int PIXELS = 256;

    private static final int[][] DISK = {
            { 0, 0, 1, 1, 1, 1, 1, 0 ,0 },
            { 0, 1, 1, 1, 1, 1, 1, 1, 0 },
            { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            { 0, 1, 1, 1, 1, 1, 1, 1, 0 },
            { 0, 0, 1, 1, 1, 1, 1, 0 ,0 }
    };

    private static final int MAX_HITS = 69;
    private static final double[][] COLORS = { {255, 0, 0}, {255, 0, 255}, {0, 0, 200}, {200, 200, 0}, {200, 0, 200}, {0, 200, 200}};
    public static final int TOTAL_PROPERTIES = 2;
    private int label;

    public Im() { }

    private Mat img2Mat(BufferedImage in) {
        Mat out;
        byte[] data;
        int[] dataBuf;

        out = new Mat(in.getHeight(), in.getWidth(), CvType.CV_8UC3);
        data = new byte[in.getHeight() * in.getWidth() * (int)out.elemSize()];
        dataBuf = in.getRGB(0, 0, in.getWidth(), in.getHeight(), null, 0, in.getWidth());
        for(int i = 0; i < dataBuf.length; i++) {
            data[i * 3] = (byte) ((dataBuf[i] >> 16) & 0xff);
            data[i * 3 + 1] = (byte) ((dataBuf[i] >> 8) & 0xff);
            data[i * 3 + 2] = (byte) (dataBuf[i] & 0xff);
        }

        out.put(0, 0, data);

        return out;
    }

    private BufferedImage mat2Img(Mat in) {
        BufferedImage out;
        int type;

        byte[] data = new byte[in.rows() * in.cols() * (int)in.elemSize()];
        in.get(0, 0 ,data);

        type = (in.channels() == 1) ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR;
        out = new BufferedImage(in.cols(), in.rows(), type);
        out.getRaster().setDataElements(0, 0 , in.cols(), in.rows(), data);

        return out;
    }

    public BufferedImage rgb2Grayscale(BufferedImage in) {
        BufferedImage out;
        Mat mIn = img2Mat(in);

        for(int i = 0; i < mIn.rows(); i++) {
            for(int j = 0; j < mIn.cols(); j++) {
                double[] pixel = mIn.get(i, j);
                double[] nPixel = new double[3];
                for(int k = 0; k < 3; k++) {
                    nPixel[k] = pixel[0] * 0.3 + pixel[1] * 0.59 + pixel[2] * 0.11;
                }
                mIn.put(i, j, nPixel);
            }
        }

        out = mat2Img(mIn);

        return out;
    }

    public int[][] grayscale2Binary(BufferedImage in) {
        int[][] out = new int[in.getHeight()][in.getWidth()];
        Mat mIn = img2Mat(in);

        int threshold = getOtsuThreshold(mIn);

        for(int i = 0; i < mIn.rows(); i++) {
            for(int j = 0; j < mIn.cols(); j++) {
                double[] pixel = mIn.get(i, j);
                if(pixel[0] <= threshold) {
                    out[i][j] = 0;
                }
                else {
                    out[i][j] = 1;
                }
            }
        }
        return out;
    }

    public BufferedImage bin2img(int[][] in) {
        BufferedImage out;
        Mat mIn = new Mat(in.length, in[0].length, CvType.CV_8UC1);

        for(int i = 0; i < mIn.rows(); i++) {
            for(int j = 0; j < mIn.cols(); j++) {
                double[] pixel = new double[3];
                double p = (in[i][j] == 0) ? 0.0 : 255.0;
                pixel[0] = pixel[1] = pixel[2] = p;
                mIn.put(i, j, p);
            }
        }

        out = mat2Img(mIn);

        return out;
    }

    private int getOtsuThreshold(Mat img) {
        int[] hist = new int[PIXELS];

        for(int i = 0; i < img.rows(); i++) {
            for(int j = 0; j < img.cols(); j++) {
                double[] pixel = img.get(i, j);
                hist[(int)pixel[0]]++;
            }
        }

        double sum = 0;
        for(int i = 0; i < PIXELS; i++) {
            sum += i * hist[i];
        }

        int wB = 0, wF = 0, threshold = 0;
        double sumB = 0, maxBetween = 0;
        for(int i = 0; i < PIXELS; i++) {
            wB += hist[i];
            if(wB == 0) continue;

            wF = img.cols() * img.rows() - wB;
            if(wF == 0) break;

            sumB += (double)(i * hist[i]);

            double mB = sumB / wB;
            double mF = (sum - sumB) / wF;
            double between = (double)wB * (double)wF * (mB - mF) * (mB - mF);

            if(between > maxBetween) {
                maxBetween = between;
                threshold = i;
            }
        }

        return threshold;
    }

    private int[][] dilation(int[][] in) {
        int[][] out = new int[in.length][in[0].length];

        for(int i = 4; i < in.length - 4; i++) {
            for(int j = 4; j < in[0].length - 4; j++) {
                int pixel = in[i][j];
                if(pixel == 1) {
                    for(int i1 = -4; i1 <= 4; i1++) {
                        for(int j1 = -4; j1 <= 4; j1++) {
                            if(DISK[i1 + 4][j1 + 4] == 1 && out[i + i1][j + j1] == 0) {
                                out[i + i1][j + j1] = DISK[i1 + 4][j1 + 4];
                            }
                        }
                    }
                }
            }
        }

        return out;
    }

    private int[][] errosion(int[][] in) {
        int[][] out = new int[in.length][in[0].length];

        for(int i = 4; i < in.length - 4; i++) {
            for(int j = 4; j < in[0].length - 4; j++) {
                int pixel;
                int hits = 0;
                for(int i1 = -4; i1 <= 4; i1++) {
                    for(int j1 = -4; j1 <= 4; j1++) {
                        pixel = in[i + i1][j + j1];
                        if(pixel == DISK[i1 + 4][j1 + 4] && DISK[i1 + 4][j1 + 4] == 1) {
                            hits++;
                        }
                    }
                }

                if(hits == MAX_HITS)
                    out[i][j] = 1;
                else
                    out[i][j] = 0;
            }
        }
        return out;
    }

    private int[][] labelImage(int[][] img) {
        int label = 0;
        Stack stack = new Stack();
        int[][] map = new int[img.length][img[0].length];

        for(int i = 1; i < img.length - 1; i++) {
            for(int j = 1; j < img[0].length - 1; j++) {
                if(img[i][j] == 0) continue;
                if(map[i][j] > 0) continue;

                stack.push(new int[] {i, j});
                label++;
                map[i][j] = label;

                int[] pos;
                while(!stack.isEmpty()) {
                    pos = (int[])stack.pop();
                    int i1 = pos[0], j1 = pos[1];

                    if(i1 == 0 || j1 == 0 || i1 == img.length - 1 || j1 == img[0].length - 1)
                        continue;

                    if(img[i1 - 1][j1 - 1] == 1 && map[i1 - 1][j1 - 1] == 0) {
                        stack.push(new int[] {i1 - 1, j1 - 1});
                        map[i1 - 1][j1 - 1] = label;
                    }
                    if(img[i1 - 1][j1] == 1 && map[i1 - 1][j1] == 0) {
                        stack.push(new int[] {i1 - 1, j1});
                        map[i1 - 1][j1] = label;
                    }
                    if(img[i1 - 1][j1 + 1] == 1 && map[i1 - 1][j1 + 1] == 0) {
                        stack.push(new int[] {i1 - 1, j1 + 1});
                        map[i1 - 1][j1 + 1] = label;
                    }
                    if(img[i1][j1 - 1] == 1 && map[i1][j1 - 1] == 0) {
                        stack.push(new int[] {i1, j1 - 1});
                        map[i1][j1 - 1] = label;
                    }
                    if(img[i1][j1 + 1] == 1 && map[i1][j1 + 1] == 0) {
                        stack.push(new int[] {i1, j1 + 1});
                        map[i1][j1 + 1] = label;
                    }
                    if(img[i1 + 1][j1 - 1] == 1 && map[i1 + 1][j1 - 1] == 0) {
                        stack.push(new int[] {i1 + 1, j1 - 1});
                        map[i1 + 1][j1 - 1] = label;
                    }
                    if(img[i1 + 1][j1] == 1 && map[i1 + 1][j1] == 0) {
                        stack.push(new int[] {i1 + 1, j1});
                        map[i1 + 1][j1] = label;
                    }
                    if(img[i1 + 1][j1 + 1] == 1 && map[i1 + 1][j1 + 1] == 0) {
                        stack.push(new int[] {i1 + 1, j1 + 1});
                        map[i1 + 1][j1 + 1] = label;
                    }
                }
            }
        }

        this.label = label;

        return map;
    }

    private int[][] getObjectsProperties(int[][] labImg) {
        int[][] out = new int[this.label][TOTAL_PROPERTIES];
        int[] area = new int[this.label];
        int[] centerOfMassX = new int[this.label];
        int[] centerOfMassY = new int[this.label];
        int[] perimeter = new int[this.label];
        int[] elongation;

        for(int i = 0; i < labImg.length; i++) {
            for(int j = 0; j < labImg[0].length; j++) {
                int pixel = labImg[i][j];
                if(pixel != 0) {
                    area[pixel - 1]++;
                    centerOfMassX[pixel - 1] += i;
                    centerOfMassY[pixel - 1] += j;

                    boolean stop = false;
                    for(int i1 = -1; i1 <= 1; i1++) {
                        if(i == 0 || i == labImg.length - 1) {
                            continue;
                        }
                        for(int j1 = -1; j1 <= 1; j1++) {
                            if(j == 0 || j == labImg[0].length - 1 || (i1 == 0 && j1 ==0)) {
                                continue;
                            }
                            if(labImg[i + i1][j + j1] == 0) {
                                perimeter[pixel - 1]++;
                                stop = true;
                                break;
                            }
                        }
                        if(stop) {
                            break;
                        }
                    }
                }
            }
        }

        for(int i = 0; i < out.length; i++) {
            out[i][0] = area[i];
            centerOfMassX[i] /= area[i];
            centerOfMassY[i] /= area[i];
            //out[i][1] = centerOfMassX[i];
            //out[i][2] = centerOfMassY[i];
            out[i][1] = perimeter[i];
            //out[i][2] = perimeter[i] * perimeter[i] / area[i]; //компактность
        }

        /*elongation = getElongation(labImg, centerOfMassX, centerOfMassY);
        for(int i = 0; i < out.length; i++) {
            out[i][2] = elongation[i];
        }*/

        return out;
    }

    private int[] getElongation(int[][] labImg, int[] centerOfMassX, int[] centerOfMassY) {
        int[] out = new int[centerOfMassX.length];

        for(int i = 0; i < out.length; i++) {
            out[i] += getCentralMoment(2, 0, centerOfMassX[i], centerOfMassY[i], labImg, i + 1);
            out[i] += getCentralMoment(0, 2, centerOfMassX[i], centerOfMassY[i], labImg, i + 1);
            double temp = getCentralMoment(2, 0, centerOfMassX[i], centerOfMassY[i], labImg, i + 1);
            temp -= getCentralMoment(0, 2, centerOfMassX[i], centerOfMassY[i], labImg, i + 1);
            temp = Math.pow(temp, 2);
            temp += 4 * Math.pow(getCentralMoment(1, 1, centerOfMassX[i], centerOfMassY[i], labImg, i + 1), 2);
            temp = Math.sqrt(temp);
            double temp2 = out[i];
            out[i] += temp;
            out[i] /= (temp2 - temp);
        }

        return out;
    }

    private int getCentralMoment(int i, int j, int centerOfMassX, int centerOfMassY, int[][] labImg, int pixel) {
        int out = 0;

        for(int i1 = 0; i1 < labImg.length; i1++) {
            for(int j1 = 0; j1 < labImg[0].length; j1++) {
                if(labImg[i1][j1] == pixel) {
                    out += Math.pow((i1 - centerOfMassX), i) * Math.pow((j1 - centerOfMassY), j);
                }
            }
        }

        return out;
    }

    public BufferedImage findObjects(BufferedImage normalImage, int[][] binaryImage) {
        BufferedImage out;
        int[][] temp = binaryImage;
        temp = errosion(temp);
        temp = dilation(temp);
        temp = labelImage(temp);

        KMeans kMeans = new KMeans(getObjectsProperties(temp));
        ArrayList<KMeans.Data> list = kMeans.cluster();

        for(int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i).getCluster());
        }

        for(int i = 0; i < temp.length; i++) {
            for(int j = 0; j < temp[0].length; j++) {
                int pixel = temp[i][j];
                if(pixel != 0) {
                    temp[i][j] = list.get(pixel - 1).getCluster() + 1;
                }
            }
        }

        out = colorizeObjects(normalImage, temp);

        return out;
    }

    private BufferedImage colorizeObjects(BufferedImage normalImage, int[][] labeledImage) {
        BufferedImage out;
        Mat mIn = img2Mat(normalImage);

        for(int i = 0; i < labeledImage.length; i++) {
            for(int j = 0; j < labeledImage[0].length; j++) {
                int pixel = labeledImage[i][j];
                if(pixel != 0) {
                    mIn.put(i, j, COLORS[pixel - 1]);
                }

            }
        }

        out = mat2Img(mIn);

        return out;
    }
}