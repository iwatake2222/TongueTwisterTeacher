/**
 * ActivityMain
 * @brief
 *    Show sentences using ListView
 *    Call ActivitySpeak
 *    Manage menu
 * @author take.iwiw
 * @version 1.0.0
 */
package com.take_iwiw.tonguetwisterteacher;

import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.opencsv.CSVReader;


public class ActivityMain extends Activity {
    /*** CONST ***/
    final static int INTENT_REQUEST_CODE = 1000;    /* ActivityMain <- ActivitySpeak */
    final static String INTENT_OBJ_SELECTED_SENTENCE = "selected_sentence";
    final static String INTENT_INT_LANGUAGE = "language_id";
    final static String INTENT_INT_LEVEL = "level_id";

    /*** Views ***/
    ListView m_listView;

    /*** Android Objects */
    SentenceListViewAdapter m_adapterListView;

    /*** Others ***/
    int m_languageId = 0;   /* from user setting */
    int languageId_temp;
    int m_levelId = 1;   /* from user setting */
    int levelId_temp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);

        getComponentId();

        initView();

        setViewListener();

        getDatabase();  /* Get sentences from DB, Show them on ListView */

        /* Reset DB at 1st use */
        if(m_adapterListView.getCount() == 0){
            //DebugUtility.logDebug("resetDatabase");
            /* Reset DB from CSV */
            resetDatabase();
            getDatabase();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadPreference();
    }

    @Override
    protected void onStop() {
        super.onStop();
        savePreference();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /* Called when coming back from ActivitySpeak */
        if(resultCode == RESULT_OK && requestCode == INTENT_REQUEST_CODE && null != data) {
            Sentence sentence = (Sentence)data.getSerializableExtra(ActivitySpeak.INTENT_OBJ_UPDATED_SENTENCE);
            updateSentenceData(sentence);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
        case R.id.menu_language:
            languageId_temp = m_languageId; /* for the case that 1. select, 2. cancel, 3. OK -> languageId_temp is selected */
            String[] strArrayLanguage = getResources().getStringArray(R.array.strList_language);
            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.text_menu_language))
                    .setSingleChoiceItems(strArrayLanguage, m_languageId, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            languageId_temp = which;
                        }
                    })
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            m_languageId = languageId_temp;
                        }
                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
            break;
        case R.id.menu_level:
            levelId_temp = m_levelId; /* for the case that 1. select, 2. cancel, 3. OK -> languageId_temp is selected */
            String[] strArrayLevel = getResources().getStringArray(R.array.strList_level);
            new AlertDialog.Builder(this)
            .setTitle(getResources().getString(R.string.text_menu_level))
                    .setSingleChoiceItems(strArrayLevel, m_levelId, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            levelId_temp = which;
                        }
                    })
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            m_levelId = levelId_temp;
                        }
                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
            break;
        case R.id.menu_reaset:
            new AlertDialog.Builder(this)
            .setTitle(getResources().getString(R.string.text_menu_dataReset))
            .setMessage("Are you sure?")
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    m_adapterListView.reset();
                    resetDatabase();
                    getDatabase();
                }
            })
            .setNegativeButton("CANCEL", null)
            .show();
            break;
        case R.id.menu_info:
            /* Show Information Dialog */
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.text_info_detail)).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            builder.show();
            break;
        default:
            DebugUtility.logError("menuId = " + item.getItemId());
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Basic Functions
     */
    private void getComponentId() {
        m_listView = (ListView)findViewById(R.id.listView1);
    }

    private void initView() {
        /* Set adapter to ListView */
        m_adapterListView = new SentenceListViewAdapter(this);
        m_listView.setAdapter(m_adapterListView);
    }

    private void setViewListener() {

        /* when a sentence is tapped, call ActivitySpeak */
        m_listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView)parent;
                Sentence selectedSentence = (Sentence)listView.getItemAtPosition(position);
                Intent intent = new Intent();
                intent.setClassName("com.take_iwiw.tonguetwisterteacher", "com.take_iwiw.tonguetwisterteacher.ActivitySpeak");
                intent.putExtra(INTENT_OBJ_SELECTED_SENTENCE, selectedSentence);
                intent.putExtra("INTENT_INT_LANGUAGE", m_languageId);
                intent.putExtra(INTENT_INT_LEVEL, m_levelId);
                startActivityForResult(intent, INTENT_REQUEST_CODE);
            }

        });
    }

    private void loadPreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        m_languageId = sharedPreferences.getInt("LANGUAGE_ID", 0);
        m_levelId = sharedPreferences.getInt("LEVEL_ID", 1);
    }

    private void savePreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = sharedPreferences.edit();
        editor.putInt("LANGUAGE_ID", m_languageId);
        editor.putInt("LEVEL_ID", m_levelId);
        editor.commit();
    }

    /**
     * Functions for Others
     */
    private void updateSentenceData(Sentence sentence) {
        updateListView(sentence);
        updateDatabase(sentence);
    }

    private void updateListView(Sentence sentence) {
        m_adapterListView.update(sentence);
    }

    /**
     * Functions for Database
     */
    private void updateDatabase(Sentence sentence) {
        SQLiteDatabase mydb;
        MySQLiteOpenHelper hlpr = new MySQLiteOpenHelper(getApplicationContext());
        mydb = hlpr.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("sentence", sentence.getSentence());
        values.put("cntAll", sentence.getCntAll());
        values.put("cntSuccess", sentence.getCntSuccess());
        values.put("record", sentence.getRecord());
        mydb.update(MySQLiteOpenHelper.TABLE_NAME, values, "_id = " + sentence.getIdDB(), null);
        mydb.close();
    }

    /* Get data from DB, and set adapter */
    private void getDatabase() {
        SQLiteDatabase mydb;
        MySQLiteOpenHelper hlpr = new MySQLiteOpenHelper(getApplicationContext());
        mydb = hlpr.getWritableDatabase();

        Cursor cursor = mydb.query(MySQLiteOpenHelper.TABLE_NAME, new String[] {"_id", "sentence", "cntAll", "cntSuccess", "record"}, null, null, null, null, "_id ASC");

        int indexId  = cursor.getColumnIndex("_id");
        int indexSentence  = cursor.getColumnIndex("sentence");
        int indexCntAll  = cursor.getColumnIndex("cntAll");
        int indexCntSuccess  = cursor.getColumnIndex("cntSuccess");
        int indexRecord  = cursor.getColumnIndex("record");

        while( cursor.moveToNext() ){
            m_adapterListView.add(new Sentence(cursor.getInt(indexId), cursor.getString(indexSentence), cursor.getInt(indexCntAll), cursor.getInt(indexCntSuccess), cursor.getFloat(indexRecord)));
        }

        cursor.close();
        mydb.close();
    }

    /* Get data from CSV text file, and reset Database */
    private void resetDatabase() {
        SQLiteDatabase mydb;
        MySQLiteOpenHelper hlpr = new MySQLiteOpenHelper(getApplicationContext());
        mydb = hlpr.getWritableDatabase();
        //mydb.execSQL("drop table mytable");
        mydb.delete(MySQLiteOpenHelper.TABLE_NAME, null, null);

        try{
            AssetManager assetManager = getResources().getAssets();
            InputStream input = assetManager.open("originalSentence.csv");
            CSVReader csv = new CSVReader(new InputStreamReader(input, "UTF-8"));
            String[] line;

            while ((line = csv.readNext()) != null) {
                if (line[0] == ""){
                    continue;   // for brank line
                }
                ContentValues values = new ContentValues();
                values.put("sentence", line[0]);
                values.put("cntAll", line[1]);
                values.put("cntSuccess", line[2]);
                values.put("record", line[3]);
                mydb.insert(MySQLiteOpenHelper.TABLE_NAME, null, values);
            }
            csv.close();
        } catch (Exception e){

        }

        mydb.close();
    }

}

