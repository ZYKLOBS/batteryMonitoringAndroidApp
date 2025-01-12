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

package de.msbattery.batterymonitoringapp.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;


import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.msbattery.batterymonitoringapp.R;
import de.msbattery.batterymonitoringapp.connection.AlertSender;
import de.msbattery.batterymonitoringapp.database.DataBaseHelper;
import de.msbattery.batterymonitoringapp.structures.EmergencyContact;
import de.msbattery.batterymonitoringapp.structures.EmergencyContactAdapter;

public class SettingsFragment extends Fragment implements EmergencyContactAdapter.OnDeleteButtonClickListener, EmergencyContactAdapter.OnCheckedChangeListener{
    //to do -> limit maximum number of displayed emergency contacts to 2

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_PARAM1 = "param1";

    private Button deleteDbRecords;

    private int mColumnCount = 1;
    private String mParam1;

    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPhone;

    private EditText oldPassword;
    private EditText newPassword;

    private void showConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Data")
                .setMessage("Are you sure you want to delete your historical data? This action cannot be undone.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { //which necessary to determine if yes or no has been pressed
                        DataBaseHelper databaseHelper = new DataBaseHelper(getContext());
                        databaseHelper.deleteAllSegmentEntries();
                        Toast.makeText(getContext(), "Historical Data deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User cancelled the dialog
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    public SettingsFragment() {
    }

    private void replaceFragment(String frag) {
        Fragment newFragment;
        switch (frag) {
            case "C":
                newFragment = CompactView.newInstance();
                break;
            case "D":
                newFragment = DetailedView.newInstance();
                break;
            default:
                newFragment = SimpleView.newInstance();
                break;
        }
        FragmentManager fragmentManager = getParentFragmentManager(); // or getChildFragmentManager() if nested
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container, newFragment); // R.id.fragment_container is the ID of the container in the activity layout
        fragmentTransaction.commit();
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static SettingsFragment newInstance(int colCount, String prevFrag) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, prevFrag);
        args.putInt(ARG_COLUMN_COUNT, colCount);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mParam1 = getArguments().getString(ARG_PARAM1);
        }

        // Consider removing adding to backstack alltogether

        // This is a workaround as pressing the back button going to detail view will crash
        // I think it tries to load the DetailedCellFragments but they dont exist
        // https://developer.android.com/guide/navigation/navigation-custom-back#java
        // We dont push to the backStack in DetailedView
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                if(Objects.equals(mParam1, "D"))
                    replaceFragment("D");
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_list, container, false);

        FrameLayout backFrameLayout = view.findViewById(R.id.fragment_back);
        backFrameLayout.setOnClickListener(v -> replaceFragment(mParam1));

        FrameLayout logoutFrameLayout = view.findViewById(R.id.logout);
        logoutFrameLayout.setOnClickListener(v -> replaceFragment("L"));

        deleteDbRecords = view.findViewById(R.id.deleteRecordButton);

        Context context = getContext();
        ArrayList<EmergencyContact> contactsCopy = null;
        List<EmergencyContact> emergencyContacts = AlertSender.getEmergencyContacts(context);
        if (emergencyContacts != null)
            contactsCopy = new ArrayList<>(emergencyContacts);

        final RecyclerView emergencyContactList = view.findViewById(R.id.emergencyContactList);
        emergencyContactList.setLayoutManager(new LinearLayoutManager(context));

        EmergencyContactAdapter adapter = new EmergencyContactAdapter(contactsCopy, this, this);
        emergencyContactList.setAdapter(adapter);

        deleteDbRecords.setOnClickListener(v -> showConfirmationDialog());

        Button buttonAddContact = view.findViewById(R.id.button_add_contact);
        editTextName = view.findViewById(R.id.edit_text_name);
        editTextEmail = view.findViewById(R.id.edit_text_mail);
        editTextPhone = view.findViewById(R.id.edit_text_phone);

        Button buttonChangePassword = view.findViewById(R.id.button_change_password);
        oldPassword = view.findViewById(R.id.oldPassword);
        newPassword = view.findViewById(R.id.newPassword);

        CheckBox checkBoxSendMails = view.findViewById(R.id.check_send_mails);
        CheckBox checkBoxSendSMS = view.findViewById(R.id.check_send_sms);

        // Shouldn't be needed, but just in case
        checkBoxSendMails.setOnCheckedChangeListener(null);
        checkBoxSendSMS.setOnCheckedChangeListener(null);

        checkBoxSendMails.setChecked(AlertSender.isMailNotifications(getContext()));
        checkBoxSendSMS.setChecked(AlertSender.isPhoneNotifications(getContext()));


        buttonAddContact.setOnClickListener(v -> {
            String name = editTextName.getText().toString();
            String mail = editTextEmail.getText().toString();
            String phone = editTextPhone.getText().toString();
            EmergencyContact contact = new EmergencyContact(name, phone, mail, false);

            if (!TextUtils.isEmpty(name)) {
                ArrayList<EmergencyContact> updatedContacts = AlertSender.getEmergencyContacts(getContext());
                if(updatedContacts == null) return;
                for(EmergencyContact con : updatedContacts) {
                    if(con.getName().equalsIgnoreCase(name)) {
                        Toast.makeText(v.getContext(), "Contact already exists!", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                if(!AlertSender.addEmergencyContact(getContext(), contact)) {
                    Toast.makeText(v.getContext(), "Creation Failed!", Toast.LENGTH_LONG).show();
                    return;
                }
                adapter.addContact(contact);
                editTextName.setText("");
                editTextEmail.setText("");
                editTextPhone.setText("");
                Toast.makeText(v.getContext(), "Contact added!", Toast.LENGTH_LONG).show();
            }
        });

        buttonChangePassword.setOnClickListener(v -> {
            String oldP = oldPassword.getText().toString();
            String newP = newPassword.getText().toString();

            if(oldP.isEmpty()) {
                Toast.makeText(v.getContext(), "Enter old password", Toast.LENGTH_LONG).show();
                return;
            } else if (newP.isEmpty()) {
                Toast.makeText(v.getContext(), "Enter new password", Toast.LENGTH_LONG).show();
                return;
            }

            DataBaseHelper databaseHelper = new DataBaseHelper(getContext());
            String oldDbPwd = databaseHelper.select_password();
            if(oldDbPwd.equals(oldP)) {
                if(databaseHelper.updatePassword(newP)) {
                    Toast.makeText(v.getContext(), "Password updated!", Toast.LENGTH_LONG).show();
                    oldPassword.setText("");
                    newPassword.setText("");
                }
                else {
                    Toast.makeText(v.getContext(), "Error updating password!", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(v.getContext(), "Old password does not match!", Toast.LENGTH_LONG).show();
            }
        });

        checkBoxSendMails.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AlertSender.setMailNotifications(getContext(), isChecked);
        });

        checkBoxSendSMS.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AlertSender.setPhoneNotifications(getContext(), isChecked);
        });

        return view;
    }

    @Override
    public void onDeleteButtonClick(EmergencyContact contact, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete this contact?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    ArrayList<EmergencyContact> contacts = AlertSender.removeEmergencyContact(getContext(), contact.getName());
                    View view = getView();
                    if(view != null){
                        RecyclerView recyclerView = view.findViewById(R.id.emergencyContactList);
                        EmergencyContactAdapter adapter = (EmergencyContactAdapter) recyclerView.getAdapter();
                        if (adapter != null) {
                            adapter.remove(position);
                            Log.d("MSETTINGS", "Deleted item at position: " + position);
                            Log.d("MSETTINGS", "Deleted item with name: " + contact.getName());
                        }
                    }

                    Toast.makeText(getContext(), "Contact deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private boolean isSwitchChanging = false;

    @Override
    public void onCheckedChange(EmergencyContact contact, boolean state, SwitchMaterial self) {
        if (isSwitchChanging) {
            return;
        }
        ArrayList<EmergencyContact> contacts = AlertSender.getEmergencyContacts(getContext());
        if(contacts == null) {
            Log.e("Settings", "AlertSender contacts is null!");
            isSwitchChanging = true;
            self.setChecked(!state);
            isSwitchChanging = false;
            return;
        }
        int retStat = AlertSender.setIs_Displayed(getContext(), contact, state);
        if(retStat == -1) {
            Toast.makeText(getContext(), "Error, nothing changed.", Toast.LENGTH_SHORT).show();
            isSwitchChanging = true;
            self.setChecked(!state);
            isSwitchChanging = false;

        } else if (retStat == 1) {
            Toast.makeText(getContext(), "Failed, Contact does not exist.", Toast.LENGTH_SHORT).show();
            isSwitchChanging = true;
            self.setChecked(!state);
            isSwitchChanging = false;
        } else if (retStat == 2) {
            Toast.makeText(getContext(), "Database error, change temporary.", Toast.LENGTH_SHORT).show();
            isSwitchChanging = true;
            self.setChecked(!state);
            isSwitchChanging = false;
        } else if (retStat == 3) {
            Toast.makeText(getContext(), "Failed, limit reached. Remove other contacts.", Toast.LENGTH_SHORT).show();
            isSwitchChanging = true;
            self.setChecked(!state);
            isSwitchChanging = false;
        }
    }
}