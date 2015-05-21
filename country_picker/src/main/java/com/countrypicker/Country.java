package com.countrypicker;

import android.util.Log;

import java.lang.reflect.Field;
import java.util.Locale;

/**
 * POJO
 *
 */
public class Country {
	private String code;
	private String name;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


    /**
     * The drawable image name has the format "flag_$countryCode". We need to
     * load the drawable dynamically from country code. Code from
     * http://stackoverflow.com/
     * questions/3042961/how-can-i-get-the-resource-id-of
     * -an-image-if-i-know-its-name
     *
     * @param countryCode
     * @return
     */
    public static int getResId(String countryCode) {
		String drawableName = "flag_"
				+ countryCode.toLowerCase(Locale.ENGLISH);

		if (BuildConfig.DEBUG) Log.v(Country.class.getSimpleName(), String.format("getResId, Name: %s", drawableName));

        try {
            Class<R.drawable> res = R.drawable.class;
            Field field = res.getField(drawableName);
            int drawableId = field.getInt(null);
            return drawableId;
        } catch (Exception e) {
			e.printStackTrace();
           if (BuildConfig.DEBUG) Log.e(Country.class.getSimpleName(), "cant get the drawable id for country code");
        }
        return -1;
    }
}