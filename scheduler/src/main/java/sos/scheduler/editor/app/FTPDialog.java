/**
 * Copyright (C) 2014 BigLoupe http://bigloupe.github.io/SoS-JobScheduler/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
/********************************************************* begin of preamble
**
** Copyright (C) 2003-2012 Software- und Organisations-Service GmbH. 
** All rights reserved.
**
** This file may be used under the terms of either the 
**
**   GNU General Public License version 2.0 (GPL)
**
**   as published by the Free Software Foundation
**   http://www.gnu.org/licenses/gpl-2.0.txt and appearing in the file
**   LICENSE.GPL included in the packaging of this file. 
**
** or the
**  
**   Agreement for Purchase and Licensing
**
**   as offered by Software- und Organisations-Service GmbH
**   in the respective terms of supply that ship with this file.
**
** THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
** IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
** THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
** PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
** BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
** CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
** SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
** INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
** CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
** ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
** POSSIBILITY OF SUCH DAMAGE.
********************************************************** end of preamble*/
package sos.scheduler.editor.app;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import sos.scheduler.editor.app.MainWindow;
import sos.scheduler.editor.app.ResourceManager;
import sos.util.SOSString;
import com.swtdesigner.SWTResourceManager;
import sos.scheduler.editor.conf.SchedulerDom;
import sos.scheduler.editor.conf.forms.SchedulerForm;
import java.io.File;
import java.util.HashMap;
import java.util.ArrayList;

import sos.ftp.profiles.FTPProfilePicker;
import sos.ftp.profiles.FTPDialogListener;
import sos.ftp.profiles.FTPProfile;

public class FTPDialog {

    private Button butOpenOrSave = null;

    private Group schedulerGroup = null;

    private Shell schedulerConfigurationShell = null;

    private FTPDialogListener listener = null;

    private Table table = null;

    private Text txtDir = null;

    private SOSString sosString = new SOSString();

    private Text txtFilename = null;

    // private MainWindow main = null;

    private Text txtLog = null;

    private String type = "Open";

    public static String OPEN = "Open";

    public static String SAVE_AS = "Save As";

    public static String SAVE_AS_HOT_FOLDER = "Save As Hot Folder";

    public static String OPEN_HOT_FOLDER = "Open Hot Folder";

    private Button butChangeDir = null;

    private Button butRefresh = null;

    private Button butNewFolder = null;

    private Button butRemove = null;

    private TableColumn newColumnTableColumn_1 = null;

    private Button butSite = null;

    private Button butClose = null;

    private FTPProfilePicker ftpProfilePicker = null;

    private TableColumn newColumnTableColumn_2 = null;

    public FTPDialog(MainWindow main_) {
        // main = main_;
    }

    /**
     * @wbp.parser.entryPoint
     */

    public void showForm(String type_) {
        try {
            type = type_;
            schedulerConfigurationShell = new Shell(MainWindow.getSShell(), SWT.CLOSE | SWT.TITLE | SWT.APPLICATION_MODAL | SWT.BORDER | SWT.RESIZE);
            schedulerConfigurationShell.setImage(ResourceManager.getImageFromResource("/sos/scheduler/editor/editor.png"));

            schedulerConfigurationShell.addTraverseListener(new TraverseListener() {
                public void keyTraversed(final TraverseEvent e) {
                    if (e.detail == SWT.TRAVERSE_ESCAPE) {
                        try {
                            listener.getCurrProfile().disconnect();
                            schedulerConfigurationShell.dispose();
                        }
                        catch (Exception r) {
                            try {
                                new ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName(), r);
                            }
                            catch (Exception ee) {
                                // tu nichts
                            }
                        }
                    }
                }
            });

            final GridLayout gridLayout = new GridLayout();
            gridLayout.numColumns = 2;
            gridLayout.marginTop = 5;
            gridLayout.marginRight = 5;
            gridLayout.marginLeft = 5;
            gridLayout.marginBottom = 5;
            schedulerConfigurationShell.setLayout(gridLayout);
            schedulerConfigurationShell.setSize(625, 486);
            schedulerConfigurationShell.setText(type);

            {
                schedulerGroup = new Group(schedulerConfigurationShell, SWT.NONE);
                schedulerGroup.setText("Open");
                final GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1);
                gridData.widthHint = 581;
                gridData.heightHint = 329;
                schedulerGroup.setLayoutData(gridData);

                final GridLayout gridLayout_1 = new GridLayout();
                gridLayout_1.numColumns = 3;
                gridLayout_1.marginTop = 5;
                gridLayout_1.marginRight = 5;
                gridLayout_1.marginLeft = 5;
                gridLayout_1.marginBottom = 5;
                schedulerGroup.setLayout(gridLayout_1);

                ftpProfilePicker = new FTPProfilePicker(schedulerGroup, SWT.NONE, new File(Options.getSchedulerData(), "config/factory.ini"));

                // ftpProfilePicker.setLayoutData(new GridData(GridData.FILL,
                // GridData.CENTER, false, false, 2, 1));
                ftpProfilePicker.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));

                listener = ftpProfilePicker.getListener();
                if (ftpProfilePicker.getSelectedProfilename() != null && ftpProfilePicker.getSelectedProfilename().length() > 0) {
                    ftpProfilePicker.getProfileByName(ftpProfilePicker.getSelectedProfilename());
                    listener = ftpProfilePicker.getListener();
                }

                // Hier: wenn ein neuer Profile im Combobox ausgew�hlt wird
                ftpProfilePicker.addSelectionListener((new SelectionAdapter() {
                    public void widgetSelected(final SelectionEvent e) {
                        try {

                            txtDir.setText("");
                            table.removeAll();
                            txtFilename.setText("");
                            listener.setCurrProfileName(ftpProfilePicker.getSelectedProfilename());
                            initForm();
                            butOpenOrSave.setEnabled(listener.getCurrProfile().isLoggedIn() && txtFilename.getText().length() > 0);
                            _setEnabled(listener.getCurrProfile().isLoggedIn());
                        }
                        catch (Exception r) {
                            MainWindow.message("error while choice Profilename: " + e.toString(), SWT.ICON_WARNING);
                            try {
                                new ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName(), r);
                            }
                            catch (Exception ee) {
                                // tu nichts
                            }
                        }
                    }

                }));

                butSite = new Button(schedulerGroup, SWT.NONE);
                butSite.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
                butSite.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(final SelectionEvent e) {

                        Utils.startCursor(schedulerConfigurationShell);
                        try {

                            if (listener.getProfileNames().length == 0) {
                                MainWindow.message("Please first define a Profile", SWT.ICON_WARNING);
                                return;
                            }

                            FTPProfile profile = listener.getCurrProfile();

                            sos.net.SOSFileTransfer p = profile.connect();

                            if (p != null && p.isConnected()) {
                                HashMap h = profile.changeDirectory(ftpProfilePicker.getSelectedProfilename(), txtDir.getText());
                                if (profile.isLoggedIn()) {
                                    butOpenOrSave.setEnabled(profile.isLoggedIn() && txtFilename.getText().length() > 0);
                                    fillTable(h);
                                    table.setSortDirection(SWT.UP);
                                    sort(newColumnTableColumn_2);
                                    _setEnabled(true);
                                }
                            }
                        }
                        catch (Exception ex) {
                            try {

                                MainWindow.message("error while connecting: " + ex.toString(), SWT.ICON_WARNING);
                                new ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName(), ex);
                            }
                            catch (Exception ee) {
                                // tu nichts
                            }
                        }
                        Utils.stopCursor(schedulerConfigurationShell);

                    }
                });
                butSite.setText("Connect");
                /*
                 * String selectProfile = Options.getProperty("last_profile");
                 * if(selectProfile != null && selectProfile.length() > 0) {
                 * if(listener == null) listener =
                 * ftpProfilePicker.getListener(); if(
                 * listener.getProfiles().get(selectProfile) != null) {
                 * listener.setCurrProfileName(selectProfile); if(txtDir !=
                 * null) { txtDir.setText(listener.getCurrProfile() != null &&
                 * listener.getCurrProfile().getRoot() != null ?
                 * listener.getCurrProfile().getRoot() : "");
                 * _setEnabled(false); } } }
                 */

                txtDir = new Text(schedulerGroup, SWT.BORDER);
                txtDir.addKeyListener(new KeyAdapter() {
                    public void keyPressed(final KeyEvent e) {
                        try {
                            if (e.keyCode == SWT.CR) {
                                FTPProfile profile = listener.getCurrProfile();
                                HashMap h = profile.changeDirectory(txtDir.getText());
                                fillTable(h);
                            }
                        }
                        catch (Exception r) {
                            MainWindow.message("error while change Directory: " + e.toString(), SWT.ICON_WARNING);
                            try {
                                new ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName(), r);
                            }
                            catch (Exception ee) {
                                // tu nichts
                            }
                        }
                    }
                });
                txtDir.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));

                butChangeDir = new Button(schedulerGroup, SWT.NONE);
                butChangeDir.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(final SelectionEvent e) {
                        try {
                            Utils.startCursor(schedulerConfigurationShell);

                            // HashMap h = listener.changeDirectory(
                            // cboConnectname.getText(), txtDir.getText());
                            HashMap h = listener.getCurrProfile().changeDirectory(ftpProfilePicker.getSelectedProfilename(), txtDir.getText());
                            fillTable(h);
                            Utils.stopCursor(schedulerConfigurationShell);
                        }
                        catch (Exception r) {
                            MainWindow.message("error: " + e.toString(), SWT.ICON_WARNING);
                            try {
                                new ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName(), r);
                            }
                            catch (Exception ee) {
                                // tu nichts
                            }
                        }
                    }
                });
                butChangeDir.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
                butChangeDir.setText("Change Directory ");

                table = new Table(schedulerGroup, SWT.FULL_SELECTION | SWT.BORDER);
                table.setSortDirection(SWT.DOWN);
                table.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(final SelectionEvent e) {
                        try {
                            if (table.getSelectionCount() > 0) {
                                TableItem item = table.getSelection()[0];

                                if (item.getData("type").equals("file") || type.equalsIgnoreCase(OPEN_HOT_FOLDER) || type.equalsIgnoreCase(SAVE_AS_HOT_FOLDER))
                                    txtFilename.setText(item.getText(0));
                                else
                                    txtFilename.setText("");
                            }

                            butOpenOrSave.setEnabled(listener.getCurrProfile().isLoggedIn() && txtFilename.getText().length() > 0);

                        }
                        catch (Exception ex) {
                            System.err.println(ex.toString());
                        }
                    }
                });

                table.addMouseListener(new MouseAdapter() {
                    public void mouseDoubleClick(final MouseEvent e) {
                        try {
                            if (table.getSelectionCount() > 0) {
                                TableItem item = table.getSelection()[0];
                                if (item.getData("type").equals("dir")) {

                                    txtDir.setText((txtDir.getText().endsWith("/") ? txtDir.getText() : txtDir.getText() + "/") + item.getText());
                                    fillTable(listener.getCurrProfile().changeDirectory(txtDir.getText()));
                                }
                                else if (item.getData("type").equals("dir_up")) {
                                    String parentPath = new java.io.File(txtDir.getText()).getParent();
                                    if (parentPath != null)
                                        txtDir.setText(parentPath.replaceAll("\\\\", "/"));
                                    else
                                        txtDir.setText(".");
                                    // test 1 fillTable(listener.cdUP());
                                    fillTable(listener.getCurrProfile().cdUP());

                                }
                                else if (item.getData("type").equals("file")) {
                                    openOrSave();
                                }
                                txtFilename.setText("");
                            }
                        }
                        catch (Exception r) {
                            try {
                                new ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName(), r);
                            }
                            catch (Exception ee) {
                                // tu nichts
                            }
                        }
                    }
                });
                table.setHeaderVisible(true);
                table.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 3));

                newColumnTableColumn_2 = new TableColumn(table, SWT.NONE);
                newColumnTableColumn_2.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(final SelectionEvent e) {
                        sort(newColumnTableColumn_2);
                    }
                });
                table.setSortColumn(newColumnTableColumn_2);
                newColumnTableColumn_2.setMoveable(true);
                newColumnTableColumn_2.setWidth(176);
                newColumnTableColumn_2.setText("Name");

                final TableColumn newColumnTableColumn = new TableColumn(table, SWT.NONE);
                newColumnTableColumn.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(final SelectionEvent e) {

                        sort(newColumnTableColumn);

                    }
                });
                newColumnTableColumn.setWidth(117);
                newColumnTableColumn.setText("Size");

                newColumnTableColumn_1 = new TableColumn(table, SWT.NONE);
                newColumnTableColumn_1.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(final SelectionEvent e) {
                        sort(newColumnTableColumn_1);
                    }
                });
                newColumnTableColumn_1.setWidth(100);
                newColumnTableColumn_1.setText("Type");

                butRefresh = new Button(schedulerGroup, SWT.NONE);
                butRefresh.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(final SelectionEvent e) {

                        refresh();
                    }
                });
                butRefresh.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false));
                butRefresh.setText("Refresh");

                butNewFolder = new Button(schedulerGroup, SWT.NONE);
                butNewFolder.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(final SelectionEvent e) {
                        openDialog();
                    }
                });
                butNewFolder.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
                butNewFolder.setText("New Folder");

                butRemove = new Button(schedulerGroup, SWT.NONE);
                butRemove.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(final SelectionEvent e) {
                        if (txtFilename.getText() != null) {
                            Utils.startCursor(schedulerConfigurationShell);
                            try {
                                FTPProfile profile = listener.getCurrProfile();
                                profile.removeFile(txtFilename.getText());
                                HashMap h = profile.changeDirectory(txtDir.getText());
                                fillTable(h);
                            }
                            catch (Exception r) {
                                try {
                                    new ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName(), r);
                                }
                                catch (Exception ee) {
                                    // tu nichts
                                }
                            }
                            Utils.stopCursor(schedulerConfigurationShell);
                        }
                    }
                });
                butRemove.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false));
                butRemove.setText("Remove");

                final Label filenameLabel = new Label(schedulerGroup, SWT.NONE);
                if (type.equalsIgnoreCase(OPEN_HOT_FOLDER) || type.equalsIgnoreCase(OPEN_HOT_FOLDER)) {
                    filenameLabel.setText("Folder");
                }
                else {
                    filenameLabel.setText("Filename");
                }

                txtFilename = new Text(schedulerGroup, SWT.BORDER);
                txtFilename.addModifyListener(new ModifyListener() {
                    public void modifyText(final ModifyEvent e) {
                        if (listener == null)
                            listener = ftpProfilePicker.getListener();
                        butOpenOrSave.setEnabled(listener.getCurrProfile().isLoggedIn() && txtFilename.getText().length() > 0);
                    }
                });

                txtFilename.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

                {
                    butOpenOrSave = new Button(schedulerGroup, SWT.NONE);
                    butOpenOrSave.setEnabled(false);
                    butOpenOrSave.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
                    butOpenOrSave.addSelectionListener(new SelectionAdapter() {
                        public void widgetSelected(final SelectionEvent e) {
                            openOrSave();
                        }
                    });
                    butOpenOrSave.setFont(SWTResourceManager.getFont("", 8, SWT.BOLD));
                    butOpenOrSave.setText(type);
                }
                new Label(schedulerGroup, SWT.NONE);
                new Label(schedulerGroup, SWT.NONE);

                butClose = new Button(schedulerGroup, SWT.NONE);
                butClose.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(final SelectionEvent e) {
                        try {
                            listener.getCurrProfile().disconnect();
                            schedulerConfigurationShell.dispose();
                        }
                        catch (Exception r) {
                            try {
                                new ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName(), r);
                            }
                            catch (Exception ee) {
                                // tu nichts
                            }
                        }
                    }
                });
                butClose.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
                butClose.setText("Close");

            }

            txtLog = new Text(schedulerConfigurationShell, SWT.NONE);
            txtLog.setEditable(false);
            txtLog.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
            ftpProfilePicker.setLogText(txtLog);

            final Button butLog = new Button(schedulerConfigurationShell, SWT.NONE);
            butLog.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(final SelectionEvent e) {

                    String text = sos.scheduler.editor.app.Utils.showClipboard(txtLog.getText(), schedulerConfigurationShell, false, null, false, null, false);

                    if (text != null)
                        txtLog.setText(text);
                }
            });
            butLog.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
            butLog.setText("Log");

            /*
             * String selectProfile = Options.getProperty("last_profile");
             * if(selectProfile != null && selectProfile.length() > 0) {
             * if(listener == null) listener = ftpProfilePicker.getListener();
             * if( listener.getProfiles().get(selectProfile) != null) {
             * listener.setCurrProfileName(selectProfile); if(txtDir != null) {
             * txtDir.setText(listener.getCurrProfile() != null &&
             * listener.getCurrProfile().getRoot() != null ?
             * listener.getCurrProfile().getRoot() : ""); _setEnabled(false); }
             * } }
             */
            initForm();

            schedulerConfigurationShell.layout();
            schedulerConfigurationShell.open();
        }
        catch (Exception e) {
            try {
                new ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName(), e);
            }
            catch (Exception ee) {
                // tu nichts
            }
            MainWindow.message("could not int FTP Profiles:" + e.getMessage(), SWT.ICON_WARNING);
        }
    }

    private void initForm() {
        try {

            setToolTipText();
            if (listener == null) {
                ftpProfilePicker.getProfileByName(ftpProfilePicker.getSelectedProfilename());
                listener = ftpProfilePicker.getListener();
            }

            listener.setRemoteDirectory(txtDir);
            txtDir.setText(listener.getCurrProfile() != null ? listener.getCurrProfile().getRoot() : "");
            _setEnabled(false);

        }
        catch (Exception e) {
            try {
                new ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName(), e);
            }
            catch (Exception ee) {
                // tu nichts
            }
            MainWindow.message("could not int FTP Profiles:" + e.getMessage(), SWT.ICON_WARNING);
        }
    }

    private void fillTable(HashMap h) {
        try {
            table.removeAll();
            java.util.Iterator it = h.keySet().iterator();
            ArrayList files = new ArrayList();

            TableItem item_ = new TableItem(table, SWT.NONE);
            item_.setData("type", "dir_up");
            item_.setImage(ResourceManager.getImageFromResource("/sos/scheduler/editor/icon_directory_up.gif"));

            // directories
            while (it.hasNext()) {
                String key = sosString.parseToString(it.next());
                if (h.get(key).equals("dir")) {
                    TableItem item = new TableItem(table, SWT.NONE);
                    item.setText(0, key);
                    item.setText(1, "");
                    item.setText(2, "Folder");
                    item.setData("type", "dir");
                    item.setImage(ResourceManager.getImageFromResource("/sos/scheduler/editor/icon_directory.gif"));

                }
                else {
                    if (!key.endsWith("_size"))
                        files.add(key);
                }
            }

            // files
            if (!type.equalsIgnoreCase(OPEN_HOT_FOLDER)) {
                for (int i = 0; i < files.size(); i++) {
                    String filename = sosString.parseToString(files.get(i));
                    TableItem item = new TableItem(table, SWT.NONE);
                    item.setText(0, filename);
                    item.setText(1, sosString.parseToString(h.get(filename + "_size")));
                    item.setText(2, "File");
                    item.setData("type", "file");
                    item.setImage(ResourceManager.getImageFromResource("/sos/scheduler/editor/icon_file.gif"));

                }
            }

        }
        catch (Exception e) {

            try {
                new ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName(), e);
            }
            catch (Exception ee) {
                // tu nichts
            }
            System.out.println("..error in FTPDialog " + e.getMessage());
        }

    }

    public void saveas(String file) {
        try {
            file = file.replaceAll("\\\\", "/");
            String localfilename = MainWindow.getContainer().getCurrentEditor().getFilename();
            String newFilename = "";
            if (localfilename != null)
                newFilename = new File(localfilename).getParent() + "/" + new File(file).getName();
            else
                newFilename = new File(sosString.parseToString(listener.getCurrProfile().getLocaldirectory()), new File(file).getName()).getCanonicalPath();

            DomParser currdom = MainWindow.getSpecifiedDom();
            if (currdom == null)
                return;

            if (currdom instanceof SchedulerDom && ((SchedulerDom) currdom).isLifeElement()) {
                File f = new File(newFilename);
                if (f.isFile())
                    newFilename = f.getParent();

                localfilename = newFilename;

                currdom.setFilename(new java.io.File(newFilename).getParent());
                String attrName = f.getName().substring(0, f.getName().indexOf("." + currdom.getRoot().getName()));

                if (currdom.getRoot().getName().equals("order")) {
                    Utils.setAttribute("job_chain", attrName.substring(0, attrName.indexOf(",")), currdom.getRoot());
                    Utils.setAttribute("id", attrName.substring(attrName.indexOf(",") + 1), currdom.getRoot());
                }
                else {
                    Utils.setAttribute("name", attrName, currdom.getRoot());
                }
                if (MainWindow.getContainer().getCurrentEditor().save()) {
                    MainWindow.getContainer().getCurrentTab().setData("ftp_profile_name", listener.getCurrProfileName());
                    MainWindow.getContainer().getCurrentTab().setData("ftp_profile", listener.getCurrProfile());
                    MainWindow.getContainer().getCurrentTab().setData("ftp_title", "[FTP::" + listener.getCurrProfileName() + "]");
                    MainWindow.getContainer().getCurrentTab().setData("ftp_remote_directory", txtDir.getText() + "/" + txtFilename.getText());
                    MainWindow.setSaveStatus();

                }

                currdom.setFilename(new java.io.File(newFilename).getCanonicalPath());

                sos.scheduler.editor.app.IContainer con = MainWindow.getContainer();
                SchedulerForm sf = (SchedulerForm) (con.getCurrentEditor());
                sf.updateTree("jobs");
                String name = currdom.getRoot().getName();
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                sf.updateTreeItem(name + ": " + attrName);

            }
            else if (currdom instanceof SchedulerDom && ((SchedulerDom) currdom).isDirectory()) {
                if (MainWindow.getContainer().getCurrentEditor().save()) {
                    ArrayList newlist = listener.getCurrProfile().saveHotFolderAs(localfilename, file);

                    MainWindow.getContainer().getCurrentTab().setData("ftp_hot_folder_elements", newlist);

                    MainWindow.getContainer().getCurrentTab().setData("ftp_profile_name", listener.getCurrProfileName());
                    MainWindow.getContainer().getCurrentTab().setData("ftp_profile", listener.getCurrProfile());
                    MainWindow.getContainer().getCurrentTab().setData("ftp_title", "[FTP::" + listener.getCurrProfileName() + "]");
                    MainWindow.getContainer().getCurrentTab().setData("ftp_remote_directory", txtDir.getText() + "/" + txtFilename.getText());
                    MainWindow.setSaveStatus();
                }
                return;

            }
            else {
                currdom.setFilename(newFilename);
                if (MainWindow.getContainer().getCurrentEditor().save()) {
                    MainWindow.getContainer().getCurrentTab().setData("ftp_profile_name", listener.getCurrProfileName());
                    MainWindow.getContainer().getCurrentTab().setData("ftp_profile", listener.getCurrProfile());
                    MainWindow.getContainer().getCurrentTab().setData("ftp_title", "[FTP::" + listener.getCurrProfileName() + "]");
                    MainWindow.getContainer().getCurrentTab().setData("ftp_remote_directory", txtDir.getText() + "/" + txtFilename.getText());
                    MainWindow.setSaveStatus();
                }
            }
            listener.getCurrProfile().saveAs(localfilename, file);

        }
        catch (Exception e) {
            try {
                new ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName() + " ; could not save File", e);
            }
            catch (Exception ee) {
                // tu nichts
            }
            MainWindow.message("could not save File: cause: " + e.getMessage(), SWT.ICON_WARNING);
        }
        finally {
            try {
                listener.getCurrProfile().disconnect();
            }
            catch (Exception r) {
                try {
                    new ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName(), r);
                }
                catch (Exception ee) {
                    // tu nichts
                }
            }
            schedulerConfigurationShell.dispose();
        }
    }

    public void openHotFolder() {
        try {
            FTPProfile profile = listener.getCurrProfile();
            String strRootFolder = profile.getRoot();
            String strParentSelectedFolder = txtDir.getText();
            String strSubFolderRoot = strParentSelectedFolder.substring(strRootFolder.length()) + "/";
            String tempSubHotFolder = txtFilename.getText();
            HashMap h = profile.changeDirectory(strParentSelectedFolder + "/" + tempSubHotFolder);
            if (listener.hasError()) {
                return;
            }

            java.util.Iterator it = h.keySet().iterator();
            // Alle Hot Folder Dateinamen merken: Grund: Beim Speichern werden
            // alle Dateien gel�scht und anschliessend
            // neu zur�ckgeschrieben
            ArrayList nameOfLifeElement = new ArrayList();
            String targetfile = sosString.parseToString(listener.getCurrProfile().getLocaldirectory());
            targetfile = targetfile.replaceAll("\\\\", "/");
            targetfile = new File(targetfile, strSubFolderRoot + tempSubHotFolder).getCanonicalPath();
            targetfile = (targetfile.endsWith("/") || targetfile.endsWith("\\") ? targetfile : targetfile + "/");

            File f = new File(targetfile);
            ArrayList l = new ArrayList();
            if (f.exists() && f.list().length > 0) {
                String[] list = f.list();
                for (int i = 0; i < list.length; i++) {
                    if (list[i] != null
                            && (list[i].endsWith(".job.xml") || list[i].endsWith(".job_chain.xml") || list[i].endsWith(".order.xml") || list[i].endsWith(".lock.xml") || list[i].endsWith(".process_class.xml")
                                    || list[i].endsWith(".config.xml") || list[i].endsWith(".schedule.xml"))) {
                        l.add(list[i]);

                    }
                }
            }

            while (it.hasNext()) {
                String key = sosString.parseToString(it.next());
                if (l.contains(key)) {
                    l.remove(key);
                }
                if (h.get(key).equals("file")) {
                    if (isLifeElement(sosString.parseToString(key))) {
                        String file = profile.openFile(sosString.parseToString(key), strSubFolderRoot + tempSubHotFolder);
                        nameOfLifeElement.add(file.replaceAll("\\\\", "/"));
                    }
                    else if (key.endsWith(".config.xml")) {
                        profile.openFile(sosString.parseToString(key), strSubFolderRoot + tempSubHotFolder);
                    }
                }
            }

            String whichFile = "";
            if (l.size() >= 0) {
                for (int i = 0; i < l.size(); i++) {
                    whichFile = whichFile + l.get(i) + "; ";
                }
            }

            if (whichFile.length() > 0) {
                int c = MainWindow.message("The files in the local directory are not synchron with the files at the server.\nShould the files in the local directory be deleted?\n" + whichFile, SWT.ICON_QUESTION | SWT.YES | SWT.NO
                        | SWT.CANCEL);

                if (c == SWT.YES) {
                    for (int j = 0; j < l.size(); j++)
                        new File(targetfile + sosString.parseToString(l.get(j))).delete();
                }
            }

            String dirname = sosString.parseToString(listener.getCurrProfile().getLocaldirectory());
            dirname = new File(dirname, strSubFolderRoot + tempSubHotFolder).getCanonicalPath();
            if (!new File(dirname).exists()) {
                new File(dirname).mkdirs();
            }

            if (MainWindow.getContainer().openDirectory(dirname) != null) {
                MainWindow.getContainer().getCurrentTab().setData("ftp_profile_name", listener.getCurrProfileName());
                MainWindow.getContainer().getCurrentTab().setData("ftp_profile", listener.getCurrProfile());
                MainWindow.getContainer().getCurrentTab().setData("ftp_title", "[FTP::" + listener.getCurrProfileName() + "]");
                MainWindow.getContainer().getCurrentTab().setData("ftp_remote_directory", strParentSelectedFolder + "/" + tempSubHotFolder);
                MainWindow.getContainer().getCurrentTab().setData("ftp_hot_folder_elements", nameOfLifeElement);

                MainWindow.setSaveStatus();
            }

            profile.disconnect();
            schedulerConfigurationShell.dispose();
        }
        catch (Exception e) {
            try {
                new ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName() + " ; could not Open Hot Folder.", e);
            }
            catch (Exception ee) {
            }
            MainWindow.message("could not Open Hot Folder: cause: " + e.getMessage(), SWT.ICON_WARNING);
        }
    }

    /**
     * �ffnet das ausgew�hlte Datei.
     * 
     * 
     * Wenn eine
     */
    public void openFile() {
        String file = "";
        try {

            FTPProfile profile = listener.getCurrProfile();
            file = profile.openFile(txtDir.getText() + "/" + txtFilename.getText(), null);

            if (!listener.hasError()) {
                if (MainWindow.getContainer().openQuick(file) != null) {
                    MainWindow.getContainer().getCurrentTab().setData("ftp_profile_name", listener.getCurrProfileName());
                    MainWindow.getContainer().getCurrentTab().setData("ftp_profile", listener.getCurrProfile());
                    MainWindow.getContainer().getCurrentTab().setData("ftp_title", "[FTP::" + listener.getCurrProfileName() + "]");
                    MainWindow.getContainer().getCurrentTab().setData("ftp_remote_directory", txtDir.getText() + "/" + txtFilename.getText());
                    MainWindow.setSaveStatus();
                }

                if (new File(file).getName().endsWith(".job_chain.xml")) {

                    // Es wurde eine Jobkette ge�ffnet. Es werden automatisch,
                    // falls vorhanden die entsprechende Job Chain Node
                    // Parameter datei ge�ffnet
                    int endP = txtFilename.getText().length() - ".job_chain.xml".length();
                    // File detailsfile = new File(txtDir.getText() + "/" +
                    // txtFilename.getText().substring(0, endP) +
                    // ".config.xml");
                    // File detailsfile = new
                    // File(txtFilename.getText().substring(0, endP) +
                    // ".config.xml");
                    java.util.Vector ftpFiles = profile.getList();
                    // fehler wird ueber nlist return value verwertet
                    if (!ftpFiles.isEmpty()) {
                        profile.openFile(txtFilename.getText().substring(0, endP) + ".config.xml", null);
                    }
                }

                profile.disconnect();
                schedulerConfigurationShell.dispose();
            }
        }
        catch (Exception r) {
            try {
                MainWindow.message("could not open File: " + file + ", cause: " + r.toString(), SWT.ICON_WARNING);
                new ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName(), r);
            }
            catch (Exception ee) {
                // tu nichts
            }
        }
    }

    public FTPDialogListener getListener() {
        return listener;
    }

    public void refresh() {
        try {
            Utils.startCursor(schedulerConfigurationShell);
            HashMap h = listener.getCurrProfile().changeDirectory(txtDir.getText());
            fillTable(h);
            Utils.stopCursor(schedulerConfigurationShell);
        }
        catch (Exception r) {
            try {
                MainWindow.message("could not refersh Table, cause: " + r.toString(), SWT.ICON_WARNING);
                new ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName(), r);
            }
            catch (Exception ee) {
                // tu nichts
            }
        }

    }

    public void openDialog() {
        final Shell shell = new Shell();
        shell.pack();
        Dialog dialog = new Dialog(shell);
        dialog.setText("Create New Folder");
        dialog.open(this);
    }

    private void _setEnabled(boolean enabled) {
        txtDir.setEnabled(enabled);
        butChangeDir.setEnabled(enabled);
        butRefresh.setEnabled(enabled);
        butNewFolder.setEnabled(enabled);
        butRemove.setEnabled(enabled);
    }

    private void sort(TableColumn col) {
        try {

            if (table.getSortDirection() == SWT.DOWN)
                table.setSortDirection(SWT.UP);
            else
                table.setSortDirection(SWT.DOWN);

            table.setSortColumn(col);

            ArrayList listOfSortData = new ArrayList();

            for (int i = 0; i < table.getItemCount(); i++) {
                TableItem item = table.getItem(i);
                if (!item.getData("type").equals("dir_up")) {
                    HashMap hash = new HashMap();
                    for (int j = 0; j < table.getColumnCount(); j++) {
                        hash.put(table.getColumn(j).getText(), item.getText(j));
                    }

                    hash.put("type", item.getData("type"));

                    listOfSortData.add(hash);
                }
            }

            listOfSortData = sos.util.SOSSort.sortArrayList(listOfSortData, col.getText());

            table.removeAll();

            TableItem item_ = new TableItem(table, SWT.NONE);
            item_.setData("type", "dir_up");
            item_.setImage(ResourceManager.getImageFromResource("/sos/scheduler/editor/icon_directory_up.gif"));

            TableItem item = null;

            if (table.getSortDirection() == SWT.DOWN) {
                // Verzeichnis
                for (int i = 0; i < listOfSortData.size(); i++) {

                    HashMap hash = (HashMap) listOfSortData.get(i);
                    if (!hash.get("type").equals("file")) {

                        item = new TableItem(table, SWT.NONE);
                        item.setData("type", hash.get("type"));

                        if (hash.get("type").equals("dir"))
                            item.setImage(ResourceManager.getImageFromResource("/sos/scheduler/editor/icon_directory.gif"));
                        else if (hash.get("type").equals("dir_up"))
                            item.setImage(ResourceManager.getImageFromResource("/sos/scheduler/editor/icon_directory_up.gif"));

                        for (int j = 0; j < table.getColumnCount(); j++) {
                            item.setText(j, sosString.parseToString(hash.get(table.getColumn(j).getText())));
                        }
                    }
                }
                // Datei
                for (int i = 0; i < listOfSortData.size(); i++) {
                    HashMap hash = (HashMap) listOfSortData.get(i);
                    if (hash.get("type").equals("file")) {
                        item = new TableItem(table, SWT.NONE);
                        item.setData("type", hash.get("type"));
                        item.setImage(ResourceManager.getImageFromResource("/sos/scheduler/editor/icon_file.gif"));

                        for (int j = 0; j < table.getColumnCount(); j++) {
                            item.setText(j, sosString.parseToString(hash.get(table.getColumn(j).getText())));
                        }
                    }
                }

            }
            else {

                for (int i = listOfSortData.size() - 1; i >= 0; i--) {
                    HashMap hash = (HashMap) listOfSortData.get(i);

                    // Datei
                    if (hash.get("type").equals("file")) {
                        item = new TableItem(table, SWT.NONE);

                        item.setData("type", hash.get("type"));
                        if (hash.get("type").equals("file"))
                            item.setImage(ResourceManager.getImageFromResource("/sos/scheduler/editor/icon_file.gif"));

                        for (int j = 0; j < table.getColumnCount(); j++) {
                            item.setText(j, sosString.parseToString(hash.get(table.getColumn(j).getText())));
                        }
                    }
                }
                // Verzeichnis
                for (int i = listOfSortData.size() - 1; i >= 0; i--) {
                    HashMap hash = (HashMap) listOfSortData.get(i);

                    if (!hash.get("type").equals("file")) {
                        item = new TableItem(table, SWT.NONE);
                        item.setData("type", hash.get("type"));

                        if (hash.get("type").equals("dir"))
                            item.setImage(ResourceManager.getImageFromResource("/sos/scheduler/editor/icon_directory.gif"));
                        else if (hash.get("type").equals("dir_up"))
                            item.setImage(ResourceManager.getImageFromResource("/sos/scheduler/editor/icon_directory_up.gif"));

                        for (int j = 0; j < table.getColumnCount(); j++) {
                            item.setText(j, sosString.parseToString(hash.get(table.getColumn(j).getText())));
                        }
                    }
                }

            }

        }
        catch (Exception e) {
            try {
                new ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName(), e);
            }
            catch (Exception ee) {
                // tu nichts
            }

        }
    }

    /*
     * private void sort(TableColumn col) { try {
     * 
     * if(table.getSortDirection() == SWT.DOWN) table.setSortDirection(SWT.UP);
     * else table.setSortDirection(SWT.DOWN);
     * 
     * table.setSortColumn(col);
     * 
     * ArrayList listOfSortData = new ArrayList();
     * 
     * for(int i = 0; i < table.getItemCount(); i++) { TableItem item =
     * table.getItem(i); if(!item.getData("type").equals("dir_up")) { HashMap
     * hash = new HashMap(); for(int j = 0; j < table.getColumnCount(); j++) {
     * hash.put(table.getColumn(j).getText(), item.getText(j)); }
     * 
     * hash.put("type", item.getData("type"));
     * 
     * listOfSortData.add(hash); } }
     * 
     * listOfSortData = sos.util.SOSSort.sortArrayList(listOfSortData,
     * col.getText());
     * 
     * table.removeAll();
     * 
     * TableItem item_ = new TableItem(table, SWT.NONE);
     * item_.setData("type","dir_up");
     * item_.setImage(ResourceManager.getImageFromResource
     * ("/sos/scheduler/editor/icon_directory_up.gif"));
     * 
     * 
     * TableItem item = null;
     * 
     * if(table.getSortDirection() == SWT.DOWN) { for(int i = 0; i <
     * listOfSortData.size(); i++) {
     * 
     * item = new TableItem(table, SWT.NONE); HashMap hash =
     * (HashMap)listOfSortData.get(i); item.setData("type", hash.get("type"));
     * if(hash.get("type").equals("file"))
     * item.setImage(ResourceManager.getImageFromResource
     * ("/sos/scheduler/editor/icon_file.gif")); else
     * if(hash.get("type").equals("dir"))
     * item.setImage(ResourceManager.getImageFromResource
     * ("/sos/scheduler/editor/icon_directory.gif")); else
     * if(hash.get("type").equals("dir_up"))
     * item.setImage(ResourceManager.getImageFromResource
     * ("/sos/scheduler/editor/icon_directory_up.gif"));
     * 
     * for(int j = 0; j < table.getColumnCount(); j++) { item.setText(j,
     * sosString.parseToString(hash.get(table.getColumn(j).getText()))); }
     * 
     * }
     * 
     * } else {
     * 
     * for(int i = listOfSortData.size() - 1; i >= 0; i--) {
     * 
     * item = new TableItem(table, SWT.NONE); HashMap hash =
     * (HashMap)listOfSortData.get(i); item.setData("type", hash.get("type"));
     * if(hash.get("type").equals("file"))
     * item.setImage(ResourceManager.getImageFromResource
     * ("/sos/scheduler/editor/icon_file.gif")); else
     * if(hash.get("type").equals("dir"))
     * item.setImage(ResourceManager.getImageFromResource
     * ("/sos/scheduler/editor/icon_directory.gif")); else
     * if(hash.get("type").equals("dir_up"))
     * item.setImage(ResourceManager.getImageFromResource
     * ("/sos/scheduler/editor/icon_directory_up.gif"));
     * 
     * for(int j = 0; j < table.getColumnCount(); j++) { item.setText(j,
     * sosString.parseToString(hash.get(table.getColumn(j).getText()))); }
     * 
     * }
     * 
     * 
     * }
     * 
     * } catch(Exception e) { try { new ErrorLog("error in " +
     * sos.util.SOSClassUtil.getMethodName(), e); } catch(Exception ee) { //tu
     * nichts }
     * 
     * } }
     */
    public void setToolTipText() {
        if (type.equalsIgnoreCase(OPEN_HOT_FOLDER)) {
            butOpenOrSave.setToolTipText(Messages.getTooltip("ftpdialog.btn_open_hot_folder"));
            txtFilename.setToolTipText(Messages.getTooltip("ftpdialog.txt_open_hot_folder"));
        }
        else if (type.equalsIgnoreCase(OPEN)) {
            butOpenOrSave.setToolTipText(Messages.getTooltip("ftpdialog.btn_open_file"));
            txtFilename.setToolTipText(Messages.getTooltip("ftpdialog.txt_open_file"));
        }
        else if (type.equalsIgnoreCase(SAVE_AS) || type.equalsIgnoreCase(SAVE_AS_HOT_FOLDER)) {
            butOpenOrSave.setToolTipText(Messages.getTooltip("ftpdialog.btn_save_as"));
            txtFilename.setToolTipText(Messages.getTooltip("ftpdialog.txt_save_as"));
        }

        table.setToolTipText(Messages.getTooltip("ftpdialog.table"));
        txtDir.setToolTipText(Messages.getTooltip("ftpdialog.directory"));

        txtLog.setToolTipText(Messages.getTooltip("ftpdialog.log"));
        butChangeDir.setToolTipText(Messages.getTooltip("ftpdialog.change_directory"));
        butRefresh.setToolTipText(Messages.getTooltip("ftpdialog.refresh"));
        butNewFolder.setToolTipText(Messages.getTooltip("ftpdialog.new_folder"));
        butRemove.setToolTipText(Messages.getTooltip("ftpdialog.remove"));
        butSite.setToolTipText(Messages.getTooltip("ftpdialog.connect"));
        butClose.setToolTipText(Messages.getTooltip("ftpdialog.close"));
    }

    private boolean isLifeElement(String filename) {

        if (filename.endsWith(".job.xml") || filename.endsWith(".schedule.xml") || filename.endsWith(".job_chain.xml") || filename.endsWith(".lock.xml") || filename.endsWith(".process_class.xml") || filename.endsWith(".order.xml")) {
            return true;
        }
        else {

            return false;
        }
    }

    private void openOrSave() {
        Utils.startCursor(schedulerConfigurationShell);
        if (butOpenOrSave.getText().equals(OPEN) || butOpenOrSave.getText().equals(OPEN_HOT_FOLDER)) {
            if (type.equals(OPEN_HOT_FOLDER)) {
                openHotFolder();

            }
            else {
                // Konfiguratoionsdatei oder HOT Folder Element
                openFile();
            }
        }
        else {
            String file = txtDir.getText() + "/" + txtFilename.getText();
            saveas(file);
        }

        Utils.stopCursor(schedulerConfigurationShell);
    }

}
