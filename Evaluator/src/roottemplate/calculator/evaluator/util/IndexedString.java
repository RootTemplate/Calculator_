/*
 * Copyright 2016-2017 RootTemplate Group 1
 *
 * This file is part of Calculator_ Engine (Evaluator).
 * Calculator_ Engine (Evaluator) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Calculator_ Engine (Evaluator) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Calculator_ Engine (Evaluator).  If not, see <http://www.gnu.org/licenses/>.
 */
package roottemplate.calculator.evaluator.util;

public class IndexedString {
    public final String str;
    public int index;

    public IndexedString(String str) {
        this(str, 0);
    }
    public IndexedString(String str, int index) {
        this.str = str;
        this.index = index;
    }
    public IndexedString(IndexedString str, int slice) {
        this.str = str.str;
        this.index = str.index + slice;
    }

    public boolean isEmpty() {
        return str.length() <= index;
    }

    public char charAt(int i) {
        return str.charAt(index + i);
    }
    public char charAt() {
        return str.charAt(index);
    }

    public boolean startsWith(String name) {
        return str.startsWith(name, index);
    }

    public int length() {
        return str.length() - index;
    }

    public IndexedString substring(int startIndex, int endIndex) {
        return new IndexedString(str.substring(index + startIndex, index + endIndex));
    }

    @Override
    public String toString() {
        return str.substring(index);
    }
}
