package com.example.l13_mingguanglin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    EditText editText;
    Button button,button2,button3;
    TextView textView;
    ListView listView;
    Cursor contactCursor;
    Uri contactUri;

    public void queryContacts(){
        String[] FROM_COLUMNS = {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY};
        int[] T0_IDS = {android.R.id.text1};

        String[] projection = {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        };
        String selection;
        String[] selectionArgs = {""};
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " DESC";

        String searchString = editText.getText().toString();
        if(TextUtils.isEmpty(searchString)){
            selection = null;
            selectionArgs = null;
        }else{
            selection = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?";
            selectionArgs[0] = "%" + searchString + "%";
        }
        contactCursor = getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
        );
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(
                getApplicationContext(),
                android.R.layout.simple_list_item_1,
                contactCursor,
                FROM_COLUMNS,
                T0_IDS,
                0
        );
        listView.setAdapter(cursorAdapter);
    }
    public void checkPermissionAndQuery(){
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS},1);
        }else{
            queryContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode==1){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                queryContacts();
            }else{
                Toast.makeText(MainActivity.this,
                        "You denied the 'Read Contacts' Permission",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
        contactCursor.moveToPosition(position);
        Long contactId = contactCursor.getLong(0);
        String contentKey = contactCursor.getString(1);
        contactUri = ContactsContract.Contacts.getLookupUri(contactId,contentKey);

        StringBuilder content = new StringBuilder();
        content.append("Name: " +contactCursor.getString(2)+"\n");

        String[] projection = {ContactsContract.CommonDataKinds.Email.ADDRESS};
        String selection = ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?";
        String[] selectionArgs = {contactId.toString()};
        Cursor emailCursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
        try {
            if(emailCursor.moveToFirst()) {
                int i = 1;
                do {
                    content.append("Email" + i +": " +emailCursor.getString(0)+"\n");
                    i = i+1;
                }while (emailCursor.moveToNext());
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            emailCursor.close();
        }

        String[] mProjection = {ContactsContract.CommonDataKinds.Phone.NUMBER};
        String mSelection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";
        String[] mSelectionArgs = {contactId.toString()};
        Cursor mCursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                mProjection,
                mSelection,
                mSelectionArgs,
                null
        );
        try {
            if(mCursor.moveToFirst()) {
                int i = 1;
                do {
                    content.append("Phone" + i +": " +mCursor.getString(0)+"\n");
                    i = i+1;
                }while (mCursor.moveToNext());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            mCursor.close();
        }
        textView.setText(content);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.editTextTextPersonName);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        textView = findViewById(R.id.textView);
        listView = findViewById(R.id.listView);

        listView.setOnItemClickListener(this);

        button.setOnClickListener(v ->{
            checkPermissionAndQuery();
        });
        button2.setOnClickListener(v ->{
            if(contactUri!=null){
                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setDataAndType(contactUri,ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                startActivity(intent);
            }
        });
        button3.setOnClickListener(v ->{
            Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
            intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
            intent.putExtra(ContactsContract.Intents.Insert.NAME,"Whatsapp Support");
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL,"Whatsapp@gmail.com");
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE,
                    ContactsContract.CommonDataKinds.Email.TYPE_WORK);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE,"950800");
            intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
            startActivity(intent);
        });
    }
}