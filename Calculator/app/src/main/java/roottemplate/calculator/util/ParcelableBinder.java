/*
 * Copyright (c) 2016 RootTemplate Group 1.
 * This file is part of Calculator_.
 *
 * Calculator_ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Calculator_ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Calculator_.  If not, see <http://www.gnu.org/licenses/>.
 */

package roottemplate.calculator.util;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableBinder implements Parcelable {
    public static final Creator<ParcelableBinder> CREATOR = new Creator<ParcelableBinder>() {
        @Override
        public ParcelableBinder createFromParcel(Parcel in) {
            return new ParcelableBinder(in);
        }

        @Override
        public ParcelableBinder[] newArray(int size) {
            return new ParcelableBinder[size];
        }
    };


    private IBinder mBinder;

    public ParcelableBinder(IBinder binder) {
        mBinder = binder;
    }
    private ParcelableBinder(Parcel in) {
        mBinder = in.readStrongBinder();
    }

    public IBinder getBinder() {
        return mBinder;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(mBinder);
    }
}