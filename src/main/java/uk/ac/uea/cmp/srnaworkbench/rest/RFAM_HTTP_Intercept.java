/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.rest;

/**
 *
 * @author w0445959
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import static uk.ac.uea.cmp.srnaworkbench.utils.Tools.DATA_DIR;

public class RFAM_HTTP_Intercept implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        HttpHeaders headers = request.getHeaders();
        
   
        headers.set(HttpHeaders.ACCEPT, "application/json");
        headers.add("Expect", "");
      
        //traceRequest(request, body);
        ClientHttpResponse clientHttpResponse = execution.execute(request, body);
        //traceResponse(clientHttpResponse);

      return clientHttpResponse;
    
    }
    
    private void traceRequest(HttpRequest request, byte[] body) throws IOException {
      LOGGER.log(Level.FINE, "request URI : {0}", request.getURI());
      LOGGER.log(Level.FINE, "request method : {0}", request.getMethod());
      LOGGER.log(Level.FINE, "request body : {0}", getRequestBody(body));
   }

   private String getRequestBody(byte[] body) throws UnsupportedEncodingException {
      if (body != null && body.length > 0) {
         return (new String(body, "UTF-8"));
      } else {
         return null;
      }
   }


  private void traceResponse(ClientHttpResponse response) throws IOException {
      String body = getBodyString(response);
      LOGGER.log(Level.FINE, "response status code: {0}", response.getStatusCode());
      LOGGER.log(Level.FINE, "response status text: {0}", response.getStatusText());
      LOGGER.log(Level.FINE, "response body : {0}", body);
   }

   private String getBodyString(ClientHttpResponse response) {
      try {
         if (response != null && response.getBody() != null) {// &&
                                                              // isReadableResponse(response))
                                                              // {
            StringBuilder inputStringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), "UTF-8"));
            String line = bufferedReader.readLine();
            while (line != null) {
               inputStringBuilder.append(line);
               inputStringBuilder.append('\n');
               line = bufferedReader.readLine();
            }
            return inputStringBuilder.toString();
         } else {
            return null;
         }
      } catch (IOException e) {
         LOGGER.log(Level.SEVERE, e.getMessage(), e);
         return null;
      }
   }
}