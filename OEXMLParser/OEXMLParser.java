package com.odie.animehub;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Odie on 03/10/14.
 */
public class OEXMLParser {

    private static final String TAG = OEXMLParser.class.getSimpleName();

    private String urlString;
    private XmlPullParserFactory xmlFactoryObject;
    private ArrayList<String> titles;
    private ArrayList<String> links;
    private ArrayList<String> descriptions;
    private ArrayList<String> imageURLS;
    private ArrayList<HashMap<String, String>> results;
    public volatile boolean parsingComplete = true;

    /** CONSTRUCTOR **/
    public OEXMLParser(String url){
        this.urlString = url;
        this.results = new ArrayList<HashMap<String, String>>();
        this.titles = new ArrayList<String>();
        this.links = new ArrayList<String>();
        this.descriptions = new ArrayList<String>();
        this.imageURLS = new ArrayList<String>();
    }

    /** GETTERS AND SETTERS **/
    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public XmlPullParserFactory getXmlFactoryObject() {
        return xmlFactoryObject;
    }

    public void setXmlFactoryObject(XmlPullParserFactory xmlFactoryObject) {
        this.xmlFactoryObject = xmlFactoryObject;
    }

    public ArrayList<HashMap<String, String>> getResults() {
        return results;
    }

    public void setResults(ArrayList<HashMap<String, String>> results) {
        this.results = results;
    }

    public boolean isParsingComplete() {
        return parsingComplete;
    }

    public void setParsingComplete(boolean parsingComplete) {
        this.parsingComplete = parsingComplete;
    }

    /**
     * Regular Java method to fetch xml data without Android API support
     */
    public void fetchXML_Support(){

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection)
                            url.openConnection();
                    conn.setReadTimeout(20000 /* milliseconds */);
                    conn.setConnectTimeout(30000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream stream = conn.getInputStream();

                    // Get XML string and escape it
                    String XMLString = getStringForXMLData(stream);
                    String escapedXMLString = removeHTMLEntities(XMLString);

                    if(escapedXMLString.equalsIgnoreCase("No results")){
                        results.clear();
                        parsingComplete = false;
                        stream.close();
                        return;
                    }

                    // Convert escaped string back to stream data
                    stream = new ByteArrayInputStream(escapedXMLString.getBytes("UTF-8"));


                    xmlFactoryObject = XmlPullParserFactory.newInstance();
                    XmlPullParser myParser = xmlFactoryObject.newPullParser();

                    myParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myParser.setInput(stream, null);
                    parseXMLAndStoreIt(myParser);
                    stream.close();

                } catch (Exception e) {
                    e.printStackTrace();
                    parsingComplete = false;
                }
            }
        });

        thread.start();
    }

    /**
     * Method to fetch xml asynchronously using Android API.
     *
     * @param callback a bit of code that is executed on completion of the xml fetch.
     */
    public void fetchXMLWithCallback(XMLTask.XMLCallback callback){
        XMLTask task = new XMLTask(urlString, null, null, callback);
        task.execute();
    }


    /**
     * Helper method to parse XML.
     *
     * @param myParser XmlPullParser instance to be used for parsing.
     */
    private void parseXMLAndStoreIt(XmlPullParser myParser) {
        int event;
        String text=null;

        try {
            event = myParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String name=myParser.getName();
                switch (event){
                    case XmlPullParser.START_TAG:
                        break;
                    case XmlPullParser.TEXT:
                        text = myParser.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if(name.equals("title")){
                            titles.add(text);
                        }
                        else if(name.equals("link")){
                            links.add(text);
                        }
                        else if(name.equals("description")){
                            descriptions.add(text);
                        }
                        else if(name.equals("image")){
                            imageURLS.add(text);
                        }
                        else{
                        }
                        break;
                }
                event = myParser.next();

            }

            // Clear results (in case it contains values from a previous query)
            results.clear();

            for(int i = 0; i < titles.size(); i++){
                HashMap<String, String> entry = new HashMap<String, String>();

                entry.put("title", titles.get(i));

                if(links.size() > i){
                    entry.put("link", links.get(i));
                }

                if(descriptions.size() > i){
                    entry.put("description", descriptions.get(i));
                }

                if(imageURLS.size() > i){
                    entry.put("image", imageURLS.get(i));
                }

                results.add(entry);
            }

            parsingComplete = false;
        } catch (Exception e) {
            e.printStackTrace();
            parsingComplete = false;
        }

    }


    /**
     * Parses the xml file only paying attention to the tags specified.
     * This method is useable without the Android API.
     *
     * @param fields An array of strings with each string representing an XML tag to be parsed.
     */
    public void fetchXMLForKeys_Support(final String... fields){

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection)
                            url.openConnection();
                    conn.setReadTimeout(20000 /* milliseconds */);
                    conn.setConnectTimeout(30000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream stream = conn.getInputStream();

                    // Get XML string and escape it
                    String XMLString = getStringForXMLData(stream);
                    String escapedXMLString = removeHTMLEntities(XMLString);

                    if(escapedXMLString.equalsIgnoreCase("No results")){
                        results.clear();
                        parsingComplete = false;
                        stream.close();
                        return;
                    }

                    // Convert escaped string back to stream data
                    stream = new ByteArrayInputStream(escapedXMLString.getBytes("UTF-8"));

                    xmlFactoryObject = XmlPullParserFactory.newInstance();
                    XmlPullParser myParser = xmlFactoryObject.newPullParser();

                    myParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myParser.setInput(stream, null);
                    parseXMLAndStoreIt(myParser, fields);
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    parsingComplete = false;
                }
            }
        });

        thread.start();
    }

    /**
     * Asynchronously parses the xml file only paying attention to the tags specified, and then
     * calls the callback provided once parsing is finished.
     * This method CANNOT be used without the Android API.
     *
     * @param callback A bit of code that is executed on completion of the xml fetch.
     * @param fields An array of strings with each string representing an XML tag to be parsed.
     */
    public void fetchXMLForKeysWithCallback(XMLTask.XMLCallback callback, final String... fields){
        XMLTask task = new XMLTask(urlString, null, null, callback);
        task.execute(fields);
    }


    /**
     * A method to be used to parse remote XML files that require authentication to access.
     * This method can be used without the Android API.
     *
     * @param username username for authentication.
     * @param password password for authentication.
     * @param fields An array of strings with each string representing an XML tag to be parsed.
     */
    public void fetchXMLForKeysWithAuthentication_Support(final String username, final String password, final String... fields){

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection)
                            url.openConnection();
                    conn.setReadTimeout(20000 /* milliseconds */);
                    conn.setConnectTimeout(30000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    String basicAuth = username + ":" + password;
                    basicAuth = "Basic " + new String(Base64.encode(basicAuth.getBytes(), Base64.NO_WRAP));
                    conn.setRequestProperty("Authorization",basicAuth);
                    conn.connect();
                    InputStream stream = conn.getInputStream();

                    // Get XML string and escape it
                    String XMLString = getStringForXMLData(stream);
                    String escapedXMLString = removeHTMLEntities(XMLString);

                    Log.d("POKEMON", "x"+escapedXMLString.trim()+"x");
                    if(escapedXMLString.trim().equalsIgnoreCase("No results") || escapedXMLString.equalsIgnoreCase("")){
                        results.clear();
                        parsingComplete = false;
                        stream.close();
                        return;
                    }

                    // Convert escaped string back to stream data
                    stream = new ByteArrayInputStream(escapedXMLString.getBytes("UTF-8"));


                    xmlFactoryObject = XmlPullParserFactory.newInstance();
                    XmlPullParser myParser = xmlFactoryObject.newPullParser();

                    myParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myParser.setInput(stream, null);
                    parseXMLAndStoreIt(myParser, fields);
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    parsingComplete = false;
                }
            }
        });

        thread.start();
    }

    /**
     * Asynchronously parse remote XML files that require authentication to access, and then
     * calls the callback provided once parsing is finished.
     * This method CANNOT be used without the Android API.
     *
     * @param username username for authentication.
     * @param password password for authentication.
     * @param callback A bit of code that is executed on completion of the xml fetch.
     * @param fields An array of strings with each string representing an XML tag to be parsed.
     */
    public void fetchXMLForKeysWithAuthentication(final String username, final String password, XMLTask.XMLCallback callback, final String... fields){
        XMLTask task = new XMLTask(urlString, username, password, callback);
        task.execute(fields);
    }


    /**
     * Helper method to parse XML only paying attention to the specified tags.
     *
     * @param myParser XmlPullParser instance to be used for parsing.
     * @param fields An array of strings with each string representing an XML tag to be parsed.
     */
    private void parseXMLAndStoreIt(XmlPullParser myParser, String... fields) {
        int event;
        String text=null;

        // 2d Array - where row is "required xml field" and column is "array of values for that field"
        ArrayList<ArrayList<String>> listOfFieldValues = new ArrayList<ArrayList<String>>();

        // Initialize list of field values
        for(int i = 0; i < fields.length; i++){
            listOfFieldValues.add(new ArrayList<String>());
        }

        try {
            event = myParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String name=myParser.getName();
                switch (event){
                    case XmlPullParser.START_TAG:
                        break;
                    case XmlPullParser.TEXT:
                        text = myParser.getText();
                        break;

                    case XmlPullParser.END_TAG:

                        // get data of required fields
                        for(int i = 0; i < fields.length; i++){
                            String field = fields[i];

                            if(name.equals(field)){
                                listOfFieldValues.get(i).add(text);
                            }

                        }


                        break;
                }
                event = myParser.next();

            }

            // Clear results (in case it contains values from a previous query)
            results.clear();

            // populate results variable with a dictionary where each key corresponds to a
            // specified xml field and maps to an array containing all the values of said field.
            if(!listOfFieldValues.isEmpty()){
                for(int i = 0; i < listOfFieldValues.get(0).size(); i++){
                    HashMap<String, String> entry = new HashMap<String, String>();

                    for(int j = 0; j < fields.length; j++){
                        entry.put(fields[j], listOfFieldValues.get(j).get(i));
                    }

                    results.add(entry);
                }

                parsingComplete = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            parsingComplete = false;
        }

    }


    /************************ HELPER METHODS *******************************/
    private String getStringForXMLData(InputStream inputStream){
        String result = "";

        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder strB = new StringBuilder();
            String str;
            while (null != (str = input.readLine())) {
                strB.append(str).append("\r\n");
            }
            input.close();
            result = strB.toString();
        } catch (IOException e) {
            e.printStackTrace();
            parsingComplete = false;
        }

        return result;
    }

    private String removeHTMLEntities(String string){
        return  string.replaceAll("&.{0,}?;", "");
    }


    /**************************** Async Tasks *********************************/

    /** Asynchronous task to query the network connection **/
    public static class NetworkTask extends AsyncTask<Void, Void, Boolean> {

        public interface NetworkCallback {
            void onComplete(Boolean status);
        }

        NetworkCallback delegate;
        int myMax;

        NetworkTask(NetworkCallback callback){
            delegate = callback;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // ping
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                Log.e(TAG, "Error checking internet connection", e);
            }

            return false;
            //return ping("http://www.google.com", 10000);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            delegate.onComplete(result);
        }

        /**
         * Ping a URL to check internet connection
         *
         * @param url URL to be pinged.
         * @param timeout Amount of seconds to wait for response.
         * @return true if there is a response from the pinged URL.
         */
        public static boolean ping(String url, int timeout) {
            url = url.replaceFirst("https", "http"); // Otherwise an exception may be thrown on invalid SSL certificates.

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);
                connection.setRequestMethod("HEAD");
                int responseCode = connection.getResponseCode();
                return (200 <= responseCode && responseCode <= 399);
            } catch (IOException exception) {
                return false;
            }
        }

    }

    /** Asynchronous Task for parsing XML files  **/
    public static class XMLTask extends AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {

        public interface XMLCallback {
            void onComplete(ArrayList<HashMap<String, String>> result);
        }

        private XMLCallback delegate;
        private XmlPullParserFactory xmlFactoryObject;
        private String urlString;
        private String username;
        private String password;

        XMLTask(String URLString, String username, String password, XMLCallback callback){
            this.delegate = callback;
            this.urlString = URLString;
            this.username = username;
            this.password = password;
        }

        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(String... params) {
            try{
                /* Get XML data */
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection)
                        url.openConnection();
                conn.setReadTimeout(20000 /* milliseconds */);
                conn.setConnectTimeout(30000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                if(username != null && password != null){
                    String basicAuth = username + ":" + password;
                    basicAuth = "Basic " + new String(Base64.encode(basicAuth.getBytes(), Base64.NO_WRAP));
                    conn.setRequestProperty("Authorization",basicAuth);
                }
                conn.connect();
                InputStream stream = conn.getInputStream();

                /* Get XML string and escape it */
                String XMLString = getStringForXMLData(stream);
                String escapedXMLString = removeHTMLEntities(XMLString);

                if(escapedXMLString.equalsIgnoreCase("No results")){
                    stream.close();
                    return null;
                }

                /* Convert escaped string back to stream data */
                stream = new ByteArrayInputStream(escapedXMLString.getBytes("UTF-8"));

                xmlFactoryObject = XmlPullParserFactory.newInstance();
                XmlPullParser myParser = xmlFactoryObject.newPullParser();
                myParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                myParser.setInput(stream, null);
                ArrayList<HashMap<String, String>> result;
                if(params != null && params.length > 0){
                    result = parseXMLAndStoreIt(myParser, params);
                }
                else{
                    /* User did not provide fields. Use default ones */
                    result = parseXMLAndStoreIt(myParser, "title", "link", "description", "image");
                }
                stream.close();
                return result;
            }
            catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
            super.onPostExecute(result);
            delegate.onComplete(result);
        }

        public ArrayList<HashMap<String, String>> parseXMLAndStoreIt(XmlPullParser myParser, String... fields) {
            int event;
            String text=null;

            // 2d Array - where row is "required xml field" and column is "array of values for that field"
            ArrayList<ArrayList<String>> listOfFieldValues = new ArrayList<ArrayList<String>>();

            // Initialize list of field values
            for(int i = 0; i < fields.length; i++){
                listOfFieldValues.add(new ArrayList<String>());
            }

            try {
                event = myParser.getEventType();
                while (event != XmlPullParser.END_DOCUMENT) {
                    String name=myParser.getName();
                    switch (event){
                        case XmlPullParser.START_TAG:
                            break;
                        case XmlPullParser.TEXT:
                            text = myParser.getText();
                            break;

                        case XmlPullParser.END_TAG:

                            // get data of required fields
                            for(int i = 0; i < fields.length; i++){
                                String field = fields[i];

                                if(name.equals(field)){
                                    listOfFieldValues.get(i).add(text);
                                }

                            }


                            break;
                    }
                    event = myParser.next();

                }

                // Initialize return value
                ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();

                // populate results variable with a dictionary where each key corresponds to a
                // specified xml field and maps to an array containing all the values of said field.
                if(!listOfFieldValues.isEmpty()){
                    for(int i = 0; i < listOfFieldValues.get(0).size(); i++){
                        HashMap<String, String> entry = new HashMap<String, String>();

                        for(int j = 0; j < fields.length; j++){
                            if(listOfFieldValues.get(j).size() > i){
                                entry.put(fields[j], listOfFieldValues.get(j).get(i));
                            }
                        }

                        results.add(entry);
                    }
                }

                return results;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        private String getStringForXMLData(InputStream inputStream){
            String result = "";

            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StringBuilder strB = new StringBuilder();
                String str;
                while (null != (str = input.readLine())) {
                    strB.append(str).append("\r\n");
                }
                input.close();
                result = strB.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        private String removeHTMLEntities(String string){
            return  string.replaceAll("&.{0,}?;", "");
        }
    }


}









