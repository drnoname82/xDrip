package com.eveningoutpost.dexdrip;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.eveningoutpost.dexdrip.Models.JoH;
import com.eveningoutpost.dexdrip.Models.Sensor;
import com.eveningoutpost.dexdrip.Models.UserError.Log;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.eveningoutpost.dexdrip.Models.Calibration;
import com.eveningoutpost.dexdrip.UtilityModels.CollectionServiceStarter;
import com.eveningoutpost.dexdrip.UtilityModels.Constants;
import com.eveningoutpost.dexdrip.utils.ActivityWithMenu;
import com.eveningoutpost.dexdrip.wearintegration.WatchUpdaterService;
import static com.eveningoutpost.dexdrip.Home.startWatchUpdaterService;


public class DoubleCalibrationActivity extends ActivityWithMenu {
    Button button;
    public static String menu_name = "Add Double Calibration";
    private static final String TAG = "DoubleCalib";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        xdrip.checkForcedEnglish(this);
        super.onCreate(savedInstanceState);
        if (CollectionServiceStarter.isBTShare(getApplicationContext())) {
            Intent intent = new Intent(this, Home.class);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_double_calibration);
        addListenerOnButton();
    }

    @Override
    protected void onResume() {
        xdrip.checkForcedEnglish(this);
        super.onResume();
    }

    @Override
    public String getMenuName() {
        return menu_name;
    }

    public void addListenerOnButton() {

        button = (Button) findViewById(R.id.save_calibration_button);
        final Activity activity = this;
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                try {
                    View view = activity.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                } catch (Exception e) {
                    // failed to close keyboard
                }

                if (Sensor.isActive()) {
                    final EditText value_1 = (EditText) findViewById(R.id.bg_value_1);
                    final EditText value_2 = (EditText) findViewById(R.id.bg_value_2);
                    String string_value_1 = value_1.getText().toString();
                    String string_value_2 = value_2.getText().toString();

                    if (!TextUtils.isEmpty(string_value_1)) {
                        if (TextUtils.isEmpty(string_value_2)) {
                            string_value_2 = string_value_1; // just use single calibration if all that is entered
                        }
                        if (!TextUtils.isEmpty(string_value_2)) {
                            final double calValue_1 = Double.parseDouble(string_value_1);
                            final double calValue_2 = Double.parseDouble(string_value_2);

                            final double multiplier = Home.getPreferencesStringWithDefault("units", "mgdl").equals("mgdl") ? 1 : Constants.MMOLL_TO_MGDL;
                            if ((calValue_1 * multiplier < 40) || (calValue_1 * multiplier > 400)
                                    || (calValue_2 * multiplier < 40) || (calValue_2 * multiplier > 400)) {
                                JoH.static_toast_long("Calibration out of range");
                            } else {
                                Calibration.initialCalibration(calValue_1, calValue_2, getApplicationContext());

                                //startWatchUpdaterService(v.getContext(), WatchUpdaterService.ACTION_SYNC_CALIBRATION, TAG);

                                Intent tableIntent = new Intent(v.getContext(), Home.class);
                                startActivity(tableIntent);
                                finish();
                            }
                        } else {
                            value_2.setError("Calibration Can Not be blank");
                        }
                    } else {
                        value_1.setError("Calibration Can Not be blank");
                    }
                } else {
                    Log.w("DoubleCalibration", "ERROR, sensor is not active");
                }
            }
        });

    }
}
