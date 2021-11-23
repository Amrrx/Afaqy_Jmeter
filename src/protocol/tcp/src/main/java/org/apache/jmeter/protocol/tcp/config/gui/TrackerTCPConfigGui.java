/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.tcp.config.gui;

import java.awt.*;
import java.awt.event.ItemEvent;

import javax.swing.*;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.ServerPanel;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.gui.util.*;
import org.apache.jmeter.protocol.tcp.sampler.TrackerTCPSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

@TestElementMetadata(labelResource = "tracker_tcp_config_title")
public class TrackerTCPConfigGui extends AbstractConfigGui {

    private static final long serialVersionUID = 1240L;

    private ServerPanel serverPanel;

    private JLabeledTextField classname;

    private JCheckBox reUseConnection;

    private TristateCheckBox setNoDelay;

    private TristateCheckBox closeConnection;

    private JTextField soLinger;

    private JTextField eolByte;

    private JSyntaxTextArea requestData;

    private boolean displayName = true;

    private JSyntaxTextArea demoHexSignal;

    public TrackerTCPConfigGui() {
        this(true);
    }

    public TrackerTCPConfigGui(boolean displayName) {
        this.displayName = displayName;
        init();
    }

    @Override
    public String getLabelResource() {
        return "tracker_tcp_config_title"; // $NON-NLS-1$
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        // N.B. this will be a config element, so we cannot use the getXXX() methods
        classname.setText(element.getPropertyAsString(TrackerTCPSampler.CLASSNAME));
        serverPanel.setServer(element.getPropertyAsString(TrackerTCPSampler.SERVER));
        // Default to original behaviour, i.e. re-use connection
        reUseConnection.setSelected(element.getPropertyAsBoolean(TrackerTCPSampler.RE_USE_CONNECTION, TrackerTCPSampler.RE_USE_CONNECTION_DEFAULT));
        serverPanel.setPort(element.getPropertyAsString(TrackerTCPSampler.PORT));
        serverPanel.setResponseTimeout(element.getPropertyAsString(TrackerTCPSampler.TIMEOUT));
        serverPanel.setConnectTimeout(element.getPropertyAsString(TrackerTCPSampler.TIMEOUT_CONNECT));
        setNoDelay.setTristateFromProperty(element, TrackerTCPSampler.NODELAY);
        requestData.setInitialText(element.getPropertyAsString(TrackerTCPSampler.REQUEST));
        requestData.setCaretPosition(0);
        closeConnection.setTristateFromProperty(element, TrackerTCPSampler.CLOSE_CONNECTION);
        soLinger.setText(element.getPropertyAsString(TrackerTCPSampler.SO_LINGER));
        eolByte.setText(element.getPropertyAsString(TrackerTCPSampler.EOL_BYTE));
        demoHexSignal.setInitialText(element.getPropertyAsString(TrackerTCPSampler.SIGNAL));
        demoHexSignal.setCaretPosition(0);
    }

    @Override
    public TestElement createTestElement() {
        ConfigTestElement element = new ConfigTestElement();
        modifyTestElement(element);
        return element;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        // N.B. this will be a config element, so we cannot use the setXXX() methods
        element.setProperty(TrackerTCPSampler.CLASSNAME, classname.getText(), "");
        element.setProperty(TrackerTCPSampler.SERVER, serverPanel.getServer());
        element.setProperty(TrackerTCPSampler.RE_USE_CONNECTION, reUseConnection.isSelected());
        element.setProperty(TrackerTCPSampler.PORT, serverPanel.getPort());
        setNoDelay.setPropertyFromTristate(element, TrackerTCPSampler.NODELAY);
        element.setProperty(TrackerTCPSampler.TIMEOUT, serverPanel.getResponseTimeout());
        element.setProperty(TrackerTCPSampler.TIMEOUT_CONNECT, serverPanel.getConnectTimeout(),"");
        element.setProperty(TrackerTCPSampler.REQUEST, requestData.getText());
        closeConnection.setPropertyFromTristate(element, TrackerTCPSampler.CLOSE_CONNECTION); // Don't use default for saving tristates
        element.setProperty(TrackerTCPSampler.SO_LINGER, soLinger.getText(), "");
        element.setProperty(TrackerTCPSampler.EOL_BYTE, eolByte.getText(), "");
        element.setProperty(TrackerTCPSampler.SIGNAL, demoHexSignal.getText());
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        serverPanel.clear();
        classname.setText(""); //$NON-NLS-1$
        requestData.setInitialText(""); //$NON-NLS-1$
        reUseConnection.setSelected(true);
        setNoDelay.setSelected(false); // TODO should this be indeterminate?
        closeConnection.setSelected(TrackerTCPSampler.CLOSE_CONNECTION_DEFAULT); // TODO should this be indeterminate?
        soLinger.setText(""); //$NON-NLS-1$
        eolByte.setText(""); //$NON-NLS-1$
        demoHexSignal.setText("");
    }


    private JPanel createNoDelayPanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("tcp_nodelay")); // $NON-NLS-1$

        setNoDelay = new TristateCheckBox();
        label.setLabelFor(setNoDelay);

        JPanel nodelayPanel = new JPanel(new FlowLayout());
        nodelayPanel.add(label);
        nodelayPanel.add(setNoDelay);
        return nodelayPanel;
    }

    private JPanel createClosePortPanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("reuseconnection")); //$NON-NLS-1$

        reUseConnection = new JCheckBox("", true);
        reUseConnection.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                closeConnection.setEnabled(true);
            } else {
                closeConnection.setEnabled(false);
            }
        });
        label.setLabelFor(reUseConnection);

        JPanel closePortPanel = new JPanel(new FlowLayout());
        closePortPanel.add(label);
        closePortPanel.add(reUseConnection);
        return closePortPanel;
    }

    private JPanel createCloseConnectionPanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("closeconnection")); // $NON-NLS-1$

        closeConnection = new TristateCheckBox("", TrackerTCPSampler.CLOSE_CONNECTION_DEFAULT);
        label.setLabelFor(closeConnection);

        JPanel closeConnectionPanel = new JPanel(new FlowLayout());
        closeConnectionPanel.add(label);
        closeConnectionPanel.add(closeConnection);
        return closeConnectionPanel;
    }

    private JPanel createSoLingerOption() {
        JLabel label = new JLabel(JMeterUtils.getResString("solinger")); //$NON-NLS-1$

        soLinger = new JTextField(5); // 5 columns size
        soLinger.setMaximumSize(new Dimension(soLinger.getPreferredSize()));
        label.setLabelFor(soLinger);

        JPanel soLingerPanel = new JPanel(new FlowLayout());
        soLingerPanel.add(label);
        soLingerPanel.add(soLinger);
        return soLingerPanel;
    }

    private JPanel createEolBytePanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("eolbyte")); //$NON-NLS-1$

        eolByte = new JTextField(3); // 3 columns size
        eolByte.setMaximumSize(new Dimension(eolByte.getPreferredSize()));
        label.setLabelFor(eolByte);

        JPanel eolBytePanel = new JPanel(new FlowLayout());
        eolBytePanel.add(label);
        eolBytePanel.add(eolByte);
        return eolBytePanel;
    }

    private JPanel createRequestPanel() {
        JLabel imeiLabel = new JLabel(JMeterUtils.getResString("tcp_request_imei")); // $NON-NLS-1$
        requestData = JSyntaxTextArea.getInstance(5, 80);
        requestData.setLanguage("text"); //$NON-NLS-1$
        imeiLabel.setLabelFor(requestData);

        JPanel reqDataPanel = new JPanel(new BorderLayout(5, 0));
        reqDataPanel.setBorder(BorderFactory.createTitledBorder(""));

        reqDataPanel.add(imeiLabel, BorderLayout.WEST);
        reqDataPanel.add(JTextScrollPane.getInstance(requestData), BorderLayout.CENTER);
        return reqDataPanel;
    }

    private JPanel createSignalDataPanel(){
        JLabel signalLabel = new JLabel(JMeterUtils.getResString("tcp_request_signal")); // $NON-NLS-1$
        demoHexSignal = JSyntaxTextArea.getInstance(10, 80);
        demoHexSignal.setLanguage("text");
        signalLabel.setLabelFor(demoHexSignal);

        JPanel signalDataPanel = new JPanel(new BorderLayout(5, 0));
        signalDataPanel.setBorder(BorderFactory.createTitledBorder(""));

        signalDataPanel.add(signalLabel, BorderLayout.WEST);
        signalDataPanel.add(JTextScrollPane.getInstance(demoHexSignal), BorderLayout.CENTER);

        return signalDataPanel;
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));

        serverPanel = new ServerPanel();

        if (displayName) {
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);
        }

        VerticalPanel mainPanel = new VerticalPanel();
        classname = new JLabeledTextField(JMeterUtils.getResString("tracker_protocol_classname")); // $NON-NLS-1$
        mainPanel.add(classname);
        mainPanel.add(serverPanel);

        HorizontalPanel optionsPanel = new HorizontalPanel();
        optionsPanel.setBorder(BorderFactory.createTitledBorder(""));
        optionsPanel.add(createClosePortPanel());
        optionsPanel.add(createCloseConnectionPanel());
        optionsPanel.add(createNoDelayPanel());
        optionsPanel.add(createSoLingerOption());
        optionsPanel.add(createEolBytePanel());
        mainPanel.add(optionsPanel);
        mainPanel.add(createSignalDataPanel());
        mainPanel.add(createRequestPanel());


        add(mainPanel, BorderLayout.CENTER);
    }
}
