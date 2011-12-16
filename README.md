DataBounce
==========

data:urls are a useful browser feature which allows developers to generate content within a Javascript client without having to make a server side call. Javascript libraries exist for generating [PDFs](http://code.google.com/p/jspdf/), Excel spreadsheets and other data formats from within a browser without having to make a server http request.

data:urls are supported by all good modern browsers. Unfortunately they are only partially supported by Internet Explorer (surprise, surprise!). An elegant and commonly used workaround for IE is to use the [Downloadify](https://github.com/dcneiner/Downloadify) library, which delegates the download to a tiny Flash actionscript. This is the recommended approach if your clients have the Flash plugin installed. 

Unfortunately sometimes it's impossible to guarantee that the Flash plugin will be available. Databounce is a extremely simply workaround for dealing with data:urls in these environments - and when you have control of the server environment (and can add servlets).
 
Advantages
----------

+ Provides support for data: urls in Internet Explorer
+ No Flash plugin required

Disadvantages
-------------

+ For IE clients, a server side round-trip call is required.
+ You need to install a servlet on your server.

How DataBounce works
--------------------

Once you've generated your data:url, the DataBounce object checks if you are using Internet Explorer. If not, it simply calls <code>window.location.href = dataUrl;</code>. If you are using Internet Explorer, the url is posted via a hidden iframe to a servlet, which decodes the URL and sends it back to the client, with a Content-Disposition header, which will force the client to download the file.

Using DataBounce
----------------

* Include the [DataBounce.js](https://raw.github.com/suprememoocow/databounce/master/src/main/webapp/DataBounce.js) script: `<script type="text/javascript" src="DataBounce.js"></script>`

* Instead of assigning `window.location.href` manually, use the DataBounce class, like so:

    <pre><code>/* Get a buffer in the usual manner */
    var dataUrl = "data:application/pdf;base64," + Base64.encode(/* buffer */);        
    new DataBounce(dataUrl, { filename: "export.pdf" }).open();
    </code></pre>

* Add the servlet [DataBounceServlet.java](https://raw.github.com/suprememoocow/databounce/master/src/main/java/me/newdigate/databounce/DataBounceServlet.java) to your codebase.

* Reference the servlet in your `web.xml`

    <pre><code>&lt;servlet&gt;  
        &lt;servlet-name&gt;data-bounce&lt;/servlet-name&gt;
        &lt;servlet-class&gt;me.newdigate.databounce.DataBounceServlet&lt;/servlet-class&gt;
        &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
    &lt;/servlet&gt;

    &lt;servlet-mapping&gt;
        &lt;servlet-name&gt;data-bounce&lt;/servlet-name&gt;
        &lt;url-pattern&gt;/dataBounce&lt;/url-pattern&gt;
    &lt;/servlet-mapping&gt;</code></pre> 
    
* That's it!    
    