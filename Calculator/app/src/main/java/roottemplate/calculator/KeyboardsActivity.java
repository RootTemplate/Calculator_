/*
 * Copyright (c) 2017 RootTemplate Group 1.
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

package roottemplate.calculator;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import roottemplate.calculator.data.KeyboardKits;
import roottemplate.calculator.data.KeyboardKitsXmlManager;
import roottemplate.calculator.util.KeyboardButtonsThemeUtils;
import roottemplate.calculator.util.Util;
import roottemplate.calculator.view.SystemButton;

public class KeyboardsActivity extends AppCompatActivity {
    private static final int DIALOG_ADD_CUSTOM_BUTTON = 1;

    private static ArrayList<ArrayList<Integer>> defaultButtonsByCategory;
    private static void initDefaultButtonsByCategory(List<KeyboardKits.Button> buttons) {
        if(defaultButtonsByCategory != null) return;

        KeyboardKits.ButtonCategory[] values = KeyboardKits.ButtonCategory.values();
        ArrayList<ArrayList<Integer>> res = defaultButtonsByCategory = new ArrayList<>(values.length - 1);
        for(int i = 0; i < values.length - 1; i++)
            res.add(new ArrayList<Integer>(5));

        int id = 0;
        ArrayList<Integer> eastLocaleIndexes = new ArrayList<>(1);
        for(KeyboardKits.Button btn : buttons) {
            if(eastLocaleIndexes.indexOf(id) == -1) {
                KeyboardKits.ButtonCategory category = btn.mCategory;
                if (category != KeyboardKits.ButtonCategory.CUSTOM) {
                    res.get(category.ordinal()).add(id);

                    int localeEastId = btn.mLocaleEastId;
                    if (localeEastId != -1) eastLocaleIndexes.add(localeEastId);
                }
            } else
                eastLocaleIndexes.remove((Integer) id);
            id++;
        }
    }

    private RelativeLayout mRootLayout;
    private ViewPager mKitPreview;
    private KitPreviewAdapter mKitPreviewAdapter;
    private RecyclerView mPalette;
    private SearchView mSearchView;

    private KeyboardKits mKits;
    private KeyboardKits.KitVersion mCurrentKitV;
    private int mCurrentKitIndex = -1;
    private int mCurrentCategoryIndex = 0;
    private KeyboardButtonsThemeUtils mKeyboardTheme;
    private boolean mIsEastLocale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboards);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        // SETUP NON-View STUFF
        PreferencesManager prefs = new PreferencesManager(this);
        mKeyboardTheme = new KeyboardButtonsThemeUtils(this, prefs);
        mIsEastLocale = !Util.isWestLocale(KeyboardsActivity.this);

        KeyboardKits kits;
        FragmentManager fm = getSupportFragmentManager();
        DataFragment dataFragment = (DataFragment) fm.findFragmentByTag(DataFragment.NAME);
        if(dataFragment == null) {
            dataFragment = new DataFragment();
            kits = dataFragment.mKits = Util.readKeyboardKits(this);
            fm.beginTransaction().add(dataFragment, DataFragment.NAME).commit();
        }
        kits = mKits = dataFragment.mKits;
        initDefaultButtonsByCategory(kits.mButtons);

        KeyboardKits.Kit preferredKit = KeyboardKitsXmlManager.getPreferredKitVersion(kits.mKits,
                prefs.kitName(), false).mParent;
        setCurrentKit(kits.mKits.indexOf(preferredKit));

        String[] kitNames = new String[kits.mKits.size()];
        int i = 0;
        for(KeyboardKits.Kit kit : kits.mKits)
            kitNames[i++] = kit.mName;

        // SETUP Views LINKS
        mRootLayout = (RelativeLayout) findViewById(R.id.content_keyboards);
        mKitPreview = (ViewPager) findViewById(R.id.kit_preview);
        mPalette = (RecyclerView) findViewById(R.id.buttons_palette);
        mSearchView = (SearchView) findViewById(R.id.search);
        Spinner mCategorySpinner = (Spinner) findViewById(R.id.button_category_spinner);

        // SETUP kitSpinner
        Spinner kitSpinner = (Spinner) findViewById(R.id.kits_spinner);
        kitSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                kitNames));
        kitSpinner.setSelection(mCurrentKitIndex); // mCurrentKitIndex is already set in setCurrentKit
        kitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setCurrentKit(position);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // SETUP kitPreview
        mKitPreviewAdapter = new KitPreviewAdapter();
        mKitPreview.setAdapter(mKitPreviewAdapter);
        mKitPreview.setCurrentItem(mCurrentKitV.mMainPageIndex, false);

        // SETUP categorySpinner
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(mCurrentCategoryIndex == position) return;
                mCurrentCategoryIndex = position;
                mPalette.getAdapter().notifyDataSetChanged();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // SETUP palette
        int layoutOrient = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) ?
                LinearLayoutManager.HORIZONTAL : LinearLayoutManager.VERTICAL;
        PaletteAdapter pa = new PaletteAdapter();
        mPalette.setHasFixedSize(true);
        mPalette.setLayoutManager(new LinearLayoutManager(this, layoutOrient, false));
        mPalette.setAdapter(pa);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if(hasFocus) {
            mSearchView.setMaxWidth(mRootLayout.getWidth() / 2 - mRootLayout.getPaddingLeft());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void onPadButtonClick(View view) {}
    public void onSystemButtonClick(View view) {}
    public void onShiftButtonClick(View view) {}
    public void onAddCustomButtonClick(View view) {
        editCustomButton(-1);
    }

    private void setCurrentKit(int index) {
        if(mCurrentKitIndex == index) return;
        KeyboardKits.KitVersion[] vers = mKits.mKits.get(index).mKitVersions;
        boolean isLand = getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
        mCurrentKitV = vers[(vers.length > 1 && vers[1].mIsLandscapeOrient == isLand)
                ? 1 : 0];
        mCurrentKitIndex = index;

        if(mKitPreview != null) {
            mKitPreviewAdapter.notifyDataSetChanged();
            mKitPreview.setCurrentItem(mCurrentKitV.mMainPageIndex, false);
        }
    }

    private void editCustomButton(int btnIndex) {
        EditCustomButtonFragment dialog = new EditCustomButtonFragment();
        Bundle args = new Bundle();
        args.putInt("btnIndex", btnIndex);
        dialog.setArguments(args);
        dialog.show(getFragmentManager(), "AddButton");
    }

    private int getCustomButtonCount() {
        return mKits.mButtons.size() - KeyboardKits.DEFAULT_BUTTONS_COUNT;
    }

    private void notifyCustomButtonsChanged() {
        mPalette.getAdapter().notifyDataSetChanged();
    }


    public static class DataFragment extends Fragment {
        private static final String NAME = "Data";
        private KeyboardKits mKits;

        public DataFragment() {
            setRetainInstance(true);
        }
    }

    private class KitPreviewAdapter extends PagerAdapter {
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            KeyboardKits.Page page = mCurrentKitV.mPages[position];
            View view = KeyboardKitsXmlManager.createContentViewFromPage(KeyboardsActivity.this,
                    mKits.mButtons, page, getLayoutInflater(),
                    mIsEastLocale,
                    PreferencesManager.THEME_DAY, false,
                    null
            );
            view.setTag(page);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return mCurrentKitV.mPages.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public int getItemPosition(Object object) {
            KeyboardKits.Page tag = (KeyboardKits.Page) ((View) object).getTag();
            KeyboardKits.Page[] pages = mCurrentKitV.mPages;
            for(int i = 0; i < pages.length; i++)
                if(pages[i] == tag) return i;
            return POSITION_NONE;
        }
    }

    private class PaletteAdapter extends RecyclerView.Adapter<PaletteViewHolder>
            implements View.OnLongClickListener, View.OnTouchListener {
        private float mLastButtonPressedCoordX;
        private float mLastButtonPressedCoordY;

        @Override
        public PaletteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int defButtonSize = getResources().getDimensionPixelSize(R.dimen.default_keyboard_button_size);
            int margin = 1;
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(defButtonSize, defButtonSize);
            params.setMargins(margin, margin, margin, margin);

            View view = null;
            switch(viewType) {
                case 0:
                    view = getLayoutInflater().inflate(R.layout.button_abstract, parent, false);
                    view.setLayoutParams(params);
                    break;
                case 1:
                    view = getLayoutInflater().inflate(R.layout.button_shift, parent, false);
                    view.setLayoutParams(params);
                    break;
                case 2:
                    SystemButton sysBtn = (SystemButton) getLayoutInflater().inflate(R.layout.button_system,
                            parent, false);
                    sysBtn.setLayoutParams(params);
                    view = sysBtn;
                    break;
                case 3:
                    view = getLayoutInflater().inflate(R.layout.keyboards_button_add, parent, false);
                    break;
            }
            if(view != null && viewType != 3) {
                view.setLongClickable(true);
                view.setOnLongClickListener(this);
                view.setOnTouchListener(this);
            }
            return new PaletteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PaletteViewHolder holder, int position) {
            int type = getItemViewType(position);
            if(type == 3) return;

            int cat = mCurrentCategoryIndex;
            int btnIndex = cat != KeyboardKits.ButtonCategory.CUSTOM.ordinal() ?
                    defaultButtonsByCategory.get(cat).get(position) :
                    position + KeyboardKits.DEFAULT_BUTTONS_COUNT;
            KeyboardKits.Button btnInfo = mKits.mButtons.get(btnIndex);
            holder.mButton.setTag(btnIndex);

            if(mIsEastLocale && btnInfo.mLocaleEastId != -1) {
                btnIndex = btnInfo.mLocaleEastId;
                btnInfo = mKits.mButtons.get(btnIndex);
            }

            if(type == 0) {
                Button btn = (Button) holder.mButton;
                btn.setText(btnInfo.mName);
                btn.setTextColor(mKeyboardTheme.mTextColors[btnInfo.mType.ordinal()]);
                btn.setBackgroundResource(mKeyboardTheme.mBackgroundDrawableIds[btnInfo.mType.ordinal()]);
            } else if(type == 2)
                ((SystemButton) holder.mButton).initButton(btnInfo.mText);
        }

        @Override
        public int getItemCount() {
            int res;
            if(mCurrentCategoryIndex != KeyboardKits.ButtonCategory.CUSTOM.ordinal()) {
                res = defaultButtonsByCategory.get(mCurrentCategoryIndex).size();
            } else {
                res = getCustomButtonCount() + 1;
            }
            return res;
        }

        @Override
        public int getItemViewType(int position) {
            int cat = mCurrentCategoryIndex;
            if(cat == 0) {
                KeyboardKits.ButtonType type = mKits.mButtons.get(defaultButtonsByCategory.get(0)
                        .get(position)).mType;
                return (type == KeyboardKits.ButtonType.SHIFT) ? 1 :
                        (type == KeyboardKits.ButtonType.SYSTEM) ? 2 : 0;
            } else if(cat == KeyboardKits.ButtonCategory.CUSTOM.ordinal())
                return (position < getCustomButtonCount()) ? 0 : 3;
            else
                return 0;
        }

        @Override
        public boolean onLongClick(View v) {
            int btnIndex = (int) v.getTag();
            View.DragShadowBuilder myShadow = new View.DragShadowBuilder(v) {
                private final int mX = (int) mLastButtonPressedCoordX;
                private final int mY = (int) mLastButtonPressedCoordY;

                @Override
                public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
                    super.onProvideShadowMetrics(outShadowSize, outShadowTouchPoint);
                    outShadowTouchPoint.set(mX, mY);
                }
            };
            v.startDrag(null, myShadow, btnIndex, 0);
            return true;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mLastButtonPressedCoordX = event.getX();
                mLastButtonPressedCoordY = event.getY();
            }
            return false;
        }
    }

    private static class PaletteViewHolder extends RecyclerView.ViewHolder {
        public View mButton;
        public PaletteViewHolder(View itemView) {
            super(itemView);
            mButton = itemView;
        }
    }

    public static class EditCustomButtonFragment extends DialogFragment implements DialogInterface.OnClickListener {
        private View mView;
        private EditText mName;
        private EditText mText;
        private CheckBox mEnableTextField;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            KeyboardsActivity activity = (KeyboardsActivity) getActivity();
            View view = activity.getLayoutInflater().inflate(R.layout.keyboards_dialog_edit_button, null);
            setupView(view);

            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.keyboards_editButton_title)
                    .setView(view)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.saveChanged, this)
                    .create();
        }

        @Override
        public void onResume() {
            super.onResume();
            if(mName.getText().length() == 0)
                ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        }

        private void setupView(View dialog) {
            mView = dialog;

            mEnableTextField = (CheckBox) dialog.findViewById(R.id.editButton_enableTextField);
            mName = (EditText) dialog.findViewById(R.id.editButton_name);
            mText = (EditText) dialog.findViewById(R.id.editButton_text);

            mEnableTextField.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mText.setEnabled(isChecked);
                    if(!isChecked)
                        mText.setText(mName.getText());
                }
            });
            mName.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(!mEnableTextField.isChecked())
                        mText.setText(s);

                    ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE)
                        .setEnabled(s.length() > 0);
                }
            });

            int index = getArguments().getInt("btnIndex");
            if(index != -1) {
                KeyboardsActivity activity = (KeyboardsActivity) getActivity();
                KeyboardKits.Button btnInfo = activity.mKits.mButtons.get(index);
                ((Spinner) dialog.findViewById(R.id.editButton_style)).setSelection(getStylePositionByType(btnInfo.mType));
                ((CheckBox) dialog.findViewById(R.id.editButton_enableCaseInverse)).setChecked(btnInfo.mEnableCaseInverse);
                mEnableTextField.setChecked(!btnInfo.mName.equals(btnInfo.mText));
                mText.setText(btnInfo.mText);
                mName.setText(btnInfo.mName);
            }
        }

        private int getStylePositionByType(KeyboardKits.ButtonType mType) {
            switch (mType) {
                case DIGIT: return 1;
                case BASE: return 0;
                case SYMBOL: return 2;
                default: return 0;
            }
        }

        private KeyboardKits.ButtonType getTypeByStylePosition(int position) {
            switch (position) {
                default:
                case 0: return KeyboardKits.ButtonType.BASE;
                case 1: return KeyboardKits.ButtonType.DIGIT;
                case 2: return KeyboardKits.ButtonType.SYMBOL;
            }
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(which == Dialog.BUTTON_POSITIVE) {
                int index = getArguments().getInt("btnIndex");
                KeyboardsActivity activity = (KeyboardsActivity) getActivity();
                ArrayList<KeyboardKits.Button> buttons = activity.mKits.mButtons;
                View view = mView;

                KeyboardKits.Button btnInfo = index == -1 ?
                        new KeyboardKits.Button(null, null, false, KeyboardKits.ButtonCategory.CUSTOM):
                        buttons.get(index);
                btnInfo.mName = mName.getText().toString();
                btnInfo.mText = mText.getText().toString();
                btnInfo.mEnableCaseInverse = ((CheckBox) view.findViewById(R.id.editButton_enableCaseInverse)).isChecked();
                btnInfo.mType = getTypeByStylePosition(((Spinner) view.findViewById(R.id.editButton_style))
                        .getSelectedItemPosition());

                if(index == -1)
                    buttons.add(btnInfo);
                else
                    buttons.set(index, btnInfo);
                activity.notifyCustomButtonsChanged();
            }
        }
    }
}
