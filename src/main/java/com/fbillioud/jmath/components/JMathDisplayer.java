/* 
 * Copyright 2016 François Billioud.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fbillioud.jmath.components;

import com.fbillioud.jmath.JsoupTools;
import com.fbillioud.jmath.MathComponent;
import com.fbillioud.jmath.components.Module.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

/**
 * JPanel in charge of displaying a MathComponent.
 * @author François Billioud
 */
public class JMathDisplayer extends JPanel implements MathComponent {

    /** The Jsoup Element to display **/
    private Element mathElement;
    /** The MathModule that will represente the element **/
    private Module module;
    /** The foreground color of this element if not inherited **/
    protected Color foreground;

    /** Create an empty JMathDisplayer **/
    public JMathDisplayer() {this("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"></math>");}
    /** Display this mathML string **/
    public JMathDisplayer(String mathML) {this(JsoupTools.parse(mathML).body().child(0), null);}
    /** Display the MathML contained in this Jsoup Element **/
    public JMathDisplayer(Element mathML) {this(mathML, null);}

    /** 
     * Create a Panel that will display the content of this Jsoup element
     * into this parent
     * @param mathElement the Jsoup element to display
     * @param parent the parent this JMathDisplayer belongs to
     */
    JMathDisplayer(Element mathElement, JComponent parent) {
        setOpaque(false);

        try {
            //Install the best module to represent the element
            setMathElement(mathElement);
        } catch (MathMLParsingException ex) {
            Logger.getLogger(JMathDisplayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(parent!=null) {
            this.setFont(parent.getFont());
            if(foreground==null) setForeground(parent.getForeground());
        }
    }
    
    /**
     * Get the current MathML string represented by this component.
     * @return the MathML, like <math>x</math>
     */
    public String getMathML() {
        return mathElement.outerHtml();
    }
    
    public void setMathML(String mathml) {
        try {
            removeAll();
            foreground = null;
            setMathElement(JsoupTools.parse(mathml).body().child(0));
            doLayout();
        } catch (MathMLParsingException ex) {
            Logger.getLogger(JMathDisplayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get the current Jsoup Element this component represents.
     * @return the Jsoup Element
     */
    public Element getMathMLElement() {
        return mathElement;
    }
    
    @Override
    public Dimension getMaximumSize() {
        return super.getPreferredSize();
    }
    
    @Override
    public void setForeground(Color color) {
        if(foreground==null) {//We don't override the mathML color attribute
            super.setForeground(color);
            if(module!=null) for(Component c : getComponents()) {c.setForeground(color);}
        }
    }
    
    @Override
    public void setFont(Font f) {
        if(isScaleRatioSet()) f = f.deriveFont(f.getSize2D()*ratio);
        for(Component c : getComponents()) {c.setFont(f);}
        super.setFont(f);
    }
    
    @Override
    public float getFontSize() {
        float fontSize = getFont().getSize2D();
        return isScaleRatioSet() ? fontSize/ratio : fontSize;
    }
    @Override
    public void setFontSize(float size) {
        float newSize = isScaleRatioSet() ? size * ratio : size;
        setFont(getFont().deriveFont(newSize));
        for(Component c : getComponents()) {if(c instanceof MathComponent) ((MathComponent)c).setFontSize(newSize);}
        invalidate();
    }
    
    /**
     * Get the font size of this component relative to its parent.
     * @return the ratio
     */
    private boolean isScaleRatioSet() {return ratio>0;}
    /** adapt the font size of this component compare to its parent **/
    private float ratio = -1f;
    /**
     * Set the font size of this component relative to its parent.
     * @param ratio The ratio
     */
    public void setScaleRatio(float ratio) {
        this.ratio = ratio;
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2D = (Graphics2D)g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int strokeSize = Math.max((int)(getFontSize()/10f), 1);
        g2D.setStroke(new BasicStroke(strokeSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if(module!=null) module.paintLines(g2D);
    }
    
    /**
     * Set the Module best suited to represent this mathElement
     * @param mathElement the Jsoup MathML Element to represent
     * @param parent the JComponent that requests this Module
     * @return the Module best suited to represent this Element
     */
    private void setMathElement(Element mathElement) throws MathMLParsingException {
        Module m;
        switch(mathElement.tagName()) {
            case "mfrac" : m = new ModuleFraction(mathElement, this); break;
            case "msqrt" : m = new ModuleSqrt(mathElement, this); break;
            case "mroot" : m = new ModuleRoot(mathElement, this); break;
            case "mfenced" : m = new ModuleFenced(mathElement, this); break;
            case "mover" : m = new ModuleOver(mathElement, this); break;
            case "munder" : m = new ModuleUnder(mathElement, this); break;
            case "msub" : m = new ModuleSub(mathElement, this); break;
            case "msup" : m = new ModuleSup(mathElement, this); break;
            case "msubsup" : m = new ModuleSubSup(mathElement, this); break;
            case "mmultiscripts" : m = new ModuleMultiscript(mathElement, this); break;
            case "mtable" : m = new ModuleTable(mathElement, this); break;
            case "mrow" :
            case "mlabeledtr" :
            case "mtr" :
            case "mtd" :
            case "mtext" :
            case "math" :
            case "mn" :
            case "mi" : m = new ModuleRow(mathElement, this); break;
            case "menclose" :
                String notation = mathElement.attr("notation");
                if("radical".equals(notation)) {
                    mathElement.tagName("msqrt");
                    m = new ModuleSqrt(mathElement, this);
                } else {
                    m = new ModuleEnclose(mathElement, this, notation);
                }
                break;
            case "mo" : 
                if(mathElement.text().trim().equals("{") && mathElement.nextElementSibling()!=null) {m = new ModuleFenceOperator(mathElement, this);}
                else {m = new ModuleRow(mathElement, this);}
                break;
            default: m = new Module(new Element(Tag.valueOf("math"),""), this, null) {
                @Override
                public void paintLines(Graphics2D g) {}
            };
        }
        this.mathElement = mathElement;
        this.module = m;
        
        Color color = JsoupTools.getColor(mathElement);
        if(color!=null) {setForeground(color);foreground = color;}
    }
    
    /**
     * Check if the JMathDisplayer knows how to represent this tag
     * @param tag the tag to look for
     * @return true if the tag is handled, false otherwise
     */
    public static boolean isModuleAvailable(Element mathElement) {
        String[] known = {
            "mrow",
            "menclose",
            "mroot",
            "mfrac",
            "msqrt",
            "math",
            "mfenced",
            "msub",
            "msup",
            "msubsup",
            "munder",
            "mover",
            "munderover",
            "mmultiscripts",
            "mtable",
            "mlabeledtr",
            "mtr",
            "mtd",
            "mtext",
            "mn",
            "mi",
            "mo"
        };
        return Arrays.asList(known).contains(mathElement.tagName());
    }
}
