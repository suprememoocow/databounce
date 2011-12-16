var DataBounce = function(dataUrl, config) {
    config = config || {};
        
    var defaults = {
        formName: "exportform",
        filename: "",
        exportFrameName: "exportFrame",
        postUrl: "dataBounce"
    };

    /** Apply defaults to the config */
    for(var key in defaults) {
        if(!config[key]) {
            config[key] = defaults[key];
        }
    }

    var isIE = function() {
        return navigator.appName == 'Microsoft Internet Explorer';            
    };
    
    var createExportElements = function() {
        /** This code will only ever be used in IE, so it uses well-known workarounds to internet explorer bugs (which won't work in other browsers) */
        var iframe = document.createElement("<IFRAME name='" + config.exportFrameName + "'>");
        iframe.id = config.exportFrameName;
        iframe.style.display = "none";
        document.body.appendChild(iframe);
        
        var form = document.createElement("form");
        form.id = config.exportFrameName;
        form.method = "post";
        form.action = config.postUrl;
        form.style.display = "none";
        form.target = config.exportFrameName;
        
        var filename = document.createElement("<input name='filename'>");
        filename.type = "hidden";
        
        var data = document.createElement("<input name='data'>");
        data.type = "hidden";
        
        form.appendChild(filename);
        form.appendChild(data);
        
        document.body.appendChild(form);
        
        iframe.setAttribute("name", config.exportFrameName);
        
        return form;
    }
    
    return {
        open: function() {
            if(isIE()) {
                var form = document.getElementById(config.formName);
                if(form == null) {
                    form = createExportElements();
                }
                var dataElement = form.elements['data']; 
                var filenameElement = form.elements['filename'];
                
                dataElement.value = dataUrl;
                filenameElement.value = config.filename;
                
                form.submit();
            } else {        
                window.location.href = dataUrl;
            }
        }
    }
}