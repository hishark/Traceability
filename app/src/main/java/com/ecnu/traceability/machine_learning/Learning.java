package com.ecnu.traceability.machine_learning;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.ecnu.traceability.Utils.HTTPUtils;
import com.ecnu.traceability.Utils.OneNetDeviceUtils;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.util.ArrayUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Learning {

    private static final int[] sampleShape = {1, 6};//数据集格式是一行三列不包括label
    // training related
    private static boolean isTraining = false;
    private static boolean isLoading = true;
    private static boolean isGenData = true;
    private double[][] samples;
    private int[] labels;

    public void setRawDataLabel(int[] labels) {
        this.labels = labels;
    }

    public void propocessData(Bundle bundle) {
        double avgStrength = bundle.getDouble("avgStrength", 0);
        double bluetoothTime = bundle.getDouble("bluetoothTime", 0);
        double transCount = bundle.getDouble("transCount", 0);
        double avgSeatDiff = bundle.getDouble("avgSeatDiff", 0);
        double avgDistance = bundle.getDouble("avgDistance", 0);
        double gpsTime = bundle.getDouble("gpsTime", 0);

        Log.i("Federated Learning", "================================");
        Log.i("avgStrength", String.valueOf(avgStrength));
        Log.i("bluetoothTime", String.valueOf(bluetoothTime));
        Log.i("transCount", String.valueOf(transCount));
        Log.i("avgSeatDiff", String.valueOf(transCount == 0 ? 10 : avgSeatDiff * 0.01));
        Log.i("avgDistance", String.valueOf(avgDistance));
        Log.i("gpsTime", String.valueOf(gpsTime));
        Log.i("Federated Learning", "================================");

        double[][] dataArray = {{avgStrength, bluetoothTime, transCount, transCount == 0 ? 10 : (avgSeatDiff * 0.01), avgDistance, gpsTime}};
        this.samples = dataArray;

        AsyncTaskRunner genDataRunner = new AsyncTaskGenData();
        genDataRunner.execute();
    }

    private class AsyncTaskRunner extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        //This block executes in UI when background thread finishes
        //This is where we update the UI with our classification results
        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

        }

    }


    private class AsyncTaskGenData extends AsyncTaskRunner {

        public AsyncTaskGenData() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            //将数据集写入文件
            try {
                BufferedWriter br = new BufferedWriter(new FileWriter(
                        TrainModel.locateToSaveDataSet.toString(), true));
                StringBuilder sb = new StringBuilder();
                int idx = 0;
                for (double[] element : samples) {
                    sb.append(String.format("%d", labels[idx]));//第一个是label
                    for (double e : element) {
                        sb.append(String.format(",%.4f", e));//这些是数据
                    }
                    br.write(sb.toString());
                    br.newLine();
                    idx++;
                }
                br.close();

                //HTTPUtils.download();//测试下载最新的模型

            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            isGenData = false;
            Log.i("data gen", "数据写入完毕");
        }
    }

    private class AsyncTaskTrainModel extends AsyncTaskRunner {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            // run training process here
            try {
                if (TrainModel.locateToSaveDataSet.length() == 0) {
                    // nothing to train
                    return 0;
                }
                Log.e("loading model", "正在尝试训练模型");
                MultiLayerNetwork trainedmodel = TrainModel.TrainingModel(TrainModel.locateToSaveDataSet);
                Log.e("loading model", "模型训练完成");

                ModelSerializer.writeModel(trainedmodel, new File(TrainModel.dir, TrainModel.id + "_updated_model.zip"), false);
                TrainModel.model = trainedmodel;
                Log.e("loading model", "模型已经写入");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }

        //This block executes in UI when background thread finishes
        //This is where we update the UI with our classification results
        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            Log.e("training complete", "模型训练完毕");
            isGenData = true;//复位
            isLoading = true;//复位
            isTraining = false;
        }
    }

    private class AsyncTaskLoadModel extends AsyncTaskRunner {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                Log.e("loading model", "正在尝试加载模型");

                //                InputStream is = getResources().openRawResource(R.raw.trained_har_nn);
                TrainModel.model = ModelSerializer.restoreMultiLayerNetwork(
                        TrainModel.locateToLoadModel, false);
                Log.e("loading model", "已经尝试加载模型");

            } catch (IOException e) {
                Log.e("loading model", "加载模型失败");

                e.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            TrainModel.isTransferred = false;
            isLoading = false;
            Log.e("load model", "模型加载完毕");
        }

    }

    public void startLearning() {

        new Thread(() -> {
            try {
                Log.e("loading model", "正在尝试加载模型");
                TrainModel.model = ModelSerializer.restoreMultiLayerNetwork(
                        TrainModel.locateToLoadModel, false);
                Log.e("loading model", "已经尝试加载模型");
                Log.e("loading model", "正在尝试训练模型");
                MultiLayerNetwork trainedmodel = TrainModel.TrainingModel(TrainModel.locateToSaveDataSet);
                Log.e("loading model", "模型训练完成");
                String macAddress= OneNetDeviceUtils.macAddress;
                ModelSerializer.writeModel(trainedmodel, new File(TrainModel.dir, "updatedModel.zip"), false);
                Log.e("loading model", "模型已经写入");
                TrainModel.model = trainedmodel;
                //上传更新后的模型到服务器

                HTTPUtils.upload(new File(TrainModel.dir, "updatedModel.zip"));

            } catch (IOException e) {
                Log.e("loading model", "加载模型失败");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void infer(double[] sample) {
        //等待生成数据和加载模型
        new Thread(() -> {
            try {
                if (TrainModel.model == null) {
                    Log.i("Learning", "model is not loaded yet");
                    Log.e("loading model", "正在尝试加载模型");
                    TrainModel.model = ModelSerializer.restoreMultiLayerNetwork(
                            TrainModel.locateToLoadModel, false);
                    Log.e("loading model", "已经尝试加载模型");
                }

                INDArray sample_to_infer = Nd4j.create(ArrayUtil.flattenDoubleArray(sample), sampleShape);
                INDArray predicted = TrainModel.model.output(sample_to_infer, false);
                INDArray index = predicted.argMax();
                int[] pl = index.toIntVector();
                int result = pl[0];
                Log.i("federated learning", "推断结果是" + result);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
