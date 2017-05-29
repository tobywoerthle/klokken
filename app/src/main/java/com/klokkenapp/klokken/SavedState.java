package com.klokkenapp.klokken;

import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;

class SavedState extends AppCompatPreferenceActivity {

    public SavedState(Parcel in) {
        super();
    }

    private static class SavedStateImpl extends Preference.BaseSavedState {
        // Member that holds the setting's value
        // Change this data type to match the type saved by your Preference
        int value;

        public SavedStateImpl(Parcelable superState) {
            super(superState);
        }

        public SavedStateImpl(Parcel source) {
            super(source);
            // Get the current preference's value
            value = source.readInt();  // Change this to read the appropriate data type
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            // Write the preference's value
            dest.writeInt(value);  // Change this to write the appropriate data type
        }

        // Standard creator object using an instance of this class
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {

                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

}

//https://developer.android.com/guide/topics/ui/settings.html#Activity
