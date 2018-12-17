/*
 * Created on Feb 24, 2006
 */
package org.mikado.imc.gui;

import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.topology.ConnectNodeCoordinator;
import org.mikado.imc.topology.Node;

/**
 * @author Lorenzo Bettini
 * @version $Revision: 1.9 $
 */
public class EstablishSessionPanel extends AcceptSessionPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param node
     */
    public EstablishSessionPanel(Node node) {
        super(node);
        initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        setButtonOkText("Establish session");
        getButtonPanel().remove(getOnlyOnceCheckBox());
        getInputPanel().remove(getFactoryButton());

        /* in establishing, the protocol button is the only one available */
        getProtocolButton().setEnabled(true);
    }

    /**
     * This is called when the button is pressed. Subclasses can override this
     * method in order to perform other actions.
     * 
     * This class tries to establish a Session with the data inserted in the
     * text fields.
     */
    @Override
    protected void performAction() {
        try {
            Protocol originalProtocol = (Protocol) Class.forName(
                    getProtocolText().getText()).newInstance();
            node.addNodeCoordinator(new ConnectNodeCoordinator(
                    originalProtocol, new SessionId(getProtocolIdText()
                            .getText(), getSessionIdText().getText())));
        } catch (Exception e) {
            showException(e);
        }
    }

}
