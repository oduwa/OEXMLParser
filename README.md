OEXMLParser
===========

A Robust Asynchronous XML Parser for Android


**OEXMLParser** is a helper class that allows for simple and flexible parsing of XML files and **particularly useful for RSS feeds**. Read below to see how simple and awesome it is!

## Getting Started ##

You can simply create an OEXMLParser object and pass it the source URL for the XML as a String and then call ``` fetchXMLWithCallback() ```. Within the callback you can specify what to be done with the results once the XML data has been parsed.

```java
  String urlString = "http://www.SomeAwesomeFeed/rss";
  OEXMLParser parser = new OEXMLParser(urlString);
  parser.fetchXMLWithCallback(new OEXMLParser.XMLTask.XMLCallback() {
            @Override
            public void onComplete(ArrayList<HashMap<String, String>> result) {
                if(result != null){
                   // Retreive results
                   ArrayList<HashMap<String, String>> parsedStuff = results;
                }
                else{
                    // error occured. 
                }
            }
        });
```

If for whatever reason, you need to parse the data synchronously, instead of calling ```fetchXMLWithCallback()```, you call ```fetchXML_Support()```. When Parsing is complete, get the results with the ``` getResults() ``` method This will return an array list of dictionaries (Hashmaps) where the keys are the XML tags and the corresponding values are the values within the XML tags. This is the synchronous approach.

```java
  String urlString = "http://www.SomeAwesomeFeed/rss";
  OEXMLParser parser = new OEXMLParser(urlString);
  parser.fetchXML_Support();
  
  while(parser.parsingComplete);
  
  ArrayList<HashMap<String, String>> results = parser.getResults();
```

Do note that the method ``` fetchXML() ``` is the default method and is intended for the simple and average RSS XML use case, therefore it only returns the "title", "description", "link", "image" and "pubDate" tags.

## Flexible And Custom Usage ##
For more practical use cases, you will probably need more than just the default tags specified above. With **OEXMLParser** you can specify what XML tags you want to retrieve. In this case you can use the ``` fetchXMLForKeys() ``` method. An example is shown below

```java
  String urlString = "http://www.SomeAwesomeFeed/rss";
  OEXMLParser parser = new OEXMLParser(urlString);
  parser.fetchXMLForKeys("title", "specialImage", "link" "video", "someOtherCoolTag");
  
  while(parser.parsingComplete);
  
  ArrayList<HashMap<String, String>> results = parser.getResults();
```

Again, the result is an array list of dictionaries (Hashmaps) where the keys are the XML tags and the corresponding values are the values within the XML tags.

## HTTP Authentication ##
Not impressed with **OEXMLParser** yet. Well make arrangements for a brain transplant because your mind is about to be blown! **OEXMLParser** also supports Http authentication for those situations where authentication is needed to access the desired XML files. This is achieved by using the ``` fetchXMLForKeysWithAuthentication() ``` method. An example is shown below

```java
  String urlString = "http://www.SomeAwesomeFeed/rss";
  OEXMLParser parser = new OEXMLParser(urlString);
  parser.fetchXMLForKeysWithAuthentication("myUsername", "myPassword", "title", "image");
  
  while(parser.parsingComplete);
  
  ArrayList<HashMap<String, String>> results = parser.getResults();
```

The first 2 arguments of the ``` fetchXMLForKeysWithAuthentication() ``` method are the Username and Password for authentication. All subsequent arguments are the XML tags to be retreived.

## The Result ##

As I mentioned above, the result is an array list of dictionaries (Hashmaps) where the keys are the XML tags and the corresponding values are the values within the XML tags. Below is a code sample to illustrate this. 

```java
  for(HashMap<String, String> entry : obj.getResults()){
      System.out.println("Title: " + entry.get("title"));
      System.out.println("Description: " + entry.get("description"));
      System.out.println("Link: " + entry.get("link"));
      System.out.println("Image URL: " + entry.get("image"));
      System.out.println();
  }
```

