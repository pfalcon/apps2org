package com.google.code.appsorganizer.dialogs;

import java.io.Serializable;

import android.content.DialogInterface;

public interface OnOkClickListener extends Serializable {

	void onClick(CharSequence charSequence, DialogInterface dialog, int which);
}
