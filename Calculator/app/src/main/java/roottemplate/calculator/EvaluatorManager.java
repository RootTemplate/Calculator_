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

package roottemplate.calculator;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import roottemplate.calculator.data.AppDatabase;
import roottemplate.calculator.data.NamespaceContract;
import roottemplate.calculator.evaluator.Evaluator;
import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.Named;
import roottemplate.calculator.evaluator.Number;
import roottemplate.calculator.evaluator.Processor;
import roottemplate.calculator.evaluator.Variable;
import roottemplate.calculator.evaluator.impls.DefaultFunction;
import roottemplate.calculator.evaluator.impls.RealNumber;
import roottemplate.calculator.util.Util;
import roottemplate.calculator.view.InputEditText;

public class EvaluatorManager {
    public static final int BRACKET_CLOSING_TYPE_NO = 0;
    public static final int BRACKET_CLOSING_TYPE_IFONE = 1;
    public static final int BRACKET_CLOSING_TYPE_ALWAYS = 2;

    private static final HashMap<Character, Character> replacementMap = new HashMap<>();
    static {
        replacementMap.put('\u2212', '-');
        replacementMap.put('\u00d7', '*');
        replacementMap.put('\u00f7', '/');
    }

    public static String closeUnclosedBrackets(String expr, int closingType) {
        if(closingType == BRACKET_CLOSING_TYPE_NO) return expr;

        int bracketsOpen = countRepeats(expr, '(');
        if(bracketsOpen == 0)
            return expr;
        int closed = bracketsOpen - countRepeats(expr, ')');
        if(closed == 0 || (closingType == BRACKET_CLOSING_TYPE_IFONE && closed > 1))
            return expr;

        StringBuilder sb = new StringBuilder(expr);
        for(int i = 0; i < closed; i++)
            sb.append(')');
        return sb.toString();
    }
    private static int countRepeats(String in, char needle) {
        int i = -1;
        int count = 0;
        while((i = in.indexOf(needle, i + 1)) != -1)
            count++;
        return count;
    }

    public static String replaceAppToEngine(String expr) {
        for(char c : replacementMap.keySet()) {
            expr = expr.replace(c, replacementMap.get(c));
        }
        return expr;
    }

    public static String replaceEngineToApp(String expr) {
        for(Map.Entry e : replacementMap.entrySet()) {
            expr = expr.replace((char) e.getValue(), (char) e.getKey());
        }
        expr = expr.replaceAll("Infinity", "\u221e");
        return expr;
    }

    public static String doubleToPreferableString(double x, int maxLen, PreferencesManager prefs) {
        String res;
        int digitGrouping = prefs.digitGrouping();
        if(prefs.doRound()) {
            res = roottemplate.calculator.evaluator.util.Util.doubleToString(x,
                    maxLen - (digitGrouping > 0 ? 1 : 0), // Reserve 1 digit for commas and stuff
                    Math.round(maxLen * 0.8F), Math.round(maxLen * 0.6F)
            );
        } else
            res = Double.toString(x);

        return res;
    }


    private final Context mContext;
    private final PreferencesManager mPrefs;
    private volatile String mKitName;
    private final NamespaceFragment mFragment;
    private final AppDatabase mDb;
    private volatile Thread mEvaluatorUpdaterThread;
    private volatile boolean mFirstUpdateAfterLaunch;

    public EvaluatorManager(Context context, NamespaceFragment fragment, PreferencesManager prefs,
                            AppDatabase db, boolean isAppJustLaunched) {
        mContext = context;
        mFragment = fragment;
        mPrefs = prefs;
        mDb = db;
        mFirstUpdateAfterLaunch = isAppJustLaunched;
    }

    private String getKitName() {
        return mPrefs.separateNamespace() ? mKitName : null;
    }

    public void setKit(String kitName) {
        if(mEvaluatorUpdaterThread != null)
            mEvaluatorUpdaterThread.interrupt();
        mKitName = kitName;
        mEvaluatorUpdaterThread = new EvaluatorUpdaterThread();
        mEvaluatorUpdaterThread.start();
    }

    public void invalidateEvaluator() {
        if(mEvaluatorUpdaterThread != null)
            mEvaluatorUpdaterThread.interrupt();
        mFragment.mEvaluator = null;
        mEvaluatorUpdaterThread = new EvaluatorUpdaterThread();
        mEvaluatorUpdaterThread.start();
    }

    public EvalResult eval(String text, int maxDigitsToFit) {
        if(mFragment.mEvaluator == null) return new EvalResult(null, null, null);
        // Namespace had not been created by UpdateThread; this should not happen often

        String text_ = text; // Unmodified text
        text = EvaluatorManager.closeUnclosedBrackets(text, mPrefs.bracketClosingType());
        String result, message = null;
        InputEditText.TextType type;
        boolean historyRightIsNull = false;

        try {
            String expr = replaceAppToEngine(text);
            roottemplate.calculator.evaluator.Number n = mFragment.mEvaluator
                    .process(expr);

            if (n != null) {
                result = EvaluatorManager.doubleToPreferableString(n.doubleValue(), maxDigitsToFit,
                        mPrefs);
                result = EvaluatorManager.replaceEngineToApp(result); // To convert "Infinity" -> symbol

                type = InputEditText.TextType.RESULT_NUMBER;
            } else {
                type = InputEditText.TextType.RESULT_MESSAGE;
                result = text_;
                historyRightIsNull = true;
            }
        } catch (EvaluatorException e) {
            message = EvaluatorManager.replaceEngineToApp(e.getLocalizedMessage());
            result = mContext.getResources().getString(R.string.error);
            type = InputEditText.TextType.RESULT_MESSAGE;
        }

        if(mPrefs.enabledHistory()) {
            mDb.getHistory().addHistoryElement(text, historyRightIsNull ? null : result, message);
        }

        return new EvalResult(type, result, message);
    }
    public static class EvalResult {
        public final InputEditText.TextType mTextType;
        public final String mText;
        public final String mMessage;

        public EvalResult(InputEditText.TextType textType, String text, String message) {
            this.mTextType = textType;
            this.mText = text;
            this.mMessage = message;
        }
    }

    public void updateEvaluatorOptions() {
        if(mFragment.mEvaluator != null) {
            updateEvaluatorOptions(mFragment.mEvaluator);
        }
    }
    private void updateEvaluatorOptions(Evaluator e) {
        e.options.ANGLE_MEASURING_UNITS = mPrefs.getAMU();
        e.options.ENABLE_HASH_COMMANDS = false;
    }

    public void clearAllNamespace() {
        mDb.getNamespace().clearAllNamespace();
        invalidateEvaluator();
    }


    public static class NamespaceFragment extends Fragment {
        public static final String FRAGMENT_NAME = "roottemplate.calculator.MainActivity.namespace";

        private Evaluator mEvaluator;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }

    private class EvaluatorUpdaterThread extends Thread {
        @Override
        public void run() {
            if(mFirstUpdateAfterLaunch) {
                mDb.getNamespace().updateDatabase(true, false);
                mFirstUpdateAfterLaunch = false;
            }
            if(isInterrupted()) return;

            Evaluator eval = new Evaluator();
            updateEvaluatorOptions(eval);
            Cursor cursor = mDb.getNamespace().getNamespace(getKitName());
            while(cursor.moveToNext()) {
                if(isInterrupted()) return;

                String name = cursor.getString(cursor.getColumnIndex(
                        NamespaceContract.NamespaceEntry.COLUMN_NAME_NAME));
                String expr = cursor.getString(cursor.getColumnIndex(
                        NamespaceContract.NamespaceEntry.COLUMN_NAME_EXPRESSION));

                Named n;
                switch (cursor.getInt(cursor.getColumnIndex(
                        NamespaceContract.NamespaceEntry.COLUMN_NAME_TYPE))) {
                    case 0:
                        // NUMBER
                        n = new Variable(name, Double.valueOf(expr));
                        break;
                    case 1:
                        // DefaultFunction
                        String[] args = expr.split(";");
                        Variable[] vars = new Variable[args.length - 1];
                        for(int i = 0; i < vars.length; i++)
                            vars[i] = new Variable(args[i + 1]);

                        try {
                            n = new DefaultFunction(eval, name, args[0], vars);
                        } catch (EvaluatorException e) {
                            Log.wtf(Util.LOG_TAG, "Why error while updating Evaluator?", e);
                            continue;
                        }
                        break;
                    default:
                        Log.e(Util.LOG_TAG, "[Namespace] New entry type added without support");
                        continue;
                }

                eval.add(n, cursor.getInt(cursor.getColumnIndex(
                        NamespaceContract.NamespaceEntry.COLUMN_NAME_IS_STANDARD)) == 1);
            }

            NamespaceWatcher defListener = new NamespaceWatcher();
            eval.processor.setDefineListener(defListener);
            if(isInterrupted()) return;
            mFragment.mEvaluator = eval;
            mEvaluatorUpdaterThread = null;
        }
    }

    public class NamespaceWatcher implements Processor.DefineListener {

        @Override
        public void onDefine(Named n, String defName, String equals, boolean firstAppearance,
                             boolean isStandard) {
            int type;
            String expr;
            switch (n.getElementType()) {
                case NUMBER:
                    if(!(n instanceof Variable) || !(((Number) n).toNumber() instanceof RealNumber))
                        Log.e(Util.LOG_TAG, "[Namespace] New Number class found - " +
                                "no support added");
                    type = 0;
                    expr = Double.toString(((Number) n).doubleValue());
                    break;
                case OPERATOR:
                    if(!(n instanceof DefaultFunction)) {
                        Log.e(Util.LOG_TAG, "[Namespace] New Operator class found - " +
                                "no support added");
                        return;
                    }
                    DefaultFunction f = (DefaultFunction) n;
                    StringBuilder sb = new StringBuilder();
                    sb.append(equals);
                    for(Variable var : f.getVars())
                        sb.append(";").append(var.getName());

                    type = 1;
                    expr = sb.toString();
                    break;
                default:
                    Log.e(Util.LOG_TAG, "[Namespace] New class found " +
                            n.getClass().toString() + " - no support added");
                    return;
            }
            mDb.getNamespace().putNamespaceElement(getKitName(), n.getName(), type, expr, isStandard);
        }
    }
}
