package fr.tunaki.stackoverflow.chat;

import java.io.IOException;
import java.util.Map;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

/**
 * Client for raw HTTP requests.
 * <p>It takes a map of current cookies and updates them at each calls.
 * @author Tunaki
 */
class HttpClient {
	
	/**
	 * Performs a HTTP GET to the given URL.
	 * @param url URL to GET.
	 * @param cookies Cookies to send with the request.
	 * @param data GET parameters.
	 * @return <code>Response</code> associated with the result of the request.
	 * @throws IOException in case of errors
	 */
	public Response get(String url, Map<String, String> cookies, String... data) throws IOException {
		return execute(Method.GET, url, cookies, false, data);
	}
	
	/**
	 * Performs a HTTP POST to the given URL.
	 * @param url URL to POST to.
	 * @param cookies Cookies to send with the request.
	 * @param data POST parameters.
	 * @return <code>Response</code> associated with the result of the request.
	 * @throws IOException in case of errors
	 */
	public Response post(String url, Map<String, String> cookies, String... data) throws IOException {
		return execute(Method.POST, url, cookies, false, data);
	}
	
	/**
	 * Performs a HTTP POST to the given URL, not throwing an exception in case the response code isn't 200. In this case,
	 * the response body will contain the error body.
	 * @param url URL to POST to.
	 * @param cookies Cookies to send with the request.
	 * @param data POST parameters.
	 * @return <code>Response</code> associated with the result of the request.
	 * @throws IOException in case of errors
	 */
	public Response postIgnoringErrors(String url, Map<String, String> cookies, String... data) throws IOException {
		return execute(Method.POST, url, cookies, true, data);
	}

	private Response execute(Method method, String url, Map<String, String> cookies, boolean ignoreErrors, String... data) throws IOException {
		Response response = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(ignoreErrors).method(method).cookies(cookies).userAgent("Mozilla").data(data).execute();
		cookies.putAll(response.cookies());
		return response;
	}
	
}
