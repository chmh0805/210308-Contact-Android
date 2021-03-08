package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity2";
    private RecyclerView rvContact;
    private ContactAdapter adapter;
    private FloatingActionButton fabAdd;
    private List<Contact> contactList = new ArrayList<>();
    private ContactService contactService;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initData();
        initEvent();
    }

    private void init() {
        rvContact = findViewById(R.id.rv_contact);
        fabAdd = findViewById(R.id.fab_add);
        mToolbar = findViewById(R.id.tb);
        adapter = new ContactAdapter(MainActivity.this);
        rvContact.setAdapter(adapter);
        rvContact.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        contactService = ContactService.retrofit.create(ContactService.class);
    }

    private void initData() {
        Call<CMRespDto<List<Contact>>> call = contactService.findAll();
        call.enqueue(new Callback<CMRespDto<List<Contact>>>() {
            @Override
            public void onResponse(Call<CMRespDto<List<Contact>>> call, Response<CMRespDto<List<Contact>>> response) {
                CMRespDto<List<Contact>> cmRespDto = response.body();

                if (cmRespDto.getCode() == 1) {
                    contactList = cmRespDto.getData();
                    adapter.setItems(contactList);
                }
            }

            @Override
            public void onFailure(Call<CMRespDto<List<Contact>>> call, Throwable t) {
                Log.d(TAG, "findAll() 실패 : " + t.getMessage());
            }
        });
    }

    private void initEvent() {
        // 추가버튼 기능
        fabAdd.setOnClickListener(v -> {
            View dialog = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_add, null);
            AlertDialog.Builder dig = new AlertDialog.Builder(MainActivity.this);

            CircleImageView ivImg = dialog.findViewById(R.id.iv_img);
            ivImg.setImageResource(R.drawable.ic_person);
            EditText etName = dialog.findViewById(R.id.et_name);
            EditText etPhone = dialog.findViewById(R.id.et_phone);
            dig.setTitle("연락처 등록");
            dig.setView(dialog);
            dig.setNegativeButton("닫기", null);
            dig.setPositiveButton("등록", (dialog1, which) -> {
                if (etName.getText().toString().equals("") || etPhone.getText().toString().equals("")) {
                    Toast.makeText(MainActivity.this, "연락처 추가 실패", Toast.LENGTH_SHORT).show();
                } else {
                    Contact contact = new Contact();
                    contact.setName(etName.getText().toString());
                    contact.setPhone(etPhone.getText().toString());
                    Call<CMRespDto<Contact>> call = contactService.save(contact);
                    call.enqueue(new Callback<CMRespDto<Contact>>() {
                        @Override
                        public void onResponse(Call<CMRespDto<Contact>> call, Response<CMRespDto<Contact>> response) {
                            CMRespDto<?> cmRespDto = response.body();
                            if (cmRespDto.getCode() == 1) {
                                Toast.makeText(MainActivity.this, "연락처 추가 완료", Toast.LENGTH_SHORT).show();
                                adapter.addItem(contact);
                                initData();
                            }
                        }

                        @Override
                        public void onFailure(Call<CMRespDto<Contact>> call, Throwable t) {
                            Toast.makeText(MainActivity.this, "연락처 추가 실패", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "save() 실패 : " + t.getMessage());
                        }
                    });
                }
            });
            dig.show();
        });

        // Swipe 삭제 기능
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Call<CMRespDto<Contact>> call = contactService.deleteById(contactList.get(viewHolder.getAdapterPosition()).getId());
                call.enqueue(new Callback<CMRespDto<Contact>>() {
                    @Override
                    public void onResponse(Call<CMRespDto<Contact>> call, Response<CMRespDto<Contact>> response) {
                        CMRespDto<?> cmRespDto = response.body();
                        if (cmRespDto.getCode() == 1) {
                            Toast.makeText(MainActivity.this, "연락처 삭제 성공", Toast.LENGTH_SHORT).show();
                            adapter.removeItem(viewHolder.getAdapterPosition());
                        }
                    }

                    @Override
                    public void onFailure(Call<CMRespDto<Contact>> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "연락처 삭제 실패", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "deleteById() 실패 : " + t.getMessage());
                    }
                });


            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvContact);
    }

    public List<Contact> getContactList() {
        return contactList;
    }

    public void updateContact(Contact contact, int position) {
        View dialog = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_update, null);
        CircleImageView ivImg = dialog.findViewById(R.id.iv_img);
        ivImg.setImageResource(R.drawable.ic_person);
        EditText etName = dialog.findViewById(R.id.et_name);
        EditText etPhone = dialog.findViewById(R.id.et_phone);

        Call<CMRespDto<Contact>> call = contactService.findById(contact.getId());
        call.enqueue(new Callback<CMRespDto<Contact>>() {
            @Override
            public void onResponse(Call<CMRespDto<Contact>> call, Response<CMRespDto<Contact>> response) {
                CMRespDto<?> cmRespDto = response.body();
                if (cmRespDto.getCode() == 1) {
                    Contact contactEntity = (Contact) cmRespDto.getData();
                    etName.setText(contactEntity.getName());
                    etPhone.setText(contactEntity.getPhone());
                }
            }

            @Override
            public void onFailure(Call<CMRespDto<Contact>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "상세보기 실패", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "findById() 실패 : " + t.getMessage());
            }
        });
        AlertDialog.Builder dig = new AlertDialog.Builder(MainActivity.this);
        dig.setTitle("연락처 수정");
        dig.setView(dialog);
        dig.setNegativeButton("삭제", (dialog1, which) -> {
           Call<CMRespDto<Contact>> callDelete = contactService.deleteById(contact.getId());
           callDelete.enqueue(new Callback<CMRespDto<Contact>>() {
               @Override
               public void onResponse(Call<CMRespDto<Contact>> call, Response<CMRespDto<Contact>> response) {
                   CMRespDto<?> cmRespDto = response.body();
                   if (cmRespDto.getCode() == 1) {
                       adapter.removeItem(position);
                       Toast.makeText(MainActivity.this, "연락처 삭제 성공", Toast.LENGTH_SHORT).show();
                   }
               }
               @Override
               public void onFailure(Call<CMRespDto<Contact>> call, Throwable t) {
                   Toast.makeText(MainActivity.this, "연락처 삭제 실패", Toast.LENGTH_SHORT).show();
                   Log.d(TAG, "deleteById() 실패 : " + t.getMessage());
               }
           });
        });
        dig.setPositiveButton("변경", (dialog1, which) -> {
            Contact mContact = new Contact();
            mContact.setName(etName.getText().toString());
            mContact.setPhone(etPhone.getText().toString());
            Call<CMRespDto<Contact>> callUpdate = contactService.update(contact.getId(), mContact);
            callUpdate.enqueue(new Callback<CMRespDto<Contact>>() {
                @Override
                public void onResponse(Call<CMRespDto<Contact>> call, Response<CMRespDto<Contact>> response) {
                    CMRespDto<?> cmRespDto = response.body();
                    if (cmRespDto.getCode() == 1) {
                        adapter.setItem(position, mContact);
                        Toast.makeText(MainActivity.this, "연락처 변경 성공", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<CMRespDto<Contact>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "연락처 변경 실패", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "update() 실패 : " + t.getMessage());
                }
            });
        });
        dig.show();
    }
}