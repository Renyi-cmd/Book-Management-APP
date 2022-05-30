package com.example.bookmanagement;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class AddBookActivity extends AppCompatActivity {

    private String ISBN;
    private Button add;
    private EditText input;
    private ListView resDisp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_add_book);

        Button check = findViewById(R.id.search);
        add = findViewById(R.id.addToShelf);
        Button scan = findViewById(R.id.scan);
        input = findViewById(R.id.inputBookName);
        resDisp = findViewById(R.id.resultDisp);

        resDisp.setAdapter(new ArrayAdapter<>(AddBookActivity.this, R.layout.list_item, new String[]{"点击按钮以开始查询"}));
        add.setEnabled(false);

        scan.setOnClickListener(view -> {
            IntentIntegrator integrator = new IntentIntegrator(AddBookActivity.this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
            integrator.setPrompt("扫描条形码");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(false);
            integrator.setBarcodeImageEnabled(true);
            integrator.initiateScan();
        });

        check.setOnClickListener(view -> exec());
    }

    public void exec() {
        ISBN = input.getText().toString();
        String urlStr = "https://api.jike.xyz/situ/book/isbn/"+ISBN+"?apikey=12754.9a34174fcd10d70b73859dd91d7d2d83.378efba1193c670e72199ec7a4613718";
        ArrayAdapter<String> adapter = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            int respCode = connection.getResponseCode();
            if (respCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                byte[] bytes = new byte[1024];
                int length;
                while ((length = inputStream.read(bytes)) != -1) {
                    arrayOutputStream.write(bytes, 0, length);
                    arrayOutputStream.flush();
                }
                String responseStr = arrayOutputStream.toString();
                Log.d("response", responseStr);
                JSONObject jObject = new JSONObject((responseStr));
                if (jObject.getInt("ret") == 0) {
                    JSONObject dataObject = new JSONObject(jObject.getString("data"));
                    String[] results = new String[]{
                            "书名：" + dataObject.getString("name"),
                            "作者：" + dataObject.getString("author"),
                            "出版社：" + dataObject.getString("publishing"),
                            "出版年份：" + dataObject.getString("published"),
                            "简介：" + dataObject.getString("description")
                    };
                    adapter = new ArrayAdapter<>(AddBookActivity.this, R.layout.list_item, results);
                    Log.i("result", results[0]);
                    resDisp.setAdapter(adapter);
                    add.setEnabled(true);
                    add.setOnClickListener(view -> {
                        Context context = AddBookActivity.this;
                        try {
                            StringBuilder resultStr = new StringBuilder();
                            for (String s : results) {
                                resultStr.append(s).append("\n");
                            }
                            String fileStr = readFile();
                            if(!fileStr.contains(resultStr.toString())) {
                                checkNeedPermissions();
                                OutputStreamWriter osw = new OutputStreamWriter(context.openFileOutput("RenyiBookStorage.txt", Context.MODE_APPEND));
                                osw.write(resultStr + "-----END-OF-ONE-BOOK-----\n");
                                osw.close();
                                Toast.makeText(AddBookActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AddBookActivity.this, "此书目已存在", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("Write", e.toString());
                        }
                    });
                } else {
                    String[] errMsg = new String[]{"错误！\n" + jObject.getString("msg")};
                    Log.i("msg", errMsg[0]);
                    adapter = new ArrayAdapter<>(AddBookActivity.this, R.layout.list_item, errMsg);
                    add.setEnabled(false);
                }
            }
        } catch (Exception e) {
            String[] errMsg = new String[]{"错误！\n" + e};
            Log.e("err", e.toString());
            adapter = new ArrayAdapter<>(AddBookActivity.this, R.layout.list_item, errMsg);
            add.setEnabled(false);
        }
        if(adapter != null) {
            resDisp.setAdapter(adapter);
        }
    }

    public String readFile() {
        Context context = AddBookActivity.this;
        String ret = "";
        try {
            InputStream is = context.openFileInput("RenyiBookStorage.txt");
            if(is != null) {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String recvStr;
                StringBuilder strBuilder = new StringBuilder();
                while ((recvStr = br.readLine()) != null) {
                    strBuilder.append("\n").append(recvStr);
                }
                is.close();
                ret = strBuilder.toString();
            }
        } catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }
        return ret;
    }

    private void checkNeedPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1);
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode,@Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "扫码取消", Toast.LENGTH_SHORT).show();
            } else {
                ISBN = result.getContents();
                input.setText(ISBN);
                exec();
                Toast.makeText(this, "扫描成功", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}