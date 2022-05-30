package com.example.bookmanagement;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;

public class BookListActivity extends AppCompatActivity implements MyAdapter.ItemClickListener {

    MyAdapter adapter;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        recyclerView = findViewById(R.id.disp);
        recyclerView.setLayoutManager(new LinearLayoutManager(BookListActivity.this));
        refresh();
    }

    public String readFile() {
        Context context = BookListActivity.this;
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

    public void refresh() {
        String ret = readFile();
        if(!ret.equals("")) {
            String[] items = ret.split("-----END-OF-ONE-BOOK-----\n");
            ArrayList<String> books = new ArrayList<>();
            for(String s : items) {
                if(s.contains("简介：")) {
                    books.add(s.substring(0, s.indexOf("简介：")));
                }
            }
            adapter = new MyAdapter(BookListActivity.this, books);
            adapter.setClickListener(BookListActivity.this);
        } else {
            adapter = new MyAdapter(BookListActivity.this, new ArrayList<>(Collections.singletonList("当前暂无书目")));
        }
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除");
        builder.setMessage("是否删除此书？");
        builder.setPositiveButton("是", (dialogInterface, i) -> {
            String ret = readFile();
            String tarBook = adapter.getItem(position);
            String[] books = ret.split(getString(R.string.SEGEND));
            try {
                checkNeedPermissions();
                OutputStreamWriter osw = new OutputStreamWriter(BookListActivity.this.openFileOutput("RenyiBookStorage.txt", Context.MODE_PRIVATE));
                for (String s : books) {
                    if ((!s.startsWith(tarBook)) && s.contains("书名：")) {
                        osw.write(s + "\n");
                        osw.write("-----END-OF-ONE-BOOK-----\n");
                    }
                }
                osw.close();
            } catch (Exception e) {
                Log.e("Write", e.toString());
            }
            Toast.makeText(BookListActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
            refresh();
        });
        builder.setNegativeButton("否", null);
        AlertDialog ad = builder
                .create();
        ad.show();

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
}
