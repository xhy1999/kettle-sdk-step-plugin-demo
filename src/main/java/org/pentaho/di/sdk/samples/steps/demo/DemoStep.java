/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.sdk.samples.steps.demo;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Step
 * 
 * @author xhy
 */
public class DemoStep extends BaseStep implements StepInterface {

    private static final Class<?> PKG = DemoStepMeta.class;

    private List<String> changeColList;
    private Map<String, String> changeStr;
    private List<String> colNameList;

    public DemoStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
        super(s, stepDataInterface, c, t, dis);
    }

    //TODO 初始化方法，可以建立数据库链接、获取文件句柄等操作，会被PDI调用​​​​​​​。
    public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
        DemoStepMeta meta = (DemoStepMeta) smi;
        DemoStepData data = (DemoStepData) sdi;
        if (!super.init(meta, data)) {
            return false;
        }
        //在这里添加特定的初始化代码
        String changeColNamesStr = meta.getChangeCol();
        colNameList = meta.getColNameList();
        changeColList = new ArrayList<>();
        while (StringUtils.isNotBlank(changeColNamesStr)) {
            int i;
            String str;
            if ((i = changeColNamesStr.indexOf(",")) > 0) {
                str = changeColNamesStr.substring(0, i).trim();
                if (StringUtils.isNotBlank(str) && colNameList.contains(str)) {
                    changeColList.add(str);
                }
                changeColNamesStr = changeColNamesStr.substring(i + 1);
            } else {
                str = changeColNamesStr.trim();
                if (StringUtils.isNotBlank(str) && colNameList.contains(str)) {
                    changeColList.add(str);
                }
                break;
            }
        }
        changeStr = meta.getChangeStr();
        return true;
    }

    //TODO 读取行的业务逻辑，会被PDI调用，当此方法返回false时，完成行读取。
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
        DemoStepMeta meta = (DemoStepMeta) smi;
        DemoStepData data = (DemoStepData) sdi;
        //从输入流中读取一行
        Object[] r = getRow();

        //若读不到下一行，则读写完成，调用setOutputDone()，return false
        if (r == null) {
            setOutputDone();
            return false;
        }

        if (first) {
            first = false;
            //如果是第一行则保存数据行元信息到data类中,后续使用
            data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
        }

        //字符替换的业务逻辑
//        for (int i = 0; i < r.length && r[i] != null; i++) {
//            String str = data.outputRowMeta.getString(r, i);
//            if (changeColList.indexOf((i + 1) + "") >= 0) {
//                Iterator iter = changeStr.entrySet().iterator();
//                while (iter.hasNext()) {
//                    Map.Entry entry = (Map.Entry) iter.next();
//                    Object before = entry.getKey();
//                    Object after = entry.getValue();
//                    str = str.replace(String.valueOf(before), String.valueOf(after));
//                }
//                r[i] = str.getBytes();
//            }
//        }
        for (int i = 0; i < changeColList.size(); i++) {
            String changeColName = changeColList.get(i);
            int index;
            if ((index = colNameList.indexOf(changeColName)) >= 0) {
                String str = data.outputRowMeta.getString(r, index);
                Iterator iter = changeStr.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    Object before = entry.getKey();
                    Object after = entry.getValue();
                    str = str.replace(String.valueOf(before), String.valueOf(after));
                }
                r[index] = str.getBytes();
            }
        }

        //将行放入输出行流
        putRow(data.outputRowMeta, r);

        //如有需要，可以进行日志记录
        if (checkFeedback(getLinesRead())) {
            logBasic(BaseMessages.getString(PKG, "DemoStep.Linenr", getLinesRead()));
        }

        //返回true则表示还应继续使用processRow()读取下一行
        return true;
    }

    //TODO 析构函数，用来释放资源，会被PDI调用。
    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
        DemoStepMeta meta = (DemoStepMeta) smi;
        DemoStepData data = (DemoStepData) sdi;

        super.dispose(meta, data);
    }

}
