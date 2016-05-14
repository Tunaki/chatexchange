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
	 * Performs a GET HTTP call to the given URL.
	 * @param url URL to GET.
	 * @return <code>Response</code> associated with the result of the request.
	 * @throws IOException in case of errors
	 */
	public Response get(String url, Map<String, String> cookies) throws IOException {
		Response response = Jsoup.connect(url).ignoreContentType(true).method(Method.GET).cookies(cookies).userAgent("Mozilla").execute();
		cookies.putAll(response.cookies());
		return response;
	}
	
	/**
	 * Performs a POST HTTP call to the given URL with the given data. The data corresponds to key=value parameters and,
	 * as such, needs to be an even number.
	 * @param url URL to POST.
	 * @return <code>Response</code> associated with the result of the request.
	 * @throws IOException in case of errors
	 */
	public Response post(String url, Map<String, String> cookies, String... data) throws IOException {
		Response response = Jsoup.connect(url).ignoreContentType(true).method(Method.POST).cookies(cookies).userAgent("Mozilla").data(data).execute();
		cookies.putAll(response.cookies());
		return response;
	}

}
