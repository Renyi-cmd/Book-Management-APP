package com.example.bookmanagement;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AddBookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_add_book);

        Button check = findViewById(R.id.search);
        Button add = findViewById(R.id.addToShelf);
        EditText input = findViewById(R.id.inputBookName);
        ListView resDisp = findViewById(R.id.resultDisp);

        resDisp.setAdapter(new ArrayAdapter<String>(AddBookActivity.this, R.layout.list_item, new String[]{"点击按钮以开始查询"}));
        add.setEnabled(false);

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ISBN = input.getText().toString();
                String urlStr = "https://api.jike.xyz/situ/book/isbn/"+ISBN+"?apikey=12754.9a34174fcd10d70b73859dd91d7d2d83.378efba1193c670e72199ec7a4613718";
                ArrayAdapter<String> adapter = null;
                try {
                    URL url = new URL(urlStr);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(3000);
                    connection.setReadTimeout(3000);
                    int respCode = connection.getResponseCode();
                    if (respCode == connection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                        byte[] bytes = new byte[1024];
                        int length = 0;
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
                            adapter = new ArrayAdapter<String>(AddBookActivity.this, R.layout.list_item, results);
                            Log.i("result", results[0]);
                            resDisp.setAdapter(adapter);
                            add.setEnabled(true);
                        } else {
                            String[] errMsg = new String[]{"错误！\n" + jObject.getString("msg")};
                            Log.i("msg", errMsg[0]);
                            adapter = new ArrayAdapter<String>(AddBookActivity.this, R.layout.list_item, errMsg);
                        }
                    }
                } catch (Exception e) {
                    String[] errMsg = new String[]{"错误！\n" + e.toString()};
                    Log.e("err", e.toString());
                    adapter = new ArrayAdapter<String>(AddBookActivity.this, R.layout.list_item, errMsg);
                }
                if(adapter != null) {
                    resDisp.setAdapter(adapter);
                }
            }
        });
    }
}