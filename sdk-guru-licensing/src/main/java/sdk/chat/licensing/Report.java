package sdk.chat.licensing;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
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
        String email = ChatSDK.shared().getLicenseEmail();

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("email", email);
        map.put("modules", modules);

        Gson gson = new Gson();
        String json = gson.toJson(map);

//        OkHttpClient client = new OkHttpClient();

//        MediaType JSON = MediaType.get("application/json; charset=utf-8");
//        RequestBody body = RequestBody.create(json, JSON);
//        Request request = new Request.Builder()
//                .url("https://chatsdk.co/log.php")
//                .post(body)
//                .build();
//        try {
//            Response response = client.newCall(request).execute();
//        } catch (Exception e) {
//            if (ChatSDK.shared().isActive()) {
//                ChatSDK.events().onError(e);
//            }
//        }

    }

}
