/**
 * Copyright (C) 2016 François Billioud - All Rights Reserved
 *
 * This file is part of MathEOS - PREMIUM
 *
 * Proprietary and confidential
 * Unauthorized copying of this file, via any medium is strictly prohibited
 *
 * No warranty, explicit or implicit, provided.
 * In no event shall the author be liable for any claim or damages.
 *
 **/

package com.fbillioud.jmath;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.EventListener;

/**
 * Il suffit d'ajouter ce listener sur un élément pour que les évènements Mouse
 * qu'il reçoit soient transmis à son parent comme si l'enfant était transparent.
 * Par exemple, le clic sur un bouton sera transmit au panel qui le contient, avec
 * les coordonnées et la source modifiées pour simuler un clic direct sur le panel
 * @author François Billioud
 */
public class DispatchMouseToParent implements MouseMotionListener, MouseListener, MouseWheelListener {

    @Override
    public void mouseMoved(MouseEvent e) {
        MouseMotionListener[] listeners = getListenerAncestor(e, MouseMotionListener.class);
        if(listeners!=null) for(MouseMotionListener l : listeners) l.mouseMoved(e);
    }
    public void mouseDragged(MouseEvent e) {
        MouseMotionListener[] listeners = getListenerAncestor(e, MouseMotionListener.class);
        if(listeners!=null) for(MouseMotionListener l : listeners) l.mouseDragged(e);
    }

    public void mouseClicked(MouseEvent e) {
        MouseListener[] listeners = getListenerAncestor(e, MouseListener.class);
        if(listeners!=null) for(MouseListener l : listeners) l.mouseClicked(e);
    }
    public void mousePressed(MouseEvent e) {
        MouseListener[] listeners = getListenerAncestor(e, MouseListener.class);
        if(listeners!=null) for(MouseListener l : listeners) l.mousePressed(e);
    }
    public void mouseReleased(MouseEvent e) {
        MouseListener[] listeners = getListenerAncestor(e, MouseListener.class);
        if(listeners!=null) for(MouseListener l : listeners) l.mouseReleased(e);
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public void mouseWheelMoved(MouseWheelEvent e) {
        MouseWheelListener[] listeners = getListenerAncestor(e, MouseWheelListener.class);
        if(listeners!=null) for(MouseWheelListener l : listeners) l.mouseWheelMoved(e);
    }

    private <T extends EventListener> T[] getListenerAncestor(MouseEvent e, Class<T> c) {
        if(c == null || e == null || e.getComponent() == null) return null;
        java.awt.Point P = e.getComponent().getLocation();
        e.translatePoint(P.x, P.y);

        Container parent = e.getComponent().getParent();
//        while(parent != null) {
        if(parent!=null && Component.class.isInstance(parent)) {
            e.setSource(parent);
            return ((Component)parent).getListeners(c);
        }
        return null;
    }

}
