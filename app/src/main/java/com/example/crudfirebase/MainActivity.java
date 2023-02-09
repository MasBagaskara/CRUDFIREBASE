package com.example.crudfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.crudfirebase.adapter.UserAdapter;
import com.example.crudfirebase.model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FirebaseUser firebaseUser;
    private TextView textName;
    private Button btn_Logout,btn_add;
    private RecyclerView recyclerView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Users> list =new ArrayList<>();
    private UserAdapter userAdapter;

    //memanggil subclass getData
    protected void onStart(){
        super.onStart();
        getData();
    }
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textName = findViewById(R.id.Name);
        btn_Logout = findViewById(R.id.btn_Logout);
        btn_add = findViewById(R.id.btn_add);
        recyclerView = findViewById(R.id.recycler_view);
        //Update Data
        userAdapter = new UserAdapter(getApplicationContext(), list);
        userAdapter.setDialog(new UserAdapter.Dialog() {
            @Override
            public void onClick(int pos) {
                final CharSequence[] dialogItem = {"Edit","Hapus"};
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setItems(dialogItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        switch (i){
                            case 0:
                                Intent intent = new Intent(getApplicationContext(),EditorActivity.class);
                                intent.putExtra("id",list.get(pos).getId());
                                intent.putExtra("fullname",list.get(pos).getFullname());
                                intent.putExtra("nickname",list.get(pos).getNickname());
                                intent.putExtra("avatar",list.get(pos).getAvatar());
                                startActivity(intent);
                                break;
                            case 1:
                                deleteData (list.get(pos).getId(),
                                            list.get(pos).getAvatar());
                                break;
                        }
                    }
                });
                dialog.show();
            }
        });

        //Recycler Veiw Data
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView.ItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(userAdapter);


       //Koneksi Database Firebase
                    firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser!=null){
            textName.setText(firebaseUser.getDisplayName());
        }else{
            textName.setText("Login Gagal");
        }

        btn_add.setOnClickListener(v ->{
            startActivity(new Intent(getApplicationContext(),EditorActivity.class));
        });
        //Tombol Logout
        btn_Logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            finish();
        });
    }
    //Subclass Get Data
    private void getData(){
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        list.clear();
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                Users users = new Users(document.getString("fullname"),document.getString("nickname"),document.getString("avatar"));
                                users.setId(document.getId());
                                list.add(users);
                            }
                            userAdapter.notifyDataSetChanged();
                        }else{
                            Toast.makeText(getApplicationContext(), "Data Gagal Di Ambil", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    //Subclass Delete Data
    private void deleteData(String id, String avatar){
        db.collection("users").document(id)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "Data gagal di hapus", Toast.LENGTH_SHORT).show();
                        }else{ // Mengapus Data Foto di Dalam Storage
                            FirebaseStorage.getInstance().getReferenceFromUrl(avatar).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    getData();
                                }
                            });
                        }
                    }
                });
    }
}