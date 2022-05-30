package com.example.bookmanagement;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BookListActivity extends AppCompatActivity implements MyAdapter.ItemClickListener {

    MyAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        ArrayList<String> animalNames = new ArrayList<>();
        animalNames.add("Horse");
        animalNames.add("Cow");
        animalNames.add("Camel");
        animalNames.add("Sheep");
        animalNames.add("Goat");
        RecyclerView recyclerView = findViewById(R.id.disp);
        recyclerView.setLayoutManager(new LinearLayoutManager(BookListActivity.this));
        adapter = new MyAdapter(BookListActivity.this, animalNames);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }
    @Override
    public void onItemClick(View view, int position) {
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle(adapter.getItem(position))
                .setMessage("是否删除此书？")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(BookListActivity.this, "删除" + adapter.getItem(position), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("否", null)
                .create();
        ad.show();

    }
}
