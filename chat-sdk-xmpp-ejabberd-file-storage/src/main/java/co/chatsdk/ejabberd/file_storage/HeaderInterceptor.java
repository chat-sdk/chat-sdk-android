package co.chatsdk.ejabberd.file_storage;

import com.androidnetworking.common.RequestBuilder;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HeaderInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request().newBuilder().removeHeader("Accept-Encoding").removeHeader("Keep-Alive").removeHeader("User-Agent").removeHeader("Content-Type").build();
        System.out.println("Log intercept");
        Response response = chain.proceed(request);
        return response;
    }
}
