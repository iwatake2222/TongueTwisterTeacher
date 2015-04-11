/**
 * Sentence
 * @brief Contain information of a sentence
 * @author take.iwiw
 * @version 1.0.0
 */
package com.take_iwiw.tonguetwisterteacher;

import java.io.Serializable;


public class Sentence implements Serializable {
    private Integer m_idDB;     // for SQL
    private Integer m_locationLIST; // for ListView
    private String  m_sentence;
    private Integer m_cntAll;
    private Integer m_cntSuccess;
    private Float  m_record;

    public Sentence(Integer idDB, String sentence, Integer cntAll, Integer cntSuccess, Float record) {
        this.m_idDB = idDB;
        this.m_sentence = sentence;
        this.m_cntAll = cntAll;
        this.m_cntSuccess = cntSuccess;
        this.m_record = record;
    }

    public Integer getIdDB() {
        return m_idDB;
    }
    public void setIdDB(Integer idDB) {
        this.m_idDB = idDB;
    }
    public Integer getLocationLIST() {
        return m_locationLIST;
    }
    public void setLocationLIST(Integer locationLIST) {
        this.m_locationLIST = locationLIST;
    }
    public String getSentence() {
        return m_sentence;
    }
    public void setSentence(String sentence) {
        this.m_sentence = sentence;
    }
    public Integer getCntSuccess() {
        return m_cntSuccess;
    }
    public void setCntSuccess(Integer cntSuccess) {
        this.m_cntSuccess = cntSuccess;
    }
    public Integer getCntAll() {
        return m_cntAll;
    }
    public void setCntAll(Integer cntAll) {
        this.m_cntAll = cntAll;
    }
    public Float getRecord() {
        return m_record;
    }
    public void setRecord(Float record) {
        this.m_record = record;
    }
}

