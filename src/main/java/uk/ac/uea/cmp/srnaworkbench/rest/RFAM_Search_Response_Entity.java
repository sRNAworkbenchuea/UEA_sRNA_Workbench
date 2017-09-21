/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author w0445959
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RFAM_Search_Response_Entity
{
    //@JsonProperty
    private String closed;
    private String status;
    
    @JsonProperty("hits")
    private Map<String, ArrayList<RFAM_Hit>> hits;
    
    public RFAM_Search_Response_Entity(){}
    public RFAM_Search_Response_Entity(String closed, String status)
    {
        this.closed = closed;
        this.status = status;
    }

    public Map<String, ArrayList<RFAM_Hit>> getHits()
    {
        return hits;
    }

    public void setHits(Map<String, ArrayList<RFAM_Hit>> hits)
    {
        this.hits = hits;
    }
    
    

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
    

    public String getClosed()
    {
        return closed;
    }

    public void setClosed(String closed)
    {
        this.closed = closed;
    }
    
    /**
     * Internal list of hits to sequence
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Hits
    {
        private List<Hit> hits;

        public Hits(){}
        public List<Hit> getHitList()
        {
            return hits;
        }

        public void setHitList(List<Hit> score)
        {
            this.hits = score;
        }
        
        
        
        
        
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Hit 
    {
        @JsonProperty("hits")
        private String score;
        public Hit(){}

        public String getScore()
        {
            return score;
        }

        public void setScore(String score)
        {
            this.score = score;
        }
        
        
        
    }
    
}
