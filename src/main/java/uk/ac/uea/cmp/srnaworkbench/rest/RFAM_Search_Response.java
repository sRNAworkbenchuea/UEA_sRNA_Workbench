/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author w0445959
 */
public class RFAM_Search_Response
{
    
    private synchronized void getSequenceSearchResults(String URL) throws InterruptedException
    {
        
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.setInterceptors(Collections.singletonList(new RFAM_HTTP_Intercept()));
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        //messageConverters.add(new MappingJackson2HttpMessageConverter());
        messageConverters.add(new MappingJackson2HttpMessageConverter());
        messageConverters.add(new StringHttpMessageConverter());
        restTemplate.setMessageConverters(messageConverters);

        RFAM_Search_Response_Entity result = restTemplate.getForObject(URL, RFAM_Search_Response_Entity.class);
        //String result = restTemplate.getForObject(URL, String.class);


        while (result.getStatus() != null)
        {
            wait(3000);//wait 3 seconds each time you request
            result = restTemplate.getForObject(URL, RFAM_Search_Response_Entity.class);
        }
        
//        while(result.contains("PEND")||result.contains("RUN"))
//       {
//           wait(3000);//wait 3 seconds each time you request
//           result = restTemplate.getForObject(URL, String.class);
//       }

        Map<String, ArrayList<RFAM_Hit>> hits = result.getHits();
        //List<RFAM_Search_Response_Entity.Hit> hitList = hits.getHitList();
        
        System.out.println("Result of search");
        System.out.println(result);
    }
    
    /**
     *
     * @param urlSearchResultLocation
     * @return the URL for the page containing the sequence
     */
    public int searchSequence(List<String> urlSearchResultLocation, String sequenceToFind)
    {
        final String url = "http://rfam.xfam.org/search/sequence";
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(new RFAM_HTTP_Intercept()));



        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("seq", sequenceToFind);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        urlSearchResultLocation.add(response.getHeaders().get("Location").get(0));
        
        String body = response.getBody();
        
        String[] headersWithWait = body.split(",");
        
        String[] waitField = headersWithWait[2].split("\"");
        
        

        System.out.println(response.getBody());
        
        
        return StringUtils.safeIntegerParse(waitField[3],3);
    }
    
    public void getFamilyInfo() throws InterruptedException
    {
        final String uri = "http://rfam.xfam.org/family/snoZ107_R87";

        RestTemplate restTemplate = new RestTemplate();

        restTemplate.setInterceptors(Collections.singletonList(new RFAM_HTTP_Intercept()));
        
        
        String result = restTemplate.getForObject(uri, String.class);
    
        System.out.println(result);
    }
    public synchronized void conductSearch(String seqToFind) throws InterruptedException
    {
        List<String> urlSearchResultLocation = new ArrayList<>();
        int waitTimeApprox = searchSequence(urlSearchResultLocation, seqToFind);
        wait(waitTimeApprox * 1000);//convert to millis
        
        getSequenceSearchResults(urlSearchResultLocation.get(0));
        
    }
    public static void main(String[] args)
    {
        try
        {
            Tools.getInstance();
            RFAM_Search_Response rfsearcher = new RFAM_Search_Response();
                                        //AGTTACGGCCATACCTCAGAGAATATACCGTATCCCGTTCGATCTGCGAAGTTAAGCTCTGAAGGGCGTCGTCAGTACTATAGTGGGTGACCATATGGGAATACGACGTGCTGTAGCTT
            rfsearcher.conductSearch("AGTTACGGCCATACCTCAGAGAATATACCGTATCCCGTTCGATCTGCGAAGTTAAGCTCTGAAGGGCGTCGTCAGTACTATAGTGGGTGACCATATGGGAATACGACGTGCTGTAGCTT");
            //rfsearcher.conductSearch("AGTTACGGCCATACCTCAGAG");

            
            //rfsearcher.conductSearch("ATTGATGTGTGTGAAAATGTGA");

            //rfsearcher.getFamilyInfo();
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(RFAM_Search_Response.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
