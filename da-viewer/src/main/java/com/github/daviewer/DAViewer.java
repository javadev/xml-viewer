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
package com.github.daviewer;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultCaret;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import com.github.daviewer.stubs.Action;
import com.github.daviewer.stubs.Checker;
import com.github.daviewer.stubs.Command;
import com.github.daviewer.stubs.CommandSet;
import com.github.daviewer.stubs.DossieranalyserConfig;
import com.github.daviewer.stubs.Profile;
import com.github.daviewer.stubs.Rule;

public class DAViewer extends javax.swing.JFrame {
    
    private DossieranalyserConfig config = null;
    private File file;
    private JFileChooser chooser;

    /** Creates new form Antenna */
    public DAViewer() {
        initComponents();
        chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "XML config file", "xml");
        chooser.setFileFilter(filter);
    }

    private void createXml() throws UnsupportedOperationException {
        JAXBContext jContext;
        try {
            jContext = JAXBContext.newInstance("com.github.daviewer.stubs");
        } catch (JAXBException e) {
            throw new UnsupportedOperationException("Can't create JAXB", e);
        }
        Marshaller marshaller;
        try {
            marshaller = jContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(config, file);
        } catch (JAXBException e) {
            Logger.getLogger(DAViewer.class.getName()).warning("Can't create JAXB Marshaller " + e.getMessage());
        }
    }

    private DossieranalyserConfig extractXml(File xml) {
        JAXBContext jContext;
        try {
            jContext = JAXBContext.newInstance("com.github.daviewer.stubs");
        } catch (JAXBException e) {
            throw new UnsupportedOperationException("Can't create JAXB", e);
        }
        Unmarshaller unmarshaller;
        config = null;
        try {
            unmarshaller = jContext.createUnmarshaller();
            config = (DossieranalyserConfig) unmarshaller.unmarshal(xml);
        } catch (JAXBException e) {
            Logger.getLogger(DAViewer.class.getName()).warning("Can't create JAXB Unmarshaller, " + xml);
        }
        return config;
    }

    private void fillActionProperties(final Action action, final DefaultTableModel tableModel) throws IOException {
        String filename = null;
        for (Object item : action.getContent()) {
            if (item instanceof com.github.daviewer.stubs.Class) {
                String className = ((com.github.daviewer.stubs.Class) item).getContent();
                tableModel.addRow(new String[]{"class", className});
                DefaultCaret caret = (DefaultCaret) jTextArea1.getCaret();
                caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
                filename =  jTextField1.getText() + "/" + className.replaceAll("\\.", "/") + ".java";
            } else if (item instanceof com.github.daviewer.stubs.Value) {
                tableModel.addRow(new String[]{"value", ((com.github.daviewer.stubs.Value) item).getContent()});
            } else if (item instanceof com.github.daviewer.stubs.Description) {
                tableModel.addRow(new String[]{"description", ((com.github.daviewer.stubs.Description) item).getContent()});
            }
        }
        if (filename != null) {
            jTextArea1.setText(readFileAsString(filename));
        }
    }

    private void fillActionTable(Object userObject) throws IOException {
        final Action action = (Action) userObject;
        jLabel1.setText("id: " + action.getId());
        final DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();
        fillActionProperties(action, tableModel);
        tableModel.addTableModelListener(new TableModelListener() {

            public void tableChanged(TableModelEvent e) {
                if (e.getColumn() >= 0) {
                    String key = (String) tableModel.getValueAt(e.getFirstRow(), 0);
                    String value = (String) tableModel.getValueAt(e.getFirstRow(), 1);
                    if ("class".equals(key)) {
                        fillClassValue(value);
                    } else if ("value".equals(key)) {
                        fillValueValue(value);
                    } else if ("description".equals(key)) {
                        fillDescriptionValue(value);
                    }
                }
            }

            private void fillDescriptionValue(String value) {
                int index = 0;
                for (Object obj : action.getContent()) {
                    if (obj instanceof com.github.daviewer.stubs.Description) {
                        com.github.daviewer.stubs.Description description = new com.github.daviewer.stubs.Description();
                        description.setContent(value);
                        action.getContent().set(index, description);
                        return;
                    }
                    index += 1;
                }
                com.github.daviewer.stubs.Description description = new com.github.daviewer.stubs.Description();
                description.setContent(value);
                action.getContent().add(description);
            }

            private void fillValueValue(String value) {
                int index = 0;
                for (Object obj : action.getContent()) {
                    if (obj instanceof com.github.daviewer.stubs.Value) {
                        com.github.daviewer.stubs.Value localValue = new com.github.daviewer.stubs.Value();
                        localValue.setContent(value);
                        action.getContent().set(index, localValue);
                        return;
                    }
                    index += 1;
                }
                com.github.daviewer.stubs.Value localValue = new com.github.daviewer.stubs.Value();
                localValue.setContent(value);
                action.getContent().add(localValue);
            }

            private void fillClassValue(String value) {
                int index = 0;
                for (Object obj : action.getContent()) {
                    if (obj instanceof com.github.daviewer.stubs.Class) {
                        com.github.daviewer.stubs.Class clazz = new com.github.daviewer.stubs.Class();
                        clazz.setContent(value);
                        action.getContent().set(index, clazz);
                        return;
                    }
                    index += 1;
                }
                com.github.daviewer.stubs.Class clazz = new com.github.daviewer.stubs.Class();
                clazz.setContent(value);
                action.getContent().add(clazz);
            }
        });
        jTable1.setModel(tableModel);
        jButton5.setEnabled(true);
    }

    private void fillActions(Rule rule, DefaultTreeModel model, DefaultMutableTreeNode ruleNode) {
        for (Object actionItem : rule.getContent()) {
            if (actionItem instanceof Action) {
                String actionName = (String) ((Action) actionItem).getContent().get(0);
                for (Action action : config.getActions().getAction()) {
                    if (action.getId().equals(actionName)) {
                        DefaultMutableTreeNode actionNode = new DefaultMutableTreeNode(action);
                        model.insertNodeInto(actionNode, ruleNode, ruleNode.getChildCount());
                    }
                }
            }
        }
    }

    private void fillChecker(Object commandItem, DefaultTreeModel model, DefaultMutableTreeNode commandNode) {
        String checkerName = (String) ((Checker) commandItem).getContent().get(0);
        for (Checker checker : config.getCheckers().getChecker()) {
            if (checker.getId().equals(checkerName)) {
                DefaultMutableTreeNode checkerNode = new DefaultMutableTreeNode(checker);
                model.insertNodeInto(checkerNode, commandNode, commandNode.getChildCount());
            }
        }
    }

    private void fillCommandSet(Profile profile, DefaultTreeModel model, DefaultMutableTreeNode node) {
        String commandSetName = (String) profile.getCommandSet().getContent().get(0);
        for (CommandSet commandSet : config.getCommandSets().getCommandSet()) {
            if (commandSet.getId().equals(commandSetName)) {
                DefaultMutableTreeNode commandSetNode = new DefaultMutableTreeNode(commandSet);
                model.insertNodeInto(commandSetNode, node, node.getChildCount());
                for (Object command : commandSet.getContent()) {
                    if (command instanceof Command) {
                        fillCommands(command, model, commandSetNode);
                    }
                }
            }
        }
    }

    private void fillCommands(Object command, DefaultTreeModel model, DefaultMutableTreeNode commandSetNode) {
        String commandName = (String) ((Command) command).getContent().get(0);
        for (Command command2 : config.getCommands().getCommand()) {
            if (command2.getId().equals(commandName)) {
                DefaultMutableTreeNode commandNode = new DefaultMutableTreeNode(command2);
                model.insertNodeInto(commandNode, commandSetNode, commandSetNode.getChildCount());
                for (Object commandItem : command2.getContent()) {
                    if (commandItem instanceof Checker) {
                        fillChecker(commandItem, model, commandNode);
                    } else if (commandItem instanceof Rule) {
                        fillRule(commandItem, model, commandNode);
                    }
                }
            }
        }
    }

    private void fillRule(Object commandItem, DefaultTreeModel model, DefaultMutableTreeNode commandNode) {
        String ruleName = (String) ((Rule) commandItem).getContent().get(0);
        for (Rule rule : config.getRules().getRule()) {
            if (rule.getId().equals(ruleName)) {
                DefaultMutableTreeNode ruleNode = new DefaultMutableTreeNode(rule);
                model.insertNodeInto(ruleNode, commandNode, commandNode.getChildCount());
                fillActions(rule, model, ruleNode);
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
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton4 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("DAViewer");

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

        jButton1.setText("Save xml file");
        jButton1.setEnabled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Save xml file as ...");
        jButton2.setEnabled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jButton3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jButton1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton3)
                    .add(jButton1)
                    .add(jButton2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 578, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel1);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Properties"));
        jPanel2.setMaximumSize(new java.awt.Dimension(610, 444));
        jPanel2.setPreferredSize(new java.awt.Dimension(662, 447));

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

        jButton5.setText("Add");
        jButton5.setEnabled(false);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setText("Remove");
        jButton6.setEnabled(false);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane3.setViewportView(jTextArea1);

        jTabbedPane1.addTab("Source code", jScrollPane3);

        jLabel2.setText("Source folder");

        jButton7.setText("Select folder");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 312, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 262, Short.MAX_VALUE)
                .add(jButton5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 97, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(14, 14, 14))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(695, Short.MAX_VALUE)
                .add(jButton4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .add(jPanel2Layout.createSequentialGroup()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
                .addContainerGap())
            .add(jPanel2Layout.createSequentialGroup()
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
                .addContainerGap())
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 329, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 124, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(194, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jButton6)
                    .add(jButton5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 222, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton7))
                .add(3, 3, 3)
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(jButton4)
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jPanel2);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1129, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 652, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void initTree() {
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode("root");
        DefaultTreeModel model = new DefaultTreeModel(treeNode);
        DefaultMutableTreeNode actionsNode = new DefaultMutableTreeNode("Actions");
        model.insertNodeInto(actionsNode, treeNode, treeNode.getChildCount());
        for (Action action : config.getActions().getAction()) {
            model.insertNodeInto(new DefaultMutableTreeNode(action), actionsNode, actionsNode.getChildCount());
        }
        DefaultMutableTreeNode checkersNode = new DefaultMutableTreeNode("Checkers");
        model.insertNodeInto(checkersNode, treeNode, treeNode.getChildCount());
        for (Checker checker : config.getCheckers().getChecker()) {
            model.insertNodeInto(new DefaultMutableTreeNode(checker), checkersNode, checkersNode.getChildCount());
        }
        DefaultMutableTreeNode rulesNode = new DefaultMutableTreeNode("Rules");
        model.insertNodeInto(rulesNode, treeNode, treeNode.getChildCount());
        for (Rule rule : config.getRules().getRule()) {
            model.insertNodeInto(new DefaultMutableTreeNode(rule), rulesNode, rulesNode.getChildCount());
        }
        DefaultMutableTreeNode commandsNode = new DefaultMutableTreeNode("Commands");
        model.insertNodeInto(commandsNode, treeNode, treeNode.getChildCount());
        for (Command command : config.getCommands().getCommand()) {
            model.insertNodeInto(new DefaultMutableTreeNode(command), commandsNode, commandsNode.getChildCount());
        }
        DefaultMutableTreeNode commandSetsNode = new DefaultMutableTreeNode("Command-sets");
        model.insertNodeInto(commandSetsNode, treeNode, treeNode.getChildCount());
        for (CommandSet commandSet : config.getCommandSets().getCommandSet()) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(commandSet);
            model.insertNodeInto(node, commandSetsNode, commandSetsNode.getChildCount());
        }
        DefaultMutableTreeNode profilesNode = new DefaultMutableTreeNode("Profiles");
        model.insertNodeInto(profilesNode, treeNode, treeNode.getChildCount());
        for (Profile profile : config.getProfiles().getProfile()) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(profile);
            model.insertNodeInto(node, profilesNode, profilesNode.getChildCount());
            fillCommandSet(profile, model, node);
        }
        jTree1.setModel(model);
        ToolTipManager.sharedInstance().registerComponent(jTree1);
        jTree1.setCellRenderer(new MyRenderer());
        jTree1.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                try {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
                    doSelected(node);
                } catch (IOException ex) {
                    Logger.getLogger(DAViewer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        MouseListener ml = new MouseAdapter() {
            private void myPopupEvent(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                JTree tree = (JTree)e.getSource();
                TreePath path = tree.getPathForLocation(x, y);
                if (path == null) {
                    return;
                }
                jTree1.setSelectionPath(path);
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                final String labelData = node.getUserObject() instanceof Action
                        ? ((Action) node.getUserObject()).getId() : node.getUserObject() instanceof CommandSet
                        ? ((CommandSet) node.getUserObject()).getId() : node.getUserObject().toString();

                String label = labelData;
                if (node.getUserObject() instanceof Action) {
                    label = "Copy " + ((Action) node.getUserObject()).getId();
                }
                JPopupMenu popup = new JPopupMenu();
                JMenuItem jMenuItem = new JMenuItem(label);
                jMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        StringSelection selection = new StringSelection(labelData);
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(selection, selection);
                    }
                });
                popup.add(jMenuItem);
                popup.show(tree, x, y);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    myPopupEvent(e);
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    myPopupEvent(e);
                }
            }

        };
        jTree1.addMouseListener(ml);
    }

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jButton4ActionPerformed

    private class MyRenderer extends DefaultTreeCellRenderer {
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected,
            boolean isExpanded, boolean isLeaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, isSelected, isExpanded, isLeaf, row, hasFocus);
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            renderer.getTreeCellRendererComponent(tree, value, isSelected, isExpanded, isLeaf, row, hasFocus);
            if (userObject instanceof Profile) {
                setText(((Profile) userObject).getId());
                renderer.setToolTipText(((Profile) userObject).getName());
            } else if (userObject instanceof CommandSet) {
                setText(((CommandSet) userObject).getId());
            } else if (userObject instanceof Command) {
                setText(((Command) userObject).getId());
                renderer.setToolTipText("Type: " + ((Command) userObject).getType());
            } else if (userObject instanceof Rule) {
                setText(((Rule) userObject).getId());
                renderer.setToolTipText("Executor: " + ((Rule) userObject).getExecuter());
            } else if (userObject instanceof Action) {
                setText(((Action) userObject).getId());
            } else if (userObject instanceof Checker) {
                setText(((Checker) userObject).getId());
            } else {
                setText(value.toString() + " [" + userObject.getClass().getName() + "]");
            }
            return this;
        }
    }

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
        file = chooser.getSelectedFile();
        jButton1.setEnabled(true);
        jButton2.setEnabled(true);
        config = extractXml(file);
        initTree();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        createXml();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        chooser.setSelectedFile(file);
        int returnVal = chooser.showSaveDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
        file = new File(chooser.getSelectedFile().getName());
        createXml();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        ((DefaultTableModel) jTable1.getModel()).addRow(new String[] {"", ""});        
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jTable1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTable1FocusGained
        jButton6.setEnabled(true);
    }//GEN-LAST:event_jTable1FocusGained

    private void jTable1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTable1FocusLost

    }//GEN-LAST:event_jTable1FocusLost

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        jTable1.setVisible(false);
        while (jTable1.getSelectedRows().length > 0) {
            ((DefaultTableModel) jTable1.getModel()).removeRow(jTable1.getSelectedRows()[0]);
        }
        jTable1.setVisible(true);
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = dirChooser.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
        jTextField1.setText(dirChooser.getSelectedFile().getAbsolutePath());
    }//GEN-LAST:event_jButton7ActionPerformed

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

    void doSelected(DefaultMutableTreeNode node) throws IOException {
        if (node.getUserObject() != null) {
            Object userObject = node.getUserObject();
            jTable1.setModel(new EmptyTableModel());
            if (userObject instanceof Action) {
                    fillActionTable(userObject);
            } else if (userObject instanceof Profile) {
                jLabel1.setText("id: " + ((Profile) userObject).getId() + ", name: " + ((Profile) userObject).getName());
            } else if(userObject instanceof Checker) {
                final Checker checker = (Checker) userObject;
                jLabel1.setText("id: " + checker.getId());
            } else {
                jLabel1.setText("");
                jButton5.setEnabled(false);
                jButton5.setEnabled(false);
            }
        } else {
            jLabel1.setText("");
            jButton5.setEnabled(false);
            jButton5.setEnabled(false);
        }
    }

    private static String readFileAsString(String filePath)
            throws java.io.IOException {
        byte[] buffer = new byte[(int) new File(filePath).length()];
        FileInputStream f = new FileInputStream(filePath);
        f.read(buffer);
        return new String(buffer);
    }

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
                Logger.getLogger(DAViewer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(DAViewer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(DAViewer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger(DAViewer.class.getName()).log(Level.SEVERE, null, ex);
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
                new DAViewer().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables
    
}
