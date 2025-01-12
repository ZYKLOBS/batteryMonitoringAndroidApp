/*
 * MIT License
 *
 * Copyright (c) 2024 RUB-SE-LAB-2024
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.msbattery.batterymonitoringapp.structures;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

import de.msbattery.batterymonitoringapp.R;
import de.msbattery.batterymonitoringapp.structures.EmergencyContact;

public class EmergencyContactAdapter extends RecyclerView.Adapter<EmergencyContactAdapter.ViewHolder> {

    private List<EmergencyContact> contacts;
    private OnDeleteButtonClickListener delListener;
    private OnCheckedChangeListener chkListener;

    public EmergencyContactAdapter(List<EmergencyContact> contacts, OnDeleteButtonClickListener delListener, OnCheckedChangeListener chkListener) {
        this.contacts = contacts;
        this.delListener = delListener;
        this.chkListener = chkListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setContacts(List<EmergencyContact> contacts) {
        this.contacts = contacts;
        notifyDataSetChanged();
    }

    public void addContact(EmergencyContact contact) {
        this.contacts.add(contact);
        notifyItemInserted(contacts.size() - 1);
    }

    public void remove(int position) {
        if (position >= 0 && position < contacts.size()) {
            contacts.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, contacts.size());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emergency_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmergencyContact contact = contacts.get(position);
        holder.nameTextView.setText(contact.getName());
        holder.phoneTextView.setText(contact.getPhoneNumber());
        holder.emailTextView.setText(contact.getEmail());

        // Remove OnCheckedListener to not trigger on setChecked
        holder.switchVisButton.setOnCheckedChangeListener(null);
        holder.switchVisButton.setChecked(contact.getIs_displayed());

        holder.deleteButton.setOnClickListener(v -> {
            int newPos = holder.getBindingAdapterPosition();
            if (newPos != RecyclerView.NO_POSITION) {
                delListener.onDeleteButtonClick(contact, newPos);
            }
        });
        holder.switchVisButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int newPos = holder.getBindingAdapterPosition();
            if (newPos != RecyclerView.NO_POSITION) {
                chkListener.onCheckedChange(contact, isChecked, holder.switchVisButton);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public interface OnDeleteButtonClickListener {
        void onDeleteButtonClick(EmergencyContact contact, int position);
    }

    public interface OnCheckedChangeListener {
        void onCheckedChange(EmergencyContact contact, boolean state, SwitchMaterial self);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView phoneTextView;
        TextView emailTextView;
        Button deleteButton;
        SwitchMaterial switchVisButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.contact_name);
            phoneTextView = itemView.findViewById(R.id.contact_phone);
            emailTextView = itemView.findViewById(R.id.contact_email);
            deleteButton = itemView.findViewById(R.id.delete_button);
            switchVisButton = itemView.findViewById(R.id.show);
        }
    }
}
