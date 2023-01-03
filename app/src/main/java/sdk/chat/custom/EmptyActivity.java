package sdk.chat.custom;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;

import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.Nullable;
import io.reactivex.functions.Consumer;
import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;
import sdk.guru.common.RX;

public class EmptyActivity extends Activity {

    public void onCreate(@Nullable Bundle savedInstanceState,
                         @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

    }

    public EmptyActivity() {


////        a().concatWith(b()).subscribe();
//        Disposable d = a().flatMap(new Function<String, SingleSource<?>>() {
//            @Override
//            public SingleSource<?> apply(String s) throws Exception {
//                return b();
//            }
//        }).subscribeOn(RX.io()).flatMap(new Function<Object, SingleSource<?>>() {
//            @Override
//            public SingleSource<?> apply(Object o) throws Exception {
//                return c();
//            }
//        }).ignoreElement().andThen(d()).observeOn(RX.main()).subscribe(o -> {
//            System.out.println(">>>>>>>>> finish :" + Thread.currentThread().getName());
//        });

//        d = a1().andThen(a1()).concatWith(a2()).concatWith(a3()).subscribe(() -> {
//            System.out.println(">>>>>>>>> finish :" + Thread.currentThread().getName());
//        });


        Observable.defer((Callable<ObservableSource<String>>) () -> {
            System.out.println(">>>>>>>>> a :" + Thread.currentThread().getName());

            return Observable.create((ObservableOnSubscribe<String>) emitter -> {
                System.out.println(">>>>>>>>> b :" + Thread.currentThread().getName());
                emitter.onNext("A");
            }).subscribeOn(RX.pool());
        }).flatMapMaybe(fileUploadResult -> {
            System.out.println(">>>>>>>>> c :" + Thread.currentThread().getName());
            return Maybe.create((MaybeOnSubscribe<String>) emitter -> {
                System.out.println(">>>>>>>>> d :" + Thread.currentThread().getName());
                emitter.onSuccess("A");
            }).subscribeOn(RX.io());
        }).firstElement().toSingle().subscribeOn(RX.computation()).subscribe();

        System.out.println("");

    }


    public Single<String> a() {
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            System.out.println(">>>>>>>>> a :" + Thread.currentThread().getName());
            emitter.onSuccess("A");
        });
    }

    public Single<String> b() {
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            System.out.println(">>>>>>>>> b :" + Thread.currentThread().getName());
            emitter.onSuccess("B");
        }).subscribeOn(RX.io());
    }
    public Single<String> c() {
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            System.out.println(">>>>>>>>> c :" + Thread.currentThread().getName());
            emitter.onSuccess("C");
        }).subscribeOn(RX.computation());
    }

    public Single<String> d() {
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            System.out.println(">>>>>>>>> d :" + Thread.currentThread().getName());
            emitter.onSuccess("D");
        }).subscribeOn(RX.single());
    }

    public Completable a1() {
        return Completable.create(emitter -> {
            System.out.println(">>>>>>>>> a1 :" + Thread.currentThread().getName());
            emitter.onComplete();
        });
    }

    public Completable a2() {
        return Completable.create(emitter -> {
            System.out.println(">>>>>>>>> a2 :" + Thread.currentThread().getName());
            emitter.onComplete();
        });
    }

    public Completable a3() {
        return Completable.create(emitter -> {
            System.out.println(">>>>>>>>> a3 :" + Thread.currentThread().getName());
            emitter.onComplete();
        });
    }
}
