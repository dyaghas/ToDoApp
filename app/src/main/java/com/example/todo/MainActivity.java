package com.example.todo;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase mDatabase;
    private DatabaseReference ref;

    private FirebaseAuth mAuth;

    private ArrayList<String> items;
    private ArrayAdapter<String> itemsAdapter;
    private ListView listView;
    private Button btnAdd;

    private EditText inputText;
    private EditText inputMonth;
    private EditText inputDay;
    private EditText inputYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        btnAdd = findViewById(R.id.buttonAdd);

        inputText = findViewById(R.id.todoEditText);

        inputMonth = findViewById(R.id.editTextMonth);
        inputDay = findViewById(R.id.editTextDay);
        inputYear = findViewById(R.id.editTextYear);

        items = new ArrayList<>();
        itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(itemsAdapter);
        setUpListViewListener();

        //Firebase authentication
        mAuth = FirebaseAuth.getInstance();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem(view);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //goes to register activity if no logged user is detected in the device
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        } else {
            //Firebase realtime database
            ref = FirebaseDatabase.getInstance().getReference().child(mAuth.getUid());

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    items.clear();
                    for (DataSnapshot i : snapshot.getChildren()) {
                        items.add(i.getValue().toString());
                    }
                    itemsAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    private void setUpListViewListener() { //method that removes an item if a long click happens
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Context context = getApplicationContext();
                Toast.makeText(context, "Item removed", Toast.LENGTH_LONG).show();

                items.remove(i); //remove the item in the list with the index i
                itemsAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    private void addItem(View view) {
        String itemText = inputText.getText().toString(); //extracts the string from the editText
        String itemDate = inputMonth.getText().toString()+"-"+inputDay.getText().toString()+"-"+
                inputYear.getText().toString();

        if(!(itemText.equals("")) && !(itemDate.equals("--"))) { //verifies if the string is not empty
                itemsAdapter.add(itemText + "   " + itemDate);
                inputText.setText(""); //resets the string inside the input so it can be used again
                inputMonth.setText("");
                inputDay.setText("");
                inputYear.setText("");
        } else {
            Toast.makeText(getApplicationContext(), "Cannot add empty text", Toast.LENGTH_SHORT).show();
        }

        //Firebase realtime database
        ref.setValue(items);
    }
}