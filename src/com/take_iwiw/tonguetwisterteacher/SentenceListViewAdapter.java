/**
 * SentenceListViewAdapter
 * @brief Adapter class for Sentence class to ListView
 * @author take.iwiw
 * @version 1.0.0
 */
package com.take_iwiw.tonguetwisterteacher;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SentenceListViewAdapter extends BaseAdapter{

    private Context m_parentContext;
    private Integer m_location;
    private List<Sentence> m_dataList = new ArrayList<Sentence>();

    public SentenceListViewAdapter(Context context) {
        super();
        m_parentContext = context;
        m_location = 0;
    }

    @Override
    public int getCount() {
        return m_dataList.size();
    }
    @Override
    public Object getItem(int position) {
        return m_dataList.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textViewSentence;
        TextView textViewCounter;
        TextView textViewRecord;
        View v = convertView;

        if(v==null){
          LayoutInflater inflater =
            (LayoutInflater)
              m_parentContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
          v = inflater.inflate(R.layout.layout_row, null);
        }

        Sentence sentence = (Sentence)getItem(position);
        if(sentence != null){
            textViewSentence = (TextView) v.findViewById(R.id.textView_sentence);
            textViewCounter = (TextView) v.findViewById(R.id.textView_counter);
            textViewRecord = (TextView) v.findViewById(R.id.textView_record);

            textViewSentence.setText(sentence.getSentence());
            textViewCounter.setText(sentence.getCntSuccess() + " / " + sentence.getCntAll());
            Float record = sentence.getRecord();    // memo: not accurate
            Integer recordInt = record.intValue();
            String strRecord = String.format("%02d:%02d.%02d", (recordInt/60)%60, recordInt%60, (int)((record - recordInt) * 100));
            textViewRecord.setText(strRecord);
        }
        return v;
    }

    public void add(Sentence sentence) {
        m_dataList.add(sentence);
        sentence.setLocationLIST(m_location);
        m_location++;
        notifyDataSetChanged();
    }

    public void update(Sentence sentence) {
        m_dataList.set(sentence.getLocationLIST(), sentence);
        notifyDataSetChanged();
    }

    public void reset() {
        m_dataList.clear();
        m_location = 0;
        notifyDataSetChanged();
    }

}
