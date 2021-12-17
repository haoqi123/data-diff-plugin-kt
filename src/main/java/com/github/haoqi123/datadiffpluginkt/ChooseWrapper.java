package com.github.haoqi123.datadiffpluginkt;

import com.intellij.database.psi.DbNamespaceImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ChooseWrapper extends DialogWrapper {
    private JPanel panel1;
    private JButton button2;
    private JButton button1;
    private JButton changeButton;
    private PsiElement[] data;

    public ChooseWrapper(@Nullable Project project) {
        super(project);
        this.setTitle("CHOOSE SOURCE/TARGET");
        setOKButtonText("OK");
        setCancelButtonText("CANCEL");
        addActionListener(changeButton);
        super.init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return panel1;
    }

    public void setData(PsiElement[] data) {
        this.data = data;
        button1.setText(((DbNamespaceImpl) this.data[0]).getName());
        button2.setText(((DbNamespaceImpl) this.data[1]).getName());
    }

    private void addActionListener(JButton saveButton) {
        // 为按钮绑定监听器
        saveButton.addActionListener(e -> {
            String text = button1.getText();
            String text1 = button2.getText();

            button2.setText(text);
            button1.setText(text1);
        });
    }

    public PsiElement[] getPsi() {
        PsiElement[] psiElements = new PsiElement[2];

        String text = button1.getText();
        String name = ((DbNamespaceImpl) this.data[0]).getName();
        if (StringUtils.equals(text, name)) {
            psiElements[0] = this.data[0];
            psiElements[1] = this.data[1];
        } else {
            psiElements[0] = this.data[1];
            psiElements[1] = this.data[0];
        }
        return psiElements;
    }

}
