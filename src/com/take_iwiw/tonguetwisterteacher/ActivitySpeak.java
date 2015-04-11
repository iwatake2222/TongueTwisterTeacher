/**
 * ActivitySpeak
 * @brief
 *    Show selected sentence
 *    Examine user speaking
 *    Measure speaking speed
 * @caller: ActivityMain
 * @params
 *    [IN] INTENT_OBJ_SELECTED_SENTENCE
 *    [IN] INTENT_INT_LANGUAGE
 *    [IN] INTENT_INT_LEVEL
 *    [OUT] INTENT_OBJ_UPDATED_SENTENCE
 * @author take.iwiw
 * @version 1.0.0
 */
package com.take_iwiw.tonguetwisterteacher;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ActivitySpeak extends Activity implements RecognitionListener {
    /*** CONST ***/
    final static int    COLOR_UNFOCUS = 0xAAAAAAAA;
    final static Float  TEXT_SIZE_MAX = 32.0f;
    final static String PREF_PROGRESS_TEXT_SIZE = "PROGRESS_TEXT_SIZE";
    final static String INTENT_OBJ_UPDATED_SENTENCE = "updated_sentence";   /* from ActivitySpeak to ActivityMain */

    /*** Views ***/
    TextView m_txtViewSentence;
    TextView m_txtViewRecognizedSentence;
    Button m_buttonStart;
    TextView m_txtViewInfo;
    TextView m_txtViewTimer;
    ImageView m_imageViewGauge;
    SeekBar m_seekBarTextSize;

    /*** Android Objects */
    SpeechRecognizer mSpeechRecognizer;

    /*** for Timer ***/
    Long m_timeStart;
    float m_measuredTime;
    Timer m_timer;
    TimerTaskCount m_timerTask;
    Handler m_handlerUI = new Handler();    // to attach UI

    /*** Others ***/
    Sentence m_sentence;    /* Get from ActivityMain. Return modified sentence */
    int m_levelId;          /* Get from ActivityMain */
    int m_languageId;       /* Get from ActivityMain */
    int m_progressTextSize;
    Boolean m_isSpeaking;   /* true :from tapping START button to finishing judgiment */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //DebugUtility.logDebug("onCreate");

        setContentView(R.layout.layout_activity_speak);

        getComponentId();

        initView();

        getIntentExtra();

        setViewListener();
    }



    @Override
    protected void onStart() {
        super.onStart();
        //DebugUtility.logDebug("onStart");

        loadPreference();

        initStatus();

        /* Init SpeechRecognizer */
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(this);

        /* Mute ON system sounds */
        AudioManager amanager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
        amanager.setStreamMute(AudioManager.STREAM_ALARM, true);
        amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        amanager.setStreamMute(AudioManager.STREAM_RING, true);
        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.putExtra(INTENT_OBJ_UPDATED_SENTENCE, m_sentence);
            setResult(RESULT_OK, intent);
            finish();
            return super.onKeyDown(keyCode, event);

        } else {
            return super.onKeyDown(keyCode, event);
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        drawGauge(0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //DebugUtility.logDebug("onStop");

        /* finish SpeechRecognizer */
        mSpeechRecognizer.destroy();

        /* Mute OFF system sounds */
        AudioManager amanager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
        amanager.setStreamMute(AudioManager.STREAM_ALARM, false);
        amanager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        amanager.setStreamMute(AudioManager.STREAM_RING, false);
        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, false);

        savePreference();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String str = m_txtViewRecognizedSentence.getText().toString();
        outState.putString("RECOGNIZED_SENTENCE", str);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String str = savedInstanceState.getString("RECOGNIZED_SENTENCE");
        m_txtViewRecognizedSentence.setText(str);

    }

    /**
     * Basic Functions
     */
    private void getComponentId() {
        m_txtViewSentence = (TextView)findViewById(R.id.textView_sentence_speak);
        m_txtViewRecognizedSentence = (TextView)findViewById(R.id.textView_recognizedSentence_speak);
        m_buttonStart = (Button)findViewById(R.id.button_start_speak);
        m_txtViewInfo = (TextView)findViewById(R.id.textView_information_speak);
        m_txtViewTimer = (TextView)findViewById(R.id.textView_timer_speak);
        m_imageViewGauge = (ImageView)findViewById(R.id.imageView_gauge_speak);
        m_seekBarTextSize = (SeekBar)findViewById(R.id.seekBar_textSize_speak);
    }

    private void initView() {
        m_txtViewRecognizedSentence.setText(getString(R.string.text_speaking_memo));
        m_txtViewSentence.setMovementMethod(ScrollingMovementMethod.getInstance());
        m_txtViewRecognizedSentence.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    private void getIntentExtra() {
        m_languageId = getIntent().getIntExtra(ActivityMain.INTENT_INT_LANGUAGE, 0);

        m_levelId = getIntent().getIntExtra(ActivityMain.INTENT_INT_LEVEL, 1);
        if (m_levelId > 2) m_levelId = 2;
        if (m_levelId < 0) m_levelId = 0;

        m_sentence = (Sentence)getIntent().getSerializableExtra(ActivityMain.INTENT_OBJ_SELECTED_SENTENCE);
        if(m_sentence!=null){
            m_txtViewSentence.setText(m_sentence.getSentence());
            m_txtViewInfo.setText(m_sentence.getCntSuccess() + " / " + m_sentence.getCntAll() + ", " + Utility.convertTimeFormat(m_sentence.getRecord()));
        } else {
            m_txtViewSentence.setText("SYSTEM ERROR");
            DebugUtility.logError("");
        }
    }

    private void setViewListener() {
        m_seekBarTextSize.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                m_txtViewSentence.setTextSize(TEXT_SIZE_MAX * (progress + 10) / 100.0f);
                m_progressTextSize = progress;
            }
        });


        /* when the START button is tapped, start recognition */
        m_buttonStart.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                /* Get Language to recognize */
                TypedArray ta = getResources().obtainTypedArray(R.array.strList_language);
                String selectedLanguage = ta.getString(m_languageId);
                String locale = "";
                if(selectedLanguage.equals(getResources().getString(R.string.text_menu_language_US))) {
                    locale = Locale.US.toString();
                } else if(selectedLanguage.equals(getResources().getString(R.string.text_menu_language_UK))) {
                    locale = Locale.UK.toString();
                } else if(selectedLanguage.equals(getResources().getString(R.string.text_menu_language_CA))) {
                    locale = Locale.CANADA.toString();
                } else {
                    locale = Locale.ENGLISH.toString();
                }

                /* Start Speech recognition */
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
                mSpeechRecognizer.startListening(intent);

                /* Change views and status */
                m_isSpeaking = true;
                m_txtViewTimer.setTextColor(COLOR_UNFOCUS);
                m_txtViewTimer.setText("00:00.00");
                m_buttonStart.setEnabled(false);
                m_buttonStart.setText(getString(R.string.text_speaking_button_WAIT));
                m_buttonStart.setTextColor(Color.RED);
            }
        });
    }

    private void loadPreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        m_progressTextSize = sharedPreferences.getInt(PREF_PROGRESS_TEXT_SIZE, 50);
        m_seekBarTextSize.setProgress(m_progressTextSize);
        m_txtViewSentence.setTextSize(TEXT_SIZE_MAX * (m_progressTextSize + 10) / 100.0f);
    }

    private void savePreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = sharedPreferences.edit();
        editor.putInt(PREF_PROGRESS_TEXT_SIZE, m_progressTextSize);
        editor.commit();
    }


    /**
     * Functions for Others
     */
    private void drawGauge(int level) {
        int width = m_imageViewGauge.getWidth();
        int height = m_imageViewGauge.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(width,  height, Bitmap.Config.ARGB_8888);
        Canvas canvas;
        canvas = new Canvas(bitmap);
        canvas.drawColor(COLOR_UNFOCUS);

        Paint paint;
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Style.FILL);

        // Margine = 5
        canvas.drawRect(0 + 5.0f, 0 + 5.0f, (float)((width - 10) * level/100.0f + 5), height - 5.0f, paint);

        m_imageViewGauge.setImageBitmap(bitmap);
    }

    private void updateSentenceInfo(Float score) {
        m_sentence.setCntAll(m_sentence.getCntAll() + 1);
        if (score >= Utility.JUDGE_SIMILAR_THRESHOLD[m_levelId]) {
            m_sentence.setCntSuccess(m_sentence.getCntSuccess() + 1);
            if (m_measuredTime < m_sentence.getRecord()) {
                m_sentence.setRecord(m_measuredTime);
                m_txtViewTimer.setTextColor(Color.RED);
            }
        }

        m_txtViewInfo.setText(m_sentence.getCntSuccess() + " / " + m_sentence.getCntAll() + ", " + Utility.convertTimeFormat(m_sentence.getRecord()));
    }

    private void initStatus() {
        m_isSpeaking = false;
        m_buttonStart.setEnabled(true);
        m_buttonStart.setText(getString(R.string.text_speaking_button_START));
        m_buttonStart.setTextColor(Color.BLACK);
    }


    /**
     * Functions for Timer
     */
    private void stopTimer() {
        if(m_timer == null) return;
        m_timer.cancel();
        m_timer = null;
    }

    private void startTimer() {
        if(m_timer != null)return;
        m_timer = new Timer();
        m_timerTask = new TimerTaskCount();
        m_timeStart = System.currentTimeMillis();
        m_timer.schedule(m_timerTask, 0, 10);
    }

    private class TimerTaskCount extends TimerTask {
        @Override
        public void run() {
            m_handlerUI.post( new Runnable() {
                public void run() {
                    Long timeNow = System.currentTimeMillis();
                    Long timeMil = timeNow - m_timeStart;
                    m_measuredTime = (float) (timeMil/1000.0f);
                    m_txtViewTimer.setText(Utility.convertTimeFormat(m_measuredTime));
                }
            });
        }
    }

    /**
     * Functions for SpeechRecognizer
     */
    @Override
    public void onReadyForSpeech(Bundle params) {
        m_buttonStart.setText(getString(R.string.text_speaking_button_SPEAK));
        m_buttonStart.setTextColor(Color.GREEN);
    }

    @Override
    public void onBeginningOfSpeech() {
        //DebugUtility.logDebug("onBeginningOfSpeech" + m_isSpeaking);
        m_buttonStart.setText(getString(R.string.text_speaking_button_SPEAKING));
        if(m_isSpeaking) {
            startTimer();
        }
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //Log.v("AAA","recieve : " + rmsdB + "dB");
        // level = 0 - 100
        int level = (int) (10*Math.pow(10, ((double)rmsdB/(double)10)));
        if (level > 100) level = 100;
        if (level < 0) level = 0;
        drawGauge(level);
    }

    @Override
    public void onEndOfSpeech() {
        //DebugUtility.logDebug("onEndOfSpeech");
        if(m_isSpeaking) {
            stopTimer();
            m_buttonStart.setText(getString(R.string.text_speaking_button_JUDGING));
            m_buttonStart.setTextColor(0x88888888);
        }
    }

    @Override
    public void onResults(Bundle results) {
        //DebugUtility.logDebug("onResults");
        ArrayList<String> recData = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        /* Examine each sentence. Use the highest score */
        Float scoreMax = 0.0f;
        for (String s : recData) {
            Float score = Utility.checkSimilar(m_sentence.getSentence(), s);
            if(score > scoreMax){
                scoreMax = score;
            }
        }

        /* Show result */
        String strResult;
        if(scoreMax >= Utility.JUDGE_SIMILAR_THRESHOLD[m_levelId]) {
            strResult = "OK";
        } else {
            strResult = "NG";
        }
        strResult += " - score = " + scoreMax.toString() + Utility.BR + recData.get(0);
        m_txtViewRecognizedSentence.setText(strResult);

        updateSentenceInfo(scoreMax);

        initStatus();
    }



    @Override
    public void onError(int error) {
        DebugUtility.logError("onError: " + error);
        switch (error) {
        case SpeechRecognizer.ERROR_AUDIO:
        case SpeechRecognizer.ERROR_CLIENT:
        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
        default:
            m_txtViewRecognizedSentence.setText("ERROR:");
            break;
        case SpeechRecognizer.ERROR_NETWORK:
        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
        case SpeechRecognizer.ERROR_SERVER:
        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
            m_txtViewRecognizedSentence.setText("Network ERROR:" + Utility.BR
                    + "Please check network, or speak faster."
                    + "If needed, choose the same language as you installed in your device."
                    );
            break;
        case SpeechRecognizer.ERROR_NO_MATCH:
            m_txtViewRecognizedSentence.setText("Recognition ERROR:" + Utility.BR + "Please speak clearly.");
            break;
        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
            m_txtViewRecognizedSentence.setText("No Input:" + Utility.BR + "Please speak clearly.");
            break;
        }

        stopTimer();
        initStatus();
    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

}

