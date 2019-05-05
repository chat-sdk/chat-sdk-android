package co.chatsdk.ui.chat;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;

import co.chatsdk.core.utils.ActivityResultPushSubjectHolder;
import co.chatsdk.ui.R;
import io.reactivex.Emitter;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;

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
    }

    public Single<Result> startChooseLocationActivity (Activity activity) {
        return Single.create(emitter -> {
            LocationSelector.this.activity = activity;
            LocationSelector.this.emitter = emitter;

            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

            try {
                Intent intent = builder.build(activity);
                if(!startActivityForResult(activity, intent, PICK_LOCATION)) {
                    notifyError(new Exception(activity.getString(R.string.unable_to_start_activity)));
                }
            }
            catch (GooglePlayServicesRepairableException e) {
                emitter.onError(e);
            }
        });
    }

    protected boolean startActivityForResult (Activity activity, Intent intent, int tag) {
        if (disposable == null && intent.resolveActivity(activity.getPackageManager()) != null) {
            disposable = ActivityResultPushSubjectHolder.shared().subscribe(activityResult -> handleResult(activity, activityResult.requestCode, activityResult.resultCode, activityResult.data));
            activity.startActivityForResult(intent, tag);
            return true;
        } else {
            return false;
        }
    }


    protected void processPickedLocation(Intent data) throws Exception {
        Place place = PlacePicker.getPlace(activity, data);
        Result result = new Result();
        result.latLng = place.getLatLng();
        notifySuccess(result);
    }

    public void handleResult (Activity activity, int requestCode, int resultCode, Intent data) throws Exception {

        disposable.dispose();

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_LOCATION) {
            processPickedLocation(data);
        } else {
            notifyError(new Exception(activity.getString(R.string.error_picking_location)));
        }
    }

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
