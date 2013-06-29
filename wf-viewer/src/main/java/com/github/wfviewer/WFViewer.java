/*
 * $Id$
 *
 * Copyright 2013 Valentyn Kolesnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.wfviewer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import com.github.wfviewer.model.Action;
import com.github.wfviewer.model.Actions;
import com.github.wfviewer.model.Case;
import com.github.wfviewer.model.Description;
import com.github.wfviewer.model.DocumentCase;
import com.github.wfviewer.model.ExtendView;
import com.github.wfviewer.model.FieldDef;
import com.github.wfviewer.model.FieldMapping;
import com.github.wfviewer.model.FieldSet;
import com.github.wfviewer.model.FieldViewAccess;
import com.github.wfviewer.model.FormView;
import com.github.wfviewer.model.IccConfig;
import com.github.wfviewer.model.OnPage;
import com.github.wfviewer.model.Property;
import com.github.wfviewer.model.UIROComponent;
import com.github.wfviewer.model.UIRWComponent;

public class WFViewer extends javax.swing.JFrame {

    private IccConfig config = null;
    private File file;
    private Stack<TreePath> history = new Stack<TreePath>();
    private JDialog searchDialog;
    private JFileChooser chooser;

    /** Creates new form */
    public WFViewer() {
        initComponents();
        config = extractXml(new File("WFViewer-1.0.xml"));
        if (config != null) {
            initTree();
        }
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent winEvt) {
                createXml(config, new File("WFViewer-1.0.xml"));
                System.exit(0);
            }
        });
        final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        final int x = (screenSize.width - getWidth()) / 2;
        final int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
    }

    private void createXml(IccConfig localConfig, File localFile) throws UnsupportedOperationException {
        JAXBContext jContext;
        try {
            jContext = JAXBContext.newInstance("com.github.wfviewer.model");
        } catch (JAXBException e) {
            throw new UnsupportedOperationException("Can't create JAXB", e);
        }
        Marshaller marshaller;
        try {
            marshaller = jContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(localConfig, localFile);
        } catch (Exception e) {
            Logger.getLogger(WFViewer.class.getName()).warning("Can't create JAXB Marshaller " + e.getMessage());
        }
    }

    private IccConfig extractXml(File xml) {
        JAXBContext jContext;
        try {
            jContext = JAXBContext.newInstance("com.github.wfviewer.model");
        } catch (JAXBException e) {
            throw new UnsupportedOperationException("Can't create JAXB", e);
        }
        Unmarshaller unmarshaller;
        config = null;
        try {
            unmarshaller = jContext.createUnmarshaller();
            config = (IccConfig) unmarshaller.unmarshal(xml);
        } catch (JAXBException e) {
            Logger.getLogger(WFViewer.class.getName()).warning("Can't create JAXB Unmarshaller, " + xml);
        }
        return config;
    }

    private void fillAction(Actions actions, DefaultTreeModel model, DefaultMutableTreeNode actionsNode) {
        for (Action action : actions.getAction()) {
            DefaultMutableTreeNode actionNode = new DefaultMutableTreeNode(action);
            model.insertNodeInto(actionNode, actionsNode, actionsNode.getChildCount());
            fillOnPage(action, model, actionNode);
        }
    }

    private void fillActions(DefaultTreeModel model, DefaultMutableTreeNode treeNode) {
        for (Actions actions : config.getActions()) {
            DefaultMutableTreeNode actionsNode = new DefaultMutableTreeNode(actions);
            model.insertNodeInto(actionsNode, treeNode, treeNode.getChildCount());
            fillAction(actions, model, actionsNode);
        }
    }

    private void fillCase(OnPage onPage, DefaultTreeModel model, DefaultMutableTreeNode onPageNode) {
        for (Case localCase : onPage.getCase()) {
            DefaultMutableTreeNode caseNode = new DefaultMutableTreeNode(localCase);
            model.insertNodeInto(caseNode, onPageNode, onPageNode.getChildCount());
        }
    }

    private void fillFormView(FormView formView, DefaultTreeModel model, DefaultMutableTreeNode formViewNode) {
        for (Object formViewItem : formView.getContent()) {
            if (formViewItem instanceof ExtendView) {
                ExtendView extendView = (ExtendView) formViewItem;
                DefaultMutableTreeNode extendViewNode = new DefaultMutableTreeNode(extendView);
                model.insertNodeInto(extendViewNode, formViewNode, formViewNode.getChildCount());
            } else if (formViewItem instanceof FieldViewAccess) {
                FieldViewAccess fieldViewAccess = (FieldViewAccess) formViewItem;
                DefaultMutableTreeNode fieldViewAccessNode = new DefaultMutableTreeNode(fieldViewAccess);
                model.insertNodeInto(fieldViewAccessNode, formViewNode, formViewNode.getChildCount());
            } else if (formViewItem instanceof DocumentCase) {
                DocumentCase documentCase = (DocumentCase) formViewItem;
                DefaultMutableTreeNode documentCaseNode = new DefaultMutableTreeNode(documentCase);
                model.insertNodeInto(documentCaseNode, formViewNode, formViewNode.getChildCount());
            }
        }
    }

    private void fillJTreeModel(DefaultTreeModel model, DefaultMutableTreeNode docFieldsDefNode) {
        for (FieldDef fieldDef : config.getDocFieldsDef().getFieldDef()) {
            DefaultMutableTreeNode fieldDefNode = new DefaultMutableTreeNode(fieldDef);
            model.insertNodeInto(fieldDefNode, docFieldsDefNode, docFieldsDefNode.getChildCount());
            if (fieldDef.getDescription() != null) {
                DefaultMutableTreeNode descriptionNode = new DefaultMutableTreeNode(fieldDef.getDescription());
                model.insertNodeInto(descriptionNode, fieldDefNode, fieldDefNode.getChildCount());
            }
            if (fieldDef.getFieldUIComponents() != null) {
                DefaultMutableTreeNode fieldUIComponentsNode = new DefaultMutableTreeNode(fieldDef.getFieldUIComponents());
                model.insertNodeInto(fieldUIComponentsNode, fieldDefNode, fieldDefNode.getChildCount());
                if (fieldDef.getFieldUIComponents().getUIRWComponent() != null) {
                    DefaultMutableTreeNode uiRwComponentsNode = new DefaultMutableTreeNode(
                        fieldDef.getFieldUIComponents().getUIRWComponent());
                    model.insertNodeInto(uiRwComponentsNode, fieldUIComponentsNode, fieldUIComponentsNode.getChildCount());
                }
                if (fieldDef.getFieldUIComponents().getUIROComponent() != null) {
                    DefaultMutableTreeNode uiRoComponentsNode = new DefaultMutableTreeNode(
                        fieldDef.getFieldUIComponents().getUIROComponent());
                    model.insertNodeInto(uiRoComponentsNode, fieldUIComponentsNode, fieldUIComponentsNode.getChildCount());
                }
            }
        }
    }

    private void fillOnPage(Action action, DefaultTreeModel model, DefaultMutableTreeNode actionNode) {
        for (OnPage onPage : action.getOnPage()) {
            DefaultMutableTreeNode onPageNode = new DefaultMutableTreeNode(onPage);
            model.insertNodeInto(onPageNode, actionNode, actionNode.getChildCount());
            fillCase(onPage, model, onPageNode);
        }
    }

    private void fillTableFieldDef(Object userObject, DefaultTableModel tableModel) {
        FieldDef fieldDef = (FieldDef) userObject;
        tableModel.addRow(new String[]{"Description", fieldDef.getDescription().getContent()});
        for (Property property : fieldDef.getFieldUIComponents().getUIROComponent().getProperty()) {
            tableModel.addRow(new String[]{"FieldUIComponents->UI_RO_Component->key", property.getContent()});
        }
        if (fieldDef.getFieldUIComponents().getUIRWComponent() != null) {
            for (Property property : fieldDef.getFieldUIComponents().getUIRWComponent().getProperty()) {
                tableModel.addRow(new String[]{"FieldUIComponents->UI_RW_Component->key", property.getContent()});
            }
        }
        FieldMapping fieldMapping = fieldDef.getFieldMapping();
        if (fieldMapping.getFieldName() != null) {
            tableModel.addRow(new String[]{"FieldMapping->FieldName", fieldMapping.getFieldName().getContent()});
        }
        if (fieldMapping.getDBFieldType() != null) {
            tableModel.addRow(new String[]{"FieldMapping->DB_FieldType", fieldMapping.getDBFieldType().getContent()});
        }
        if (fieldMapping.getESFieldName() != null) {
            tableModel.addRow(new String[]{"FieldMapping->ES_FieldName", fieldMapping.getESFieldName().getContent()});
        }
        if (fieldMapping.getESOffset() != null) {
            tableModel.addRow(new String[]{"FieldMapping->ES_Offset", fieldMapping.getESOffset().getContent()});
        }
        if (fieldMapping.getESSize() != null) {
            tableModel.addRow(new String[]{"FieldMapping->ES_Size", fieldMapping.getESSize().getContent()});
        }
        if (fieldMapping.getESUsage() != null) {
            tableModel.addRow(new String[]{"FieldMapping->ES_Usage", fieldMapping.getESUsage().getContent()});
        }
        if (fieldMapping.getESDefaultValue() != null) {
            tableModel.addRow(new String[]{"FieldMapping->ES_DefaultValue", fieldMapping.getESDefaultValue().getContent()});
        }
        jLabel1.setText("FieldSet " + fieldDef.getName());
    }

    private void fillUI(DefaultTreeModel model, DefaultMutableTreeNode uiNode) {
        for (Object uiItem : config.getUI().getContent()) {
            if (uiItem instanceof FormView) {
                FormView formView = (FormView) uiItem;
                DefaultMutableTreeNode formViewNode = new DefaultMutableTreeNode(formView);
                model.insertNodeInto(formViewNode, uiNode, uiNode.getChildCount());
                fillFormView(formView, model, formViewNode);
            } else if (uiItem instanceof FieldSet) {
                FieldSet fieldSet = (FieldSet) uiItem;
                DefaultMutableTreeNode fieldSetNode = new DefaultMutableTreeNode(fieldSet);
                model.insertNodeInto(fieldSetNode, uiNode, uiNode.getChildCount());
            }
        }
    }

    private boolean findLevel2(DefaultMutableTreeNode node, String toObject) {
        for (int indexNode = 0; indexNode < node.getChildCount(); indexNode += 1) {
            DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) node.getChildAt(indexNode);
            if (findLevel3(node2, toObject, node)) {
                return true;
            }
        }
        return false;
    }

    // Find in formView
    private boolean findLevel22(DefaultMutableTreeNode node, String toObject) {
        for (int indexNode = 0; indexNode < node.getChildCount(); indexNode += 1) {
            DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) node.getChildAt(indexNode);
            if (((DefaultMutableTreeNode) node.getChildAt(indexNode)).getUserObject() instanceof FormView) {
                FormView formView = (FormView) node2.getUserObject();
                if (formView.getName().equals(toObject)) {
                    TreePath treePath = find(jTree1, new Object[]{jTree1.getModel().getRoot(),
                        node, node2});
                    history.push(jTree1.getSelectionPath());
                    jTree1.setSelectionPath(treePath);
                    jTree1.scrollPathToVisible(treePath);
                    return true;
                }
            }
        }
        return false;
    }

    // Find in fieldDef
    private boolean findLevel23(DefaultMutableTreeNode node, String toObject) {
        for (int indexNode = 0; indexNode < node.getChildCount(); indexNode += 1) {
            DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) node.getChildAt(indexNode);
            if (((DefaultMutableTreeNode) node.getChildAt(indexNode)).getUserObject() instanceof FieldDef) {
                FieldDef fieldDef = (FieldDef) node2.getUserObject();
                if (fieldDef.getName().equals(toObject)) {
                    TreePath treePath = find(jTree1, new Object[]{jTree1.getModel().getRoot(),
                        node, node2});
                    history.push(jTree1.getSelectionPath());
                    jTree1.setSelectionPath(treePath);
                    jTree1.scrollPathToVisible(treePath);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean findLevel3(DefaultMutableTreeNode node2, String toObject, DefaultMutableTreeNode node) {
        for (int indexNode2 = 0; indexNode2 < node2.getChildCount(); indexNode2 += 1) {
            if (((DefaultMutableTreeNode) node2.getChildAt(indexNode2)).getUserObject() instanceof OnPage) {
                OnPage onPage = (OnPage) ((DefaultMutableTreeNode) node2.getChildAt(indexNode2)).getUserObject();
                if (onPage.getId().equals(toObject)) {
                    TreePath treePath = find(jTree1, new Object[]{jTree1.getModel().getRoot(), node, node2, node2.getChildAt(indexNode2)});
                    history.push(jTree1.getSelectionPath());
                    jTree1.setSelectionPath(treePath);
                    jTree1.scrollPathToVisible(treePath);
                    return true;
                }
            }
        }
        return false;
    }

    private void findRelativeNode(String userObject) {
        String toObject = userObject;
        out:
        for (int index = 0; index < ((DefaultTreeModel) jTree1.getModel()).getChildCount(jTree1.getModel().getRoot()); index += 1) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) ((DefaultTreeModel) jTree1.getModel()).getChild(jTree1.getModel().getRoot(), index);
            if (findLevel2(node, toObject)) {
                break out;
            }
        }
    }

    private void findRelativeNode2(String userObject) {
        String toObject = userObject;
        out:
        for (int index = 0; index < ((DefaultTreeModel) jTree1.getModel()).getChildCount(jTree1.getModel().getRoot()); index += 1) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) ((DefaultTreeModel) jTree1.getModel()).getChild(jTree1.getModel().getRoot(), index);
            if (findLevel22(node, toObject)) {
                break out;
            }
        }
    }

    private void findRelativeNode3(String userObject) {
        String toObject = userObject;
        out:
        for (int index = 0; index < ((DefaultTreeModel) jTree1.getModel()).getChildCount(jTree1.getModel().getRoot()); index += 1) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) ((DefaultTreeModel) jTree1.getModel()).getChild(jTree1.getModel().getRoot(), index);
            if (findLevel23(node, toObject)) {
                break out;
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton4 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Workflow Config Viewer");

        jSplitPane1.setDividerLocation(350);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Components "));

        jButton3.setText("Select file");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jTree1.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jTree1.setRootVisible(false);
        jScrollPane1.setViewportView(jTree1);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jButton3))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jButton3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel1);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Properties"));
        jPanel2.setMaximumSize(new java.awt.Dimension(610, 444));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Key", "Value"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTable1FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTable1FocusLost(evt);
            }
        });
        jScrollPane2.setViewportView(jTable1);

        jButton4.setText("OK");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel1.setText(" ");

        jButton5.setText("Go to ref item");
        jButton5.setEnabled(false);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton7.setText("Go to FormView");
        jButton7.setEnabled(false);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setText("Search");
        jButton8.setEnabled(false);
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton6.setText("Back");
        jButton6.setEnabled(false);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane2)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2Layout.createSequentialGroup()
                        .addContainerGap(97, Short.MAX_VALUE)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jPanel2Layout.createSequentialGroup()
                                .add(jButton8)
                                .add(6, 6, 6)
                                .add(jButton7)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButton5)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButton6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jButton4)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton6)
                    .add(jButton5)
                    .add(jButton7)
                    .add(jButton8))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton4)
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jPanel2);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 836, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(6, 6, 6)
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private class MyRenderer extends DefaultTreeCellRenderer {

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected,
                boolean isExpanded, boolean isLeaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, isSelected, isExpanded, isLeaf, row, hasFocus);
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            setText(getNodeText(userObject));
            return this;
        }

        public String getNodeText(Object userObject) {
            String text;
            if (userObject instanceof Actions) {
                text = "Actions (" + ((Actions) userObject).getId() + ") -> " + ((Actions) userObject).getStartView();
            } else if (userObject instanceof Action) {
                text = "Action (" + ((Action) userObject).getId() + ")";
            } else if (userObject instanceof OnPage) {
                text = "onPage (" + ((OnPage) userObject).getId() + ")" + (((OnPage) userObject).getDefaultTargetPage() != null ? (" -> " + ((OnPage) userObject).getDefaultTargetPage()) : "");
            } else if (userObject instanceof Case) {
                text = ((Case) userObject).getValue() + " (" + ((Case) userObject).getDescription() + ") -> " + ((Case) userObject).getTargetPage().getContent();
            } else if (userObject instanceof FormView) {
                text = "FormView (" + ((FormView) userObject).getName() + ")";
            } else if (userObject instanceof FieldViewAccess) {
                text = ((FieldViewAccess) userObject).getRef() + " (" + ((FieldViewAccess) userObject).getContent() + ")";
            } else if (userObject instanceof FieldDef) {
                text = "FieldDef (" + ((FieldDef) userObject).getName() + ")";
            } else if (userObject instanceof Description) {
                text = "Description (" + ((Description) userObject).getContent() + ")";
            } else if (userObject instanceof UIROComponent) {
                text = "UIROComponent (" + ((UIROComponent) userObject).getProperty().get(0).getKey() + " -> " + ((UIROComponent) userObject).getProperty().get(0).getContent();
            } else if (userObject instanceof UIRWComponent) {
                text = "UIRWComponent (" + ((UIRWComponent) userObject).getProperty().get(0).getKey() + " -> " + ((UIRWComponent) userObject).getProperty().get(0).getContent();
            } else if (userObject instanceof FieldSet) {
                text = "FieldSet (" + ((FieldSet) userObject).getName() + ")";
            } else if (userObject instanceof String) {
                text = userObject.toString();
            } else {
                text = userObject.getClass().getName().replaceFirst(".*\\.(\\w+)", "$1");
            }
            return text;
        }
    }

    private void initTree() {
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode("root");
        DefaultTreeModel model = new DefaultTreeModel(treeNode);
        fillActions(model, treeNode);
        DefaultMutableTreeNode uiNode = new DefaultMutableTreeNode("UI");
        model.insertNodeInto(uiNode, treeNode, treeNode.getChildCount());
        fillUI(model, uiNode);
        DefaultMutableTreeNode docFieldsDefNode = new DefaultMutableTreeNode("DocFieldsDef");
        model.insertNodeInto(docFieldsDefNode, treeNode, treeNode.getChildCount());
        fillJTreeModel(model, docFieldsDefNode);
        jTree1.setModel(model);
        jTree1.setCellRenderer(new MyRenderer());
        jTree1.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
                doSelected(node);
            }
        });
        jButton6.setEnabled(true);
        jButton8.setEnabled(true);
    }

    class EmptyTableModel extends DefaultTableModel {

        boolean[] canEdit = new boolean[]{
            true, true
        };

        public EmptyTableModel() {
            super(new Object[][]{},
                    new String[]{
                        "Key", "Value"
                    });
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit[columnIndex];
        }
    }

    void doSelected(DefaultMutableTreeNode node) {
        if (node.getUserObject() == null) {
            return;
        }
        Object userObject = node.getUserObject();
        jTable1.setModel(new EmptyTableModel());
        jLabel1.setText("");
        jButton5.setEnabled(false);
        jButton7.setEnabled(false);
        DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();
        if (userObject instanceof Actions) {
            final Actions actions = (Actions) userObject;
            tableModel.addRow(new String[]{"id", actions.getId()});
            tableModel.addRow(new String[]{"startView", actions.getStartView()});
        } else if (userObject instanceof Action) {
            Action action = (Action) userObject;
            tableModel.addRow(new String[]{"id", action.getId()});
            tableModel.addRow(new String[]{"default", action.getDefault()});
            tableModel.addRow(new String[]{"rendered", action.getRendered()});
        } else if (userObject instanceof OnPage) {
            OnPage onPage = (OnPage) userObject;
            tableModel.addRow(new String[]{"id", onPage.getId()});
            tableModel.addRow(new String[]{"defaultTargetPage", onPage.getDefaultTargetPage()});
            jButton5.setEnabled(true);
            jButton7.setEnabled(true);
        } else if (userObject instanceof Case) {
            Case localCase = (Case) userObject;
            tableModel.addRow(new String[]{"value", localCase.getValue()});
            tableModel.addRow(new String[]{"description", localCase.getDescription()});
            tableModel.addRow(new String[]{"targetPage", localCase.getTargetPage().getContent()});
            jLabel1.setText("case");
            jButton5.setEnabled(true);
            jButton7.setEnabled(true);
        } else if (userObject instanceof FormView) {
            FormView formView = (FormView) userObject;
            tableModel.addRow(new String[]{"name", formView.getName()});
            jLabel1.setText("FormView");
        } else if (userObject instanceof FieldViewAccess) {
            FieldViewAccess fieldViewAccess = (FieldViewAccess) userObject;
            tableModel.addRow(new String[]{"ref", fieldViewAccess.getRef()});
            jLabel1.setText("FieldViewAccess");
            jButton5.setEnabled(true);
        } else if (userObject instanceof DocumentCase) {
            DocumentCase documentCase = (DocumentCase) userObject;
            tableModel.addRow(new String[]{"FieldCase", documentCase.getFieldCase().getContent()
                    + " -> " + documentCase.getFieldCase().getRef()});
            jLabel1.setText("DocumentCase");
        } else if (userObject instanceof FieldSet) {
            FieldSet fieldSet = (FieldSet) userObject;
            for (Property property : fieldSet.getProperty()) {
                tableModel.addRow(new String[]{property.getKey(),
                    property.getContent()});
            }
            jLabel1.setText("FieldSet");
        } else if (userObject instanceof FieldDef) {
            fillTableFieldDef(userObject, tableModel);
        }
    }

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        createXml(config, new File("WFViewer-1.0.xml"));
        System.exit(0);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "XML config file", "xml");
        chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(new File("."));
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
        file = chooser.getSelectedFile();
        this.setTitle("Workflow Config Viewer - " + file.getAbsolutePath());
        config = extractXml(file);
        initTree();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        if (jTree1.getSelectionPath() == null) {
            return;
        }
        Object object = jTree1.getSelectionPath().getLastPathComponent();
        if (object instanceof DefaultMutableTreeNode) {
            Object userObject = ((DefaultMutableTreeNode) object).getUserObject();
            if (userObject instanceof OnPage) {
                findRelativeNode(((OnPage) userObject).getDefaultTargetPage());
            } else if (userObject instanceof Case) {
                findRelativeNode(((Case) userObject).getTargetPage().getContent());
            } else if (userObject instanceof FieldViewAccess) {
                findRelativeNode3(((FieldViewAccess) userObject).getRef());
            }
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    // Finds the path in tree as specified by the node array. The node array is a sequence 
    // of nodes where nodes[0] is the root and nodes[i] is a child of nodes[i-1]. 
    // Comparison is done using Object.equals(). Returns null if not found. 
    public TreePath find(JTree tree, Object[] nodes) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        return find2(tree, new TreePath(root), nodes, 0, false);
    }
    // Finds the path in tree as specified by the array of names. The names array is a 
    // sequence of names where names[0] is the root and names[i] is a child of names[i-1]. 
    // Comparison is done using String.equals(). Returns null if not found. 

    public TreePath findByName(JTree tree, String[] names) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        return find2(tree, new TreePath(root), names, 0, true);
    }

    private TreePath find2(JTree tree, TreePath parent, Object[] nodes, int depth, boolean byName) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        Object o = node; // If by name, convert node to a string 
        if (byName) {
            o = o.toString();
        } // If equal, go down the branch 
        if (o.equals(nodes[depth])) { // If at end, return match 
            if (depth == nodes.length - 1) {
                return parent;
            } // Traverse children 
            if (node.getChildCount() >= 0) {
                for (Enumeration e = node.children(); e.hasMoreElements();) {
                    TreeNode n = (TreeNode) e.nextElement();
                    TreePath path = parent.pathByAddingChild(n);
                    TreePath result = find2(tree, path, nodes, depth + 1, byName); // Found a match
                    if (result != null) {
                        return result;
                    }
                }
            }
        } // No match at this branch 
        return null;
    }

    private void jTable1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTable1FocusGained
        jButton6.setEnabled(true);
    }//GEN-LAST:event_jTable1FocusGained

    private void jTable1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTable1FocusLost
    }//GEN-LAST:event_jTable1FocusLost

    private class PathNode {
        private TreePath treePath;
        private DefaultMutableTreeNode node;
        public PathNode(TreePath treePath, DefaultMutableTreeNode node) {
            this.treePath = treePath;
            this.node = node;
        }

        public DefaultMutableTreeNode getNode() {
            return node;
        }

        public TreePath getTreePath() {
            return treePath;
        }
        @Override
        public String toString() {
            DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) ((DefaultMutableTreeNode)
                    treePath.getLastPathComponent()).getParent();
            String postfix = "";
            if (prevNode.getUserObject() instanceof FormView) {
                postfix = " in " + ((FormView) prevNode.getUserObject()).getName();
            }
            return new MyRenderer().getNodeText(node.getUserObject()) + postfix;
        }
    }
    private List<PathNode> searchNodes(String message) {
        DefaultTreeModel model = (DefaultTreeModel) jTree1.getModel();
        List<PathNode> pathes = new ArrayList<PathNode>();
        out:
        for (int index1 = 0; index1 < ((DefaultMutableTreeNode) model.getRoot()).getChildCount(); index1 += 1) {
            DefaultMutableTreeNode node1 =
                    (DefaultMutableTreeNode) ((DefaultMutableTreeNode) model.getRoot()).getChildAt(index1);
            pathes.add(new PathNode(find(jTree1, new Object[]{jTree1.getModel().getRoot(),
                node1}), node1));
            for (int index2 = 0; index2 < node1.getChildCount(); index2 += 1) {
                DefaultMutableTreeNode node2 =
                        (DefaultMutableTreeNode) node1.getChildAt(index2);
                pathes.add(new PathNode(find(jTree1, new Object[]{jTree1.getModel().getRoot(),
                    node1, node2}), node2));
                for (int index3 = 0; index3 < node2.getChildCount(); index3 += 1) {
                    DefaultMutableTreeNode node3 =
                            (DefaultMutableTreeNode) node2.getChildAt(index3);
                    pathes.add(new PathNode(find(jTree1, new Object[]{jTree1.getModel().getRoot(),
                        node1, node2, node3}), node3));
                }
            }
        }
        List<PathNode> result = new ArrayList<PathNode>();
        for (PathNode path : pathes) {
            if (new MyRenderer().getNodeText(path.getNode().getUserObject()).toLowerCase().contains(
                    message.toLowerCase())) {
                result.add(path);
            }
        }
        return result;
    }
    
    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        if (jTree1.getSelectionPath() == null) {
            return;
        }
        Object object = jTree1.getSelectionPath().getLastPathComponent();
        if (object instanceof DefaultMutableTreeNode) {
            Object userObject = ((DefaultMutableTreeNode) object).getUserObject();
            if (userObject instanceof OnPage) {
                findRelativeNode2(((OnPage) userObject).getDefaultTargetPage());
            } else if (userObject instanceof Case) {
                findRelativeNode2(((Case) userObject).getTargetPage().getContent());
            }
        }
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        if (searchDialog == null) {
            searchDialog = new JDialog(this);
            final SearchPanel searchPanel = new SearchPanel();
            searchDialog.setContentPane(searchPanel);
            searchDialog.pack();
            searchDialog.setLocationRelativeTo(this);
            final List<PathNode> searchResult = new ArrayList<PathNode>();
            searchPanel.getjButton1().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (searchPanel.getjList1().getSelectedIndex() >= 0) {
                        if (jTree1.getSelectionPath() != null) {
                            history.push(jTree1.getSelectionPath());
                        }
                        TreePath treePath = searchResult.get(
                                searchPanel.getjList1().getSelectedIndex()).getTreePath();
                        jTree1.setSelectionPath(treePath);
                        jTree1.scrollPathToVisible(treePath);
                    }
                }
            });
            searchPanel.getjTextField1().addKeyListener(new KeyListener() {
                public void keyPressed(KeyEvent keyEvent) {
                }

                public void keyTyped(KeyEvent keyEvent) {
                }

                public void keyReleased(KeyEvent keyEvent) {
                    String message = ((JTextField) keyEvent.getSource()).getText();
                    searchPanel.getjList1().setModel(new DefaultListModel());
                    searchResult.clear();
                    searchResult.addAll(searchNodes(message));
                    for (PathNode pathNode : searchResult) {
                        ((DefaultListModel) searchPanel.getjList1().getModel()).addElement(pathNode);
                    }
                    searchPanel.getjLabel2().setText("Items found " + searchResult.size());
                }
            });
        }
        searchDialog.setVisible(true);
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        if (!history.isEmpty()) {
            TreePath path = history.pop();
            jTree1.setSelectionPath(path);
            jTree1.scrollPathToVisible(path);
        }
}//GEN-LAST:event_jButton6ActionPerformed

    private static void setLookAndFeel() {
        javax.swing.UIManager.LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
        String firstFoundClass = null;
        for (javax.swing.UIManager.LookAndFeelInfo info : infos) {
            String foundClass = info.getClassName();
            if ("Nimbus".equals(info.getName())) {
                firstFoundClass = foundClass;
                break;
            }
            if (null == firstFoundClass) {
                firstFoundClass = foundClass;
            }
        }

        if (null == firstFoundClass) {
            throw new IllegalArgumentException("No suitable Swing looks and feels");
        } else {
            try {
                UIManager.setLookAndFeel(firstFoundClass);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(WFViewer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(WFViewer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(WFViewer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger(WFViewer.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        setLookAndFeel();
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new WFViewer().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables
}
