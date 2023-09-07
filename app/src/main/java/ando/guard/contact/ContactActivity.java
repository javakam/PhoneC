package ando.guard.contact;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ando.guard.R;

public class ContactActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    private RecyclerView recyclerViewContacts;
    private ContactAdapter contactAdapter;
    private List<Contact> contactList;
    private Spinner spinnerSort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        recyclerViewContacts = findViewById(R.id.recyclerViewContacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));

        spinnerSort = findViewById(R.id.spinnerSort);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.sort_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(spinnerAdapter);
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                sortContactList(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        spinnerSort.setSelection(0); // 默认选择第一个排序选项

        FloatingActionButton fabAddContact = findViewById(R.id.fabAddContact);
        fabAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddContactDialog();
            }
        });

        contactList = new ArrayList<>();
        contactAdapter = new ContactAdapter(contactList);
        recyclerViewContacts.setAdapter(contactAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasPermission()) {
            loadContacts();
        } else {
            requestPermission();
        }
    }

    private boolean hasPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
    }

    @SuppressLint("Range")
    private void loadContacts() {
        contactList.clear();

        String[] projection = {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
                ContactsContract.CommonDataKinds.StructuredName.SUFFIX,
                ContactsContract.CommonDataKinds.StructuredName.PREFIX,
                ContactsContract.CommonDataKinds.Nickname.NAME,
                ContactsContract.CommonDataKinds.Organization.COMPANY,
                ContactsContract.CommonDataKinds.Organization.TITLE,
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Website.URL,
                ContactsContract.CommonDataKinds.Note.NOTE,
                ContactsContract.Contacts.PHOTO_URI,
                ContactsContract.Contacts.LAST_TIME_CONTACTED,
                ContactsContract.Contacts.TIMES_CONTACTED,
                ContactsContract.Contacts.STARRED,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.LABEL,
                ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
                ContactsContract.CommonDataKinds.StructuredPostal.LABEL,
                ContactsContract.CommonDataKinds.Event.START_DATE
        };
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        //todo 2023年9月7日 11:37:23 联系人bug
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, selection, selectionArgs, sortOrder);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // 创建一个 Contact 对象
                Contact contact = new Contact();

                // 从系统的 Cursor 中获取联系人信息，并赋值给 Contact 对象
               long id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String givenName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
                String familyName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
                String middleName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
                String suffix = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.SUFFIX));
                String prefix = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PREFIX));
                String nickname = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME));
                String company = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY));
                String jobTitle = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
                String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                String website = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL));
                String notes = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
                String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
                long lastTimeContacted = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts.LAST_TIME_CONTACTED));
                int timesContacted = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.TIMES_CONTACTED));
                boolean starred = (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.STARRED)) != 0);
                int hasPhoneNumber = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                // 获取电话号码相关的信息
                if (hasPhoneNumber > 0) {
                    Cursor phoneCursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{String.valueOf(id)},
                            null);

                    if (phoneCursor != null && phoneCursor.moveToFirst()) {
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        int phoneType = phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        String phoneLabel = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));

                        contact.setHasPhoneNumber(true);
                        contact.setPhoneNumber(phoneNumber);
                        contact.setPhoneType(phoneType);
                        contact.setPhoneLabel(phoneLabel);
                    }

                    if (phoneCursor != null) {
                        phoneCursor.close();
                    }
                } else {
                    contact.setHasPhoneNumber(false);
                }

                // 获取地址相关的信息
                Cursor addressCursor = getContentResolver().query(
                        ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = ?",
                        new String[]{String.valueOf(id)},
                        null);

                if (addressCursor != null && addressCursor.moveToFirst()) {
                    String address = addressCursor.getString(addressCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
                    int addressType = addressCursor.getInt(addressCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
                    String addressLabel = addressCursor.getString(addressCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.LABEL));

                    contact.setAddress(address);
                    contact.setAddressType(addressType);
                    contact.setAddressLabel(addressLabel);
                }

                if (addressCursor != null) {
                    addressCursor.close();
                }

                // 获取生日相关的信息
                Cursor birthdayCursor = getContentResolver().query(
                        ContactsContract.Data.CONTENT_URI,
                        null,
                        ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
                        new String[]{String.valueOf(id), ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE},
                        null);

                if (birthdayCursor != null && birthdayCursor.moveToFirst()) {
                    String birthday = birthdayCursor.getString(birthdayCursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                    contact.setBirthday(birthday);
                }

                if (birthdayCursor != null) {
                    birthdayCursor.close();
                }

                // 将所有联系人信息添加到联系人列表中
                contact.setId(id);
                contact.setDisplayName(displayName);
                contact.setGivenName(givenName);
                contact.setFamilyName(familyName);
                contact.setMiddleName(middleName);
                contact.setSuffix(suffix);
                contact.setPrefix(prefix);
                contact.setNickname(nickname);
                contact.setCompany(company);
                contact.setJobTitle(jobTitle);
                contact.setEmail(email);
                contact.setWebsite(website);
                contact.setNotes(notes);
                contact.setPhotoUri(photoUri);
                contact.setLastTimeContacted(lastTimeContacted);
                contact.setTimesContacted(timesContacted);
                contact.setStarred(starred);

                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

// 使用联系人列表进行后续操作
// ...

        contactAdapter.notifyDataSetChanged();
    }

    private void sortContactList(int position) {
        switch (position) {
            case 0: // Sort by name
                Collections.sort(contactList, new Comparator<Contact>() {
                    @Override
                    public int compare(Contact contact1, Contact contact2) {
                        return contact1.getDisplayName().compareToIgnoreCase(contact2.getDisplayName());
                    }
                });
                break;
            case 1: // Sort by phone number
                Collections.sort(contactList, new Comparator<Contact>() {
                    @Override
                    public int compare(Contact contact1, Contact contact2) {
                        return contact1.getPhoneNumber().compareTo(contact2.getPhoneNumber());
                    }
                });
                break;
            case 2:
                // 假设 contacts 是您的联系人列表
                Collections.sort(contactList, new Comparator<Contact>() {
                    @Override
                    public int compare(Contact contact1, Contact contact2) {
                        return (int) Math.max(contact1.getLastTimeContacted(), contact2.getLastTimeContacted());
                    }
                });

                break;
            default:
        }

        contactAdapter.notifyDataSetChanged();
    }

    private void showAddContactDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_contact, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = dialogView.findViewById(R.id.editTextName);
        final EditText editTextNumber = dialogView.findViewById(R.id.editTextNumber);
        Button buttonAdd = dialogView.findViewById(R.id.buttonAdd);

        final AlertDialog dialog = dialogBuilder.create();

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();
                String phoneNumber = editTextNumber.getText().toString().trim();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(ContactActivity.this, "请填写姓名和手机号", Toast.LENGTH_SHORT).show();
                } else {
                    addContact(name, phoneNumber);
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    private void addContact(String name, String phoneNumber) {
        ContentResolver contentResolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(ContactsContract.RawContacts.ACCOUNT_TYPE, "");
        values.put(ContactsContract.RawContacts.ACCOUNT_NAME, "");

        Uri rawContactUri = contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, values);
        long rawContactId = Long.parseLong(rawContactUri.getLastPathSegment());

        values.clear();
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name);
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, values);

        values.clear();
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber);
        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, values);

        loadContacts();
    }
}
