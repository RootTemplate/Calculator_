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

import roottemplate.calculator.evaluator.Evaluator;
import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.Number;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Tester {
    public static void main(String[] args) throws EvaluatorException, IOException {
        Evaluator e = new Evaluator();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while(true) {
            System.out.print("> ");
            String in = br.readLine();
            if(in.equals("#exit")) break;

            Number res = e.process(in);
            if(res != null)
                System.out.println("< " + res.stringValue());
        }
    }
}
