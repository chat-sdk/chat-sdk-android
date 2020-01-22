package sdk.chat.examples;

import co.chatsdk.core.utils.DisposableMap;
import io.reactivex.functions.Consumer;

public class BaseExample implements Consumer<Throwable> {

    // Add the disposables to a map so you can dispose of them all at one time
    protected DisposableMap dm = new DisposableMap();

    @Override
    public void accept(Throwable throwable) throws Exception {
        // Handle exception
    }

}
