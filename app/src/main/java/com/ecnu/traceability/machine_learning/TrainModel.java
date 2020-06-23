package com.ecnu.traceability.machine_learning;

import android.os.Environment;

import com.ecnu.traceability.Utils.OneNetDeviceUtils;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.transferlearning.FineTuneConfiguration;
import org.deeplearning4j.nn.transferlearning.TransferLearning;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;

import java.io.File;

public class TrainModel {

    public static final File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    public static final File syncDir = Environment.getExternalStoragePublicDirectory("DropsyncFiles");
    public static final File locateToSaveDataSet = new File(dir, "COVID19.csv");
    public static final File locateToLoadModel = new File(dir, "updatedModel.zip");
    public static String id = OneNetDeviceUtils.macAddress;

//    private static final int numHiddenNodes = 1000;
//    private static final int numOutputs = 6;
    private static final int nEpochs = 10;

    public static boolean isTransferred = false;

    public static FineTuneConfiguration fineTuneConf = new FineTuneConfiguration.Builder()
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .updater(new Nesterovs(5e-5))
            .seed(100)
            .build();

    public static MultiLayerNetwork model = null;

    public static MultiLayerNetwork TrainingModel(File file) {
        MultiLayerNetwork transferred_model = model;
        if (!isTransferred) {
            transferred_model = new TransferLearning.Builder(model)
                    .fineTuneConfiguration(fineTuneConf)
                    .setFeatureExtractor(1)
                    .build();
            isTransferred = true;
        }
        RecordReader rr = new CSVRecordReader();
        try {
            rr.initialize(new FileSplit(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        DataSetIterator trainIter = new RecordReaderDataSetIterator(rr, 4, 0, 3);
        transferred_model.fit(trainIter, nEpochs);
//        transferred_model.addListeners(new ScoreIterationListener(1));
        return transferred_model;
    }

}
