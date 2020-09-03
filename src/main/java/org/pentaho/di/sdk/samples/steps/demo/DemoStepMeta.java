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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Meta
 *
 * @author xhy
 */
//配置的具体细节请参阅官方的开发手册
@Step(
        id = "DemoStep",
        name = "DemoStep.Name",
        description = "DemoStep.TooltipDesc",
        image = "org/pentaho/di/sdk/samples/steps/demo/resources/demo.svg",
        categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Transform",
        i18nPackageName = "org.pentaho.di.sdk.samples.steps.demo",
        documentationUrl = "DemoStep.DocumentationURL",
        casesUrl = "DemoStep.CasesURL",
        forumUrl = "DemoStep.ForumURL"
)
@InjectionSupported(localizationPrefix = "DemoStepMeta.Injection.")
public class DemoStepMeta extends BaseStepMeta implements StepMetaInterface {

    private static final Class<?> PKG = DemoStepMeta.class;

    //需要替换的列名
    private String changeCol;
    //替换前后的字符
    private Map<String, String> changeStr;

    public DemoStepMeta() {
        super();
    }

    public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name) {
        return new DemoStepDialog(shell, meta, transMeta, name);
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp) {
        return new DemoStep(stepMeta, stepDataInterface, cnr, transMeta, disp);
    }

    public StepDataInterface getStepData() {
        return new DemoStepData();
    }

    //TODO 设置创建步骤时的初始化值
    public void setDefault() {
        changeStr = new HashMap<>();
        changeStr.put("$", "dollar");
        changeStr.put("#", "sharp");
        setChangeStr(changeStr);
        setChangeCol("1,2,3");
    }

    public String getChangeCol() {
        return changeCol;
    }

    public void setChangeCol(String changeCol) {
        this.changeCol = changeCol;
    }

    public Map<String, String> getChangeStr() {
        return changeStr;
    }

    public void setChangeStr(Map<String, String> changeStr) {
        this.changeStr = changeStr;
    }

    //TODO 复制步骤，必须是深拷贝
    public Object clone() {
        Object retval = super.clone();
        return retval;
    }

    //TODO 当步骤需要将其配置序列化为XML时 Spoon会调用此方法
    public String getXML() {
        StringBuilder xml = new StringBuilder();
        String obj = JSONObject.toJSONString(changeStr);
        xml.append(XMLHandler.addTagValue("changeStr", obj));
        xml.append(XMLHandler.addTagValue("changeCol", changeCol));
        return xml.toString();
    }

    //TODO 当步骤需要从XML加载其配置时，PDI会调用这个方法
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore) throws KettleXMLException {
        try {
            String obj = XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "changeStr"));
            setChangeStr(JSONObject.parseObject(obj, HashMap.class));
            setChangeCol(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "changeCol")));
        } catch (Exception e) {
            throw new KettleXMLException("Demo plugin unable to read step info from XML node", e);
        }
    }

    //TODO 当步骤需要将其配置保存到存储库时，Spoon将调用此方法
    public void saveRep(Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step) throws KettleException {
        try {
            //rep.saveStepAttribute(id_transformation, id_step, "outputfield", outputField); //$NON-NLS-1$
        } catch (Exception e) {
            throw new KettleException("Unable to save step into repository: " + id_step, e);
        }
    }
    //TODO 当一个步骤需要从存储库中读取其配置时，PDI将调用此方法
    public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases) throws KettleException {
        try {
            //outputField = rep.getStepAttributeString(id_step, "outputfield"); //$NON-NLS-1$
        } catch (Exception e) {
            throw new KettleException("Unable to load step from repository", e);
        }
    }

    //TODO 在Step对行流所做任何更改时，必须调用此方法
    //     如果想在下一个步骤获取这个步骤新增的字段名称，必须在这里新增字段
    public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space, Repository repository, IMetaStore metaStore) throws KettleStepException {

        /*
         * This implementation appends the outputField to the row-stream
         */

        // a value meta object contains the meta data for a field
        //ValueMetaInterface v = new ValueMetaString(outputField);

        // setting trim type to "both"
        //v.setTrimType(ValueMetaInterface.TRIM_TYPE_BOTH);

        // the name of the step that adds this field
        //v.setOrigin(name);

        // modify the row structure and add the field this step generates
        //inputRowMeta.addValueMeta(v);
    }

    //TODO 当用户选择"Verify Transformation"时，Spoon将调用此方法
    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository, IMetaStore metaStore) {
        CheckResult cr;
        // See if there are input streams leading to this step!
        if (input != null && input.length > 0) {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "Demo.CheckResult.ReceivingRows.OK"), stepMeta);
            remarks.add(cr);
        } else {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "Demo.CheckResult.ReceivingRows.ERROR"), stepMeta);
            remarks.add(cr);
        }
    }

}
