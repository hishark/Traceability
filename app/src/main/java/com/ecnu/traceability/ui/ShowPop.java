package com.ecnu.traceability.ui;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ecnu.traceability.R;

public class ShowPop extends BasePop {
    public ShowPop(@NonNull Context context) {
        super(context);
    }

    public void setText(String timeText, String addressText) {
        TextView timeView = findViewById(R.id.convert_time);
        timeView.setText(timeText);
        TextView addressView = findViewById(R.id.convert_address);
        addressView.setText(addressText);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.popview;
    }
}
