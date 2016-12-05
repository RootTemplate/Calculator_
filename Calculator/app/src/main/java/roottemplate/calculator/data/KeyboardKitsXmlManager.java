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

package roottemplate.calculator.data;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import roottemplate.calculator.R;
import roottemplate.calculator.util.Util;

public class KeyboardKitsXmlManager {
    public static final String PADS_FILENAME = "keyboard_kits.xml";

    public static void invalidateInstalledKeyboardKits(Context context) {
        context.deleteFile(PADS_FILENAME);
    }

    public static KeyboardKits parse(Context context) throws IOException, XmlPullParserException {
        FileInputStream file;

        try {
            file = context.openFileInput(PADS_FILENAME);
        } catch (FileNotFoundException e) {
            restoreDefaultButtonKitsXml(context);
            file = context.openFileInput(PADS_FILENAME);
        }

        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(file, null);
        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, null, "KeyboardKits");
        boolean isDefault = Boolean.parseBoolean(parser.getAttributeValue(null, "isDefault"));

        KeyboardKits.Button[] buttons = null;
        KeyboardKits.Kit[] kits = null;
        while(parser.next() != XmlPullParser.END_TAG) {
            if(parser.getEventType() != XmlPullParser.START_TAG)
                continue;
            if(buttons == null) {
                parser.require(XmlPullParser.START_TAG, null, "Buttons");
                buttons = readButtons(parser);
            } else {
                parser.require(XmlPullParser.START_TAG, null, "Kits");
                kits = readKits(parser);
                break;
            }
        }

        return new KeyboardKits(buttons, kits, isDefault);
    }

    private static KeyboardKits.Button[] readButtons(XmlPullParser parser) throws IOException, XmlPullParserException {
        ArrayList<KeyboardKits.Button> buttons = new ArrayList<>();
        while(parser.nextTag() != XmlPullParser.END_TAG) {
            parser.require(XmlPullParser.START_TAG, null, "Button");
            int id;
            String name = parser.getAttributeValue(null, "name");
            String text = parser.getAttributeValue(null, "text");
            if(name == null && text == null)
                throw new XmlPullParserException("<Button> must have 'name' or 'text' attributes, or both", parser, null);
            try {
                id = Integer.parseInt(parser.getAttributeValue(null, "id"));
            } catch(Exception e) {
                throw new XmlPullParserException("Id of <Button> cannot be parsed", parser, e);
            }
            if(id < buttons.size() && buttons.get(id) != null)
                throw new XmlPullParserException("Id of a <Button> must be unique. Another <Button> with the " +
                        "same id = " + id + " found", parser, null);
            try {
                buttons.add(id,
                        new KeyboardKits.Button(
                                name == null ? text : name,
                                text == null ? name : text,
                                parser.getAttributeValue(null, "type"),
                                parser.getAttributeValue(null, "localeEast"),
                                Boolean.parseBoolean(parser.getAttributeValue(null, "enableCaseInverse"))
                        )
                );
            } catch(Exception e) {
                throw new XmlPullParserException("Exception at <Button> tag", parser, e);
            }
            parser.next(); // END_TAG
        }

        return buttons.toArray(new KeyboardKits.Button[buttons.size()]);
    }

    private static KeyboardKits.Kit[] readKits(XmlPullParser parser) throws IOException, XmlPullParserException {
        ArrayList<KeyboardKits.Kit> kits = new ArrayList<>();

        while(parser.nextTag() != XmlPullParser.END_TAG) { // END_TAG of <Kits>
            parser.require(XmlPullParser.START_TAG, null, "Kit");
            String kitName = parser.getAttributeValue(null, "name");
            String kitShortName = parser.getAttributeValue(null, "shortName");
            boolean kitActionBarAccess = Boolean.parseBoolean(parser.getAttributeValue(null, "actionBarAccess"));
            ArrayList<KeyboardKits.KitVersion> kitVersions = new ArrayList<>();

            int prevPagesCount = -1;
            while(parser.nextTag() != XmlPullParser.END_TAG) { // END_TAG of <Kit>
                KeyboardKits.KitVersion kv = readKitVersion(parser);
                if(prevPagesCount != -1 && kv.mPages.length != prevPagesCount)
                    throw new XmlPullParserException("In all <Variant>s <Page>s count must be the same", parser, null);
                kitVersions.add(kv);
                prevPagesCount = kv.mPages.length;
            }

            KeyboardKits.KitVersion[] kitVArr = kitVersions.toArray(new KeyboardKits.KitVersion[kitVersions.size()]);
            kits.add(new KeyboardKits.Kit(kitName, kitShortName, kitActionBarAccess, kitVArr));
        }

        if(kits.isEmpty())
            throw new XmlPullParserException("<Kit> has 0 <Variant>s", parser, null);

        return kits.toArray(new KeyboardKits.Kit[kits.size()]);
    }

    private static KeyboardKits.KitVersion readKitVersion(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "Version");
        boolean orient = parser.getAttributeValue(null, "orientation").equalsIgnoreCase("landscape");
        ArrayList<KeyboardKits.Page> pages = new ArrayList<>();

        int mainPageIndex = -1;
        int pageId = 0;
        while(parser.nextTag() != XmlPullParser.END_TAG) { // END_TAG of <Version>
            parser.require(XmlPullParser.START_TAG, null, "Page");
            boolean isMain = Boolean.parseBoolean(parser.getAttributeValue(null, "main"));
            String moveToMain = parser.getAttributeValue(null, "moveToMain");
            boolean layoutLandscape = parser.getAttributeValue(null, "layoutOrientation").equalsIgnoreCase("landscape");
            int[][] buttons = readPageRows(parser);

            try {
                pages.add(new KeyboardKits.Page(moveToMain, layoutLandscape, buttons));
            } catch(Exception e) {
                throw new XmlPullParserException("Exception at <Page> tag", parser, e);
            }

            if(isMain) {
                if(mainPageIndex != -1)
                    throw new XmlPullParserException("One and only one <Page> must have 'main' attribute", parser, null);
                mainPageIndex = pageId;
            }

            pageId++;
        }

        if(mainPageIndex == -1)
            throw new XmlPullParserException("One and only one <Page> must have 'main' attribute", parser, null);

        KeyboardKits.Page[] pagesArr = pages.toArray(new KeyboardKits.Page[pages.size()]);
        return new KeyboardKits.KitVersion(pagesArr, orient, mainPageIndex);
    }

    private static int[][] readPageRows(XmlPullParser parser) throws IOException, XmlPullParserException {
        ArrayList<int[]> rows = new ArrayList<>();

        while(parser.nextTag() != XmlPullParser.END_TAG) { // END_TAG if <Page>
            parser.require(XmlPullParser.START_TAG, null, "PageRow");

            ArrayList<Integer> row = new ArrayList<>();
            while(parser.nextTag() != XmlPullParser.END_TAG) { // END_TAG of <PageRow>
                parser.require(XmlPullParser.START_TAG, null, "Button");
                row.add(Integer.parseInt(parser.getAttributeValue(null, "id")));
                parser.next(); // END_TAG of <Button>
            }

            int[] rowArr = new int[row.size()];
            for(int i = 0; i < rowArr.length; i++)
                rowArr[i] = row.get(i);
            rows.add(rowArr);
        }

        return rows.toArray(new int[rows.size()][]);
    }

    public static void restoreDefaultButtonKitsXml(Context context) throws IOException {
        FileOutputStream fileOut;
        InputStream fileDefault;
        try {
            fileOut = context.openFileOutput(PADS_FILENAME, Context.MODE_PRIVATE);
            fileDefault = context.getResources().openRawResource(R.raw.default_keyboard_kits);
        } catch (FileNotFoundException e1) {
            Log.e(Util.LOG_TAG, "Cannot open keyboard_pads.xml for writing", e1);
            throw new IOException(e1);
        }

        byte[] buffer = new byte[256];
        int length;
        while((length = fileDefault.read(buffer)) != -1) {
            fileOut.write(buffer, 0, length);
        }

        fileOut.flush();
        fileOut.close();
        fileDefault.close();
    }


    public static View createContentViewFromPage(Context context, KeyboardKits.Button[] buttons,
                                                 KeyboardKits.Page page, LayoutInflater inflater,
                                                 boolean isEastLocale,
                                                 View.OnLongClickListener btnLongClkListener) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(!page.mIsLayoutLandscape ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        root.setBackgroundColor(context.getResources().getColor(R.color.colorBackground_mainActivity));
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        int[][] table = page.mButtons;
        for (int[] aTable : table) {
            LinearLayout row = supportCreateRowLLWithStyle(context,
                    !page.mIsLayoutLandscape ? R.style.panelRow : R.style.panelRowLand);

            for (int anATable : aTable) {
                int btnId = anATable;
                if (buttons[btnId].mLocaleEastId != -1 && isEastLocale)
                    btnId = buttons[btnId].mLocaleEastId;

                KeyboardKits.Button btnInfo = buttons[btnId];
                View view = inflater.inflate(getButtonLayoutByType(btnInfo.mType), row, false);
                view.setTag(btnId);
                if(view instanceof Button)
                    ((Button) view).setText(btnInfo.mName);
                if(btnInfo.mEnableCaseInverse) {
                    view.setLongClickable(true);
                    view.setOnLongClickListener(btnLongClkListener);
                }

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
                int margin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.5F,
                        context.getResources().getDisplayMetrics()));
                params.setMargins(margin, margin, margin, margin);
                if (page.mIsLayoutLandscape) {
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    params.height = 0;
                }

                row.addView(view, params);
            }

            root.addView(row);
        }

        return root;
    }

    private static int getButtonLayoutByType(KeyboardKits.ButtonType type) {
        switch (type) {
            case EQUALS: return R.layout.button_panelelem_equals;
            case DIGIT:
            case BASE: return R.layout.button_panelelem_base;
            case SYMBOL: return R.layout.button_panelelem_system;
            case SHIFT: return R.layout.button_panelelem_shift;
            default: throw new RuntimeException("Unknown ButtonType: " + type.toString());
        }
    }

    private static LinearLayout supportCreateRowLLWithStyle(Context context, int style) {
        LinearLayout result = new LinearLayout(context);
        result.setPadding(0, 0, 0, 0);
        result.setOrientation(style != R.style.panelRowLand ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params;
        if(style == R.style.panelRow) {
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
        } else {
            params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        }
        result.setLayoutParams(params);
        return result;
    }

    public static KeyboardKits.KitVersion getPreferredKitVersion(KeyboardKits.Kit[] kits,
                                                                 String preferredKitName, boolean landscape) {
        KeyboardKits.Kit kit = null;
        if(preferredKitName == null)
            kit = kits[0];
        else {
            for(KeyboardKits.Kit aKit : kits) {
                if(aKit.mName.equals(preferredKitName))
                    kit = aKit;
            }
            if(kit == null)
                kit = kits[0];
        }

        for(KeyboardKits.KitVersion kv : kit.mKitVersions)
            if(kv.mIsLandscapeOrient == landscape)
                return kv;
        return kit.mKitVersions[0];
    }

}