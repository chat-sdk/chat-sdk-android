package sdk.chat.licensing;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import sdk.chat.core.session.ChatSDK;

public class Report {

    protected static final Report instance = new Report();
    protected Disposable timerDisposable = null;

    public static Report shared() {
        return instance;
    }

    protected List<String> modules = new ArrayList<>();

    public void add(String name) {
        modules.add(name);
        if (timerDisposable != null) {
            timerDisposable.dispose();
        }
        timerDisposable = Completable.timer(3, TimeUnit.SECONDS).subscribe(this::report);
    }

    protected void report() {
        String id = ChatSDK.ctx().getPackageName();
        String identifier = ChatSDK.shared().getLicenseIdentifier();

        Gson gson = new Gson();
        String json = gson.toJson(modules);

        OkHttpClient client = new OkHttpClient();

        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("path", id.replaceAll("[^a-zA-Z0-9]", "_"))
                .addFormDataPart("id", id)
                .addFormDataPart("modules", json);

        if (identifier != null && !identifier.isEmpty()) {
                builder.addFormDataPart("email", identifier);
                if (identifier.contains("Patreon:")) {
                    builder.addFormDataPart("patreon", identifier.replace("Patreon:", ""));
                }
                if (identifier.contains("Email:")) {
                    builder.addFormDataPart("email", identifier.replace("Email:", ""));
                }
                if (identifier.contains("Github:")) {
                    builder.addFormDataPart("github", identifier.replace("Github:", ""));
                }
        }

        RequestBody body = builder.build();

        Request request = new Request.Builder()
                .url("https://api.sdk.guru/log.php")
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
//            Logger.debug(response);
//            Logger.debug(response.body().string());
//            Logger.debug("");
        } catch (Exception e) {
            if (ChatSDK.shared().isActive()) {
                ChatSDK.events().onError(e);
            }
        }

    }

}
