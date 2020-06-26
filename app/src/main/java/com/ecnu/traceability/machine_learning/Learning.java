package com.ecnu.traceability.machine_learning;

import android.util.Log;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.HTTPUtils;
import com.ecnu.traceability.Utils.OneNetDeviceUtils;
import com.ecnu.traceability.judge.JudgeActivity;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Learning {
    private static final String TAG = "Learning";

    private static final int[] sampleShape = {1, 6};//数据集格式是一行三列不包括label
    // training related
    private static boolean isTraining = false;
    private static boolean isLoading = true;
    private static boolean isGenData = true;
    private double[][] samples;
    private int[] labels;

    public void propocessData(List<LearningData> dataList) {
        samples = new double[dataList.size()][6];
        labels = new int[dataList.size()];
        for (int i = 0; i < dataList.size(); i++) {
            LearningData ld = dataList.get(i);
            labels[i] = ld.getLabel();
            samples[i][0] = ld.getSignal_strength();
            samples[i][1] = ld.getAvg_mac_time();
            samples[i][2] = ld.getTrans_count();
            samples[i][3] = ld.getAvg_seat_differ();
            samples[i][4] = ld.getGps_distance();
            samples[i][5] = ld.getAvg_gps_time();
        }
    }

    public List<LearningData> getLearningDataFromDB(DBHelper dbHelper) {
        List<LearningData> dataList = dbHelper.getSession().getLearningDataDao().loadAll();
        if (null != dataList) {
            return dataList;
        } else {
            return new ArrayList<>();
        }
    }


    /**
     * 更新联邦学习数据库中最后一条数据
     *
     * @param isInfection 是否感染
     * @param dbHelper
     */
    public void updateLearningData(boolean isInfection, DBHelper dbHelper) {
        //查询最近的一条记录
        List<LearningData> dataList = dbHelper.getSession().getLearningDataDao().queryBuilder()
                .orderDesc(LearningDataDao.Properties.Date)
                .limit(1)
                .list();
        //如果记录不为空
        if (null != dataList && dataList.size() > 0) {

            LearningData latestData = dataList.get(0);
            int label = latestData.getLabel();
            Log.i(TAG, "原来的label是" + label);
            if (isInfection) {//如果确认感染
                latestData.setLabel(JudgeActivity.MAX_RISK_LEVEL);
                Log.i(TAG, "修改后的label是" + JudgeActivity.MAX_RISK_LEVEL);
                dbHelper.getSession().getLearningDataDao().update(latestData);//更新风险
            }
        }
    }

    public void startLearning() {

        new Thread(() -> {
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
                Log.e(TAG, "数据集的数量是："+String.valueOf(idx));
                if(idx==0){
                    Log.e(TAG, "数据不足停止训练" );
                    return;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


            //先加载模型再训练然后上传模型到服务器
            try {
                Log.e("loading model", "正在尝试加载模型");
                TrainModel.model = ModelSerializer.restoreMultiLayerNetwork(
                        TrainModel.locateToLoadModel, false);
                Log.e("loading model", "已经尝试加载模型");
                Log.e("loading model", "正在尝试训练模型");
                MultiLayerNetwork trainedmodel = TrainModel.TrainingModel(TrainModel.locateToSaveDataSet);
                Log.e("loading model", "模型训练完成");
                String macAddress = OneNetDeviceUtils.macAddress;
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

    public void downloadModel() {
        HTTPUtils.download();//测试下载最新的模型
    }


//    private class AsyncTaskRunner extends AsyncTask<Void, Integer, Integer> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected Integer doInBackground(Void... params) {
//            return 0;
//        }
//
//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            super.onProgressUpdate(values);
//        }
//
//        //This block executes in UI when background thread finishes
//        //This is where we update the UI with our classification results
//        @Override
//        protected void onPostExecute(Integer result) {
//            super.onPostExecute(result);
//
//        }
//
//    }

//    private class AsyncTaskGenData extends AsyncTaskRunner {
//
//        public AsyncTaskGenData() {
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected Integer doInBackground(Void... params) {
//            //将数据集写入文件
//            try {
//                BufferedWriter br = new BufferedWriter(new FileWriter(
//                        TrainModel.locateToSaveDataSet.toString(), true));
//                StringBuilder sb = new StringBuilder();
//                int idx = 0;
//                for (double[] element : samples) {
//                    sb.append(String.format("%d", labels[idx]));//第一个是label
//                    for (double e : element) {
//                        sb.append(String.format(",%.4f", e));//这些是数据
//                    }
//                    br.write(sb.toString());
//                    br.newLine();
//                    idx++;
//                }
//                br.close();
//
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return 0;
//        }
//
//        @Override
//        protected void onPostExecute(Integer result) {
//            super.onPostExecute(result);
//            isGenData = false;
//            Log.i("data gen", "数据写入完毕");
//        }
//    }

//    public void infer(double[] sample) {
//        //等待生成数据和加载模型
//        new Thread(() -> {
//            try {
//                if (TrainModel.model == null) {
//                    Log.i("Learning", "model is not loaded yet");
//                    Log.e("loading model", "正在尝试加载模型");
//                    TrainModel.model = ModelSerializer.restoreMultiLayerNetwork(
//                            TrainModel.locateToLoadModel, false);
//                    Log.e("loading model", "已经尝试加载模型");
//                }
//
//                INDArray sample_to_infer = Nd4j.create(ArrayUtil.flattenDoubleArray(sample), sampleShape);
//                INDArray predicted = TrainModel.model.output(sample_to_infer, false);
//                INDArray index = predicted.argMax();
//                int[] pl = index.toIntVector();
//                int result = pl[0];
//                Log.i("federated learning", "推断结果是" + result);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }).start();
//    }
//
//    private class AsyncTaskTrainModel extends AsyncTaskRunner {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected Integer doInBackground(Void... params) {
//            // run training process here
//            try {
//                if (TrainModel.locateToSaveDataSet.length() == 0) {
//                    // nothing to train
//                    return 0;
//                }
//                Log.e("loading model", "正在尝试训练模型");
//                MultiLayerNetwork trainedmodel = TrainModel.TrainingModel(TrainModel.locateToSaveDataSet);
//                Log.e("loading model", "模型训练完成");
//
//                ModelSerializer.writeModel(trainedmodel, new File(TrainModel.dir, TrainModel.id + "_updated_model.zip"), false);
//                TrainModel.model = trainedmodel;
//                Log.e("loading model", "模型已经写入");
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return 0;
//        }
//
//        //This block executes in UI when background thread finishes
//        //This is where we update the UI with our classification results
//        @Override
//        protected void onPostExecute(Integer result) {
//            super.onPostExecute(result);
//            Log.e("training complete", "模型训练完毕");
//            isGenData = true;//复位
//            isLoading = true;//复位
//            isTraining = false;
//        }
//    }


}
