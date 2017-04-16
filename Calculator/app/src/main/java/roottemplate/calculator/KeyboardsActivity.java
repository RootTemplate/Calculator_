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

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.ColorFilter;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
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
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.DragEvent;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import roottemplate.calculator.data.KeyboardKits;
import roottemplate.calculator.data.KeyboardKitsXmlManager;
import roottemplate.calculator.util.KeyboardButtonsThemeUtils;
import roottemplate.calculator.util.Util;
import roottemplate.calculator.view.IfDialogFragment;
import roottemplate.calculator.view.OnDialogResultListener;
import roottemplate.calculator.view.SaveDialogFragment;
import roottemplate.calculator.view.SystemButton;

public class KeyboardsActivity extends AppCompatActivity implements View.OnLongClickListener,
        View.OnDragListener, OnDialogResultListener {
    private static final int DIALOG_EDIT_CUSTOM_BUTTON = 1;
    private static final int DIALOG_DELETE_BUTTON = 2;
    private static final int DIALOG_DELETE_KIT = 3;
    private static final int DIALOG_DELETE_PAGE = 4;
    private static final int DIALOG_CLOSE_ACTIVITY = 5;

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

    private CoordinatorLayout mRootView;
    private Spinner mKitSpinner;
    private ViewPager mKitPreview;
    private KitPreviewAdapter mKitPreviewAdapter;
    private RecyclerView mPalette;
    private FrameLayout mDraggingButtonRoot;
    private Spinner mCategorySpinner;
    private LinearLayout mDragActionBar;
    private TextView mDragActionInfo;
    private TextView mDragActionDelete;

    private DataFragment mData;
    private KeyboardKits mKits;
    private KeyboardKits.KitVersion mCurrentKitV;
    private int mCurrentKitIndex = -1;
    private int mCurrentCategoryIndex = 0;
    private KeyboardButtonsThemeUtils mKeyboardTheme;
    private boolean mIsEastLocale;
    private int mWorkingButtonId;

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
        DataFragment dataFragment = mData = (DataFragment) fm.findFragmentByTag(DataFragment.NAME);
        if(dataFragment == null) {
            dataFragment = mData = new DataFragment();
            dataFragment.mKits = Util.readKeyboardKits(this);
            fm.beginTransaction().add(dataFragment, DataFragment.NAME).commit();
        }
        kits = mKits = dataFragment.mKits;
        initDefaultButtonsByCategory(kits.mButtons);

        KeyboardKits.Kit preferredKit = KeyboardKitsXmlManager.getPreferredKitVersion(kits.mKits,
                prefs.kitName(), false).mParent;
        setCurrentKit(kits.mKits.indexOf(preferredKit));

        // SETUP Views LINKS
        mRootView = (CoordinatorLayout) findViewById(R.id.coordinator);
        mKitPreview = (ViewPager) findViewById(R.id.kit_preview);
        mPalette = (RecyclerView) findViewById(R.id.buttons_palette);
        mCategorySpinner = (Spinner) findViewById(R.id.button_category_spinner);
        mDraggingButtonRoot = (FrameLayout) findViewById(R.id.draggingViewRoot);

        // SETUP kitSpinner
        mKitSpinner = (Spinner) findViewById(R.id.kits_spinner);
        mKitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == mKits.mKits.size()) {
                    parent.setSelection(mCurrentKitIndex);
                    mData.mHasChanges = true;
                    editCurrentKit(false);
                } else
                    setCurrentKit(position);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        notifyKitsChanged();

        // SETUP buttons
        findViewById(R.id.editPageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCurrentKitV.mParent.mIsSystem) {
                    showMessageCannotEditSystemKit();
                    return;
                }
                mData.mHasChanges = true;
                editPage(mKitPreview.getCurrentItem());
            }
        });
        findViewById(R.id.editKitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mData.mHasChanges = true;
                editCurrentKit(true);
            }
        });

        // SETUP kitPreview
        mKitPreviewAdapter = new KitPreviewAdapter();
        mKitPreview.setAdapter(mKitPreviewAdapter);
        mKitPreview.setCurrentItem(mCurrentKitV.mMainPageIndex, false);
        mKitPreview.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                return onRootDrag(v, event);
            }
        });

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

        // SETUP dragActionBar
        mDragActionBar = (LinearLayout) findViewById(R.id.dragActionBar);
        ColorFilter filter = new PorterDuffColorFilter(getResources().getColor(R.color.colorDragActionImage),
                PorterDuff.Mode.MULTIPLY);
        (mDragActionInfo = (TextView) findViewById(R.id.dragActionInfo)).getCompoundDrawables()[0]
                .setColorFilter(filter);
        TextView dragActionClear;
        (dragActionClear = (TextView) findViewById(R.id.dragActionClear)).getCompoundDrawables()[0]
                .setColorFilter(filter);
        (mDragActionDelete = (TextView) findViewById(R.id.dragActionDelete)).getCompoundDrawables()[0]
                .setColorFilter(filter);
        View.OnDragListener dragListener = new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                return onDragActionBarEvent(v, event);
            }
        };
        mDragActionInfo.setOnDragListener(dragListener);
        dragActionClear.setOnDragListener(dragListener);
        mDragActionDelete.setOnDragListener(dragListener);

        // SETUP palette
        int layoutOrient = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) ?
                LinearLayoutManager.HORIZONTAL : LinearLayoutManager.VERTICAL;
        PaletteAdapter pa = new PaletteAdapter();
        mPalette.setHasFixedSize(true);
        mPalette.setLayoutManager(new LinearLayoutManager(this, layoutOrient, false));
        mPalette.setAdapter(pa);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mLastButtonPressedCoordX = ev.getX();
            mLastButtonPressedCoordY = ev.getY();
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().popBackStackImmediate()) return;

        boolean hasChanges = mData.mHasChanges;
        setResult(hasChanges ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
        if(hasChanges) {
            SaveDialogFragment dialog = new SaveDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", DIALOG_CLOSE_ACTIVITY);
            dialog.setArguments(args);
            dialog.show(getFragmentManager(), "CloseActivity");
        } else
            super.onBackPressed();
    }

    @Override
    public boolean onLongClick(View v) {
        // On Button (from ViewPager) long click
        dragButton((int) v.getTag(), v, true);
        return true;
    }

    public void onPadButtonClick(View view) {}
    public void onSystemButtonClick(View view) {}
    public void onShiftButtonClick(View view) {}
    public void onAddCustomButtonClick(View view) {
        editCustomButton(-1);
    }

    private View generatePage(KeyboardKits.Page page) {
        return KeyboardKitsXmlManager.createContentViewFromPage(
                KeyboardsActivity.this,
                mKits.mButtons, page, getLayoutInflater(),
                mIsEastLocale,
                PreferencesManager.THEME_DAY, false,
                null
        );
    }
    private void deleteButtonFromPageInternal(KeyboardKits.Page page, int i, int j) {
        int[][] buttons = page.mButtons;
        if(buttons[i].length == 1) {
            page.mButtons = Util.removeFromObjectArray(buttons, i, new int[buttons.length - 1][]);
        } else {
            buttons[i] = Util.removeFromIntArray(buttons[i], j);
        }
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

    private void createPageAt(int index, boolean scrollToNewPage) {
        for(KeyboardKits.KitVersion kv : mCurrentKitV.mParent.mKitVersions) {
            KeyboardKits.Page page = new KeyboardKits.Page(KeyboardKits.PageReturnType.DEFAULT_VALUE,
                    true, new int[0][]);
            KeyboardKits.Page[] pages = kv.mPages;
            kv.mPages = Util.appendToObjectArray(pages, index, page,
                    new KeyboardKits.Page[pages.length + 1]);
            if (kv.mMainPageIndex >= index)
                kv.mMainPageIndex++;
        }
        mKitPreviewAdapter.notifyDataSetChanged();
        if (scrollToNewPage)
            mKitPreview.setCurrentItem(index, true);
    }
    private void setMainPageTo(int pageIndex) {
        for(KeyboardKits.KitVersion kv : mCurrentKitV.mParent.mKitVersions) {
            KeyboardKits.Page previousMain = kv.mPages[kv.mMainPageIndex];
            if(previousMain.mMoveToMain == null)
                previousMain.mMoveToMain = KeyboardKits.PageReturnType.DEFAULT_VALUE;
            kv.mMainPageIndex = pageIndex;
        }
    }

    /**
     * @param editCurrent true if required to edit current kit; false if required to create new kit
     *                    with content copied from current kit
     */
    private void editCurrentKit(boolean editCurrent) {
        EditKitFragment dialog = new EditKitFragment();
        Bundle args = new Bundle();
        args.putBoolean("editCurrentKit", editCurrent);
        dialog.setArguments(args);
        dialog.show(getFragmentManager(), "EditKit");
    }
    private void editPage(int pageIndex) {
        EditPageFragment dialog = new EditPageFragment();
        Bundle args = new Bundle();
        args.putInt("pageIndex", pageIndex);
        dialog.setArguments(args);
        dialog.show(getFragmentManager(), "EditPage");
    }
    private void editCustomButton(int btnIndex) {
        EditCustomButtonFragment dialog = new EditCustomButtonFragment();
        Bundle args = new Bundle();
        args.putInt("btnIndex", btnIndex);
        dialog.setArguments(args);
        dialog.show(getFragmentManager(), "EditButton");
    }

    private void deleteCurrentKit() {
        mKits.mKits.remove(mCurrentKitV.mParent);
        notifyKitsChanged();
    }
    private void deletePage(int pageIndex) {
        for(KeyboardKits.KitVersion kv : mCurrentKitV.mParent.mKitVersions) {
            KeyboardKits.Page[] pages = kv.mPages;
            kv.mPages = Util.removeFromObjectArray(pages, pageIndex, new KeyboardKits.Page[pages.length - 1]);
            if (kv.mMainPageIndex > pageIndex)
                kv.mMainPageIndex--;
            else if(kv.mMainPageIndex >= kv.mPages.length)
                kv.mMainPageIndex = kv.mPages.length - 1;
        }
        mKitPreviewAdapter.notifyDataSetChanged();
    }
    private void deleteCustomButton(int btnIndex) {
        mKits.mButtons.remove(btnIndex);
        boolean dataChanged = false;

        for(KeyboardKits.Kit kit : mKits.mKits)
            for(KeyboardKits.KitVersion kv : kit.mKitVersions)
                for(KeyboardKits.Page page : kv.mPages) {
                    int[][] buttons = page.mButtons;
                    for(int i = 0; i < buttons.length; i++)
                        for(int j = 0; j < buttons[i].length; j++) {
                            if(buttons[i][j] == btnIndex) {
                                deleteButtonFromPageInternal(page, i, j);
                                buttons = page.mButtons; // Update our cache
                                if(kv == mCurrentKitV) {
                                    dataChanged = true;
                                    mKitPreviewAdapter.mInvalidatePages.add(page);
                                }
                            } else if(buttons[i][j] > btnIndex)
                                buttons[i][j]--;
                        }
                }

        if(dataChanged)
            mKitPreviewAdapter.notifyDataSetChanged();
        notifyCustomButtonsChanged();
    }

    private int getCustomButtonCount() {
        return mKits.mButtons.size() - KeyboardKits.DEFAULT_BUTTONS_COUNT;
    }

    private void notifyCustomButtonsChanged() {
        if(mCurrentCategoryIndex == KeyboardKits.ButtonCategory.CUSTOM.ordinal())
            mPalette.getAdapter().notifyDataSetChanged();
        // Invalidate all pages
        int childCount = mKitPreview.getChildCount();
        for(int i = 0; i < childCount; i++)
            mKitPreviewAdapter.mInvalidatePages.add((KeyboardKits.Page) mKitPreview.getChildAt(i).getTag());
        mKitPreviewAdapter.notifyDataSetChanged();
    }
    private void notifyPageChanged(int pageIndex) {
        mKitPreviewAdapter.mInvalidatePages.add(mCurrentKitV.mPages[pageIndex]);
        mKitPreviewAdapter.notifyDataSetChanged();
    }
    private void notifyKitsChanged() {
        Spannable[] kitNames = new Spannable[mKits.mKits.size() + 1];
        Spannable.Factory factory = Spannable.Factory.getInstance();
        int i = 0;
        for(KeyboardKits.Kit kit : mKits.mKits)
            kitNames[i++] = factory.newSpannable(kit.mName);
        kitNames[i] = factory.newSpannable(getResources().getString(R.string.keyboards_addKit));
        kitNames[i].setSpan(new StyleSpan(Typeface.ITALIC), 0, kitNames[i].length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if(mCurrentKitIndex >= kitNames.length - 1) {
            setCurrentKit(kitNames.length - 2);
        }

        mKitSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                kitNames));
        mKitSpinner.setSelection(mCurrentKitIndex); // mCurrentKitIndex is already set in setCurrentKit
    }


    /*  Stuff related to drag'n'drop  */

    private KeyboardKits.Page mTempPage = new KeyboardKits.Page(KeyboardKits.PageReturnType.NEVER,
            true, new int[][] {new int[] {0}});
    private ViewGroup mDraggingView;
    private ViewGroup.LayoutParams mDraggingButtonLayoutParams;
    private float mLastButtonPressedCoordX;
    private float mLastButtonPressedCoordY;
    private boolean mMessageCannotEditSystemShown = false;

    private void dragButton(final int btnId, final View initiator, final boolean removeInitiator) {
        mDragTempPage = mCurrentKitV.mPages[mKitPreview.getCurrentItem()];
        mDragTempCannotEditKit = mCurrentKitV.mParent.mIsSystem;
        mMessageCannotEditSystemShown = false;
        if(!mDragTempCannotEditKit)
            mData.mHasChanges = true;

        mTempPage.mButtons[0][0] = btnId;
        mTempPage.mIsVerticalOrient = mDragTempPage.mIsVerticalOrient;
        final ViewGroup vDoubleRoot = (ViewGroup) generatePage(mTempPage);
        final LinearLayout vRoot = (LinearLayout) vDoubleRoot.getChildAt(0);
        final View v = vRoot.getChildAt(0);

        mDraggingView = vRoot;
        mDraggingButtonLayoutParams = v.getLayoutParams();

        int size = getResources().getDimensionPixelSize(R.dimen.default_keyboard_button_size);
        v.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        mDraggingButtonRoot.addView(vDoubleRoot); // We have to use vDoubleRoot here (not vRoot)
        // otherwise LayoutParams of vRoot, which we need in case of adding a layout, will be overriden

        final int[] loc = new int[2];
        initiator.getLocationOnScreen(loc);
        int fX_ = (int) (mLastButtonPressedCoordX - loc[0]);
        int fY_ = (int) (mLastButtonPressedCoordY - loc[1]);
        final int fX = Math.min(fX_, size);
        final int fY = Math.min(fY_, size);

        new Thread() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(removeInitiator)
                            onDragInitiatedFromKitPreview(initiator);

                        View.DragShadowBuilder myShadow = new View.DragShadowBuilder(v) {
                            @Override
                            public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
                                super.onProvideShadowMetrics(outShadowSize, outShadowTouchPoint);
                                outShadowTouchPoint.set(fX, fY);
                            }
                        };
                        showDragActionBar(btnId >= KeyboardKits.DEFAULT_BUTTONS_COUNT);
                        v.startDrag(null, myShadow, btnId, 0);
                    }
                });
            }
        }.start();
    }

    private void onDragInitiatedFromKitPreview(View v) {
        if(mDragTempCannotEditKit) return;
        ViewGroup parent1 = (ViewGroup) v.getParent();
        ViewGroup parent2 = (ViewGroup) parent1.getParent();
        int index1 = parent1.indexOfChild(v), index2 = parent2.indexOfChild(parent1);
        parent1.removeViewAt(index1);

        if(parent1.getChildCount() == 0) {
            parent2.removeViewAt(index2);
            mDragAddingNearView = parent2.getChildCount() == 0 ? parent2 :
                    parent2.getChildAt(Math.max(0, index2 - 1));
            mDragAddOnTop = index2 == 0;
            mDragAddingLayout = true;
        } else {
            mDragAddingNearView = parent1.getChildAt(Math.max(0, index1 - 1));
            mDragAddOnTop = index1 == 0;
            mDragAddingLayout = false;
        }
        deleteButtonFromPageInternal(mDragTempPage, index2, index1); // mDragTempPage must be not null
        setDropHereLine();
    }

    private View mDragAddingNearView;
    private boolean mDragAddOnTop; // true - adding item on top of the view
    private boolean mDragAddingLayout; // true if adding a layout; false if adding an item
    private KeyboardKits.Page mDragTempPage;
    private boolean mDragTempCannotEditKit;
    private View mDropHereLine;

    private boolean onRootDrag(View v, DragEvent event) {
        Log.d(Util.LOG_TAG, v.getTag() + " " + event.getAction()); // todo clear this
        int act = event.getAction();
        if(mDragTempCannotEditKit && act != DragEvent.ACTION_DRAG_ENDED) {
            showMessageCannotEditSystemKit();
            return true;
        }

        switch (act) {
            case DragEvent.ACTION_DRAG_ENTERED:
                if(mDragAddingNearView == null && mDragTempPage.mButtons.length == 0) {
                    View rootLL = null;
                    ViewGroup kitPreview = (ViewGroup) v;
                    int children = kitPreview.getChildCount();
                    for(int i = 0; i < children; i++)
                        if(mKitPreviewAdapter.doesViewRepresentPage(rootLL = kitPreview.getChildAt(i),
                                mDragTempPage))
                            break;
                    mDragAddingNearView = rootLL;
                    mDragAddOnTop = true;
                    mDragAddingLayout = true;
                    setDropHereLine();
                }
                break;

            case DragEvent.ACTION_DROP:
                if(mDropHereLine == null) break;

                int[][] buttons = mDragTempPage.mButtons;
                int btnId = (int) event.getLocalState();
                ViewGroup parent = (ViewGroup) mDropHereLine.getParent();
                int index = parent.indexOfChild(mDropHereLine);
                removeDropHereLine();

                View button = mDraggingView.getChildAt(0);
                button.setLayoutParams(mDraggingButtonLayoutParams);
                // These lines must be synchronized with KitPreviewAdapter.instantiateItem
                button.setLongClickable(true);
                button.setOnLongClickListener(this);
                button.setOnDragListener(this);

                if(mDragAddingLayout) {
                    buttons = Util.appendToObjectArray(buttons, index, new int[] {btnId},
                            new int[buttons.length + 1][]);

                    ((ViewGroup) mDraggingView.getParent()).removeView(mDraggingView);
                    parent.addView(mDraggingView, index);
                } else {
                    int rootIndex = ((ViewGroup) parent.getParent()).indexOfChild(parent);
                    buttons[rootIndex] = Util.appendToIntArray(buttons[rootIndex],
                            index, btnId);

                    mDraggingView.removeView(button);
                    parent.addView(button, index);
                }

                mDragTempPage.mButtons = buttons;
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                if(mDraggingView == null) break;

                removeDropHereLine();
                mDraggingButtonRoot.removeAllViews();
                hideDragActionBar();
                mDraggingView = null;
                mDraggingButtonLayoutParams = null;
                mDragTempPage = null;
                mDragTempCannotEditKit = false;
                mMessageCannotEditSystemShown = false; // Set this to false on drag start & end
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                if(v.getTag() == null) { // Not a button => root
                    removeDropHereLine();
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        int act = event.getAction();
        if(mDragTempCannotEditKit && act != DragEvent.ACTION_DRAG_ENDED) {
            showMessageCannotEditSystemKit();
            return true;
        }

        float x = event.getX(), y = event.getY();
        switch (act) {
            case DragEvent.ACTION_DRAG_STARTED:
                return true;
            case DragEvent.ACTION_DRAG_ENTERED:
            case DragEvent.ACTION_DRAG_LOCATION:
                float xRelative = x / v.getWidth();
                float yRelative = y / v.getHeight();
                boolean vertical = mDragTempPage.mIsVerticalOrient;
                View newAddingNearView;
                boolean newAddOnTop;
                if(vertical ? yRelative < 0.2 : xRelative < 0.2) {
                    // Add layout on top
                    newAddingNearView = (View) v.getParent();
                    newAddOnTop = true;
                } else if(vertical ? yRelative > 0.8 : xRelative > 0.8) {
                    // Add layout at bottom
                    newAddingNearView = (View) v.getParent();
                    newAddOnTop = false;
                } else if(vertical ? xRelative < 0.5 : yRelative < 0.5) {
                    // Add view on top
                    newAddingNearView = v;
                    newAddOnTop = true;
                } else {
                    // Add view at bottom
                    newAddingNearView = v;
                    newAddOnTop = false;
                }
                mDragAddingLayout = newAddingNearView != v;
                if(mDragAddingNearView != newAddingNearView || mDragAddOnTop != newAddOnTop) {
                    removeDropHereLine();
                    mDragAddingNearView = newAddingNearView;
                    mDragAddOnTop = newAddOnTop;
                    setDropHereLine();
                }
                return true;
            default:
                return onRootDrag(v, event); // TODO: maybe fix of this crutch
        }
    }

    private static final int MARGIN_SIZE = 4;
    private void setDropHereLine() {
        View line = mDropHereLine;
        if(line == null) {
            line = mDropHereLine = new View(this);
            line.setBackgroundColor(getResources().getColor(R.color.colorAccentAlternative));
        }
        boolean noItemsInPage = mDragTempPage.mButtons.length == 0;
        LinearLayout parent = (LinearLayout) (noItemsInPage ? mDragAddingNearView :
                mDragAddingNearView.getParent());
        boolean vert = parent.getOrientation() == LinearLayout.VERTICAL;
        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(
                vert ? ViewGroup.LayoutParams.MATCH_PARENT : MARGIN_SIZE,
                vert ? MARGIN_SIZE : ViewGroup.LayoutParams.MATCH_PARENT
        );
        int index = noItemsInPage ? 0 : parent.indexOfChild(mDragAddingNearView);
        if(!mDragAddOnTop) index++;
        parent.addView(line, index, params);
    }
    private void removeDropHereLine() {
        if(mDragAddingNearView == null || mDropHereLine == null) return;
        ((ViewGroup) mDropHereLine.getParent()).removeView(mDropHereLine);
        mDragAddingNearView = null;
    }

    private void showMessageCannotEditSystemKit() {
        if(mMessageCannotEditSystemShown) return;
        Toast.makeText(this, R.string.keyboards_message_cannotEditSystemKit, Toast.LENGTH_LONG).show();
        mMessageCannotEditSystemShown = true;
    }

    private boolean onDragActionBarEvent(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                ((TransitionDrawable) v.getBackground()).resetTransition();
                return true;
            case DragEvent.ACTION_DRAG_ENTERED:
                ((TransitionDrawable) v.getBackground()).startTransition(80);
                return true;
            case DragEvent.ACTION_DRAG_EXITED:
                ((TransitionDrawable) v.getBackground()).reverseTransition(80);
                return true;
            case DragEvent.ACTION_DROP:
                if(v == mDragActionInfo)
                    editCustomButton((Integer) event.getLocalState());
                else if(v == mDragActionDelete) {
                    mWorkingButtonId = (Integer) event.getLocalState();
                    IfDialogFragment dialog = new IfDialogFragment();
                    Bundle args = new Bundle();
                    args.putInt("title", R.string.keyboards_deleteButton_title);
                    args.putInt("message", R.string.keyboards_deleteButton_message);
                    args.putInt("positiveBtn", R.string.yes);
                    args.putInt("negativeBtn", R.string.no);
                    args.putInt("id", DIALOG_DELETE_BUTTON);
                    dialog.setArguments(args);
                    dialog.show(getFragmentManager(), "DeleteButton");
                }
                return true;
            default:
                return true;
        }
    }

    private void showDragActionBar(boolean draggingCustomButton) {
        mDragActionInfo.setVisibility(draggingCustomButton ? View.VISIBLE : View.GONE);
        mDragActionDelete.setVisibility(draggingCustomButton ? View.VISIBLE : View.GONE);
        Util.animateAlpha(mCategorySpinner, 0, 200);
        Util.animateAlpha(mDragActionBar, 1, 200);
    }
    private void hideDragActionBar() {
        Util.animateAlpha(mCategorySpinner, 1, 200);
        Util.animateAlpha(mDragActionBar, 0, 200);
    }

    @Override
    public void onDialogPositiveClick(int dialogId) {
        if(dialogId == DIALOG_DELETE_BUTTON) {
            deleteCustomButton(mWorkingButtonId);
        } else if(dialogId == DIALOG_DELETE_KIT) {
            deleteCurrentKit();
        } else if(dialogId == DIALOG_DELETE_PAGE) {
            deletePage(mKitPreview.getCurrentItem());
        } else if(dialogId == DIALOG_CLOSE_ACTIVITY) {
            try {
                KeyboardKitsXmlManager.invalidateInstalledKeyboardKits(this, mKits);
            } catch (IOException e) {
                Log.e(Util.LOG_TAG, "Unable to save new kits", e);
                Util.fatalError(this, R.string.dialog_kitsCannotBeSaved, e);
            }
            finish();
        }
    }
    @Override
    public void onDialogNegativeClick(int dialogId) {
        if(dialogId == DIALOG_CLOSE_ACTIVITY)
            finish();
    }
    @Override public void onDialogNeutralClick(int dialogId) {}


    public static class DataFragment extends Fragment {
        private static final String NAME = "Data";
        private KeyboardKits mKits;
        private boolean mHasChanges = false;

        public DataFragment() {
            setRetainInstance(true);
        }
    }

    private class KitPreviewAdapter extends PagerAdapter {
        private HashSet<KeyboardKits.Page> mInvalidatePages = new HashSet<>(4);

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            KeyboardKits.Page page = mCurrentKitV.mPages[position];
            ViewGroup view = (ViewGroup) generatePage(page);
            int rootChildCount = view.getChildCount();
            for(int i = 0; i < rootChildCount; i++) {
                ViewGroup line = (ViewGroup) view.getChildAt(i);
                int childCount = line.getChildCount();
                for(int j = 0; j < childCount; j++) {
                    View v = line.getChildAt(j);
                    v.setLongClickable(true);
                    v.setOnLongClickListener(KeyboardsActivity.this);
                    v.setOnDragListener(KeyboardsActivity.this);
                }
            }
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

        public boolean doesViewRepresentPage(View view, KeyboardKits.Page page) {
            return view.getTag() == page;
        }

        public int getItemPosition(Object object) {
            KeyboardKits.Page tag = (KeyboardKits.Page) ((View) object).getTag();
            if(mInvalidatePages.remove(tag)) {
                return POSITION_NONE;
            }

            KeyboardKits.Page[] pages = mCurrentKitV.mPages;
            for(int i = 0; i < pages.length; i++)
                if(pages[i] == tag) return i;
            return POSITION_NONE;
        }
    }

    private class PaletteAdapter extends RecyclerView.Adapter<PaletteViewHolder>
            implements View.OnLongClickListener {

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
            dragButton((int) v.getTag(), v, false);
            return true;
        }
    }

    private static class PaletteViewHolder extends RecyclerView.ViewHolder {
        public View mButton;
        public PaletteViewHolder(View itemView) {
            super(itemView);
            mButton = itemView;
        }
    }

    public static abstract class NamedDialogFragment extends DialogFragment {
        protected final TextWatcher mTextListener = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePositiveButton();
            }
        };

        protected abstract boolean areTextFilled();

        protected void updatePositiveButton() {
            AlertDialog dialog = (AlertDialog) getDialog();
            if (dialog != null)
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(areTextFilled());
        }

        @Override
        public void onResume() {
            super.onResume();
            updatePositiveButton();
        }

    }

    public static class EditCustomButtonFragment extends NamedDialogFragment implements DialogInterface.OnClickListener {
        private View mDialogView;
        private EditText mName;
        private EditText mText;
        private CheckBox mEnableTextField;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            KeyboardsActivity activity = (KeyboardsActivity) getActivity();
            View view = activity.getLayoutInflater().inflate(R.layout.keyboards_dialog_edit_button, null);
            setupDialogView(view);

            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.keyboards_editButton_title)
                    .setView(view)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.saveChanges, this)
                    .create();
        }

        private void setupDialogView(View dialog) {
            mDialogView = dialog;
            mEnableTextField = (CheckBox) dialog.findViewById(R.id.editKit_actionBarAccess);
            mName = (EditText) dialog.findViewById(R.id.editPage_orient);
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
                    mTextListener.onTextChanged(s, start, before, count);
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

        protected boolean areTextFilled() {
            return mName.length() > 0;
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
                View view = mDialogView;

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

    public static class EditPageFragment extends DialogFragment implements Dialog.OnClickListener, View.OnClickListener {
        private KeyboardsActivity mActivity;
        private View mDialogView;
        private int mPageIndex;
        private boolean mSetAsMainValue = false;

        private Spinner mPageOrient;
        private Spinner mReturnToMain;
        private Button mSetAsMain;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            KeyboardsActivity activity = (KeyboardsActivity) getActivity();
            View dialog = activity.getLayoutInflater().inflate(R.layout.keyboards_dialog_edit_page, null);
            KeyboardKits.KitVersion kitV = activity.mCurrentKitV;
            mActivity = activity;
            mDialogView = dialog;

            mPageIndex = getArguments().getInt("pageIndex");
            KeyboardKits.Page page = activity.mCurrentKitV.mPages[mPageIndex];

            (mPageOrient = (Spinner) dialog.findViewById(R.id.editPage_orient))
                    .setSelection(page.mIsVerticalOrient ? 0 : 1);
            mReturnToMain = (Spinner) dialog.findViewById(R.id.editPage_returnToMain);
            mSetAsMain = (Button) dialog.findViewById(R.id.editPage_setMain);
            if(activity.mCurrentKitV.mMainPageIndex == mPageIndex) {
                mReturnToMain.setVisibility(View.GONE);
                mSetAsMain.setEnabled(false);
            } else {
                mReturnToMain.setSelection(page.mMoveToMain.ordinal());
                mSetAsMain.setOnClickListener(this);
            }

            dialog.findViewById(R.id.editPage_addPageLeft).setOnClickListener(this);
            dialog.findViewById(R.id.editPage_addPageRight).setOnClickListener(this);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.keyboards_editPage_title)
                    .setView(dialog)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.save, this);
            if(kitV.mPages.length > 1)
                builder.setNeutralButton(R.string.keyboards_editPage_deletePage, this);
            return builder.create();
        }

        private void saveChanges() {
            KeyboardKits.Page page = mActivity.mCurrentKitV.mPages[mPageIndex];
            page.mMoveToMain = KeyboardKits.PageReturnType.values()[mReturnToMain
                    .getSelectedItemPosition()];

            boolean newOrient = mPageOrient.getSelectedItemPosition() == 0;
            boolean orientChanged = newOrient != page.mIsVerticalOrient;
            page.mIsVerticalOrient = newOrient;

            if(mSetAsMainValue)
                mActivity.setMainPageTo(mPageIndex);
            if(orientChanged)
                mActivity.notifyPageChanged(mPageIndex);
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(which == Dialog.BUTTON_POSITIVE) {
                saveChanges();
            } else if(which == Dialog.BUTTON_NEUTRAL) {
                IfDialogFragment ifDialog = new IfDialogFragment();
                Bundle args = new Bundle();
                args.putInt("title", R.string.keyboards_deletePage_title);
                args.putInt("message", R.string.keyboards_deletePage_message);
                args.putInt("positiveBtn", R.string.yes);
                args.putInt("negativeBtn", R.string.no);
                args.putInt("id", DIALOG_DELETE_PAGE);
                ifDialog.setArguments(args);
                ifDialog.show(getFragmentManager(), "DeletePage");
            }
        }

        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.editPage_addPageLeft:
                case R.id.editPage_addPageRight:
                    int newIndex = mPageIndex + (v.getId() == R.id.editPage_addPageLeft ? 0 : 1);
                    mActivity.createPageAt(newIndex, true);
                    //saveChanges();
                    dismiss();
                    break;
                case R.id.editPage_setMain:
                    mSetAsMainValue = true;
                    v.setEnabled(false);
                    mDialogView.findViewById(R.id.editPage_returnToMain).setVisibility(View.GONE);
                    break;
            }
        }
    }

    public static class EditKitFragment extends NamedDialogFragment implements DialogInterface.OnClickListener {
        private KeyboardsActivity mActivity;
        private KeyboardKits.Kit mKit;
        private EditText mName;
        private EditText mShortName;
        private CheckBox mActionBarAccess;
        private TextView mErrorOfExistence;

        @Override
        protected boolean areTextFilled() {
            boolean kitAlreadyExists = false;
            String kitName = mName.getText().toString();
            for (KeyboardKits.Kit kit : mActivity.mKits.mKits)
                if (mKit != kit && kit.mName.equalsIgnoreCase(kitName)) {
                    kitAlreadyExists = true;
                    break;
                }
            mErrorOfExistence.setVisibility(kitAlreadyExists ? View.VISIBLE : View.GONE);
            if(kitAlreadyExists) {
                mErrorOfExistence.setText(String.format(getResources().getString(
                        R.string.keyboards_editKit_alreadyExists), kitName));
            }

            return !kitAlreadyExists && !kitName.isEmpty() &&
                    !(mActionBarAccess.isChecked() && mShortName.length() == 0);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            KeyboardsActivity activity = (KeyboardsActivity) getActivity();
            View dialog = activity.getLayoutInflater().inflate(R.layout.keyboards_dialog_edit_kit, null);
            mActivity = activity;
            mKit = getArguments().getBoolean("editCurrentKit") ? activity.mCurrentKitV.mParent : null;

            mName = (EditText) dialog.findViewById(R.id.editPage_orient);
            mShortName = (EditText) dialog.findViewById(R.id.editKit_shortName);
            mActionBarAccess = (CheckBox) dialog.findViewById(R.id.editKit_actionBarAccess);
            mActionBarAccess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mShortName.setEnabled(isChecked);
                    updatePositiveButton();
                    if(isChecked && mShortName.length() == 0)
                        mShortName.setText(mName.getText());
                }
            });
            mErrorOfExistence = (TextView) dialog.findViewById(R.id.editKit_errorOfExistence);

            mName.addTextChangedListener(mTextListener);
            mShortName.addTextChangedListener(mTextListener);

            if(mKit != null) {
                mName.setText(mKit.mName);
                mName.setEnabled(!mKit.mIsSystem);
                mShortName.setText(mKit.mShortName);
                mActionBarAccess.setChecked(mKit.mActionBarAccess);
            } else {
                TextView tv = (TextView) dialog.findViewById(R.id.editKit_contentCopied);
                tv.setText(
                        String.format(getResources().getString(R.string.keyboards_editKit_contentCopied),
                                activity.mCurrentKitV.mParent.mName)
                );
                tv.setVisibility(View.VISIBLE);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.keyboards_editKit_title)
                    .setView(dialog)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.save, this);
            if(mKit != null && !mKit.mIsSystem)
                builder.setNeutralButton(R.string.keyboards_editKit_deleteKit, this);
            return builder.create();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            KeyboardsActivity activity = mActivity;
            if(which == Dialog.BUTTON_POSITIVE) {
                KeyboardKits.Kit kit = mKit;
                if(kit == null) {
                    KeyboardKits.KitVersion[] kitVers = KeyboardKits.cloneKitVersionsFrom(
                            activity.mCurrentKitV.mParent);
                    kit = new KeyboardKits.Kit(null, null, false, false, kitVers);
                    activity.mKits.mKits.add(kit);
                }

                kit.mName = mName.getText().toString();
                kit.mShortName = mShortName.getText().toString();
                kit.mActionBarAccess = mActionBarAccess.isChecked();
                activity.notifyKitsChanged();
                if(mKit == null)
                    activity.mKitSpinner.setSelection(activity.mKits.mKits.size() - 1);
                    // Kind of a hack: setCurrentKit will be called through spinner change listener
            } else if(which == Dialog.BUTTON_NEUTRAL) {
                IfDialogFragment ifDialog = new IfDialogFragment();
                Bundle args = new Bundle();
                args.putInt("title", R.string.keyboards_deleteKit_title);
                args.putInt("message", R.string.keyboards_deleteKit_message);
                args.putInt("positiveBtn", R.string.yes);
                args.putInt("negativeBtn", R.string.no);
                args.putInt("id", DIALOG_DELETE_KIT);
                ifDialog.setArguments(args);
                ifDialog.show(getFragmentManager(), "DeleteKit");
            }
        }
    }
}
