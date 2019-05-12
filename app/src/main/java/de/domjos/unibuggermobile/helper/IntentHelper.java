/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniBuggerMobile <https://github.com/domjos1994/UniBuggerMobile>.
 *
 * UniBuggerMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UniBuggerMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UniBuggerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unibuggermobile.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;

public class IntentHelper {
    private static final int GALLERY = 44;

    public static void openGalleryIntent(Activity activity) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        activity.startActivityForResult(photoPickerIntent, IntentHelper.GALLERY);
    }

    public static Bitmap getImageFromGallery(int requestCode, int resultCode, Intent data, Context context) throws FileNotFoundException {
        if (requestCode == IntentHelper.GALLERY && resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            if (imageUri != null) {
                final InputStream imageStream = context.getContentResolver().openInputStream(imageUri);
                return BitmapFactory.decodeStream(imageStream);
            }
        }
        return null;
    }
}
