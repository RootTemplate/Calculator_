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

import java.util.ArrayList;

public class BufferIndexManager {
    private final ArrayList<Boolean> indexes = new ArrayList<>(7);
    
    public int allocIndex() {
        int freeIndex = indexes.indexOf(false);
        if(freeIndex != -1) {
            indexes.set(freeIndex, true);
            return freeIndex;
        }
        indexes.add(true);
        return indexes.size() - 1;
    }
    
    public int freeIndex(int index) {
        indexes.set(index, false);
        return index;
    }
    
    public int getBufferSize() {
        return indexes.size();
    }
}
