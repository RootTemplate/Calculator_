/* 
 * Copyright 2016 RootTemplate Group 1
 *
 * This file is part of Calculator_ Engine (Evaluator).
 *
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
package roottemplate.calculator.evaluator;

import roottemplate.calculator.evaluator.impls.RealNumber;
import java.util.ArrayList;
import roottemplate.calculator.evaluator.impls.DefaultFunction;

public class Processor {
    private final Evaluator namespace;
    
    public Processor(Evaluator namespace) {
        this.namespace = namespace;
    }
    
    /**
     * @param expr The expression string
     * @return true if given string is a definition
     */
    public static boolean isDefinition(String expr) {
        return expr.indexOf('=') != -1;
    }
    
    /**
     * Evaluates or defines expression. See eval(String) and define(String).
     * @param expr The expression string
     * @return Result number or null if definition
     * @throws EvaluatorException if expression is bad-formed
     */
    public Number process(String expr) throws EvaluatorException {
        return process(expr, false);
    }
    Number process(String expr, boolean isStandard) throws EvaluatorException {
        Number result = null;
        for(String curExpr : expr.split(";")) {
            String _expr;
            if((_expr = curExpr.trim()).startsWith("#") && namespace.options.ENABLE_HASH_COMMANDS) {
                _expr = _expr.substring(1);
                result = processHashCommand(_expr);
            } else if(isDefinition(curExpr)) {
                define(curExpr, isStandard);
                result = null;
            } else
                result = eval(curExpr);
        }
        return result;
    }
    
    /**
     * Proccess hash command. The hash symbol itself must be erased from the command.
     * @param command The command to process
     * @return Result number
     * @throws EvaluatorException If error occurs
     */
    public Number processHashCommand(String command) throws EvaluatorException {
        if(command.startsWith("delete")) {
            command = command.substring(6).trim();
            return namespace.removeAny(command) ? new RealNumber(1) : new RealNumber(0);
        } else if(command.equals("clear")) {
            return new RealNumber(namespace.clear());
        } else if(command.startsWith("set")) {
            command = command.substring(3).trim();
            if(command.startsWith("AMU")) {
                try {
                    namespace.options.ANGLE_MEASURING_UNITS = Integer.parseInt(command.substring(3));
                } catch(NumberFormatException ex) {
                    throw new EvaluatorException(ex);
                }
                return new RealNumber(1);
            }
        }
        
        // Any function above should return something
        throw new EvaluatorException("Bad number sign command");
    }
    
    /**
     * Evaluates given expression string with the namespace that processor was created with
     * @param expr The expression string
     * @return Evaluated number
     * @throws EvaluatorException if exception occur
     */
    public Number eval(String expr) throws EvaluatorException {
        return Expression.parse(expr, namespace).eval();
    }
    
    /**
     * Defines a variable(s) or function(s) to the namespace this processor was created with
     * @param expr The expression string
     * @throws EvaluatorException if expression is bad-formed
     */
    public void define(String expr) throws EvaluatorException {
        define(expr, false);
    }
    void define(String expr, boolean isStandard) throws EvaluatorException {
        String[] def = expr.split("=");
        if(def.length < 2) // non definition
            throw new EvaluatorException("Given expression is not a definition");
        
        try {
            int indexOf = expr.indexOf('(');
            String equalsTo = def[def.length - 1];
            boolean func = indexOf != -1 && (expr.length() - equalsTo.length() > expr.indexOf('('));
            
            Number evaluated = null;
            
            for(int i = 0; i < def.length - 1; i++) {
                String var = def[i].trim();
                if(var.isEmpty())
                    throw new EvaluatorException("Variable name is empty");
                boolean curFunc = (indexOf = var.indexOf('(')) != -1;
                if(curFunc != func)
                    throw new EvaluatorException("Function and variable definition");
                
                if(curFunc) {
                    // function
                    if(!var.endsWith(")"))
                        throw new EvaluatorException("Name of function must end with \")\"");
                    String[] args = var.substring(indexOf + 1, var.length() - 1).split(",");
                    String name = var.substring(0, indexOf).trim();

                    int j = 0;
                    Variable[] vars = new Variable[args.length];
                    ArrayList<String> names = new ArrayList<>(args.length);
                    for(String arg : args) {
                        arg = arg.trim();
                        if(names.contains(arg))
                            throw new EvaluatorException("Function contains two arguments with same names \"" + arg + "\"");
                        names.add(arg);
                        
                        vars[j++] = new Variable(arg);
                    }
                    
                    namespace.removeOperator(name, Function.FUNCTION_USES);
                    namespace.add(new DefaultFunction(namespace, name, equalsTo, vars), isStandard);
                } else {
                    // variable
                    if(evaluated == null)
                        evaluated = eval(equalsTo);
                    
                    Named obj = namespace.getOther(var);
                    if(obj == null) {
                        obj = new Variable(var);
                        namespace.add(obj, isStandard);
                    } else if(!(obj instanceof Variable))
                        throw new EvaluatorException(var + " has been already defined as non variable");
                    ((Variable) obj).changeValue(evaluated);
                }
            }
        } catch(IllegalArgumentException ex) {
            throw new EvaluatorException(ex);
        }
    }
}
