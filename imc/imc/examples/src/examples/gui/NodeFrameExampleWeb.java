/*
 * Created on Apr 12, 2006
 */
/**
 * 
 */
package examples.gui;

import java.util.Vector;

import org.mikado.imc.gui.AcceptEstablishSessionPanel;
import org.mikado.imc.gui.AcceptSessionPanel;
import org.mikado.imc.gui.ClassChooserDialog;
import org.mikado.imc.gui.EstablishSessionPanel;

/**
 * As NodeFrameExample, but this is thought to be executed with WebStart, and so
 * the list of available protocols is fixed.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class NodeFrameExampleWeb extends NodeFrameExample {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Initializes the fields of the AcceptEstablishSessionPanel and sets the
     * ClassChooserDialogs.
     * 
     * @see org.mikado.imc.gui.NodeDesktopFrame#createAcceptEstablishSessionPanel()
     */
    @Override
    public AcceptEstablishSessionPanel createAcceptEstablishSessionPanel() {
        AcceptEstablishSessionPanel acceptEstablishSessionPanel = super
                .createAcceptEstablishSessionPanel();

        AcceptSessionPanel acceptSessionPanel = acceptEstablishSessionPanel
                .getAcceptSessionPanel();

        EstablishSessionPanel establishSessionPanel = acceptEstablishSessionPanel
                .getEstablishSessionPanel();

        Vector<String> protocols = new Vector<String>();
        protocols.add(ConnectionMonitor.EchoProtocol.class.getName());
        ClassChooserDialog classChooserDialog = new ClassChooserDialog(
                protocols, this, "Choose a protocol", true);

        acceptSessionPanel.setProtocolChooser(classChooserDialog);
        establishSessionPanel.setProtocolChooser(classChooserDialog);

        protocols = new Vector<String>();
        protocols.add(ConnectionMonitor.EchoAcceptProtocolFactory.class
                .getName());
        classChooserDialog = new ClassChooserDialog(protocols, this,
                "Choose a protocol", true);
        acceptSessionPanel.setFactoryChooser(classChooserDialog);

        return acceptEstablishSessionPanel;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        NodeFrameExampleWeb nodeFrameExampleWeb = new NodeFrameExampleWeb();
        nodeFrameExampleWeb.setSize(500, 400);
        nodeFrameExampleWeb.setVisible(true);
    }

}
