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
		return execute(Method.GET, url, cookies, data);
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
		return execute(Method.POST, url, cookies, data);
	}

	private Response execute(Method method, String url, Map<String, String> cookies, String... data) throws IOException {
		Response response = Jsoup.connect(url).ignoreContentType(true).method(method).cookies(cookies).userAgent("Mozilla").data(data).execute();
		cookies.putAll(response.cookies());
		return response;
	}
	
}
