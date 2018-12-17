/*
 * Created on Feb 23, 2006
 */
package klava.gui;

import java.util.Enumeration;

import klava.Environment;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.events.EnvironmentEvent;

import org.mikado.imc.events.Event;
import org.mikado.imc.events.EventListener;
import org.mikado.imc.gui.DefaultListPanel;
import org.mikado.imc.gui.PanelWithTitle;


/**
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class EnvironmentListPanel extends PanelWithTitle implements EventListener {
    DefaultListPanel listPanel = new DefaultListPanel();
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public EnvironmentListPanel() {
        super("environment");
        
        addMainPanel(listPanel);
    }
    
    /**
     * @param environment
     */
    public EnvironmentListPanel(Environment environment) {
        this();
        
        synchronized (environment) {
            Enumeration<LogicalLocality> logicalLocalities = environment.keys();
            while (logicalLocalities.hasMoreElements()) {
                LogicalLocality logicalLocality = logicalLocalities.nextElement();
                PhysicalLocality physicalLocality = environment.toPhysical(logicalLocality);
                listPanel.addElement(new Environment.EnvironmentEntry(logicalLocality, physicalLocality).toString());
            }
        }
    }

    /**
     * @param title
     */
    public EnvironmentListPanel(String title) {
        super(title);
        
        addMainPanel(listPanel);
    }

    /**
     * @see org.mikado.imc.events.EventListener#notify(org.mikado.imc.events.Event)
     */
    public void notify(Event event) {
        if (! (event instanceof EnvironmentEvent))
            return;
        
        EnvironmentEvent environmentEvent = (EnvironmentEvent)event;
        
        if (environmentEvent.eventType == EnvironmentEvent.EventType.ADDED) {
            listPanel.addElement(environmentEvent.element.toString());
        } else { /* removed */
            listPanel.removeElement(environmentEvent.element.toString());
        }
    }

}
