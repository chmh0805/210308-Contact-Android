package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private static final String TAG = "ContactAdapter";
    private List<Contact> contactList;
    private MainActivity mActivity;

    public ContactAdapter(MainActivity mainActivity) {
        this.mActivity = mainActivity;
        this.contactList = mActivity.getContactList();
    }

    public void setItems(List<Contact> contacts) {
        this.contactList = contacts;
        notifyDataSetChanged();
    }

    public void setItem(int position, Contact contact) {
        contactList.get(position).setName(contact.getName());
        contactList.get(position).setPhone(contact.getPhone());
        notifyDataSetChanged();
    }

    public void addItem(Contact contact) {
        this.contactList.add(contact);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        this.contactList.remove(position);
        notifyDataSetChanged();
    }

    public void removeAll() {
        this.contactList = new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.setItem(contact);
        holder.itemView.setOnClickListener(v -> {
            mActivity.updateContact(contact, position);
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName, tvPhone;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tvName = itemView.findViewById(R.id.tv_name);
            this.tvPhone = itemView.findViewById(R.id.tv_phone);
        }

        public void setItem(Contact contact) {
            tvName.setText(contact.getName());
            tvPhone.setText(contact.getPhone());
        }

    }
}
