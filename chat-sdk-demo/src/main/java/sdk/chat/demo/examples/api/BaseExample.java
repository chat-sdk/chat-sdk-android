package sdk.chat.demo.examples.api;

import io.reactivex.functions.Consumer;
import sdk.guru.common.DisposableMap;

public class BaseExample implements Consumer<Throwable> {

    // Add the disposables to a map so you can dispose of them all at one time
    protected DisposableMap dm = new DisposableMap();

    @Override
    public void accept(Throwable throwable) throws Exception {
        // Handle exception
    }

}
