package sylllys.insideout.factories;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sylllys.insideout.controllers.InsideOutController;
import sylllys.insideout.entities.exceptions.ExceptionStackTraceResponseBody;

public class HTTPFactory {

  private static final Logger logger = LogManager.getLogger(HTTPFactory.class);

  CookieManager cookieManager;
  String redirectURL;
  HttpServletRequest request;
  HttpServletResponse response;

  HttpURLConnection urlConnection;

  public HTTPFactory(HttpServletRequest request, HttpServletResponse response, String redirectURL) {

    this.request = request;
    this.response = response;
    this.redirectURL = redirectURL;
    cookieManager = new CookieManager();
    CookieHandler.setDefault(cookieManager);
  }

  private URIBuilder getURIDetails() throws URISyntaxException {

    URIBuilder uriDetails = new URIBuilder(redirectURL);

    Enumeration<String> parameterNames = request.getParameterNames();

    while (parameterNames.hasMoreElements()) {

      String paramName = parameterNames.nextElement();
      request.getHeader(paramName);
      uriDetails.addParameter(paramName, request.getParameter(paramName));

    }

    return uriDetails;
  }

  private void createURLConnection() throws URISyntaxException, IOException {

    URL url = getURIDetails().build().toURL();
    urlConnection = (HttpURLConnection) url.openConnection();
    urlConnection.setRequestMethod(request.getMethod());
  }

  private void setURLConnectionHeaderDetails() {

    Enumeration<String> headerNames = request.getHeaderNames();

    while (headerNames.hasMoreElements()) {

      String headerName = headerNames.nextElement();
      request.getHeader(headerName);

      if (!headerName.equals("insideout-redirect-url")) {
        urlConnection.setRequestProperty(headerName, request.getHeader(headerName));
      }

    }
  }

  private void setURLConnectionCookieDetails() {

    if (request.getCookies() != null) {
      urlConnection.setRequestProperty("Cookie", request.getCookies().toString());
    }
  }

  private void setURLConnectionOutputStreamDetails() throws IOException {

    if (!request.getMethod().equalsIgnoreCase("GET")) {
      urlConnection.setDoOutput(true);
      OutputStream urlConnectionOutputStream = urlConnection.getOutputStream();
      OutputStreamWriter urlConnectionOutputStreamWriter = new OutputStreamWriter(
          urlConnectionOutputStream, request.getCharacterEncoding());
      urlConnectionOutputStreamWriter
          .write(request.getReader().lines().collect(Collectors.joining()));
      urlConnectionOutputStreamWriter.flush();
      urlConnectionOutputStreamWriter.close();
      urlConnectionOutputStream.close();
    }
  }

  private void prepareHTTPURLConnection() throws IOException, URISyntaxException {

    createURLConnection();
    setURLConnectionHeaderDetails();
    setURLConnectionCookieDetails();
    setURLConnectionOutputStreamDetails();
  }

  private void redirectURLConnectionInputStreamToResponseOutputStream() throws IOException {

    InputStream inputStream;

    try {
      inputStream = urlConnection.getInputStream();
    } catch (Exception e) {
      inputStream = urlConnection.getErrorStream();
    }

    if (inputStream != null) {
      OutputStream outputStream = response.getOutputStream();

      IOUtils.copy(inputStream, outputStream);
    }
  }

  private void setResponseHeadersFromURLConnection() {

    Map<String, List<String>> map = urlConnection.getHeaderFields();
    for (Map.Entry<String, List<String>> entry : map.entrySet()) {

      if (entry.getKey() != null) {

        if (entry.getKey().equalsIgnoreCase("Date")
            || entry.getKey().equalsIgnoreCase("Content-Length")) {
          continue;
        }

        if (urlConnection.getHeaderField(entry.getKey()).matches("\\d+")) {
          response
              .addIntHeader(entry.getKey(),
                  Integer.parseInt(urlConnection.getHeaderField(entry.getKey())));
        } else {
          response.addHeader(entry.getKey(), urlConnection.getHeaderField(entry.getKey()));
        }
      } else {
        response.addHeader("null", urlConnection.getHeaderField(entry.getKey()));
      }
    }
  }

  private void setResponseCookies() {

    List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
    for (HttpCookie cookie : cookies) {
      Cookie oC = new Cookie(cookie.getName(), cookie.getValue());

      oC.setDomain(cookie.getDomain().charAt(0) == '.' ? cookie.getDomain().substring(1)
          : cookie.getDomain());
      oC.setMaxAge((int) cookie.getMaxAge());
      oC.setPath(cookie.getPath());
      oC.setSecure(cookie.getSecure());

      response.addCookie(oC);
    }
  }

  private void captureHTTPURLConnectionResponse() throws IOException {

    redirectURLConnectionInputStreamToResponseOutputStream();
    response.setStatus(urlConnection.getResponseCode());
    setResponseHeadersFromURLConnection();
    response.setCharacterEncoding(urlConnection.getContentEncoding());
    setResponseCookies();

  }

  public void redirectRequest() {

    try {

      prepareHTTPURLConnection();
      captureHTTPURLConnectionResponse();
    } catch (Exception e) {

      logger.error("Exception occured while redirecting a request", e);
    } finally {

      urlConnection.disconnect();
      CookieHandler.setDefault(null);
    }
  }
}
