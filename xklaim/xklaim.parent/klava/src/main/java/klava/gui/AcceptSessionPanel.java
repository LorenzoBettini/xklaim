/*
 * Created on Mar 1, 2006
 */
package klava.gui;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.protocols.SessionId;

import klava.PhysicalLocality;
import klava.topology.AcceptNodeCoordinator;
import klava.topology.KlavaNode;

/**
 * Spawns an AcceptNodeCoordinator when the button is pressed.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class AcceptSessionPanel extends org.mikado.imc.gui.AcceptSessionPanel {
    protected KlavaNode klavaNode;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param node
     */
    public AcceptSessionPanel(KlavaNode node) {
        super(node);
        klavaNode = node;
        hideProtocolChoice();
    }

    /**
     * When the accept button is pressed, spawns a process waiting for
     * an accept request.
     * 
     * @see org.mikado.imc.gui.AcceptSessionPanel#performAction()
     */
    @Override
    protected void performAction() {
        PhysicalLocality local = new PhysicalLocality(new SessionId(
                getProtocolIdText().getText(), getSessionIdText().getText()));
        try {
            AcceptNodeCoordinator acceptNodeCoordinator = new AcceptNodeCoordinator(local);
            acceptNodeCoordinator.setLoop(!getOnlyOnceCheckBox().isSelected());
            klavaNode.addNodeCoordinator(acceptNodeCoordinator);
        } catch (IMCException e) {
            showException(e);
        }
    }
}
