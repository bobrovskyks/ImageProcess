package project;

import java.util.ArrayList;

public class KMeans {
    private int[][] properties;
    private ArrayList<Data> dataList = new ArrayList<>();
    private ArrayList<Centroid> centroids = new ArrayList<>();
    private static final int TOTAL_CLUSTERS = 2;

    public KMeans(int[][] properties) {
        this.properties = properties;
        for(int i = 0; i < TOTAL_CLUSTERS; i++) {
            Centroid c = new Centroid(Im.TOTAL_PROPERTIES);
            for (int j = 0; j < Im.TOTAL_PROPERTIES; j++) {
                c.setValue(j, properties[i][j]);
            }
            centroids.add(c);
        }
    }

    public ArrayList<Data> cluster() {
        double min = 100000000;
        double distance = 0;
        int cluster = 0, currentElement = 0;

        while(dataList.size() < properties.length) {
            Data data = new Data(Im.TOTAL_PROPERTIES);
            for(int i = 0; i < Im.TOTAL_PROPERTIES; i++) {
                data.setValue(i, properties[currentElement][i]);
            }
            dataList.add(data);

            min = 100000000;

            for(int i = 0; i < TOTAL_CLUSTERS; i++) {
                distance = getDistance(data, centroids.get(i));
                if(distance < min) {
                    min = distance;
                    cluster = i;
                }
            }
            dataList.get(dataList.size() - 1).setCluster(cluster);

            for(int i = 0; i < TOTAL_CLUSTERS; i++) {
                int[] total = new int[Im.TOTAL_PROPERTIES];
                int inCluster = 0;

                for(int j = 0; j < dataList.size(); j++) {
                    if(dataList.get(j).getCluster() == i) {
                        for(int k = 0; k < Im.TOTAL_PROPERTIES; k++) {
                            total[k] += dataList.get(j).getValue(k);
                        }
                        inCluster++;
                    }
                }

                if(inCluster > 0) {
                    for(int j = 0; j < Im.TOTAL_PROPERTIES; j++) {
                        centroids.get(i).setValue(j, total[j] / inCluster);
                    }
                }
            }

            currentElement++;
        }

        boolean stop = false;

        while(!stop) {
            for (int i = 0; i < TOTAL_CLUSTERS; i++) {
                int[] total = new int[Im.TOTAL_PROPERTIES];
                int inCluster = 0;

                for (int j = 0; j < dataList.size(); j++) {
                    if (dataList.get(j).getCluster() == i) {
                        for (int k = 0; k < Im.TOTAL_PROPERTIES; k++) {
                            total[k] += dataList.get(j).getValue(k);
                            inCluster++;
                        }
                    }
                }

                if (inCluster > 0) {
                    for (int j = 0; j < Im.TOTAL_PROPERTIES; j++) {
                        centroids.get(i).setValue(j, total[j] / inCluster);
                    }
                }
            }

            stop = true;

            for (int i = 0; i < dataList.size(); i++) {
                min = 100000000;

                for (int j = 0; j < TOTAL_CLUSTERS; j++) {
                    distance = getDistance(dataList.get(i), centroids.get(j));
                    if (distance < min) {
                        min = distance;
                        cluster = j;
                    }
                }

                dataList.get(i).setCluster(cluster);
                if (dataList.get(i).getCluster() != cluster) {
                    dataList.get(i).setCluster(cluster);
                    stop = false;
                }
            }
        }

        return dataList;
    }

    private double getDistance(Data data, Centroid centroid) {
        double out = 0;

        for(int i = 0; i < Im.TOTAL_PROPERTIES; i++) {
            out += Math.pow((centroid.getValue(i) - data.getValue(i)), 2);
        }
        out = Math.sqrt(out);

        return out;
    }

    private class Centroid {
        private ArrayList<Double> values;

        public Centroid(int size) {
            values = new ArrayList<>();
            for(int i = 0; i < size; i++) {
                values.add(0.0);
            }
        }

        public void setValue(int pos, double value) {
            values.set(pos, value);
        }

        public double getValue(int pos) {
            return values.get(pos);
        }
    }

    public class Data {
        private ArrayList<Double> data;
        private int cluster;

        public Data(int size) {
            data = new ArrayList<>();
            for(int i = 0; i < size; i++) {
                data.add(0.0);
            }
        }

        public void setValue(int pos, double value) {
            data.set(pos, value);
        }

        public double getValue(int pos) {
            return data.get(pos);
        }

        public void setCluster(int cluster) {
            this.cluster = cluster;
        }

        public int getCluster() {
            return cluster;
        }
    }
}