The code in this repository was created in 2021.

# Description
JsonParserWrapper is a wrapper around 
<a href="https://fasterxml.github.io/jackson-core/javadoc/2.8/com/fasterxml/jackson/core/JsonParser.html">Jackson's JsonParser</a> 
that simplifies JSON parsing by abstracting away low-level `JsonParser` logic.  


The main motivation behind this library was to extract a JSON array from a JSON object and access it as a Java Stream&lt;T&gt;, where `T` represents each object in the array. This library processes JSON input as an InputStream, allowing it to handle large JSON arrays efficiently without loading all data into memory, preventing OutOfMemoryError exceptions.  


### Example
The `phone_numbers` array inside the following JSON can be read as a Stream&lt;String&gt;:
```
{
    "name" : "John",
    "age" : 30,
    "contact_details" : {
        "emails" : ["example@example.com","email@email.com"],
        "phone_numbers" : ["+60112398402","+60162598413"]
    }
}
```
In addition to the JSON InputStream, we need to pass the path to the array that we need to parse, for example 
in the previous JSON, the path for `phone_numbers` is provided as a list: `["contact_details","phone_numbers"]`. 