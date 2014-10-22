package com.odie.animehub;

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

    public OEXMLParser(String url){
        this.urlString = url;
        this.results = new ArrayList<HashMap<String, String>>();
        this.titles = new ArrayList<String>();
        this.links = new ArrayList<String>();
        this.descriptions = new ArrayList<String>();
        this.imageURLS = new ArrayList<String>();
    }

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


    public void fetchXML(){

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


    public void parseXMLAndStoreIt(XmlPullParser myParser) {
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



    public void fetchXMLForKeys(final String... fields){

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


    public void fetchXMLForKeysWithAuthentication(final String username, final String password, final String... fields){

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
                    String basicAuth = username + ":" + password;
                    basicAuth = "Basic " + new String(Base64.encode(basicAuth.getBytes(), Base64.NO_WRAP));
                    conn.setRequestProperty("Authorization",basicAuth);
                    conn.connect();
                    InputStream stream = conn.getInputStream();

                    // Get XML string and escape it
                    String XMLString = getStringForXMLData(stream);
                    String escapedXMLString = removeHTMLEntities(XMLString);

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


    public void parseXMLAndStoreIt(XmlPullParser myParser, String... fields) {
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



}








