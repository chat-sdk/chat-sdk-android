package co.chatsdk.ui.chat;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import co.chatsdk.core.base.LocationProvider;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.ActivityResultPushSubjectHolder;
import co.chatsdk.core.utils.GoogleUtils;
import co.chatsdk.ui.R;
import io.reactivex.Emitter;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

/**
 * Created by benjaminsmiley-andrews on 23/05/2017.
 */

public class LocationSelector {

    public static final int PICK_LOCATION = 102;

    protected Activity activity;
    protected SingleEmitter<Result> emitter;
    protected Disposable disposable;

    public class Result {
        public LatLng latLng;
        public String snapshotPath;
        public Result (LatLng latLng, String snapshotPath) {
            this.latLng = latLng;
            this.snapshotPath = snapshotPath;
        }
    }

    public Single<Result> startChooseLocationActivity (Activity activity) {
        return ChatSDK.locationProvider().getLastLocation(activity).map(location -> {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            int width = activity.getResources().getDimensionPixelSize(R.dimen.message_image_max_width);
            int height = activity.getResources().getDimensionPixelSize(R.dimen.message_image_max_height);

            return new Result(latLng, GoogleUtils.getMapImageURL(latLng, width, height));
        });

//        return Single.create(emitter -> {
//            emitter.onSuccess(new Result());
//            LocationSelector.this.activity = activity;
//            LocationSelector.this.emitter = emitter;
//
//            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
//
//            Autocomplete.IntentBuilder builder = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields);
//
//            try {
//                Intent intent = builder.build(activity);
//                if(!startActivityForResult(activity, intent, PICK_LOCATION)) {
//                    notifyError(new Exception(activity.getString(R.string.unable_to_start_activity)));
//                }
//            }
//            catch (GooglePlayServicesRepairableException e) {
//                emitter.onError(e);
//            }
//        });
    }

//    protected boolean startActivityForResult (Activity activity, Intent intent, int tag) {
//        if (disposable == null && intent.resolveActivity(activity.getPackageManager()) != null) {
//            disposable = ActivityResultPushSubjectHolder.shared().subscribe(activityResult -> handleResult(activity, activityResult.requestCode, activityResult.resultCode, activityResult.data));
//            activity.startActivityForResult(intent, tag);
//            return true;
//        } else {
//            return false;
//        }
//    }


//    protected void processPickedLocation(Intent data) throws Exception {
//        Place place = PlacePicker.getPlace(activity, data);
//        Result result = new Result();
//        result.latLng = place.getLatLng();
//        notifySuccess(result);
//    }

//    public void handleResult (Activity activity, int requestCode, int resultCode, Intent data) throws Exception {
//
//        disposable.dispose();
//
//        if (resultCode == Activity.RESULT_OK && requestCode == PICK_LOCATION) {
//            processPickedLocation(data);
//        } else {
//            notifyError(new Exception(activity.getString(R.string.error_picking_location)));
//        }
//    }

    protected void notifySuccess (@NotNull Result result) {
        if (emitter != null) {
            emitter.onSuccess(result);
        }
        clear();
    }

    protected void notifyError (@NotNull Throwable throwable) {
        if (emitter != null) {
            emitter.onError(throwable);
        }
        clear();
    }

    public void clear () {
        emitter = null;
    }

}
