package com.ecnu.traceability.machine_learning;

import android.os.Environment;
import android.util.Log;

import com.ecnu.traceability.BaseActivity;

import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.net.ssl.HttpsURLConnection;

public class TensorflowActivity extends BaseActivity {

    private static final String TAG = "TensorflowActivity";
    Graph graph = new Graph();
    Session sess = new Session(graph);

    public void test(){
        InputStream inputStream;
        byte[] graphDef = new byte[0];
        try {
            inputStream = getAssets().open("FINAL_GRAPH.pb");
            byte[] buffer = new byte[inputStream.available()];
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            graphDef = output.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }


        graph.importGraphDef(graphDef);
        Tensor<String> checkpointPrefix = Tensors.create("Path to checkpoint.ckpt");

        sess.runner().feed("save/Const", checkpointPrefix).addTarget("save/restore_all").run();
        sess.runner().addTarget("init").run();
    }

    private float predict(float[][] features) {
        Tensor input = Tensor.create(features);
        float[][] output = new float[1][1];
        Tensor op_tensor = sess.runner().feed("input", input).fetch("output").run().get(0).expect(Float.class);
        Log.i("Tensor Shape", op_tensor.shape()[0] + ", " + op_tensor.shape()[1]);
        op_tensor.copyTo(output);
        return output[0][0];
    }

    private String train(float[][] features, float[] label, int epochs) {
        Tensor x_train = Tensor.create(features);
        Tensor y_train = Tensor.create(label);
        int ctr = 0;
        while (ctr < epochs) {
            sess.runner().feed("input", x_train).feed("target", y_train).addTarget("train_op").run();
            ctr++;
        }
        Log.i("TAG", "Model Trained");
        return "Model Trained";
    }

    public ArrayList<ArrayList<Tensor<?>>> getWeights() {
        ArrayList<Tensor<?>> w1 = (ArrayList<Tensor<?>>) sess.runner().fetch("w1:0").run();
        ArrayList<Tensor<?>> b1 = (ArrayList<Tensor<?>>) sess.runner().fetch("b1:0").run();
        ArrayList<Tensor<?>> w2 = (ArrayList<Tensor<?>>) sess.runner().fetch("w2:0").run();
        ArrayList<Tensor<?>> b2 = (ArrayList<Tensor<?>>) sess.runner().fetch("b2:0").run();
        ArrayList<Tensor<?>> w3 = (ArrayList<Tensor<?>>) sess.runner().fetch("wo:0").run();
        ArrayList<Tensor<?>> b3 = (ArrayList<Tensor<?>>) sess.runner().fetch("bo:0").run();

        ArrayList<ArrayList<Tensor<?>>> ls = new ArrayList<>();
        ls.add(w1);
        ls.add(b1);
        ls.add(w2);
        ls.add(b2);
        ls.add(w3);
        ls.add(b3);
        Log.i("Shapes: ", w1.get(0).shape()[0] + ", " + w1.get(0).shape()[1]);
        Log.i("Shapes: ", b1.get(0).shape()[0] + ", " + b1.get(0).shape()[1]);
        Log.i("Shapes: ", w2.get(0).shape()[0] + ", " + w2.get(0).shape()[1]);
        Log.i("Shapes: ", b2.get(0).shape()[0] + ", " + b2.get(0).shape()[1]);
        Log.i("Shapes: ", w3.get(0).shape()[0] + ", " + w3.get(0).shape()[1]);
        Log.i("Shapes: ", b3.get(0).shape()[0] + ", " + b3.get(0).shape()[1]);

        return ls;
    }


    public void finalSave() {
        ArrayList<ArrayList<Tensor<?>>> at = getWeights();
        int ctr = 0;
        ArrayList<float[]> diff = new ArrayList<>();
        for (int x = 0; x < 6; x++) {

//            float[] aw = flattenedWeight(at.get(x).get(0));
            float[] aw =null;
            diff.add(aw);
            for(float[] f: diff)
            {
                for(float z: f)
                {
                    if(z == 0)
                    {
                        ctr++;
                    }
                }

            }
        }
        Log.i("COUNTER: ", String.valueOf(ctr));
        save(diff);
    }



    public void save(ArrayList<float[]> diff) {

        float[] d1 = diff.get(0);
        float[] d2 = diff.get(1);
        float[] d3 = diff.get(2);
        float[] d4 = diff.get(3);
        float[] d5 = diff.get(4);
        float[] d6 = diff.get(5);
        int l1 = diff.get(0).length;
        int l2 = diff.get(1).length;
        int l3 = diff.get(2).length;
        int l4 = diff.get(3).length;
        int l5 = diff.get(4).length;
        int l6 = diff.get(5).length;
        int ctr = 0;
        int i = 0;
        int j = 0;
        float[] result = new float[l1 + l2 + l3 + l4 + l5 + l6];
        for(i = 0, j = 0; j < l1; i++, j++)
        {
            result[i] = d1[j];
        }
        for(int k = 0; k < l2; i++, k++)
        {
            result[i] = d2[k];
        }
        for(int l = 0; l < l3; i++, l++)
        {
            result[i] = d3[l];
        }
        for(int m = 0; m < l4; i++, m++)
        {
            result[i] = d4[m];
        }
        for(int n = 0; n < l5; i++, n++)
        {
            result[i] = d5[n];
        }
        for(int o = 0; o < l6; i++, o++)
        {
            result[i] = d6[o];
        }
        for(float x: result)
        {
            if(x == 0.0)
                ctr++;
        }
        Log.i("COUNTER_A: ", String.valueOf(ctr));
        Log.i("Result Length:  ", String.valueOf(ctr));

        saveWeights(result, "Weights.bin");
    }

    //Divide by distance to weight weights
    public void saveWeights(float[] diff, String name) {
        byte[] byteArray = new byte[diff.length * 4];
        // wrap the byte array to the byte buffer
        ByteBuffer byteBuf = ByteBuffer.wrap(byteArray);
        for(byte b: byteBuf.array())
        {
            Log.i("ByteBuffer: ", String.valueOf(b));

        }
        // create a view of the byte buffer as a float buffer
        FloatBuffer floatBuf = byteBuf.asFloatBuffer();


        // now put the float array to the float buffer,
        // it is actually stored to the byte array
        floatBuf.put(diff);
        saveFile(byteArray, name);
    }

    public void saveFile(byte[] byteArray, String name) {
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), name);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.e("Error", "Error: FILE" + "File not Created!");
            }
        }
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            Log.i("TAG", "Error: FILE" + "File not found!");
        }
        try {
            os.write(byteArray);
            Log.i("TAG", "FileWriter" + "File written successfully");
        } catch (IOException e) {
            Log.i("TAG", "Error: FILE" + "File not written!");
        }
    }


    private String isModelUpdated() {
        URL url = null;
        try {
            url = new URL("https://aqifedserver.herokuapp.com/isModelUpdated");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HttpsURLConnection urlConnection = null;

        try {
            if (url != null) {
                urlConnection = (HttpsURLConnection) url.openConnection();
            }
            if (urlConnection != null) {
                urlConnection.setRequestMethod("GET");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String response = null;
        try {
            if (urlConnection != null) {
                urlConnection.connect();
            }
            InputStream in = null;
            if (urlConnection != null) {
                in = new BufferedInputStream(urlConnection.getInputStream());
                response = readStream(in);
                Log.i("Response", response);
                in.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();

            }
        }
        return response;
    }
    private String readStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        inputStream.close();
        return output.toString();
    }

//    private String uploadWeights() throws MalformedURLException {
//        URL url;
//        HttpsURLConnection conn;
//
//        url = new URL("Link to server");
//
//        try {
//            conn = (HttpsURLConnection) url.openConnection();
//            conn.setDoOutput(true); // Allow Outputs
//            uploadWeight(conn);
//            conn.disconnect();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return serverResponseMessage;
//    }

//    private void uploadWeight(HttpURLConnection conn) {
//        try {
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Connection", "Keep-Alive");
//            conn.setRequestProperty("ENCTYPE",
//                    "multipart/form-data");
//            conn.setRequestProperty("Content-Type",
//                    "multipart/form-data;boundary=" + boundary);
//            conn.setRequestProperty("file", "weights");
//
//            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
//            dos.writeBytes(twoHyphens + boundary + lineEnd);
//            dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""
//                    + "weights.bin" + "\"" + lineEnd);
//
//            dos.writeBytes(lineEnd);
//            //Write File
//            dos.write(readArrayFromDevice());
//
//            dos.writeBytes(lineEnd);
//            dos.writeBytes(twoHyphens + boundary + twoHyphens
//                    + lineEnd);
//            int serverResponseCode = conn.getResponseCode();
//            String serverResponseMessage = conn.getResponseMessage();
//            Log.i("Response Message: ", serverResponseMessage);
//            Log.i("Response Code: ", String.valueOf(serverResponseCode));
//            dos.flush();
//            dos.close();
//        } catch (ProtocolException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            conn.disconnect();
//        }
//    }
//
//    private byte[] readArrayFromDevice() {
//        int size = (int) file.length();
//        byte[] bytes = new byte[size];
//        try {
//            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
//            buf.read(bytes, 0, bytes.length);
//            buf.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Log.i("Bytes:", String.valueOf(bytes.length));
//        return bytes;
//    }
//
//    private String downloadFiles() {
//        String response = "Failed";
//        HttpURLConnection conn;
//
//        try {
//            URL url = new URL("https://aqifedserver.herokuapp.com/getGlobalModel");
//            conn = (HttpsURLConnection) url.openConnection();
//            conn.setRequestMethod("GET");
//            InputStream is = conn.getInputStream();
//
//            DataInputStream dis = new DataInputStream(is);
//
//            byte[] buffer = new byte[1024];
//            int length;
//
//            FileOutputStream fos = new FileOutputStream(new File(weakActivity.get().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "/checkpoint.zip"));
//            while ((length = dis.read(buffer)) > 0) {
//                fos.write(buffer, 0, length);
//            }
//            fos.close();
//            unzip(weakActivity.get().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/checkpoint.zip", weakActivity.get().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
//            response = "Download Succeeded";
//            int serverResponseCode = conn.getResponseCode();
//            String serverResponseMessage = conn.getResponseMessage();
//            Log.i("Response Message: ", serverResponseMessage);
//            Log.i("Response Code: ", String.valueOf(serverResponseCode));
//            dis.close();
//            conn.disconnect();
//        } catch (MalformedURLException mue) {
//            Log.e("Error", mue.getMessage());
//        } catch (IOException ioe) {
//            Log.e("Error", ioe.getMessage());
//        } catch (SecurityException se) {
//            Log.e("Error", se.getMessage());
//        }
//        return response;
//    }
//    private void unzip(String zipFile, String location) throws IOException {
//        try {
//            File f = new File(location);
//            if (!f.isDirectory()) {
//                f.mkdirs();
//            }
//            ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
//            try {
//                ZipEntry ze = null;
//                while ((ze = zin.getNextEntry()) != null) {
//                    String path = location + File.separator + ze.getName();
//
//                    if (ze.isDirectory()) {
//                        File unzipFile = new File(path);
//                        if (!unzipFile.isDirectory()) {
//                            unzipFile.mkdirs();
//                        }
//                    } else {
//                        FileOutputStream fout = new FileOutputStream(path, false);
//
//                        try {
//                            for (int c = zin.read(); c != -1; c = zin.read()) {
//                                fout.write(c);
//                            }
//                            zin.closeEntry();
//                        } finally {
//                            fout.close();
//                        }
//                    }
//                }
//            } finally {
//                zin.close();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.e(TAG, "Unzip exception", e);
//        }
//    }
}
