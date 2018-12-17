/*
 * Created on Mar 1, 2006
 */
package klava.gui;

import klava.KlavaException;
import klava.PhysicalLocality;
import klava.topology.KlavaNode;

import org.mikado.imc.protocols.SessionId;

/**
 * Tries to login when the button is pressed.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class EstablishSessionPanel extends
        org.mikado.imc.gui.EstablishSessionPanel {
    protected KlavaNode klavaNode;
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param node
     */
    public EstablishSessionPanel(KlavaNode node) {
        super(node);
        this.klavaNode = node;
        hideProtocolChoice();
    }

    /**
     * When the establish button is pressed, tries to connect to
     * the specified session id.
     * 
     * @see org.mikado.imc.gui.AcceptSessionPanel#performAction()
     */
    @Override
    protected void performAction() {
        PhysicalLocality remote = new PhysicalLocality(new SessionId(
                getProtocolIdText().getText(), getSessionIdText().getText()));
        try {
            klavaNode.login(remote);
        } catch (KlavaException e) {
            showException(e);
        }
    }
}
