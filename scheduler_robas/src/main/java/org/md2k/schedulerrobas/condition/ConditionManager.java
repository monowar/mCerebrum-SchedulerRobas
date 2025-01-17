package org.md2k.schedulerrobas.condition;
/*
 * Copyright (c) 2016, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import com.udojava.evalex.Expression;

import org.md2k.schedulerrobas.condition.function.Function;
import org.md2k.schedulerrobas.condition.function.day_of_week;
import org.md2k.schedulerrobas.condition.function.get_2nd_last_sample_time;
import org.md2k.schedulerrobas.condition.function.get_data_quality;
import org.md2k.schedulerrobas.condition.function.get_last_sample;
import org.md2k.schedulerrobas.condition.function.get_last_sample_time;
import org.md2k.schedulerrobas.condition.function.get_phone_battery;
import org.md2k.schedulerrobas.condition.function.get_sample_no;
import org.md2k.schedulerrobas.condition.function.get_study_week;
import org.md2k.schedulerrobas.condition.function.is_active;
import org.md2k.schedulerrobas.condition.function.is_active_day;
import org.md2k.schedulerrobas.condition.function.is_day_of_week;
import org.md2k.schedulerrobas.condition.function.is_driving;
import org.md2k.schedulerrobas.condition.function.is_privacy_on;
import org.md2k.schedulerrobas.condition.function.now;
import org.md2k.schedulerrobas.condition.function.random;
import org.md2k.schedulerrobas.condition.function.time_offset;
import org.md2k.schedulerrobas.condition.function.today;
import org.md2k.schedulerrobas.datakit.DataKitManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Locale;

public class ConditionManager {
    private ArrayList<Function> functions;
    private static ConditionManager instance;
    public static ConditionManager getInstance() {
        if(instance==null) instance = new ConditionManager();
        return instance;
    }

    private ConditionManager() {
        functions = new ArrayList<>();

        functions.add(new day_of_week());

        functions.add(new get_data_quality());
        functions.add(new get_last_sample());
        functions.add(new get_last_sample_time());
        functions.add(new get_2nd_last_sample_time());
        functions.add(new get_phone_battery());
        functions.add(new get_sample_no());

        functions.add(new get_study_week());
        functions.add(new is_active());
        functions.add(new is_active_day());
        functions.add(new is_day_of_week());
        functions.add(new is_driving());
        functions.add(new is_privacy_on());

        functions.add(new now());
        functions.add(new time_offset());
        functions.add(new today());

        functions.add(new random());

    }

    private String prepare(String str) {
        str = uppercaseFix(str);
        for (int i = 0; i < functions.size(); i++)
            str = functions.get(i).prepare(str);
        return str;
    }
    private String uppercaseFix(String s){
        return s.replaceAll("([A-Z]+(_*[A-Z]*[0-9]*)*)","\"$1\"");
    }
    public boolean isTrue(String str){
        return isTrue(str, new ArrayList<>());
    }
    public boolean isTrue(String str, ArrayList<String> details){
        BigDecimal res = evaluate(str, details);
        return res.doubleValue() != 0;
    }
    public BigDecimal evaluate(String str){
        return evaluate(str, new ArrayList<>());
    }

    public BigDecimal evaluate(String str, ArrayList<String> details) {

        if(str==null || str.length()==0 || str.equalsIgnoreCase("true")) {
            details.add("condition");
            if(str==null) details.add("null");
            else details.add(str);
            details.add("1");
            return new BigDecimal(1);
        }
        if(str.equalsIgnoreCase("false")) {
            details.add("condition");
            details.add(str);
            details.add("0");
            return new BigDecimal(0);
        }
        String strNew = prepare(str);
        Expression ex = new Expression(strNew);
        ex = ex.setPrecision(0);
        for (int i = 0; i < functions.size(); i++)
            ex = functions.get(i).add(ex, details);
        BigDecimal res=ex.eval();
        details.add("condition");
        details.add(str);
        details.add(String.format(Locale.getDefault(),"%.2f",res.doubleValue()));

        return res;
    }
}