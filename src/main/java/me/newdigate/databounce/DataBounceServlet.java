package me.newdigate.databounce;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

/**
 * Allow Internet Explorer to download data url files larger than ~2k
 */
public class DataBounceServlet extends HttpServlet {

    private static final String REQUIRE_LOGGED_IN_USER_PARAM = "requireLoggedInUser";
    private static final String FILENAME_PARAM = "filename";
    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    private static final String DEFAULT_CHARSET = "US-ASCII";

    private static final long serialVersionUID = -3930208419053616713L;

    private String defaultFileName;
    private boolean requireLoggedInUser;

    /**
     * Initialise default values
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        defaultFileName = getInitParameter(FILENAME_PARAM);

        String requireLoggedInUserStr = getInitParameter(REQUIRE_LOGGED_IN_USER_PARAM);
        requireLoggedInUser = requireLoggedInUserStr == null ? false : new Boolean(requireLoggedInUserStr);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        verifyRequest(req);

        DataBounceRequest request = new DataBounceRequest(req);

        if (request.filename == null) {
            resp.setHeader("Content-Disposition", "attachment");
        } else {
            resp.setHeader("Content-Disposition", "attachment; filename=" + request.filename);
        }

        resp.setContentType(request.mimeType);
        resp.getOutputStream().print(request.data);
    }

    /**
     * Verify that this request is valid
     */
    protected void verifyRequest(HttpServletRequest req) throws ServletException, MalformedURLException {
        if (requireLoggedInUser) {
            if (req.getRemoteUser() == null) {
                throw new ServletException("This servlet can only be used by logged in users");
            }
        }

        String referrer = req.getHeader("Referer");
        URL x = new URL(referrer);

        if (!req.getServerName().equals(x.getHost())) {
            throw new ServletException("This servlet can only be used locally.");
        }
    }

    /**
     * Parse the data url
     */
    class DataBounceRequest {

        private final String mimeType;
        private final String charSet;
        private final String data;
        private final String filename;

        public DataBounceRequest(HttpServletRequest req) throws ServletException, UnsupportedEncodingException {
            String requestData = req.getParameter("data");
            if (requestData == null || !requestData.startsWith("data:")) {
                throw new ServletException("Invalid format");
            }
            requestData = requestData.substring(5);

            String requestFilename = req.getParameter(FILENAME_PARAM);
            this.filename = requestFilename == null ? defaultFileName : requestFilename;

            if (requestData == null) {
                throw new ServletException("No data supplied");
            }

            String[] a = requestData.split(",", 2);
            if (a.length != 2) {
                throw new ServletException("Illegal data format");
            }

            String header = a[0];
            List<String> headers = new ArrayList<String>(Arrays.asList(header.split(";")));
            if (headers.size() > 0) {
                mimeType = headers.remove(0);
            } else {
                mimeType = DEFAULT_MIME_TYPE;
            }

            boolean base64 = headers.remove("base64");

            if (!headers.isEmpty()) {
                String contentTypeHeader = headers.remove(0);
                String[] contentTypeArray = contentTypeHeader.split("=", 2);
                if (!"charset".equals(contentTypeArray[0])) {
                    throw new ServletException("Illegal data format");
                }

                charSet = contentTypeArray[1];

            } else {
                charSet = DEFAULT_CHARSET;
            }

            /* More, unexpected, headers? */
            if (!headers.isEmpty()) {
                throw new ServletException("Illegal data format");
            }

            /* Decode base64 if required */
            String content = a[1];
            if (base64) {
                byte[] b = DatatypeConverter.parseBase64Binary(content);
                this.data = new String(b, charSet);
            } else {
                this.data = content;
            }
        }

    }
}
