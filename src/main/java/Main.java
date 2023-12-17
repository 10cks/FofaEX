import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.BorderFactory;
import javax.swing.event.*;

import java.awt.Color;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;
import plugins.FofaPlugin;
import tableInit.SelectedCellBorderHighlighter;

import javax.swing.table.*;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

import static java.awt.BorderLayout.*;
import static plugins.FofaPlugin.loadFileIntoTable;
import static tableInit.GetjTableHeader.adjustColumnWidths;
import static tableInit.GetjTableHeader.getjTableHeader;

import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Main {

    // 创建输入框
    private static JTextField fofaUrl = createTextField("https://fofa.info");
    private static JTextField fofaEmail = createTextField("请输入邮箱");
    private static JTextField fofaKey = createTextField("请输入API key");

    private static String rulesPath = "rules.txt";
    private static String accountsPath = "accounts.txt";

    // 设置 field 规则
    private static boolean ipMark = true;
    private static boolean portMark = true;
    private static boolean protocolMark = true;
    private static boolean titleMark = true;
    private static boolean domainMark = true;
    private static boolean linkMark = true;
    private static boolean icpMark = false;
    private static boolean cityMark = false;
    private static boolean countryMark = false;
    /* 下面未完成 */
    private static boolean country_nameMark = false;
    private static boolean regionMark = false;
    private static boolean longitudeMark = false;
    private static boolean latitudeMark = false;
    private static boolean asNumberMark = false;
    private static boolean asOrganizationMark = false;
    private static boolean hostMark = true;
    private static boolean osMark = false;
    private static boolean serverMark = false;
    private static boolean jarmMark = false;
    private static boolean headerMark = false;
    private static boolean bannerMark = false;
    private static boolean baseProtocolMark = false;
    private static boolean certsIssuerOrgMark = false;
    private static boolean certsIssuerCnMark = false;
    private static boolean certsSubjectOrgMark = false;
    private static boolean certsSubjectCnMark = false;
    private static boolean tlsJa3sMark = false;
    private static boolean tlsVersionMark = false;
    private static boolean productMark = false;
    private static boolean productCategoryMark = false;
    private static boolean versionMark = false;
    private static boolean lastupdatetimeMark = false;
    private static boolean cnameMark = false;
    private static boolean iconHashMark = false;
    private static boolean certsValidMark = false;
    private static boolean cnameDomainMark = false;
    private static boolean bodyMark = false;
    private static boolean iconMark = false;
    private static boolean fidMark = false;
    private static boolean structinfoMark = false;

    /* 上面未完成 */

    private static boolean scrollPaneMark = true;
    private static JDialog searchDialog = null;

    // 标记
    private static boolean exportButtonAdded = false;
    private static boolean timeAdded = false;
    private static JLabel timeLabel;
    private static int queryTotalNumber;
    private static int numberOfItems;
    private static int currentPage = 1;
    private static int sizeSetting = 10000;
    // 创建全局数据表
    private static JTable table;

    // 在类的成员变量中创建弹出菜单
    private static JPopupMenu popupMenu = new JPopupMenu();
    private static JMenuItem itemSelectColumn = new JMenuItem("选择当前整列");
    private static JMenuItem itemDeselectColumn = new JMenuItem("取消选择整列");
    private static JMenuItem itemOpenLink = new JMenuItem("打开链接");
    static JMenuItem itemCopy = new JMenuItem("复制");

    private static JMenuItem itemSearch = new JMenuItem("表格搜索");

    private static File lastOpenedPath; // 添加一个成员变量来保存上次打开的文件路径

    static TableCellRenderer highlightRenderer = new HighlightRenderer();
    private static TableCellRenderer defaultRenderer;

    public static void initializeTable() {
        // 添加菜单项到弹出菜单
        popupMenu.add(itemOpenLink);
        popupMenu.add(itemCopy);
        popupMenu.add(itemSearch);
        popupMenu.add(itemSelectColumn);
        popupMenu.add(itemDeselectColumn);
        defaultRenderer = table.getDefaultRenderer(Object.class);
        // 为右键菜单项添加全选当前列监听器
        itemSelectColumn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (table != null) {
                    int col = table.getSelectedColumn();
                    if (col >= 0) {
                        table.setColumnSelectionAllowed(true);
                        table.setRowSelectionAllowed(false);
                        table.clearSelection();
                        table.addColumnSelectionInterval(col, col);
                    }
                }
            }
        });

        // 为取消选择列的菜单项添加事件监听器
        itemDeselectColumn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 取消选择当前列
                if (table != null) {
                    table.clearSelection();
                    // 恢复默认的行选择模式
                    table.setRowSelectionAllowed(true);
                    table.setColumnSelectionAllowed(false);
                }
            }
        });


        // 为打开链接的菜单项添加事件监听器
        itemOpenLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 执行打开链接的操作
                if (table != null) {
                    int selectedRow = table.getSelectedRow();
                    int selectedCol = table.getSelectedColumn();
                    if (selectedRow >= 0 && selectedCol >= 0) {
                        Object cellContent = table.getValueAt(selectedRow, selectedCol);
                        if (cellContent != null && cellContent.toString().startsWith("http")) {
                            try {
                                Desktop desktop = Desktop.getDesktop();
                                URI uri = new URI(cellContent.toString());
                                desktop.browse(uri);
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(popupMenu, "无法打开链接：" + cellContent, "错误", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(popupMenu, "当前单元格不包含有效链接", "警告", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            }
        });

        // 为复制的菜单项添加事件监听器
        itemCopy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 执行复制操作
                if (table != null) {
                    int[] selectedRows = table.getSelectedRows();
                    int[] selectedColumns = table.getSelectedColumns();
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < selectedRows.length; i++) {
                        for (int j = 0; j < selectedColumns.length; j++) {
                            Object value = table.getValueAt(selectedRows[i], selectedColumns[j]);
                            sb.append(value == null ? "" : value.toString());
                            if (j < selectedColumns.length - 1) {
                                sb.append("\t"); // 列之间添加制表符分隔
                            }
                        }
                        if (i < selectedRows.length - 1) {
                            sb.append("\n"); // 行之间添加换行符分隔
                        }
                    }

                    StringSelection stringSelection = new StringSelection(sb.toString());
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSelection, null);
                }
            }
        });

        itemSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    createSearchDialog();
            }
        });
    }

    private static void createSearchDialog() {

        // 检查对话框是否已经存在
        if (searchDialog != null) {
            // 对话框已经存在，可能需要将其带到前面
            searchDialog.toFront();
            searchDialog.requestFocus();
            return;
        }

        // 创建一个新的JDialog
        JDialog searchDialog = new JDialog((Frame) null, "搜索", false); // false表示非模态对话框
        searchDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // 点击关闭按钮时释放窗口资源

        searchDialog.setLayout(new FlowLayout());
        searchDialog.setAlwaysOnTop(true);
        JLabel label = new JLabel("输入搜索内容：");
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("搜索");
        JButton closeButton = new JButton("退出高亮");

        // 添加组件到对话框
        searchDialog.add(label);
        searchDialog.add(searchField);
        searchDialog.add(searchButton);
        searchDialog.add(closeButton);

        // 搜索按钮监听器
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchText = searchField.getText();
                if (searchText != null && !searchText.isEmpty()) {
                    // 执行搜索并高亮显示匹配的单元格
                    searchTable(searchText);
                }
            }
        });

        // 退出按钮监听器
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchDialog.dispose(); // 关闭对话框
                resetSearch(); // 重置搜索结果
                //searchDialog = null; // 重置searchDialog引用
            }
        });

        // 显示对话框
        searchDialog.pack();
        searchDialog.setLocationRelativeTo(null); // 在屏幕中央显示
        searchDialog.setVisible(true);
    }

    private static void searchTable(String searchText) {
        if (!(highlightRenderer instanceof HighlightRenderer)) {
            highlightRenderer = new HighlightRenderer();
        }
        // Update the search text in the highlight renderer
        ((HighlightRenderer) highlightRenderer).setSearchText(searchText);

        // Apply the highlight renderer to all columns
        for (int col = 0; col < table.getColumnCount(); col++) {
            table.getColumnModel().getColumn(col).setCellRenderer(highlightRenderer);
        }

        // Repaint the table to show the changes
        table.repaint();
    }

    private static void resetSearch() {
        // Reset the renderer to the default for all columns
        for (int col = 0; col < table.getColumnCount(); col++) {
            table.getColumnModel().getColumn(col).setCellRenderer(defaultRenderer);
        }

        // 退出时恢复表格颜色
        table.repaint();
    }

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, FileNotFoundException {

        JFrame jFrame = new JFrame("fofaEX");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try{
            URL resource = Main.class.getResource("icon.png");
            jFrame.setIconImage((new ImageIcon(resource).getImage())); //给Frame设置图标
        }catch(Exception e){
            System.out.println(e);
        }
        // 创建 CardLayout 布局管理器
        CardLayout cardLayout = new CardLayout();
        jFrame.setLayout(cardLayout);

        // 设置外观风格
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        // 刷新jf容器及其内部组件的外观
        SwingUtilities.updateComponentTreeUI(jFrame);
        jFrame.setSize(1000, 800);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 确保按下关闭按钮时结束程序

        // 创建 fofa 输入框
        JTextField textField0 = createTextFieldFofa("fofaEX: FOFA Extension");

        // 创建数据表
        if (table == null) {
            table = new JTable();
        }
        // 初始化 table 右键
        initializeTable();

        textField0.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // 当输入框内的文字是提示文字时，先清空输入框再允许输入
                if (textField0.getText().equals("fofaEX: FOFA Extension")) {
                    textField0.setText("");
                }
            }
        });

        // 设置背景色为 (4, 12, 31)
        textField0.setBackground(new Color(48, 49, 52));
        // 设置光标
        textField0.setCaret(new CustomCaret(Color.WHITE));

        // 设置字体
        Font font = new Font("Mono", Font.BOLD, 14);
        textField0.setFont(font);

        // fofaEX: FOFA Extension 事件
        textField0.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // 当输入框得到焦点时，如果当前是提示文字，则清空输入框并将文字颜色设置为白色
                if (textField0.getText().equals("fofaEX: FOFA Extension")) {
                    textField0.setText("");
                    textField0.setForeground(Color.WHITE);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // 当输入框失去焦点时，如果输入框为空，则显示提示文字，并将文字颜色设置为灰色
                if (textField0.getText().isEmpty()) {
                    textField0.setText("fofaEX: FOFA Extension");
                    textField0.setForeground(Color.GRAY);
                }
            }

        });

        textField0.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (textField0.getText().equals("fofaEX: FOFA Extension")) {
                    textField0.setText("");
                    textField0.setForeground(Color.WHITE);
                }
            }
        });

        textField0.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                textField0.setForeground(Color.WHITE);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (textField0.getText().isEmpty()) {
                    textField0.setForeground(Color.GRAY);
                } else {
                    textField0.setForeground(Color.WHITE);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // 平滑字体，无需处理
            }
        });

        // 将光标放在末尾
        // textField0.setCaretPosition(textField0.getText().length());

        // 编辑撤销

        // 创建UndoManager和添加UndoableEditListener。
        final UndoManager undoManager = new UndoManager();
        Document doc = textField0.getDocument();
        doc.addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent e) {
                undoManager.addEdit(e.getEdit());
            }
        });

        // 添加KeyListener到textField。
        textField0.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_Z) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)) {
                    if (undoManager.canUndo()) {
                        undoManager.undo();
                    }
                } else {
                    if ((e.getKeyCode() == KeyEvent.VK_Z)) {
                        e.getModifiersEx();
                    }
                }
            }
        });


//        String asciiIcon =
//                "   __            __             _____  __  __\n" +
//                "  / _|   ___    / _|   __ _    | ____| \\ \\/ /\n" +
//                " | |_   / _ \\  | |_   / _` |   |  _|    \\  / \n" +
//                " |  _| | (_) | |  _| | (_| |   | |___   /  \\ \n" +
//                " |_|    \\___/  |_|    \\__,_|   |_____| /_/\\_\\";
//
//        JLabel labelIcon = new JLabel("<html><pre>" + asciiIcon + "</pre></html>");
        JLabel labelIcon = new JLabel(" FOFA EX");
        labelIcon.setForeground(new Color(48, 49, 52)); // 设置文本颜色为红色
        Font iconFont = new Font("Times New Roman", Font.BOLD, 60);
        labelIcon.setFont(iconFont);


        // 创建按钮面板，不改变布局（保持BoxLayout）
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        // 创建主面板并使用BoxLayout布局
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // 创建一个子面板，用来在搜索框边上新增按钮
        JPanel subPanel1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        // 创建面板并使用FlowLayout布局
        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4)); // hgap: 组件间的水平间距 vgap: 件间的垂直间距
        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

        JPanel panel3 = new JPanel(new GridLayout(0, 10, 0, 0));

        JPanel panel4 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        // 创建面板并使用GridLayout布局
        JPanel panel5 = new JPanel(new GridLayout(0, 5, 10, 10)); // 0表示行数不限，5表示每行最多5个组件，10, 10是组件之间的间距

        JPanel panel6 = new JPanel(new BorderLayout());
        panel6.setBorder(BorderFactory.createEmptyBorder(20, 5, 10, 5));

        JPanel panel7 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        // panel8 用来放导出表格的按键
        JPanel panel8 = new JPanel();

        JPanel panel9 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        // 创建"更新规则"按钮 创建"新增"按钮
        JButton updateButton = new JButton("➕");
        updateButton.setFocusPainted(false);
        updateButton.setFocusable(false);
        // 新增一个LinkedHashMap，用于存储按钮的键名和键值
        Map<String, JButton> buttonsMap = new LinkedHashMap<>();

        BufferedReader rulesReader = null;
        BufferedReader accountsReader = null;
        try {
            // 创建 rules.txt 文件如果它不存在
            File rulesFile = new File(rulesPath);
            if (!rulesFile.exists()) {
                rulesFile.createNewFile();
                System.out.println("[+] The current path does not contain rules.txt. Create rules.txt.");
            }
            rulesReader = new BufferedReader(new FileReader(rulesFile));

            // 创建 accounts.txt 文件如果它不存在
            File accountsFile = new File(accountsPath);
            if (!accountsFile.exists()) {
                accountsFile.createNewFile();
                System.out.println("[+] The current path does not contain accounts.txt. Create accounts.txt.");
            }
            accountsReader = new BufferedReader(new FileReader(accountsFile));
        } catch (IOException e) {
            // IO 异常处理
            e.printStackTrace();
        }

        settingInit(rulesReader, accountsReader, panel5, textField0, fofaEmail, fofaKey, buttonsMap);
        updateButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // 创建一个JPanel来包含两个输入框
                JPanel inputPanel = new JPanel(new GridLayout(4, 4));
                inputPanel.add(new JLabel("键名:"));
                JTextField nameField = new JTextField(10);
                inputPanel.add(nameField);
                inputPanel.add(new JLabel("键值:"));
                JTextField valueField = new JTextField(10);
                inputPanel.add(valueField);


                // 弹出自定义对话框
                int result = JOptionPane.showConfirmDialog(null, inputPanel, "新增按键",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                // 当用户点击OK时处理输入
                if (result == JOptionPane.OK_OPTION) {
                    String keyName = nameField.getText().trim();
                    String keyValue = valueField.getText().trim();

                    // 验证输入是否非空
                    if (!keyName.isEmpty() && !keyValue.isEmpty()) {
                        // 将键名和键值以"键名":{键值}的形式保存在rule.txt的最后一行
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rulesPath, true))) {
                            System.out.println(keyValue);
                            writer.write("\"" + keyName + "\":{" + keyValue + "},");
                            writer.newLine(); // Ensure the new entry is on a new line
                        } catch (IOException addError) {
                            addError.printStackTrace();
                            JOptionPane.showMessageDialog(null, "无法写入文件", "错误", JOptionPane.ERROR_MESSAGE);
                        }

                        // 添加右键菜单功能
                        try {
                            BufferedReader reader = new BufferedReader(new FileReader(rulesPath));
                            Map<String, String> newMap = new LinkedHashMap<>();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                line = line.trim();
                                // 跳过井号注释
                                if (line.startsWith("#")) {
                                    continue;
                                }
                                if (line.startsWith("\"") && line.contains("{") && line.contains("}")) {
                                    String[] parts = line.split(":", 2);

                                    String key = parts[0].substring(1, parts[0].length() - 1).trim();

                                    String value = parts[1].substring(1, parts[1].length() - 2).trim();
                                    newMap.put(key, value);
                                }
                            }
                            reader.close();
                            // 配置文件更新并新增按钮
                            for (Map.Entry<String, String> entry : newMap.entrySet()) {
                                JButton existingButton = buttonsMap.get(entry.getKey());
                                if (existingButton == null) {
                                    // 新按钮
                                    JButton newButton = new JButton(entry.getKey());
                                    newButton.setActionCommand(entry.getValue());
                                    newButton.setToolTipText(entry.getValue()); // 设置按钮的 ToolTip 为键值，悬浮显示
                                    newButton.setFocusPainted(false); // 添加这一行来取消焦点边框的绘制
                                    newButton.setFocusable(false);  // 禁止了按钮获取焦点，因此按钮不会在被点击后显示为"激活"或"选中"的状态
                                    newButton.addActionListener(actionEvent -> {
                                        if (newButton.getForeground() != Color.RED) {
                                            // 如果文本为提示文字，则清空文本
                                            if (textField0.getText().contains("fofaEX: FOFA Extension")) {
                                                textField0.setText("");
                                            }
                                            textField0.setText(textField0.getText() + " " + newButton.getActionCommand());
                                            newButton.setForeground(Color.RED);
                                            newButton.setFont(newButton.getFont().deriveFont(Font.BOLD)); // 设置字体为粗体
                                        } else {
                                            textField0.setText(textField0.getText().replace(" " + newButton.getActionCommand(), ""));
                                            newButton.setForeground(null);
                                            newButton.setFont(null);
                                            // 如果为空则设置 prompt
                                            if (textField0.getText().isEmpty()) {
                                                textField0.setText("fofaEX: FOFA Extension");
                                                textField0.setForeground(Color.GRAY);
                                                // 将光标放在开头
                                                textField0.setCaretPosition(0);

                                            }
                                        }
                                    });

                                    // 添加右键单击事件的处理
                                    newButton.addMouseListener(new MouseAdapter() {
                                        @Override
                                        public void mousePressed(MouseEvent e) {
                                            if (SwingUtilities.isRightMouseButton(e)) {
                                                // 在这里处理右键单击事件
                                                JPopupMenu popupMenu = new JPopupMenu();
                                                JMenuItem deleteItem = new JMenuItem("删除");
                                                JMenuItem editItem = new JMenuItem("修改");
                                                deleteItem.addActionListener(actionEvent -> {
                                                    // 删除：在这里处理删除操作
                                                    int dialogResult = JOptionPane.showConfirmDialog(panel5,
                                                            "是否删除?", "删除确认",
                                                            JOptionPane.YES_NO_OPTION,
                                                            JOptionPane.QUESTION_MESSAGE);
                                                    if (dialogResult == JOptionPane.YES_OPTION) {
                                                        // 确认删除操作
                                                        panel5.remove(newButton);
                                                        panel5.revalidate();
                                                        panel5.repaint();
                                                        // 从文件中删除
                                                        removeButtonAndLineFromFile(entry.getKey(), rulesPath);
                                                    }
                                                });

                                                editItem.addActionListener(actionEvent -> {
                                                    // 获取当前按钮的名称和对应的JButton对象
                                                    String oldName = entry.getKey();
                                                    JButton buttonToUpdate = newButton; // 确保newButton是当前要修改的按钮的引用

                                                    // 创建一个JPanel来包含两个输入框
                                                    JPanel panel = new JPanel(new GridLayout(4, 4));

                                                    panel.add(new JLabel("键名:"));
                                                    JTextField nameField = new JTextField(newButton.getText());
                                                    panel.add(nameField);
                                                    panel.add(new JLabel("键值:"));
                                                    JTextField valueField = new JTextField(buttonToUpdate.getActionCommand());
                                                    panel.add(valueField);

                                                    // 弹出自定义对话框
                                                    int result = JOptionPane.showConfirmDialog(panel5, panel, "修改配置",
                                                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                                                    // 当用户点击OK时处理输入
                                                    if (result == JOptionPane.OK_OPTION) {
                                                        String newName = nameField.getText().trim();
                                                        String newValue = valueField.getText().trim();

                                                        // 验证输入是否已变更且非空
                                                        if (!newName.isEmpty() && !newValue.isEmpty()) {
                                                            // 修改按钮名称和键值
                                                            updateButtonNameAndValue(oldName, newName, newValue, buttonToUpdate, buttonsMap, rulesPath);

                                                            // 更新界面
                                                            panel5.revalidate();
                                                            panel5.repaint();
                                                        }
                                                    }
                                                });

                                                popupMenu.add(editItem);
                                                popupMenu.add(deleteItem);

                                                popupMenu.show(e.getComponent(), e.getX(), e.getY());

                                                popupMenu.add(deleteItem);
                                                popupMenu.show(e.getComponent(), e.getX(), e.getY());
                                            }
                                        }
                                    });


                                    panel5.add(newButton);
                                    buttonsMap.put(entry.getKey(), newButton);
                                } else {
                                    // This is an existing button
                                    existingButton.setActionCommand(entry.getValue());
                                    existingButton.setText(entry.getKey()); // Update button text
                                }
                            }

                            panel5.revalidate();
                            panel5.repaint();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                        // 更新界面
                        panel5.revalidate();
                        panel5.repaint();
                    }
                }

                // 读取文件内容，并创建新的按钮

            }
        });


        // 搜索按钮
        // 将textField0添加到新的SubPanel
        subPanel1.add(textField0);
        searchButton("搜索", true, subPanel1, textField0, fofaEmail, fofaKey, fofaUrl, panel6, panel8, labelIcon, panel2, panel3, panel7, panel9, "null");

        panel1.add(labelIcon);
        panel2.add(subPanel1); // 搜索框 + 搜索按钮

        searchButton("◁", false, panel7, textField0, fofaEmail, fofaKey, fofaUrl, panel6, panel8, labelIcon, panel2, panel3, panel7, panel9, "left");
        searchButton("▷", false, panel7, textField0, fofaEmail, fofaKey, fofaUrl, panel6, panel8, labelIcon, panel2, panel3, panel7, panel9, "right");


        // 添加逻辑运算组件
        createLogicAddButton("=", "=", panel4, textField0);
        createLogicAddButton("==", "==", panel4, textField0);
        createLogicAddButton("&&", "&&", panel4, textField0);
        createLogicAddButton("||", "||", panel4, textField0);
        createLogicAddButton("!=", "!=", panel4, textField0);
        createLogicAddButton("*=", "*=", panel4, textField0);


        // 新增折叠按钮到panel3
        JButton foldButton = new JButton("▼");
        foldButton.setFocusPainted(false); //添加这一行来取消焦点边框的绘制
        foldButton.setFocusable(false);

        // 添加点击事件
        foldButton.addActionListener(new ActionListener() {
            boolean isFolded = false;

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!isFolded) {
                    // 折叠 panel5
                    panel5.setVisible(false);
                    foldButton.setText("◀");
                    scrollPaneMark = false;
                } else {
                    // 展开 panel5
                    panel5.setVisible(true);
                    foldButton.setText("▼");
                    scrollPaneMark = true;
                }
                isFolded = !isFolded;

                // 重新验证和重绘包含 panel5 和 panel6 的主面板
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });

        panel4.add(updateButton); // 更新规则
        panel4.add(foldButton);


        // 创建复选框
        addRuleBox(panel3, "host", newValue -> hostMark = newValue, hostMark, true);
        addRuleBox(panel3, "ip", newValue -> ipMark = newValue, ipMark, true);
        addRuleBox(panel3, "port", newValue -> portMark = newValue, portMark, true);
        addRuleBox(panel3, "protocol", newValue -> protocolMark = newValue, protocolMark);
        addRuleBox(panel3, "title", newValue -> titleMark = newValue, titleMark);
        addRuleBox(panel3, "domain", newValue -> domainMark = newValue, domainMark);
        addRuleBox(panel3, "link", newValue -> linkMark = newValue, linkMark);
        addRuleBox(panel3, "icp", newValue -> icpMark = newValue, icpMark);
        addRuleBox(panel3, "city", newValue -> cityMark = newValue, cityMark);

        /* 下面代码未完成 */

        addRuleBox(panel3, "country", newValue -> countryMark = newValue, countryMark);

        /* 测试一下 */
        addRuleBox(panel3, "country_name", newValue -> country_nameMark = newValue, country_nameMark);
        addRuleBox(panel3, "region", newValue -> regionMark = newValue, regionMark);
        addRuleBox(panel3, "longitude", newValue -> longitudeMark = newValue, longitudeMark);
        addRuleBox(panel3, "latitude", newValue -> latitudeMark = newValue, latitudeMark);
        addRuleBox(panel3, "asNumber", newValue -> asNumberMark = newValue, asNumberMark);
        addRuleBox(panel3, "asOrganization", newValue -> asOrganizationMark = newValue, asOrganizationMark);
        addRuleBox(panel3, "os", newValue -> osMark = newValue, osMark);
        addRuleBox(panel3, "server", newValue -> serverMark = newValue, serverMark);
        addRuleBox(panel3, "jarm", newValue -> jarmMark = newValue, jarmMark);
        addRuleBox(panel3, "header", newValue -> headerMark = newValue, headerMark);
        addRuleBox(panel3, "banner", newValue -> bannerMark = newValue, bannerMark);
        addRuleBox(panel3, "baseProtocol", newValue -> baseProtocolMark = newValue, baseProtocolMark);
        addRuleBox(panel3, "certsIssuerOrg", newValue -> certsIssuerOrgMark = newValue, certsIssuerOrgMark);
        addRuleBox(panel3, "certsIssuerCn", newValue -> certsIssuerCnMark = newValue, certsIssuerCnMark);
        addRuleBox(panel3, "certsSubjectOrg", newValue -> certsSubjectOrgMark = newValue, certsSubjectOrgMark);
        addRuleBox(panel3, "certsSubjectCn", newValue -> certsSubjectCnMark = newValue, certsSubjectCnMark);
        addRuleBox(panel3, "tlsJa3s", newValue -> tlsJa3sMark = newValue, tlsJa3sMark);
        addRuleBox(panel3, "tlsVersion", newValue -> tlsVersionMark = newValue, tlsVersionMark);
        addRuleBox(panel3, "product", newValue -> productMark = newValue, productMark);
        addRuleBox(panel3, "productCategory", newValue -> productCategoryMark = newValue, productCategoryMark);
        addRuleBox(panel3, "version", newValue -> versionMark = newValue, versionMark);
        addRuleBox(panel3, "lastupdatetime", newValue -> lastupdatetimeMark = newValue, lastupdatetimeMark);
        addRuleBox(panel3, "cname", newValue -> cnameMark = newValue, cnameMark);
        addRuleBox(panel3, "iconHash", newValue -> iconHashMark = newValue, iconHashMark);
        addRuleBox(panel3, "certsValid", newValue -> certsValidMark = newValue, certsValidMark);
        addRuleBox(panel3, "cnameDomain", newValue -> cnameDomainMark = newValue, cnameDomainMark);
        addRuleBox(panel3, "body", newValue -> bodyMark = newValue, bodyMark);
        addRuleBox(panel3, "icon", newValue -> iconMark = newValue, iconMark);
        addRuleBox(panel3, "fid", newValue -> fidMark = newValue, fidMark);
        addRuleBox(panel3, "structinfo", newValue -> structinfoMark = newValue, structinfoMark);


        // 设置全局边框：创建一个带有指定的空白边框的新面板，其中指定了上、左、下、右的边距
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 添加面板到主面板
        mainPanel.add(panel1);
        mainPanel.add(panel2);
        mainPanel.add(panel3);
        mainPanel.add(panel4);
        mainPanel.add(panel5);
        mainPanel.add(panel6);
        mainPanel.add(panel7);
        mainPanel.add(panel8);
        mainPanel.add(panel9);

        // 把面板添加到JFrame
        jFrame.add(mainPanel, NORTH);
        jFrame.add(buttonPanel, WEST);
        // 设置窗口居中并显示
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);

        // 在程序运行时，使 textField0 获得焦点
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                textField0.requestFocusInWindow();
            }
        });

        // 菜单栏

        // 创建菜单栏
        JMenuBar menuBar = new JMenuBar();

        // 创建"账户设置"菜单项
        JMenu settingsMenu = new JMenu("账户设置");

        // 在此菜单项下可以添加更多的子菜单项，以下只是一个示例
        JMenuItem changePasswordMenuItem = new JMenuItem("FOFA API");
        settingsMenu.add(changePasswordMenuItem);

        menuBar.add(settingsMenu);

        // 更改"账户设置"菜单项的事件监听
        changePasswordMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 创建新的JFrame
                JFrame settingsFrame = new JFrame("Settings");

                // 创建新的面板并添加组件
                JPanel settingsPanel = new JPanel(new GridLayout(4, 2, 5, 5)); // 使用4行2列的GridLayout
                settingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 设置边距

                JButton checkButton = new JButton("检查账户");
                checkButton.setFocusPainted(false); // 添加这一行来取消焦点边框的绘制
                checkButton.setFocusable(false);
                checkButton.addActionListener(e1 -> {
                    // 点击按钮时显示输入的数据
                    String email = fofaEmail.getText();
                    String key = fofaKey.getText();
                    String fofaUrl_str = fofaUrl.getText();

                    // https://fofa.info/api/v1/info/my?email=
                    String authUrl = fofaUrl_str + "/api/v1/info/my?email=" + email + "&key=" + key;

                    HttpClient httpClient = HttpClientBuilder.create().build();
                    HttpGet request = new HttpGet(authUrl);

                    try {
                        HttpResponse response = httpClient.execute(request);
                        HttpEntity entity = response.getEntity();
                        String responseBody = EntityUtils.toString(entity);

                        // 解析JSON数据
                        JSONObject json = new JSONObject(responseBody);

                        if (!json.getBoolean("error")) {
                            // 账户验证有效
                            StringBuilder output = new StringBuilder();
                            output.append("账户验证有效\n");
                            output.append("邮箱地址: ").append(json.getString("email")).append("\n");
                            output.append("用户名: ").append(json.getString("username")).append("\n");

                            if (json.getBoolean("isvip")) {
                                output.append("身份权限：FOFA会员\n");
                            } else {
                                output.append("身份权限：普通用户\n");
                            }
                            ;
                            output.append("F点数量: ").append(json.getInt("fofa_point")).append("\n");
                            output.append("API月度剩余查询次数: ").append(json.getInt("remain_api_query")).append("\n");
                            output.append("API月度剩余返回数量: ").append(json.getInt("remain_api_data")).append("\n");
                            JOptionPane.showMessageDialog(null, output.toString());
                        } else {
                            // 账户验证无效
                            JOptionPane.showMessageDialog(null, "账户验证无效！", "提示", JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (IOException | JSONException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "发生错误，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                });

                // 创建"保存设置"按钮
                JButton saveSettingsButton = new JButton("保存设置");
                saveSettingsButton.setFocusPainted(false); // 取消焦点边框的绘制
                saveSettingsButton.setFocusable(false);
                saveSettingsButton.addActionListener(SaveError -> {
                    // 获取文本框中的值
                    String emailValue = fofaEmail.getText();
                    String keyValue = fofaKey.getText();

                    // 准备写入到文件的内容
                    String contentToWrite = "fofaEmail:" + emailValue + "\n" +
                            "fofaKey:" + keyValue + "\n";

                    File rulesFile = new File(accountsPath);
                    try {
                        // 如果文件不存在，则创建新文件
                        if (!rulesFile.exists()) {
                            rulesFile.createNewFile();
                        }

                        // 写入内容到文件，使用 try-with-resources 自动关闭 FileWriter
                        try (FileWriter writer = new FileWriter(rulesFile, false)) {
                            writer.write(contentToWrite);
                            JOptionPane.showMessageDialog(null, "设置已保存。");
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "保存设置时发生错误！", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                });

                // 添加组件到设置面板
                settingsPanel.add(new JLabel("FOFA URL:"));
                settingsPanel.add(fofaUrl);
                settingsPanel.add(new JLabel("Email:"));
                settingsPanel.add(fofaEmail);
                settingsPanel.add(new JLabel("API Key:"));
                settingsPanel.add(fofaKey);
                settingsPanel.add(checkButton);
                settingsPanel.add(saveSettingsButton);

                // 添加设置面板到设置窗口，并显示设置窗口
                settingsFrame.add(settingsPanel);
                settingsFrame.pack();
                settingsFrame.setLocationRelativeTo(null); // 使窗口居中显示
                settingsFrame.setResizable(false);
                settingsFrame.setVisible(true);
            }
        });

        // 创建"搜索设置"菜单项
        JMenu configureMenu = new JMenu("查询设置");
        JMenuItem configureMenuItem = new JMenuItem("默认查询数量");

        configureMenu.add(configureMenuItem);
        menuBar.add(configureMenu);
        configureMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 创建一个JTextField 初始化为 sizeSetting 的值
                JTextField inputField = new JTextField(String.valueOf(sizeSetting));
                int result = JOptionPane.showConfirmDialog(null, inputField,
                        "请输入默认查询数量", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    try {
                        // 尝试将输入的文本解析为整数并更新 sizeSetting
                        sizeSetting = Integer.parseInt(inputField.getText());
                    } catch (NumberFormatException ex) {
                        // 输入的不是有效的整数，可以在这里处理错误
                        JOptionPane.showMessageDialog(null, "请输入一个有效的整数值");
                    }
                }
            }
        });


        JMenu labMenu = new JMenu("实验功能");
        JMenuItem iconHashlabMenuItem = new JMenuItem("iconHash 计算");
        JMenuItem freeGetMenuItem = new JMenuItem("低速模式（暂未开放）");
        JMenuItem openFileMenuItem = new JMenuItem("打开文件");
        labMenu.add(iconHashlabMenuItem);
        labMenu.add(freeGetMenuItem);
        menuBar.add(labMenu);
        labMenu.add(openFileMenuItem);

        iconHashlabMenuItem.addActionListener((ActionEvent event) -> {
            EventQueue.invokeLater(() -> {
                IconHashCalculator calculator = new IconHashCalculator();
                calculator.setVisible(true);
            });
        });

        freeGetMenuItem.addActionListener((ActionEvent event) -> {
            EventQueue.invokeLater(() -> {
                FofaPlugin.main();
            });
        });

        openFileMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                // 如果存在上次打开的路径，则设置文件选择器的当前目录
                if (lastOpenedPath != null) {
                    fileChooser.setCurrentDirectory(lastOpenedPath);
                }

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    // 更新 lastOpenedPath 为当前选择的文件或文件夹
                    lastOpenedPath = fileChooser.getCurrentDirectory();
                    // 调用方法来处理文件
                    loadFileIntoTable(file,panel6,table);
                }
            }
        });

        // 创建"关于"菜单项
        JMenu aboutMenu = new JMenu("关于");
        JMenuItem aboutMenuItem = new JMenuItem("关于项目");

        aboutMenu.add(aboutMenuItem);
        menuBar.add(aboutMenu);

        // 为"关于项目"菜单项添加动作监听器
        aboutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JEditorPane editorPane = new JEditorPane("text/html", "");
                editorPane.setText(
                        "<html><body>" +
                                "<b>fofa EX:</b><br>" +
                                "Project: <a href='https://github.com/10cks/fofaEX'>https://github.com/10cks/fofaEX</a><br>" +
                                "Author: bwner@OverSpace<br>" +
                                "version: 1.0<br>" +
                                "Update: 2023.12.11" +
                                "</body></html>"
                );
                editorPane.setEditable(false);
                editorPane.setOpaque(false);
                editorPane.addHyperlinkListener(new HyperlinkListener() {
                    @Override
                    public void hyperlinkUpdate(HyperlinkEvent evt) {
                        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                            try {
                                Desktop.getDesktop().browse(evt.getURL().toURI());
                            } catch (IOException | URISyntaxException ex) {
                                JOptionPane.showMessageDialog(null,
                                        "无法打开链接，错误: " + ex.getMessage(),
                                        "错误",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                });

                // 弹出一个包含JEditorPane的消息对话框
                JOptionPane.showMessageDialog(null, new JScrollPane(editorPane),
                        "关于项目", JOptionPane.PLAIN_MESSAGE);
            }
        });

        // 在JFrame中添加菜单栏
        jFrame.setJMenuBar(menuBar);

    }

    private static JTextField createTextField(String text) {
        JTextField textField = new JTextField(text, 20);
        textField.setPreferredSize(new Dimension(200, 20));

        // 创建只有底边的边框
        Border blueBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.RED);
        Border defaultBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY);

        // 设置默认边框
        textField.setBorder(defaultBorder);

        // 添加鼠标监听器
        textField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // 鼠标进入时，设置边框颜色为蓝色
                textField.setBorder(blueBorder);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // 鼠标离开时，将边框颜色设回默认颜色
                textField.setBorder(defaultBorder);
            }
        });

        return textField;
    }

    private static JTextField createTextFieldFofa(String text) {
        RoundJTextField textField = new RoundJTextField(0);
        textField.setText(text);
        textField.setPreferredSize(new Dimension(800, 50));

        // 设置文本与边框的间距
        textField.setMargin(new Insets(0, 10, 0, 5));

        // 创建只有底边的边框
        Border blueBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.RED);
        Border defaultBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY);

        // 设置默认边框
        // textField.setBorder(defaultBorder);

        return textField;
    }

    private static void createLogicAddButton(String buttonText, String appendText, JPanel panel, JTextField textField) {
        // 创建按钮
        JButton button = new JButton(buttonText);
        button.setFocusPainted(false); // 不显示按钮焦点外边框
        button.setFocusable(false); // 禁止按钮获取焦点

        // 添加点击事件
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (textField.getText().contains("fofaEX: FOFA Extension")) {
                    textField.setText("");
                }
                // 追加指定文本到文本框中
                textField.setText(textField.getText() + " " + appendText);
            }
        });
        // 将按钮添加到指定面板中
        panel.add(button);
    }

    private static void searchButton(String buttonText, boolean shouldSetSize, JPanel panel, JTextField textField, JTextField emailField, JTextField keyField, JTextField urlField, JPanel resultPanel, JPanel exportPanel, JLabel changeIcon, JPanel disablePanel2, JPanel disablePanel3, JPanel disablePanel7, JPanel totalPanel8, String pageButton) {

        JButton button = new JButton(buttonText);
        button.setFocusPainted(false);
        button.setFocusable(false);

        if (shouldSetSize) {
            button.setPreferredSize(new Dimension(60, 50));
        }
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                String domain = urlField.getText().trim();
                String email = emailField.getText().trim();
                String key = keyField.getText().trim();
                String grammar = textField.getText().trim();

                String orginIconStr = changeIcon.getText();

//                String searchAsciiIcon = "                ____                                 _       _                             \n" +
//                        "               / ___|    ___    __ _   _ __    ___  | |__   (_)  _ __     __ _             \n" +
//                        "               \\___ \\   / _ \\  / _` | | '__|  / __| | '_ \\  | | | '_ \\   / _` |            \n" +
//                        "                ___) | |  __/ | (_| | | |    | (__  | | | | | | | | | | | (_| |  \n" +
//                        "               |____/   \\___|  \\__,_| |_|     \\___| |_| |_| |_| |_| |_|  \\__, | \n" ;
//
//
//                changeIcon.setText("<html><pre>" + searchAsciiIcon + "</pre></html>");
                changeIcon.setText(" FOFA EX");
                changeIcon.setForeground(new Color(89, 154, 248)); // 设置文本颜色为红色
                Font font = new Font("Times New Roman", Font.BOLD, 60);
                changeIcon.setFont(font);

                setComponentsEnabled(disablePanel2, false);
                setComponentsEnabled(disablePanel3, false);
                setComponentsEnabled(disablePanel7, false);


                // 创建 SwingWorker 来处理搜索任务
                SwingWorker<SearchResults, Void> worker = new SwingWorker<SearchResults, Void>() {

                    private void fontSet(JLabel changeIcon, String originIconStr) {
                        changeIcon.setText(originIconStr);
                        changeIcon.setForeground(new Color(48, 49, 52));
                    }

                    @Override
                    protected SearchResults doInBackground() throws Exception {

                        SearchResults results = new SearchResults();
                        QueryResponse queryResponse = new QueryResponse();
                        String errorMessage = null; // 用于存储错误消息
                        errorMessage = queryResponse.errmsg; // 存储错误消息以便后面使用
                        String fieldsTotal = "";

                        long startTime = System.nanoTime();
                        try {

                            String query = grammar;
                            if (query.equals("fofaEX: FOFA Extension")) {
                                query = ""; // 将字符串设置为空
                            }


                            if (protocolMark) {

                                fieldsTotal += ",protocol";

                            }

                            if (titleMark) {

                                fieldsTotal += ",title";
                            }

                            if (domainMark) {

                                fieldsTotal += ",domain";
                            }

                            if (linkMark) {

                                fieldsTotal += ",link";
                            }

                            if (icpMark) {

                                fieldsTotal += ",icp";
                            }

                            if (cityMark) {

                                fieldsTotal += ",city";
                            }

                            if (countryMark) {

                                fieldsTotal += ",country";
                            }
                            if (country_nameMark) {

                                fieldsTotal += ",country_name";
                            }
                            if (regionMark) {

                                fieldsTotal += ",region";
                            }
                            if (longitudeMark) {
                                fieldsTotal += ",longitude";
                            }
                            if (latitudeMark) {
                                fieldsTotal += ",latitude";
                            }
                            if (asNumberMark) {
                                fieldsTotal += ",as_number";
                            }
                            if (asOrganizationMark) {
                                fieldsTotal += ",as_organization";
                            }
                            if (osMark) {
                                fieldsTotal += ",os";
                            }
                            if (serverMark) {
                                fieldsTotal += ",server";
                            }
                            if (jarmMark) {
                                fieldsTotal += ",jarm";
                            }
                            if (headerMark) {
                                fieldsTotal += ",header";
                            }
                            if (bannerMark) {
                                fieldsTotal += ",banner";
                            }
                            if (baseProtocolMark) {
                                fieldsTotal += ",base_protocol";
                            }
                            if (certsIssuerOrgMark) {
                                fieldsTotal += ",certs_issuer_org";
                            }
                            if (certsIssuerCnMark) {
                                fieldsTotal += ",certs_issuer_cn";
                            }
                            if (certsSubjectOrgMark) {
                                fieldsTotal += ",certs_subject_org";
                            }
                            if (certsSubjectCnMark) {
                                fieldsTotal += ",certs_subject_cn";
                            }
                            if (tlsJa3sMark) {
                                fieldsTotal += ",tls_ja3s";
                            }
                            if (tlsVersionMark) {
                                fieldsTotal += ",tls_version";
                            }
                            if (productMark) {
                                fieldsTotal += ",product";
                            }
                            if (productCategoryMark) {
                                fieldsTotal += ",product_category";
                            }
                            if (versionMark) {
                                fieldsTotal += ",version";
                            }
                            if (lastupdatetimeMark) {
                                fieldsTotal += ",lastupdatetime";
                            }
                            if (cnameMark) {
                                fieldsTotal += ",cname";
                            }
                            if (iconHashMark) {
                                fieldsTotal += ",icon_hash";
                            }
                            if (certsValidMark) {
                                fieldsTotal += ",certs_valid";
                            }
                            if (cnameDomainMark) {
                                fieldsTotal += ",cname_domain";
                            }
                            if (bodyMark) {
                                fieldsTotal += ",body";
                            }
                            if (iconMark) {
                                fieldsTotal += ",icon";
                            }
                            if (fidMark) {
                                fieldsTotal += ",fid";
                            }
                            if (structinfoMark) {
                                fieldsTotal += ",structinfo";
                            }

                            // 创建字典
                            Map<String, Boolean> marks = new LinkedHashMap<>();
                            marks.put("host", ipMark);
                            marks.put("ip", ipMark);
                            marks.put("port", portMark);
                            marks.put("protocol", protocolMark);
                            marks.put("title", titleMark);
                            marks.put("domain", domainMark);
                            marks.put("link", linkMark);
                            marks.put("icp", icpMark);
                            marks.put("city", cityMark);
                            marks.put("country", countryMark);
                            marks.put("country_name", country_nameMark);
                            marks.put("region", regionMark);
                            marks.put("longitude", longitudeMark);
                            marks.put("latitude", latitudeMark);
                            marks.put("asNumber", asNumberMark);
                            marks.put("asOrganization", asOrganizationMark);
                            marks.put("os", osMark);
                            marks.put("server", serverMark);
                            marks.put("jarm", jarmMark);
                            marks.put("header", headerMark);
                            marks.put("banner", bannerMark);
                            marks.put("baseProtocol", baseProtocolMark);
                            marks.put("certsIssuerOrg", certsIssuerOrgMark);
                            marks.put("certsIssuerCn", certsIssuerCnMark);
                            marks.put("certsSubjectOrg", certsSubjectOrgMark);
                            marks.put("certsSubjectCn", certsSubjectCnMark);
                            marks.put("tlsJa3s", tlsJa3sMark);
                            marks.put("tlsVersion", tlsVersionMark);
                            marks.put("product", productMark);
                            marks.put("productCategory", productCategoryMark);
                            marks.put("version", versionMark);
                            marks.put("lastupdatetime", lastupdatetimeMark);
                            marks.put("cname", cnameMark);
                            marks.put("iconHash", iconHashMark);
                            marks.put("certsValid", certsValidMark);
                            marks.put("cnameDomain", cnameDomainMark);
                            marks.put("body", bodyMark);
                            marks.put("icon", iconMark);
                            marks.put("fid", fidMark);
                            marks.put("structinfo", structinfoMark);

                            // 标记为真的放在一起
                            List<String> trueMarks = extractTrueMarks(marks);
                            System.out.println(trueMarks);


                            if (pageButton.equals("left")) {
                                if (currentPage != 0) {
                                    currentPage = currentPage - 1;
                                } else {

                                }
                                currentPage = 1;

                            } else if (pageButton.equals("right")) {

                                currentPage = currentPage + 1;
                            }
                            // 开始查询
                            JSONObject jsonResponse = FofaAPI.getAllJsonResult(domain, email, key, query, fieldsTotal, sizeSetting, currentPage);
                            // 检查错误信息
                            queryResponse.error = (boolean) FofaAPI.getValueFromJson(jsonResponse, "error");
                            queryResponse.errmsg = (String) FofaAPI.getValueFromJson(jsonResponse, "errmsg");
                            // 取出整体 results
                            queryResponse.results = (List<List<String>>) FofaAPI.getValueFromJson(jsonResponse, "results");

                            if (queryResponse.error) {
                                throw new Exception(queryResponse.errmsg);
                            }

                            List<List<String>> allShow = queryResponse.results;
                            // 需要放在异常后面

//                            currentPage = (int) FofaAPI.getValueFromJson(jsonResponse, "page");

                            queryTotalNumber = (int) FofaAPI.getValueFromJson(jsonResponse, "size");

                            List<String> hostShow = FofaAPI.getColumn(allShow, 0);

                            numberOfItems = hostShow.size();
                            int i = 0;
                            for (String mark : trueMarks) {
                                switch (mark) {
                                    case "host":
                                        results.host = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "ip":
                                        results.ip = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "protocol":
                                        results.protocol = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "port":
                                        results.port = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "title":
                                        results.title = decodeHtmlEntities(FofaAPI.getColumn(allShow, i));
                                        break;
                                    case "domain":
                                        results.domain = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "link":
                                        results.link = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "icp":
                                        results.icp = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "city":
                                        results.city = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "country":
                                        results.country = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "country_name":
                                        results.country_name = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "region":
                                        results.region = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "longitude":
                                        results.longitude = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "latitude":
                                        results.latitude = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "asNumber":
                                        results.asNumber = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "asOrganization":
                                        results.asOrganization = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "os":
                                        results.os = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "server":
                                        results.server = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "jarm":
                                        results.jarm = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "header":
                                        results.header = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "banner":
                                        results.banner = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "baseProtocol":
                                        results.baseProtocol = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "certsIssuerOrg":
                                        results.certsIssuerOrg = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "certsIssuerCn":
                                        results.certsIssuerCn = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "certsSubjectOrg":
                                        results.certsSubjectOrg = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "certsSubjectCn":
                                        results.certsSubjectCn = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "tlsJa3s":
                                        results.tlsJa3s = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "tlsVersion":
                                        results.tlsVersion = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "product":
                                        results.product = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "productCategory":
                                        results.productCategory = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "version":
                                        results.version = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "lastupdatetime":
                                        results.lastupdatetime = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "cname":
                                        results.cname = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "iconHash":
                                        results.iconHash = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "certsValid":
                                        results.certsValid = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "cnameDomain":
                                        results.cnameDomain = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "body":
                                        results.body = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "icon":
                                        results.icon = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "fid":
                                        results.fid = FofaAPI.getColumn(allShow, i);
                                        break;
                                    case "structinfo":
                                        results.structinfo = FofaAPI.getColumn(allShow, i);
                                        break;
                                }
                                i = i + 1;
                            }

                            // 导出表格
                            JButton exportButton = new JButton("Export to Excel");
                            exportButton.setFocusPainted(false); // 添加这一行来取消焦点边框的绘制
                            exportButton.setFocusable(false);  // 禁止了按钮获取焦点，因此按钮不会在被点击后显示为"激活"或"选中"的状态


                            if (!exportButtonAdded) {
                                exportPanel.add(exportButton);
                                exportButtonAdded = true;
                            }
                            exportButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    // 在这里检查 table 是否被初始化
                                    if (table == null) {
                                        JOptionPane.showMessageDialog(null, "表格没有被初始化");
                                        fontSet(changeIcon, orginIconStr);
                                        setComponentsEnabled(disablePanel2, true);
                                        setComponentsEnabled(disablePanel3, true);
                                        setComponentsEnabled(disablePanel7, true);
                                        return;
                                    }
                                    // 检查 table 是否有模型和数据
                                    if (table.getModel() == null || table.getModel().getRowCount() <= 0) {
                                        JOptionPane.showMessageDialog(null, "当前无数据");
                                        fontSet(changeIcon, orginIconStr);
                                        setComponentsEnabled(disablePanel2, true);
                                        setComponentsEnabled(disablePanel3, true);
                                        setComponentsEnabled(disablePanel7, true);
                                        return;
                                    }
                                    exportTableToExcel(table);
                                }
                            });

                        } catch (JSONException ex) {
                            currentPage = 1;
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "发生错误，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
                            fontSet(changeIcon, orginIconStr);
                            setComponentsEnabled(disablePanel2, true);
                            setComponentsEnabled(disablePanel3, true);
                            setComponentsEnabled(disablePanel7, true);
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(null, errorMessage != null ? errorMessage : e.getMessage(), "执行失败", JOptionPane.ERROR_MESSAGE);
                            currentPage = 1;
                            fontSet(changeIcon, orginIconStr);
                            setComponentsEnabled(disablePanel2, true);
                            setComponentsEnabled(disablePanel3, true);
                            setComponentsEnabled(disablePanel7, true);
                            throw new RuntimeException(e);
                        } finally {
                            long endTime = System.nanoTime(); // Stop the timer
                            long duration = endTime - startTime;
                            double seconds = (double) duration / 1_000_000_000.0; // Convert nanoseconds to seconds
                            SwingUtilities.invokeLater(() -> {
                                String totalLabelShow = "<html>" + "Page: " + currentPage + "<br>" + "Items/Totals: " + numberOfItems * currentPage + "/" + queryTotalNumber + "<br>Time: " + seconds + " seconds</html>";
                                if (!timeAdded) {
                                    // 只有当timeLabel为null时才创建新的实例
                                    if (timeLabel == null) {
                                        timeLabel = new JLabel();
                                    }
                                    timeLabel.setText(totalLabelShow);
                                    totalPanel8.add(timeLabel);
                                    timeAdded = true;
                                } else {
                                    if (timeLabel != null) { // 确保timeLabel不为null
                                        timeLabel.setText(totalLabelShow);
                                    }
                                }
                                totalPanel8.revalidate();
                                totalPanel8.repaint();
                            });
                        }

                        fontSet(changeIcon, orginIconStr);
                        setComponentsEnabled(disablePanel2, true);
                        setComponentsEnabled(disablePanel3, true);
                        setComponentsEnabled(disablePanel7, true);

                        return results;
                    }

                    @Override
                    protected void done() {
                        try {
                            SearchResults searchResults = get();
                            assert searchResults != null;
                            showResultsInTable(
                                    searchResults.host,
                                    searchResults.ip,
                                    searchResults.port,
                                    searchResults.protocol,
                                    searchResults.title,
                                    searchResults.domain,
                                    searchResults.link,
                                    searchResults.icp,
                                    searchResults.city,
                                    searchResults.country,
                                    searchResults.country_name,
                                    searchResults.region,
                                    searchResults.longitude,
                                    searchResults.latitude,
                                    searchResults.asNumber,
                                    searchResults.asOrganization,
                                    searchResults.os,
                                    searchResults.server,
                                    searchResults.jarm,
                                    searchResults.header,
                                    searchResults.banner,
                                    searchResults.baseProtocol,
                                    searchResults.certsIssuerOrg,
                                    searchResults.certsIssuerCn,
                                    searchResults.certsSubjectOrg,
                                    searchResults.certsSubjectCn,
                                    searchResults.tlsJa3s,
                                    searchResults.tlsVersion,
                                    searchResults.product,
                                    searchResults.productCategory,
                                    searchResults.version,
                                    searchResults.lastupdatetime,
                                    searchResults.cname,
                                    searchResults.iconHash,
                                    searchResults.certsValid,
                                    searchResults.cnameDomain,
                                    searchResults.body,
                                    searchResults.icon,
                                    searchResults.fid,
                                    searchResults.structinfo,
                                    resultPanel
                            );

                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
                // 启动 SwingWorker
                worker.execute();
            }
        });
        panel.add(button);
    }

    private static void showResultsInTable(List<String> host, List<String> tableIpShow, List<String> tablePortShow, List<String> protocolShow, List<String> titleShow, List<String> domainShow, List<String> linkShow, List<String> icpShow, List<String> cityShow, List<String> countryShow, List<String> country_nameShow, List<String> regionShow, List<String> longitudeShow, List<String> latitudeShow, List<String> asNumberShow, List<String> asOrganizationShow, List<String> osShow, List<String> serverShow, List<String> jarmShow, List<String> headerShow, List<String> bannerShow, List<String> baseProtocolShow, List<String> certsIssuerOrgShow, List<String> certsIssuerCnShow, List<String> certsSubjectOrgShow, List<String> certsSubjectCnShow, List<String> tlsJa3sShow, List<String> tlsVersionShow, List<String> productShow, List<String> productCategoryShow, List<String> versionShow, List<String> lastupdatetimeShow, List<String> cnameShow, List<String> iconHashShow, List<String> certsValidShow, List<String> cnameDomainShow, List<String> bodyShow, List<String> iconShow, List<String> fidShow, List<String> structinfoShow, JPanel panel) {
        List<String> columnNamesList = new ArrayList<String>(List.of("host"));

        if (structinfoMark) {
            columnNamesList.add(1, "structinfo");
        }
        if (fidMark) {
            columnNamesList.add(1, "fid");
        }
        if (iconMark) {
            columnNamesList.add(1, "icon");
        }
        if (bodyMark) {
            columnNamesList.add(1, "body");
        }
        if (cnameDomainMark) {
            columnNamesList.add(1, "cnameDomain");
        }
        if (certsValidMark) {
            columnNamesList.add(1, "certsValid");
        }
        if (iconHashMark) {
            columnNamesList.add(1, "iconHash");
        }
        if (cnameMark) {
            columnNamesList.add(1, "cname");
        }
        if (lastupdatetimeMark) {
            columnNamesList.add(1, "lastupdatetime");
        }
        if (versionMark) {
            columnNamesList.add(1, "version");
        }
        if (productCategoryMark) {
            columnNamesList.add(1, "productCategory");
        }
        if (productMark) {
            columnNamesList.add(1, "product");
        }
        if (tlsVersionMark) {
            columnNamesList.add(1, "tlsVersion");
        }
        if (tlsJa3sMark) {
            columnNamesList.add(1, "tlsJa3s");
        }
        if (certsSubjectCnMark) {
            columnNamesList.add(1, "certsSubjectCn");
        }
        if (certsSubjectOrgMark) {
            columnNamesList.add(1, "certsSubjectOrg");
        }
        if (certsIssuerCnMark) {
            columnNamesList.add(1, "certsIssuerCn");
        }
        if (certsIssuerOrgMark) {
            columnNamesList.add(1, "certsIssuerOrg");
        }
        if (baseProtocolMark) {
            columnNamesList.add(1, "baseProtocol");
        }
        if (bannerMark) {
            columnNamesList.add(1, "banner");
        }
        if (headerMark) {
            columnNamesList.add(1, "header");
        }
        if (jarmMark) {
            columnNamesList.add(1, "jarm");
        }
        if (serverMark) {
            columnNamesList.add(1, "server");
        }
        if (osMark) {
            columnNamesList.add(1, "os");
        }
        if (asOrganizationMark) {
            columnNamesList.add(1, "asOrganization");
        }
        if (asNumberMark) {
            columnNamesList.add(1, "asNumber");
        }
        if (latitudeMark) {
            columnNamesList.add(1, "latitude");
        }
        if (longitudeMark) {
            columnNamesList.add(1, "longitude");
        }
        if (regionMark) {
            columnNamesList.add(1, "region");
        }

        if (country_nameMark) {
            columnNamesList.add(1, "country_name");
        }

        if (countryMark) {
            columnNamesList.add(1, "country");
        }

        if (cityMark) {
            columnNamesList.add(1, "city");
        }

        if (icpMark) {
            columnNamesList.add(1, "icp");
        }

        if (linkMark) {
            columnNamesList.add(1, "link");
        }

        if (domainMark) {
            columnNamesList.add(1, "domain");
        }

        if (titleMark) {
            columnNamesList.add(1, "title");
        }

        if (protocolMark) {
            columnNamesList.add(1, "protocol");
        }

        if (portMark) {
            columnNamesList.add(1, "port");
        }

        if (ipMark) {
            columnNamesList.add(1, "ip");
        }

        String[] columnNames = columnNamesList.toArray(new String[0]);
        Object[][] data = new Object[host.size()][columnNames.length];

        for (int i = 0; i < host.size(); i++) {
            data[i][0] = host.get(i);

            int columnIndex = 1;

            if (ipMark && tableIpShow.size() > i) {
                data[i][columnIndex++] = tableIpShow.get(i);
            }

            if (portMark && tablePortShow.size() > i) {
                data[i][columnIndex++] = tablePortShow.get(i);
            }
            if (protocolMark && protocolShow.size() > i) {
                data[i][columnIndex++] = protocolShow.get(i);
            }
            if (titleMark && titleShow.size() > i) {
                data[i][columnIndex++] = titleShow.get(i);
            }
            if (domainMark && domainShow.size() > i) {
                data[i][columnIndex++] = domainShow.get(i);
            }
            if (linkMark && linkShow.size() > i) {
                data[i][columnIndex++] = linkShow.get(i);
            }
            if (icpMark && icpShow.size() > i) {
                data[i][columnIndex++] = icpShow.get(i);
            }
            if (cityMark && cityShow.size() > i) {
                data[i][columnIndex++] = cityShow.get(i);
            }
            if (countryMark && countryShow.size() > i) {
                data[i][columnIndex++] = countryShow.get(i);
            }
            if (country_nameMark && country_nameShow.size() > i) {
                data[i][columnIndex++] = country_nameShow.get(i);
            }
            if (regionMark && regionShow.size() > i) {
                data[i][columnIndex++] = regionShow.get(i);
            }
            if (longitudeMark && longitudeShow.size() > i) {
                data[i][columnIndex++] = longitudeShow.get(i);
            }
            if (latitudeMark && latitudeShow.size() > i) {
                data[i][columnIndex++] = latitudeShow.get(i);
            }
            if (asNumberMark && asNumberShow.size() > i) {
                data[i][columnIndex++] = asNumberShow.get(i);
            }
            if (asOrganizationMark && asOrganizationShow.size() > i) {
                data[i][columnIndex++] = asOrganizationShow.get(i);
            }
            if (osMark && osShow.size() > i) {
                data[i][columnIndex++] = osShow.get(i);
            }
            if (serverMark && serverShow.size() > i) {
                data[i][columnIndex++] = serverShow.get(i);
            }
            if (jarmMark && jarmShow.size() > i) {
                data[i][columnIndex++] = jarmShow.get(i);
            }
            if (headerMark && headerShow.size() > i) {
                data[i][columnIndex++] = headerShow.get(i);
            }
            if (bannerMark && bannerShow.size() > i) {
                data[i][columnIndex++] = bannerShow.get(i);
            }
            if (baseProtocolMark && baseProtocolShow.size() > i) {
                data[i][columnIndex++] = baseProtocolShow.get(i);
            }
            if (certsIssuerOrgMark && certsIssuerOrgShow.size() > i) {
                data[i][columnIndex++] = certsIssuerOrgShow.get(i);
            }
            if (certsIssuerCnMark && certsIssuerCnShow.size() > i) {
                data[i][columnIndex++] = certsIssuerCnShow.get(i);
            }
            if (certsSubjectOrgMark && certsSubjectOrgShow.size() > i) {
                data[i][columnIndex++] = certsSubjectOrgShow.get(i);
            }
            if (certsSubjectCnMark && certsSubjectCnShow.size() > i) {
                data[i][columnIndex++] = certsSubjectCnShow.get(i);
            }
            if (tlsJa3sMark && tlsJa3sShow.size() > i) {
                data[i][columnIndex++] = tlsJa3sShow.get(i);
            }
            if (tlsVersionMark && tlsVersionShow.size() > i) {
                data[i][columnIndex++] = tlsVersionShow.get(i);
            }
            if (productMark && productShow.size() > i) {
                data[i][columnIndex++] = productShow.get(i);
            }
            if (productCategoryMark && productCategoryShow.size() > i) {
                data[i][columnIndex++] = productCategoryShow.get(i);
            }
            if (versionMark && versionShow.size() > i) {
                data[i][columnIndex++] = versionShow.get(i);
            }
            if (lastupdatetimeMark && lastupdatetimeShow.size() > i) {
                data[i][columnIndex++] = lastupdatetimeShow.get(i);
            }
            if (cnameMark && cnameShow.size() > i) {
                data[i][columnIndex++] = cnameShow.get(i);
            }
            if (iconHashMark && iconHashShow.size() > i) {
                data[i][columnIndex++] = iconHashShow.get(i);
            }
            if (certsValidMark && certsValidShow.size() > i) {
                data[i][columnIndex++] = certsValidShow.get(i);
            }
            if (cnameDomainMark && cnameDomainShow.size() > i) {
                data[i][columnIndex++] = cnameDomainShow.get(i);
            }
            if (bodyMark && bodyShow.size() > i) {
                data[i][columnIndex++] = bodyShow.get(i);
            }
            if (iconMark && iconShow.size() > i) {
                data[i][columnIndex++] = iconShow.get(i);
            }
            if (fidMark && fidShow.size() > i) {
                data[i][columnIndex++] = fidShow.get(i);
            }
            if (structinfoMark && structinfoShow.size() > i) {
                data[i][columnIndex++] = structinfoShow.get(i);
            }
        }
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        table.setModel(model);

        // 重新设置表格头，以便新的渲染器生效
        JTableHeader header = getjTableHeader(table);
        table.setTableHeader(header);

        adjustColumnWidths(table); // 自动调整列宽
        JScrollPane scrollPane = new JScrollPane(table);

        table.setRowHeight(24); // 设置表格的行高
        table.setFillsViewportHeight(true);

        panel.removeAll();
        panel.add(scrollPane, CENTER);
        panel.revalidate();
        panel.repaint();

        // 下面的代码将确保 table 被正确地初始化和更新
        if (table == null) {
            table = new JTable(model);
        } else {
            table.setModel(model);
        }

        // 设置表格的默认渲染器
        table.setDefaultRenderer(Object.class, new SelectedCellBorderHighlighter());
        // 添加右键鼠标事件
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    if (row >= 0 && col >= 0) {
                        // 右键显示弹出菜单
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
    }

    // 账户设置
    static public void settingInit(BufferedReader rules, BufferedReader accounts, JPanel initPanel, JTextField initTextField, JTextField fofaEmail, JTextField fofaKey, Map<String, JButton> initButtonsMap) {


        try {
            String fofaEmailLine = accounts.readLine();
            String fofaKeyLine = accounts.readLine();

            // 检查是否有内容需要解析和赋值
            if (fofaEmailLine != null && fofaKeyLine != null) {
                fofaEmail.setText(fofaEmailLine.split(":")[1]);
                fofaKey.setText(fofaKeyLine.split(":")[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 读取文件内容，并创建新的按钮
        try {
            Map<String, String> newMap = new LinkedHashMap<>();
            String line;
            while ((line = rules.readLine()) != null) {
                line = line.trim();

                // 跳过井号注释
                if (line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("\"") && line.contains("{") && line.contains("}")) {
                    String[] parts = line.split(":", 2);

                    String key = parts[0].substring(1, parts[0].length() - 1).trim();

                    String value = parts[1].substring(1, parts[1].length() - 2).trim();
                    newMap.put(key, value);
                }
            }
            rules.close();

            // 移除按钮
            Iterator<Map.Entry<String, JButton>> iterator = initButtonsMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, JButton> entry = iterator.next();
                if (!newMap.containsKey(entry.getKey())) {
                    initPanel.remove(entry.getValue());
                    iterator.remove();
                }
            }

            // 更新并新增按钮
            for (Map.Entry<String, String> entry : newMap.entrySet()) {
                JButton existingButton = initButtonsMap.get(entry.getKey());
                if (existingButton == null) {
                    // 新按钮
                    JButton newButton = new JButton(entry.getKey());
                    newButton.setActionCommand(entry.getValue());
                    newButton.setToolTipText(entry.getValue()); // 设置按钮的 ToolTip 为键值，悬浮显示
                    newButton.setFocusPainted(false); // 添加这一行来取消焦点边框的绘制
                    newButton.setFocusable(false);  // 禁止了按钮获取焦点，因此按钮不会在被点击后显示为"激活"或"选中"的状态
                    newButton.addActionListener(actionEvent -> {
                        if (newButton.getForeground() != Color.RED) {
                            // 如果文本为提示文字，则清空文本
                            if (initTextField.getText().contains("fofaEX: FOFA Extension")) {
                                initTextField.setText("");
                            }
                            initTextField.setText(initTextField.getText() + " " + newButton.getActionCommand());
                            newButton.setForeground(Color.RED);
                            newButton.setFont(newButton.getFont().deriveFont(Font.BOLD)); // 设置字体为粗体
                        } else {
                            initTextField.setText(initTextField.getText().replace(" " + newButton.getActionCommand(), ""));
                            newButton.setForeground(null);
                            newButton.setFont(null);
                            // 如果为空则设置 prompt
                            if (initTextField.getText().isEmpty()) {
                                initTextField.setText("fofaEX: FOFA Extension");
                                initTextField.setForeground(Color.GRAY);
                                // 将光标放在开头
                                initTextField.setCaretPosition(0);

                            }
                        }
                    });

                    // 添加右键单击事件的处理
                    newButton.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            if (SwingUtilities.isRightMouseButton(e)) {
                                // 在这里处理右键单击事件
                                JPopupMenu popupMenu = new JPopupMenu();
                                JMenuItem deleteItem = new JMenuItem("删除");
                                JMenuItem editItem = new JMenuItem("修改");
                                deleteItem.addActionListener(actionEvent -> {
                                    // 在这里处理删除操作
                                    int dialogResult = JOptionPane.showConfirmDialog(initPanel,
                                            "是否删除?", "删除确认",
                                            JOptionPane.YES_NO_OPTION,
                                            JOptionPane.QUESTION_MESSAGE);
                                    if (dialogResult == JOptionPane.YES_OPTION) {
                                        // 确认删除操作
                                        initPanel.remove(newButton);
                                        initButtonsMap.remove(entry.getKey());
                                        initPanel.revalidate();
                                        initPanel.repaint();
                                        // 从文件中删除
                                        removeButtonAndLineFromFile(entry.getKey(), rulesPath);
                                    }
                                });

                                editItem.addActionListener(actionEvent -> {
                                    // 获取当前按钮的名称和对应的JButton对象
                                    String oldName = entry.getKey();
                                    JButton buttonToUpdate = newButton; // 确保newButton是当前要修改的按钮的引用

                                    // 创建一个JPanel来包含两个输入框
                                    JPanel panel = new JPanel(new GridLayout(4, 4));
                                    panel.add(new JLabel("键名:"));
                                    JTextField nameField = new JTextField(newButton.getText());
                                    panel.add(nameField);
                                    panel.add(new JLabel("键值:"));
                                    JTextField valueField = new JTextField(buttonToUpdate.getActionCommand());
                                    panel.add(valueField);

                                    // 弹出自定义对话框
                                    int result = JOptionPane.showConfirmDialog(initPanel, panel, "修改配置",
                                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                                    // 当用户点击OK时处理输入
                                    if (result == JOptionPane.OK_OPTION) {
                                        String newName = nameField.getText().trim();
                                        String newValue = valueField.getText().trim();

                                        // 验证输入是否已变更且非空
                                        if (!newName.isEmpty() && !newValue.isEmpty()) {
                                            // 修改按钮名称和键值
                                            updateButtonNameAndValue(oldName, newName, newValue, buttonToUpdate, initButtonsMap, rulesPath);

                                            // 更新界面
                                            initPanel.revalidate();
                                            initPanel.repaint();
                                        }
                                    }
                                });

                                popupMenu.add(editItem);
                                popupMenu.add(deleteItem);

                                popupMenu.show(e.getComponent(), e.getX(), e.getY());

                                popupMenu.add(deleteItem);
                                popupMenu.show(e.getComponent(), e.getX(), e.getY());
                            }
                        }
                    });


                    initPanel.add(newButton);
                    initButtonsMap.put(entry.getKey(), newButton);
                } else {
                    // This is an existing button
                    existingButton.setActionCommand(entry.getValue());
                    existingButton.setText(entry.getKey()); // Update button text
                }
            }
            initPanel.revalidate();
            initPanel.repaint();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    // 按钮修改
    public static void updateButtonNameAndValue(String oldName, String newName, String newValue, JButton buttonToUpdate, Map<String, JButton> buttonsMap, String filePath) {
        // 更新按钮名称和命令
        buttonToUpdate.setText(newName);
        buttonToUpdate.setActionCommand(newValue);
        buttonsMap.remove(oldName);
        buttonsMap.put(newName, buttonToUpdate);

        // 更新rules.txt文件中的内容
        File inputFile = new File(filePath);
        File tempFile = new File(inputFile.getAbsolutePath() + ".tmp");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String lineToReplace = "\"" + oldName + "\":";
            String currentLine;

            while ((currentLine = reader.readLine()) != null) {
                // 检查当前行是否包含旧键名
                if (currentLine.trim().startsWith(lineToReplace)) {
                    // 替换整行为新键名和新键值
                    currentLine = "\"" + newName + "\":{" + newValue + "},";
                }
                writer.write(currentLine + System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 删除原始文件，并将临时文件重命名为原始文件名
        if (!inputFile.delete()) {
            System.out.println("Could not delete file");
            return;
        }
        if (!tempFile.renameTo(inputFile)) {
            System.out.println("Could not rename file");
        }
    }

    // 按钮删除
    public static void removeButtonAndLineFromFile(String buttonName, String filePath) {
        File inputFile = new File(filePath);
        File tempFile = new File(inputFile.getAbsolutePath() + ".tmp");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String lineToRemove = "\"" + buttonName + "\":";
            String currentLine;

            while ((currentLine = reader.readLine()) != null) {
                // trim newline when comparing with lineToRemove
                String trimmedLine = currentLine.trim();
                if (trimmedLine.startsWith(lineToRemove)) continue;
                writer.write(currentLine + System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Delete the original file
        if (!inputFile.delete()) {
            System.out.println("Could not delete file");
            return;
        }

        // Rename the new file to the filename the original file had.
        if (!tempFile.renameTo(inputFile)) {
            System.out.println("Could not rename file");
        }
    }

    private static void exportTableToExcel(JTable table) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Table Data");

        // 创建表头
        XSSFRow headerRow = sheet.createRow(0);
        for (int i = 0; i < table.getColumnCount(); i++) {
            headerRow.createCell(i).setCellValue(table.getColumnName(i));
        }

        // 写入数据行
        for (int i = 0; i < table.getRowCount(); i++) {
            XSSFRow dataRow = sheet.createRow(i + 1);
            for (int j = 0; j < table.getColumnCount(); j++) {
                Object value = table.getValueAt(i, j);
                String text = (value == null) ? "" : value.toString(); // 检查是否为null
                dataRow.createCell(j).setCellValue(text);
            }
        }

        // 将工作簿保存到文件
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timestamp = dateFormat.format(new Date());

            String directoryName = "exportData";
            File directory = new File(directoryName);

            if (!directory.exists()) {
                directory.mkdir();
            }

            String fileName = directoryName + "/TableData_" + timestamp + ".xlsx";
            FileOutputStream output = new FileOutputStream(fileName);
            workbook.write(output);
            workbook.close();
            output.close();
            JOptionPane.showMessageDialog(null, "Export successful!\n File saved at: " + new File(fileName).getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    public static void addRuleBox(JPanel panel, String checkBoxName, RuleMarkChangeCallback callback, Boolean selectMark) {
        // 创建复选框
        JCheckBox newBox = new JCheckBox(checkBoxName);
        newBox.setFocusPainted(false);
        newBox.setSelected(callback != null && callback instanceof RuleMarkChangeCallback);
        newBox.setSelected(selectMark);  // 直接使用 ipMark 的当前值

        // 添加 ItemListener
        newBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                // 使用回调接口来通知外部变量的更改
                if (callback != null) {
                    callback.onRuleMarkChange(e.getStateChange() == ItemEvent.SELECTED);
                }
            }
        });

        // 添加到面板
        panel.add(newBox);
    }

    public static void addRuleBox(JPanel panel, String checkBoxName, RuleMarkChangeCallback callback, Boolean selectMark, Boolean setAlways) {
        // 创建复选框
        JCheckBox newBox = new JCheckBox(checkBoxName);
        newBox.setFocusPainted(false);
        newBox.setSelected(callback != null && callback instanceof RuleMarkChangeCallback);
        newBox.setSelected(selectMark);  // 直接使用 ipMark 的当前值
        newBox.setName("noChange");
        if (setAlways) {
            newBox.setSelected(true);   // 始终勾选
            newBox.setEnabled(false);   // 禁用复选框，使其无法修改
        }

        // 添加 ItemListener
        newBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                // 使用回调接口来通知外部变量的更改
                if (callback != null) {
                    callback.onRuleMarkChange(e.getStateChange() == ItemEvent.SELECTED);
                }
            }
        });

        // 添加到面板
        panel.add(newBox);
    }

    // addRuleBox 的回调函数
    public interface RuleMarkChangeCallback {
        void onRuleMarkChange(boolean newValue);
    }

    // 处理 title 实体编码问题
    public static List<String> decodeHtmlEntities(List<String> encodedTitles) {
        return encodedTitles.stream()
                .map(StringEscapeUtils::unescapeHtml4)
                .collect(Collectors.toList());
    }

    private static void setComponentsEnabled(Container container, boolean enabled) {
        for (Component component : container.getComponents()) {
            // 检查组件是否是复选框并且名字为 "host"
            if (component instanceof JCheckBox && "noChange".equals(component.getName())) {
                // 忽略名为 "host" 的复选框，不改变其可用性
                continue;
            }

            component.setEnabled(enabled);

            if (component instanceof Container) {
                setComponentsEnabled((Container) component, enabled);
            }
        }
    }

    private static List<String> extractTrueMarks(Map<String, Boolean> marks) {
        List<String> trueMarks = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : marks.entrySet()) {
            if (entry.getValue()) {
                trueMarks.add(entry.getKey());
            }
        }
        return trueMarks;
    }

}