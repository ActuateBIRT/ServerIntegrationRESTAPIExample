#Server Integration REST API Example

##Overview

This is a set of REST API examples intended to show specific API features that may be difficult to understand, such as:

* Using all HTTP verbs supported in REST API: GET, POST, PUT, DELETE.
* Retrieving a long list of items using fetching mechanism.
* Using multi-part requests/responses for file upload/download, with and without chunking/streaming, as Base64-encoded and as plain binary.
* Passing certain request parameters in the HTTP header, such as authentication ID or volume name.
* Passing JSON-formatted request body for certain requests.
* Parsing error responses and error response structure.
* more

There are several other sample apps, which have UIs and show how to use the REST API together with JSAPI. The current set of examples focuses on pure-API experience and is not meant to cover the whole spectrum of REST API requests/features.

The examples are implemented using industry-favorite tools, such as HTTPClient 4.3.x for HTTP processing and GSON 2.3 for response parsing.

##Setup

No special setup is required. You can control certain properties using command line parameters however (see below).

##Usage

For building and running examples, run the provided Ant script. The script recognizes several command line parameters. All parameters are optional:

* restapi.host - REST API server host name; localhost by default
* restapi.port - REST API server port number; 5000 by default
* restapi.volume - Volume name to which to send requests; Default Volume is the default value
* restapi.gzip - Whether to allow compressed responses from REST server; useful for debugging in TCP tunnel; false by default

Here is an example command line:

```
ant -Drestapi.host=restServerHostName -Drestapi.port=5000 -Drestapi.volume="My Volume Name" -Drestapi.gzip=true 
```

##Troubleshooting

* Read comments in the code. They may explain how certain features work.
* Setup a TCP tunnel to see all REST API traffic between examples and the REST server.