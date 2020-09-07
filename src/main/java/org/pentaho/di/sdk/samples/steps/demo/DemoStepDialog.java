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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.LabelText;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * UI
 *
 * @author xhy
 */
public class DemoStepDialog extends BaseStepDialog implements StepDialogInterface {

    private static Class<?> PKG = DemoStepMeta.class;

    private DemoStepMeta meta;

    private LabelText changeColLabelText;
    private TableView changeTableView;

    public DemoStepDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
        super(parent, (BaseStepMeta) in, transMeta, sname);
        meta = (DemoStepMeta) in;
    }

    //TODO 当用户打开对话框时调用此方法，仅当用户关闭对话框时返回
    //     此方法必须在用户确认时返回这个步骤的名字，或者在用户取消时返回null
    //     其中，changed标记必须反映对话框是否更改了步骤配置，用户取消时，标志位不能改变
    public String open() {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
        props.setLook(shell);
        setShellImage(shell, meta);

        changed = meta.hasChanged();

        ModifyListener lsMod = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                meta.setChanged();
            }
        };

        // ------------------------------------------------------- //
        // SWT code for building the actual settings dialog        //
        // ------------------------------------------------------- //
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;
        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "Demo.Shell.Title"));
        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        wlStepname = new Label(shell, SWT.RIGHT);
        wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
        props.setLook(wlStepname);
        fdlStepname = new FormData();
        fdlStepname.left = new FormAttachment(0, 0);
        fdlStepname.right = new FormAttachment(middle, -margin);
        fdlStepname.top = new FormAttachment(0, margin);
        wlStepname.setLayoutData(fdlStepname);

        wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wStepname.setText(stepname);
        props.setLook(wStepname);
        wStepname.addModifyListener(lsMod);
        fdStepname = new FormData();
        fdStepname.left = new FormAttachment(middle, 0);
        fdStepname.top = new FormAttachment(0, margin);
        fdStepname.right = new FormAttachment(100, 0);
        wStepname.setLayoutData(fdStepname);

        changeColLabelText = new LabelText(shell, "需要替换的列名", null);
        props.setLook(changeColLabelText);
        changeColLabelText.addModifyListener(lsMod);
        FormData fdValName = new FormData();
        fdValName.left = new FormAttachment(0, 0);
        fdValName.right = new FormAttachment(100, 0);
        fdValName.top = new FormAttachment(wStepname, margin);
        changeColLabelText.setLayoutData(fdValName);

        final int FieldsCols = 2;
        final int FieldsRows = 10;
        Control lastControl = changeColLabelText;

        ColumnInfo[] colinf;
        colinf = new ColumnInfo[FieldsCols];
        colinf[0] = new ColumnInfo("替换前的字符", ColumnInfo.COLUMN_TYPE_TEXT, new String[]{""}, false);
        colinf[1] = new ColumnInfo("替换后的字符", ColumnInfo.COLUMN_TYPE_TEXT, new String[]{""}, false);

        changeTableView = new TableView(transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props);

        FormData fd = new FormData();
        fd.left = new FormAttachment(0, 0);
        fd.top = new FormAttachment(lastControl, margin);
        fd.right = new FormAttachment(100, 0);
        fd.bottom = new FormAttachment(100, -50);
        changeTableView.setLayoutData(fd);
        lastControl = changeTableView;


        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
        setButtonPositions(new Button[]{wOK, wCancel}, margin, lastControl);

        lsCancel = new Listener() {
            public void handleEvent(Event e) {
                cancel();
            }
        };
        lsOK = new Listener() {
            public void handleEvent(Event e) {
                ok();
            }
        };
        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);

        lsDef = new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                ok();
            }
        };
        wStepname.addSelectionListener(lsDef);
        changeColLabelText.addSelectionListener(lsDef);

        shell.addShellListener(new ShellAdapter() {
            public void shellClosed(ShellEvent e) {
                cancel();
            }
        });

        setSize();

        populateDialog();

        changeTableView.removeEmptyRows();
        changeTableView.setRowNums();
        changeTableView.optWidth(true);

        meta.setChanged(changed);
        getColNames();

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        return stepname;
    }

    //TODO 数据填充
    private void populateDialog() {
        wStepname.selectAll();
        Map<String, String> changeStr = meta.getChangeStr();
        Iterator iter = changeStr.entrySet().iterator();
        int row = 0;
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            changeTableView.setText(key.toString(), 1, row);
            changeTableView.setText(val.toString(), 2, row++);
        }
        changeColLabelText.setText(meta.getChangeCol());
    }

    public void getColNames() {
        try {
            RowMetaInterface rowMetaInterface = transMeta.getPrevStepFields(stepname);
            int num = rowMetaInterface.size();
            List<String> colList = new ArrayList<>();
            for (int i = 0; i < num; i++) {
                ValueMetaInterface v = rowMetaInterface.getValueMeta(i);
                colList.add(v.getName());
            }
            meta.setColNameList(colList);
        } catch (KettleStepException e) {
            e.printStackTrace();
        }
    }

    //TODO '取消'方法
    private void cancel() {
        stepname = null;
        meta.setChanged(changed);
        dispose();
    }

    //TODO '确定'方法
    private void ok() {
        stepname = wStepname.getText();
        int count = changeTableView.nrNonEmpty();
        Map<String, String> tableData = new HashMap<>();
        for (int i = 0; i < count; i++) {
            TableItem item = changeTableView.getNonEmpty(i);
            tableData.put(item.getText(1), item.getText(2));
        }
        meta.setChangeStr(tableData);
        meta.setChangeCol(changeColLabelText.getText());
        dispose();
    }

}
